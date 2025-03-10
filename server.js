const express = require('express');
const cors = require('cors');
const mongoose = require('mongoose');
const path = require('path');
const fs = require('fs');
const shortid = require('shortid');
require('dotenv').config();

const app = express();
const port = process.env.PORT || 3000;

// Import routes
const shareRoutes = require('./routes/share');
const templateRoutes = require('./routes/templates');
const categoryIconRoutes = require('./routes/categoryIcons'); 
const adMobRoutes = require('./routes/adMobRoutes');



// Validate MongoDB URI
if (!process.env.MONGODB_URI) {
    console.error('ERROR: MONGODB_URI environment variable is not set!');
    console.error('Please set MONGODB_URI in your environment variables or .env file');
    console.error('Example: MONGODB_URI=mongodb+srv://<username>:<password>@<cluster>.mongodb.net/<dbname>');
    process.exit(1); // Exit with error
}

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

// Debug logging middleware
app.use((req, res, next) => {
    console.log(`[${new Date().toISOString()}] ${req.method} ${req.path}`);
    next();
});

// Middleware
app.use(cors());
app.use(express.json());
app.use(validateRequest);

// Mount routes BEFORE static files
app.use('/api/templates', templateRoutes);
app.use('/share', shareRoutes);
app.use('/api/category-icons', categoryIconRoutes);
app.use('/api/festivals', require('./routes/festivals'));
app.use('/api/categoryIcons', require('./routes/categoryIcons'));
apiRouter.use('/admob-ads', adMobRoutes);




// Serve static files from the public directory
app.use(express.static(path.join(__dirname, 'public')));

