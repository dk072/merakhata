const http = require('http');
const apiHandler = require('./api/index.js');

const PORT = process.env.PORT || 8080;

const server = http.createServer((req, res) => {
    apiHandler(req, res);
});

server.listen(PORT, '0.0.0.0', () => {
    console.log(`Mera Khata Cloud Auth & Sync Server running on port ${PORT}`);
});
