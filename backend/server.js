const express = require('express');
const cors = require('cors');
const mongoose = require('mongoose');
const path = require('path');
const fs = require('fs');
const shortid = require('shortid');
require('dotenv').config();

const app = express();
const port = process.env.PORT || 3000;

// Error handling middleware
const errorHandler = (err, req, res, next) => {
    console.error('Error:', err.stack);
    res.status(err.status || 500).json({
        error: {
            message: err.message || 'Internal Server Error',
            status: err.status || 500
        }
    });
};

// Request validation middleware
const validateRequest = (req, res, next) => {
    if (req.method === 'POST' && !req.is('application/json')) {
        return res.status(415).json({
            error: {
                message: 'Content-Type must be application/json',
                status: 415
            }
        });
    }
    next();
};

// Middleware
app.use(cors());
app.use(express.json());
app.use(validateRequest);

// Serve static files from the public directory
app.use(express.static(path.join(__dirname, 'public')));

// Debug logging middleware
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

// Serve .well-known directory with correct content type
app.use('/.well-known', express.static(path.join(__dirname, 'public/.well-known'), {
    setHeaders: (res, path) => {
        if (path.endsWith('assetlinks.json')) {
            res.setHeader('Content-Type', 'application/json');
        }
    }
}));

// Health check endpoint
app.get('/health', (req, res) => {
    res.json({
        status: 'healthy',
        timestamp: new Date().toISOString(),
        mongodb: mongoose.connection.readyState === 1 ? 'connected' : 'disconnected'
    });
});

// Start server first
const server = app.listen(port, () => {
    console.log(`Server running on port ${port}`);
});

// Graceful shutdown
process.on('SIGTERM', () => {
    console.log('SIGTERM signal received. Closing server...');
    server.close(() => {
        console.log('Server closed.');
        mongoose.connection.close(false, () => {
            console.log('MongoDB connection closed.');
            process.exit(0);
        });
    });
});

// Then try to connect to MongoDB
console.log('Attempting to connect to MongoDB...');
mongoose.connect(process.env.MONGODB_URI)
    .then(() => {
        console.log('Successfully connected to MongoDB Atlas!');
        
        // Import models after successful connection
        const Template = require('./models/template.js');
        const SharedWish = require('./models/sharedWish.js');
        
        // Import routes
        const shareRouter = require('./routes/share');
        
        // Use routes
        app.use('/api/share', shareRouter);
        
        // Serve wish page
        app.get('/wish/:shortCode', async (req, res) => {
            try {
                const wish = await SharedWish.findOne({ shortCode: req.params.shortCode })
                    .populate('templateId');

                if (!wish) {
                    return res.status(404).send('Wish not found');
                }

                // Update view count
                wish.views += 1;
                wish.lastViewedAt = new Date();
                await wish.save();

                // Get preview image from template or use default
                const previewImage = wish.templateId?.previewUrl || 
                    `${process.env.BASE_URL || 'https://eventwishes.onrender.com'}/images/default-preview.jpg`;

                // Send HTML page with meta tags
                res.send(getWishPageHtml(wish, previewImage));
            } catch (error) {
                console.error('Error serving wish page:', error);
                res.status(500).send('Error loading wish');
            }
        });
        
        // API Routes for templates
        app.get('/api/templates', async (req, res) => {
            try {
                const templates = await Template.find().sort({ updatedAt: -1 });
                res.json(templates);
            } catch (error) {
                console.error('Error fetching templates:', error);
                res.status(500).json({ message: error.message });
            }
        });

        app.get('/api/templates/:id', async (req, res) => {
            try {
                const template = await Template.findById(req.params.id);
                if (template) {
                    res.json(template);
                } else {
                    res.status(404).json({ message: 'Template not found' });
                }
            } catch (error) {
                console.error('Error fetching template:', error);
                res.status(500).json({ message: error.message });
            }
        });

        // Share API endpoint
        app.post('/api/share', async (req, res) => {
            try {
                const { templateId, recipientName, senderName, htmlContent } = req.body;
                console.log('Share request:', { templateId, recipientName, senderName, htmlContent: !!htmlContent });

                // Validate required fields
                const missingFields = [];
                if (!templateId) missingFields.push('templateId');
                if (!recipientName) missingFields.push('recipientName');
                if (!senderName) missingFields.push('senderName');
                if (!htmlContent) missingFields.push('htmlContent');

                if (missingFields.length > 0) {
                    return res.status(400).json({ 
                        error: 'Missing required fields',
                        missingFields,
                        received: { templateId, recipientName, senderName, hasHtml: !!htmlContent }
                    });
                }

                // Validate templateId format
                if (!mongoose.Types.ObjectId.isValid(templateId)) {
                    return res.status(400).json({
                        error: 'Invalid templateId format',
                        received: templateId
                    });
                }

                // Check if template exists
                const template = await Template.findById(templateId);
                if (!template) {
                    return res.status(404).json({
                        error: 'Template not found',
                        templateId
                    });
                }

                // Generate unique short code
                const shortCode = shortid.generate();

                // Create shared wish
                const sharedWish = new SharedWish({
                    shortCode,
                    templateId,
                    recipientName: recipientName.trim(),
                    senderName: senderName.trim(),
                    customizedHtml: htmlContent,
                    createdAt: new Date()
                });

                await sharedWish.save();

                // Generate share URL
                const baseUrl = process.env.BASE_URL || 'https://eventwishes.onrender.com';
                const shareUrl = `${baseUrl}/wish/${shortCode}`;

                res.json({
                    shareUrl,
                    shortCode,
                    message: 'Wish shared successfully'
                });
            } catch (error) {
                console.error('Share error:', error);
                res.status(500).json({ 
                    error: 'Failed to create share link',
                    details: error.message,
                    type: error.name
                });
            }
        });

        // Get shared wish
        app.get('/api/wish/:shortCode', async (req, res) => {
            try {
                const sharedWish = await SharedWish.findOne({ shortCode: req.params.shortCode })
                    .populate('templateId');

                if (!sharedWish) {
                    return res.status(404).json({ error: 'Shared wish not found' });
                }

                // Update view count
                sharedWish.views += 1;
                sharedWish.lastViewedAt = new Date();
                await sharedWish.save();

                res.json(sharedWish);
            } catch (error) {
                console.error('Error fetching shared wish:', error);
                res.status(500).json({ error: 'Failed to get shared wish' });
            }
        });
    })
    .catch(err => {
        console.error('MongoDB connection error:', err);
        console.log('Server will continue running without MongoDB features');
    });

