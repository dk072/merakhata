const crypto = require('crypto');
const fs = require('fs');
const path = require('path');

const os = require('os');

// In-Memory & File-backed Database Store
const DATA_FILE = path.join(os.tmpdir(), 'merakhata_db.json');

function loadDb() {
    try {
        if (fs.existsSync(DATA_FILE)) {
            const content = fs.readFileSync(DATA_FILE, 'utf8');
            return JSON.parse(content);
        }
    } catch (e) {
        console.error("Error reading DB file:", e);
    }
    return { users: {}, syncData: {} };
}

function saveDb(db) {
    try {
        const dir = path.dirname(DATA_FILE);
        if (!fs.existsSync(dir)) {
            fs.mkdirSync(dir, { recursive: true });
        }
        fs.writeFileSync(DATA_FILE, JSON.stringify(db, null, 2), 'utf8');
    } catch (e) {
        console.error("Error writing DB file:", e);
    }
}

const db = loadDb();

function hashPassword(password) {
    return crypto.createHash('sha256').update(password + 'MeraKhataSalt2026').digest('hex');
}

module.exports = (req, res) => {
    // Enable CORS for mobile application
    res.setHeader('Access-Control-Allow-Origin', '*');
    res.setHeader('Access-Control-Allow-Methods', 'GET, POST, OPTIONS');
    res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization');

    if (req.method === 'OPTIONS') {
        res.writeHead(204);
        res.end();
        return;
    }

    const urlPath = req.url.split('?')[0];

    // Serve static files or update.json if requested
    if (req.method === 'GET' && (urlPath === '/' || urlPath === '/update.json')) {
        const updateFilePath = path.join(__dirname, '..', 'update.json');
        if (fs.existsSync(updateFilePath)) {
            res.writeHead(200, { 'Content-Type': 'application/json' });
            res.end(fs.readFileSync(updateFilePath, 'utf8'));
            return;
        }
    }

    let bodyData = '';
    req.on('data', chunk => { bodyData += chunk; });
    req.on('end', () => {
        let body = {};
        if (bodyData) {
            try { body = JSON.parse(bodyData); } catch (e) {}
        }

        // Endpoint: Register New Account
        if (urlPath === '/api/auth/register' && req.method === 'POST') {
            const { email, password, ownerName, businessName } = body;
            if (!email || !password) {
                res.writeHead(400, { 'Content-Type': 'application/json' });
                res.end(JSON.stringify({ success: false, message: 'Email and Password are required.' }));
                return;
            }

            const cleanEmail = email.trim().toLowerCase();
            if (db.users[cleanEmail]) {
                res.writeHead(400, { 'Content-Type': 'application/json' });
                res.end(JSON.stringify({ success: false, message: 'An account with this email address already exists. Please log in.' }));
                return;
            }

            const userId = 'usr_' + crypto.randomBytes(8).toString('hex');
            const token = 'token_' + crypto.randomBytes(16).toString('hex');
            const pwdHash = hashPassword(password);

            db.users[cleanEmail] = {
                userId,
                email: cleanEmail,
                passwordHash: pwdHash,
                ownerName: ownerName || '',
                businessName: businessName || '',
                createdAt: Date.now()
            };
            saveDb(db);

            res.writeHead(200, { 'Content-Type': 'application/json' });
            res.end(JSON.stringify({
                success: true,
                userId,
                email: cleanEmail,
                token,
                ownerName: ownerName || '',
                businessName: businessName || ''
            }));
            return;
        }

        // Endpoint: Login to Account
        if (urlPath === '/api/auth/login' && req.method === 'POST') {
            const { email, password } = body;
            if (!email || !password) {
                res.writeHead(400, { 'Content-Type': 'application/json' });
                res.end(JSON.stringify({ success: false, message: 'Email and Password are required.' }));
                return;
            }

            const cleanEmail = email.trim().toLowerCase();
            const user = db.users[cleanEmail];

            if (!user) {
                res.writeHead(404, { 'Content-Type': 'application/json' });
                res.end(JSON.stringify({ success: false, message: 'No account found with this email address. Please register first.' }));
                return;
            }

            const pwdHash = hashPassword(password);
            if (user.passwordHash !== pwdHash) {
                res.writeHead(401, { 'Content-Type': 'application/json' });
                res.end(JSON.stringify({ success: false, message: 'Incorrect Password! Please check your password and try again.' }));
                return;
            }

            const token = 'token_' + crypto.randomBytes(16).toString('hex');
            res.writeHead(200, { 'Content-Type': 'application/json' });
            res.end(JSON.stringify({
                success: true,
                userId: user.userId,
                email: user.email,
                token,
                ownerName: user.ownerName,
                businessName: user.businessName
            }));
            return;
        }

        // Endpoint: Cloud Data Sync Push
        if (urlPath === '/api/sync/push' && req.method === 'POST') {
            const { userId, backupJson } = body;
            if (!userId || !backupJson) {
                res.writeHead(400, { 'Content-Type': 'application/json' });
                res.end(JSON.stringify({ success: false, message: 'UserId and Backup JSON required.' }));
                return;
            }

            db.syncData[userId] = {
                backupJson,
                updatedAt: Date.now()
            };
            saveDb(db);

            res.writeHead(200, { 'Content-Type': 'application/json' });
            res.end(JSON.stringify({ success: true, message: 'Ledger data synced to Cloud successfully.' }));
            return;
        }

        // Endpoint: Cloud Data Sync Pull
        if (urlPath === '/api/sync/pull' && (req.method === 'GET' || req.method === 'POST')) {
            const queryUrl = new URL(req.url, `http://${req.headers.host || 'localhost'}`);
            const userId = queryUrl.searchParams.get('userId') || body.userId;

            if (!userId || !db.syncData[userId]) {
                res.writeHead(200, { 'Content-Type': 'application/json' });
                res.end(JSON.stringify({ success: true, backupJson: null }));
                return;
            }

            res.writeHead(200, { 'Content-Type': 'application/json' });
            res.end(JSON.stringify({
                success: true,
                backupJson: db.syncData[userId].backupJson,
                updatedAt: db.syncData[userId].updatedAt
            }));
            return;
        }

        // Default 404
        res.writeHead(404, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ success: false, message: 'Endpoint Not Found' }));
    });
};