// Asset links data
const assetLinksData = [{
    "relation": ["delegate_permission/common.handle_all_urls"],
    "target": {
        "namespace": "android_app",
        "package_name": "com.ds.eventwish",
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

// MongoDB Connection with retry mechanism
async function connectWithRetry(retries = 5, delay = 5000) {
    for (let i = 0; i < retries; i++) {
        try {
            console.log(`MongoDB connection attempt ${i + 1} of ${retries}...`);
            await mongoose.connect(process.env.MONGODB_URI, {
                useNewUrlParser: true,
                useUnifiedTopology: true
            });
            console.log('MongoDB connected successfully');
            return;
        } catch (err) {
            console.error(`Connection attempt ${i + 1} failed:`, err.message);
            if (i < retries - 1) {
                console.log(`Retrying in ${delay / 1000} seconds...`);
                await new Promise(resolve => setTimeout(resolve, delay));
            }
        }
    }
    console.error('Failed to connect to MongoDB after multiple attempts');
    process.exit(1);
}

// Start server and MongoDB connection
async function startServer() {
    try {
        // Connect to MongoDB first
        await connectWithRetry();

        // Import models after successful connection
        const Template = require('./models/template.js');
        const SharedWish = require('./models/sharedWish.js');

        // Serve wish page and API endpoint
        const serveWish = async (req, res, isApi = false) => {
            try {
                const wish = await SharedWish.findOne({ shortCode: req.params.shortCode })
                    .populate('templateId');

                if (!wish) {
                    return isApi 
                        ? res.status(404).json({ error: 'Wish not found' })
                        : res.status(404).send('Wish not found');
                }

                // Update view count
                wish.views += 1;
                wish.lastViewedAt = new Date();
                await wish.save();

                if (isApi) {
                    // Return JSON for API requests
                    return res.json(wish);
                } else {
                    // Get preview image from template or use default
                    const previewImage = wish.templateId?.previewUrl || 
                        `${process.env.BASE_URL || 'https://eventwishes.onrender.com'}/images/default-preview.jpg`;

                    // Send HTML page with meta tags
                    res.send(getWishPageHtml(wish, previewImage));
                }
            } catch (error) {
                console.error('Error serving wish:', error);
                return isApi
                    ? res.status(500).json({ error: 'Error loading wish' })
                    : res.status(500).send('Error loading wish');
            }
        };

        // Web page endpoint
        app.get('/wish/:shortCode', (req, res) => serveWish(req, res, false));
        
        // API endpoint
        app.get('/api/share/:shortCode', (req, res) => serveWish(req, res, true));
        
        // API Routes for templates
        app.get('/api/templates', async (req, res) => {
            try {
                const templates = await Template.find({
                    status: true,
                    isActive: true
                }).sort({ updatedAt: -1 });
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

        //for category wise fetch data
        // Add this inside startServer()
app.get('/api/templates/category/:category', async (req, res) => {
    try {
        const category = req.params.category;
        const { page = 1, limit = 20 } = req.query;
        const templates = await Template.find({ category })
            .sort({ updatedAt: -1 })
            .skip((page - 1) * limit)
            .limit(parseInt(limit));

        if (templates.length === 0) {
            return res.status(404).json({ message: 'No templates found for this category' });
        }

        res.json({
            data: templates,
            page: parseInt(page),
            totalPages: Math.ceil(templates.length / limit),
            totalItems: templates.length
        });
    } catch (error) {
        console.error('Error fetching templates by category:', error);
        res.status(500).json({ message: 'Server error' });
    }
});


        // Share API endpoint
        app.post('/api/share', async (req, res) => {
            try {
                const { templateId, recipientName, senderName, htmlContent, cssContent, jsContent, sharedVia } = req.body;
                console.log('Share request:', { templateId, recipientName, senderName, htmlContent: !!htmlContent,
                cssContent: !!cssContent,
                jsContent: !!jsContent,
                sharedVia 
                });

                // Validate required fields
                const missingFields = [];
                if (!templateId) missingFields.push('templateId');
                if (!recipientName) missingFields.push('recipientName');
                if (!senderName) missingFields.push('senderName');
                if (!htmlContent) missingFields.push('htmlContent');
                if (!sharedVia) missingFields.push('sharedVia');


                if (missingFields.length > 0) {
                    return res.status(400).json({ 
                        error: 'Missing required fields',
                        missingFields,
                        received: { templateId, recipientName, senderName, hasHtml: !!htmlContent, sharedVia }
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
                    cssContent: cssContent || '',  // Add CSS content
                    jsContent: jsContent || '',    // Add JS content
                    sharedVia: sharedVia || 'LINK', // Add share platform

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

        // Start server
        app.listen(port, () => {
            console.log(`Server is running on port ${port}`);
            console.log('- /api/admob-ads');
            console.log(`App URL: ${process.env.APP_URL || `http://localhost:${port}`}`);
        });

        // Handle MongoDB disconnection
        mongoose.connection.on('disconnected', async () => {
            console.log('MongoDB disconnected. Attempting to reconnect...');
            const reconnected = await connectWithRetry(3, 3000);
            if (!reconnected) {
                console.error('Failed to reconnect to MongoDB. Shutting down...');
                process.exit(1);
            }
        });

        // Handle MongoDB errors
        mongoose.connection.on('error', err => {
            console.error('MongoDB connection error:', err);
            // Only exit on critical errors
            if (err.name === 'MongoNetworkError' || err.name === 'MongoServerSelectionError') {
                process.exit(1);
            }
        });

        // Graceful shutdown
        const shutdown = async () => {
            console.log('Received shutdown signal. Closing connections...');
            try {
                await mongoose.connection.close();
                console.log('MongoDB connection closed.');
                process.exit(0);
            } catch (err) {
                console.error('Error during shutdown:', err);
                process.exit(1);
            }
        };

        process.on('SIGINT', shutdown);
        process.on('SIGTERM', shutdown);

    } catch (err) {
        console.error('Error starting server:', err);
        process.exit(1);
    }
}

// Add error handler middleware last
app.use(errorHandler);

// Start the application
startServer();

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