// Handle MongoDB connection events
mongoose.connection.on('error', err => {
    console.error('MongoDB connection error:', err);
});

mongoose.connection.on('disconnected', () => {
    console.log('MongoDB disconnected. Attempting to reconnect...');
});

mongoose.connection.on('reconnected', () => {
    console.log('MongoDB reconnected');
});

// Add error handler middleware last
app.use(errorHandler);

// HTML template for wish page
const getWishPageHtml = (wish, previewImage) => {
    // Replace template variables in customizedHtml
    const customizedContent = wish.customizedHtml
        .replace(/{{recipientName}}/g, wish.recipientName)
        .replace(/{{senderName}}/g, wish.senderName);

    // Ensure preview image is an absolute URL
    const fullPreviewUrl = previewImage.startsWith('http') ? previewImage : 
        `${process.env.BASE_URL || 'https://eventwishes.onrender.com'}${previewImage}`;

    return `<!DOCTYPE html>
<html lang="en" prefix="og: http://ogp.me/ns#">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=2.0, minimum-scale=0.5">
    <title>${wish.recipientName}'s Special Wish from ${wish.senderName}</title>
    
    <!-- WhatsApp and Open Graph meta tags -->
    <meta property="og:site_name" content="Event Wishes" />
    <meta property="og:title" content="Special Wish for ${wish.recipientName} 🎉" />
    <meta property="og:description" content="Click to view a special wish from ${wish.senderName}! 🎈" />
    <meta property="og:image" content="${fullPreviewUrl}" />
    <meta property="og:image:width" content="1200" />
    <meta property="og:image:height" content="630" />
    <meta property="og:url" content="${process.env.BASE_URL || 'https://eventwishes.onrender.com'}/wish/${wish.shortCode}" />
    <meta property="og:type" content="website" />
    
    <!-- Twitter Card meta tags -->
    <meta name="twitter:card" content="summary_large_image" />
    <meta name="twitter:site" content="@eventwishes" />
    <meta name="twitter:title" content="Special Wish for ${wish.recipientName} 🎉" />
    <meta name="twitter:description" content="Click to view a special wish from ${wish.senderName}! 🎈" />
    <meta name="twitter:image" content="${fullPreviewUrl}" />
    
    <!-- Additional meta tags -->
    <meta name="description" content="A special wish created for ${wish.recipientName} from ${wish.senderName}. Open to view your personalized message! 🎉" />
    <meta name="theme-color" content="#ff4081" />
    
    <!-- WhatsApp specific -->
    <link rel="icon" type="image/png" href="${process.env.BASE_URL || 'https://eventwishes.onrender.com'}/favicon.ico">
    <meta property="og:image:alt" content="Event Wishes Preview" />
    
    <style>
        * { box-sizing: border-box; margin: 0; padding: 0; }
        html, body { 
            width: 100%;
            min-height: 100vh;
            font-family: Arial, sans-serif;
            line-height: 1.6;
            background-color: #f5f5f5;
        }
        body {
            display: flex;
            flex-direction: column;
            align-items: center;
            padding: 16px;
        }
        .content {
            width: 100%;
            max-width: 800px;
            margin: 20px auto;
            background: white;
            padding: 24px;
            border-radius: 12px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.1);
            word-wrap: break-word;
            overflow-wrap: break-word;
        }
        .content img {
            max-width: 100%;
            height: auto;
            display: block;
            margin: 16px auto;
            border-radius: 8px;
        }
        .app-promo {
            width: 100%;
            max-width: 800px;
            margin: 24px auto;
            text-align: center;
            padding: 16px;
            background: #f8f9fa;
            border-radius: 8px;
        }
        .download-btn {
            display: inline-block;
            padding: 12px 24px;
            background: #ff4081;
            color: white;
            text-decoration: none;
            border-radius: 24px;
            margin-top: 12px;
            font-weight: bold;
            transition: background-color 0.3s;
        }
        .download-btn:hover {
            background: #f50057;
        }
        pre, code {
            white-space: pre-wrap;
            word-wrap: break-word;
            overflow-wrap: break-word;
            max-width: 100%;
        }
        @media (prefers-color-scheme: dark) {
            body { background-color: #121212; color: #ffffff; }
            .content { background: #1e1e1e; color: #ffffff; }
            .app-promo { background: #2d2d2d; color: #ffffff; }
        }
        @media screen and (max-width: 600px) {
            body { padding: 8px; }
            .content { padding: 16px; margin: 10px auto; }
            .app-promo { margin: 16px auto; }
        }
    </style>
</head>
<body>
    <main class="content">
        ${customizedContent}
    </main>
    <div class="app-promo">
        <p>Create your own special wishes with Event Wishes app!</p>
        <a href="https://play.google.com/store/apps/details?id=com.ds.eventwishes" class="download-btn">
            Get the App
        </a>
    </div>
</body>
</html>`;
};
