const express = require('express');
const cors = require('cors');
const mongoose = require('mongoose');
const shortid = require('shortid');
require('dotenv').config();

const app = express();
const port = process.env.PORT || 3000;

// Middleware
app.use(cors());
app.use(express.json());

// MongoDB Atlas connection
const uri = 'mongodb+srv://ylalit0022:jBRgqv6BBfj2lYaG@cluster0.3d1qt.mongodb.net/eventwishes?retryWrites=true&w=majority';

console.log('Attempting to connect to MongoDB...');

// Connect to MongoDB before starting the server
mongoose.connect(uri)
  .then(() => {
    console.log('Successfully connected to MongoDB Atlas!');
    
    // Only start the server after successful database connection
    app.listen(port, () => {
      console.log(`Server is running on port ${port}`);
    });
  })
  .catch(err => {
    console.error('MongoDB connection error:', err);
    process.exit(1);
  });

// Handle MongoDB connection events
mongoose.connection.on('error', err => {
    console.error('MongoDB connection error:', err);
});

mongoose.connection.on('disconnected', () => {
    console.log('MongoDB disconnected');
});

process.on('SIGINT', async () => {
    await mongoose.connection.close();
    process.exit(0);
});

// HTML template for wish page
const getWishPageHtml = (wish, previewImage) => {
    // Replace template variables in customizedHtml
    const customizedContent = wish.customizedHtml
        .replace(/{{recipientName}}/g, wish.recipientName)
        .replace(/{{senderName}}/g, wish.senderName);

    return `<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=2.0, minimum-scale=0.5">
    <title>${wish.recipientName}'s Special Wish from ${wish.senderName}</title>
    
    <!-- Open Graph meta tags for rich previews -->
    <meta property="og:title" content="${wish.recipientName}'s Special Wish from ${wish.senderName}" />
    <meta property="og:description" content="Click to view your personalized wish! " />
    <meta property="og:image" content="${previewImage}" />
    <meta property="og:url" content="${process.env.BASE_URL || 'https://eventwishes.onrender.com'}/wish/${wish.shortCode}" />
    <meta property="og:type" content="website" />
    
    <!-- Twitter Card meta tags -->
    <meta name="twitter:card" content="summary_large_image" />
    <meta name="twitter:title" content="${wish.recipientName}'s Special Wish from ${wish.senderName}" />
    <meta name="twitter:description" content="Click to view your personalized wish! " />
    <meta name="twitter:image" content="${previewImage}" />
    
    <!-- Additional meta tags -->
    <meta name="description" content="A special wish created for ${wish.recipientName} from ${wish.senderName}. Open to view your personalized message!" />
    <meta name="theme-color" content="#ff4081" />
    
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

// Import models
const Template = require('./models/template.js');
const SharedWish = require('./models/sharedWish.js');

// Serve static files
app.use(express.static('public'));

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
