const express = require('express');
const cors = require('cors');
const app = express();
const port = 3000;

// Middleware
app.use(cors());
app.use(express.json());

// Debug middleware
app.use((req, res, next) => {
    console.log(`[${new Date().toISOString()}] ${req.method} ${req.path}`);
    next();
});

// Asset links data
const assetLinksData = [{
    "relation": ["delegate_permission/common.handle_all_urls"],
    "target": {
        "namespace": "android_app",
        "package_name": "com.ds.eventwishes",
        "sha256_cert_fingerprints": [
            "B2:2F:26:9A:82:99:97:6C:FB:D3:6D:1D:80:DE:B0:93:22:F9:30:D2:0B:69:05:28:2F:05:60:39:0B:F1:4D:5D"
        ]
    }
}];

// Function to serve assetlinks.json
const serveAssetLinks = (req, res) => {
    console.log('Serving assetlinks.json for path:', req.path);
    res.setHeader('Content-Type', 'application/json');
    res.json(assetLinksData);
};

// Routes for assetlinks.json (both root and .well-known)
app.get('/assetlinks.json', serveAssetLinks);
app.get('/.well-known/assetlinks.json', serveAssetLinks);

// Start server
app.listen(port, () => {
    console.log(`Server running on port ${port}`);
    console.log('Try accessing:');
    console.log(`http://localhost:${port}/assetlinks.json`);
    console.log(`http://localhost:${port}/.well-known/assetlinks.json`);
});
