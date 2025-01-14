const fs = require('fs');
const path = require('path');

// Create directories if they don't exist
const publicDir = path.join(__dirname, 'public');
const wellKnownDir = path.join(publicDir, '.well-known');

// Create public directory if it doesn't exist
if (!fs.existsSync(publicDir)) {
    console.log('Creating public directory...');
    fs.mkdirSync(publicDir);
}

// Create .well-known directory if it doesn't exist
if (!fs.existsSync(wellKnownDir)) {
    console.log('Creating .well-known directory...');
    fs.mkdirSync(wellKnownDir);
}

// Create or update assetlinks.json
const assetlinksPath = path.join(wellKnownDir, 'assetlinks.json');
const assetlinksContent = [{
    "relation": ["delegate_permission/common.handle_all_urls"],
    "target": {
        "namespace": "android_app",
        "package_name": "com.ds.eventwishes",
        "sha256_cert_fingerprints": ["B2:2F:26:9A:82:99:97:6C:FB:D3:6D:1D:80:DE:B0:93:22:F9:30:D2:0B:69:05:28:2F:05:60:39:0B:F1:4D:5D"]
    }
}];

fs.writeFileSync(assetlinksPath, JSON.stringify(assetlinksContent, null, 2));
console.log('assetlinks.json created/updated at:', assetlinksPath);

// Verify file permissions
try {
    const stats = fs.statSync(assetlinksPath);
    console.log('File permissions:', stats.mode.toString(8));
    console.log('File exists:', fs.existsSync(assetlinksPath));
    console.log('File contents:', fs.readFileSync(assetlinksPath, 'utf8'));
} catch (error) {
    console.error('Error checking file:', error);
}
