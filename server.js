const http = require('http');
const fs = require('fs');
const path = require('path');

const PORT = 8080;

const server = http.createServer((req, res) => {
    // Enable CORS for mobile app
    res.setHeader('Access-Control-Allow-Origin', '*');
    res.setHeader('Access-Control-Allow-Methods', 'GET, OPTIONS');

    if (req.method === 'OPTIONS') {
        res.writeHead(204);
        res.end();
        return;
    }

    let filePath = path.join(__dirname, req.url === '/' ? 'update.json' : req.url);

    if (!fs.existsSync(filePath)) {
        res.writeHead(404, { 'Content-Type': 'text/plain' });
        res.end('404 Not Found');
        return;
    }

    const ext = path.extname(filePath);
    let contentType = 'text/plain';
    if (ext === '.json') contentType = 'application/json';
    if (ext === '.apk') contentType = 'application/vnd.android.package-archive';

    const stat = fs.statSync(filePath);
    res.writeHead(200, {
        'Content-Type': contentType,
        'Content-Length': stat.size
    });

    fs.createReadStream(filePath).pipe(res);
});

server.listen(PORT, '0.0.0.0', () => {
    console.log(`Update server listening on http://0.0.0.0:${PORT}`);
});
