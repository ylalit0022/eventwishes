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
const MONGODB_URI = process.env.MONGODB_URI || 'mongodb+srv://ylalit0022:jBRgqv6BBfj2lYaG@cluster0.3d1qt.mongodb.net/eventwishes?retryWrites=true&w=majority';

mongoose.connect(MONGODB_URI)
    .then(() => console.log('Successfully connected to MongoDB Atlas'))
    .catch(err => {
        console.error('MongoDB connection error:', err);
        process.exit(1); // Exit if cannot connect to database
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

// Template Schema
const templateSchema = new mongoose.Schema({
    title: { type: String, required: true },
    category: { type: String, required: true },
    htmlContent: { type: String, required: true },
    previewUrl: { type: String },
    createdAt: { type: Date, default: Date.now },
    updatedAt: { type: Date, default: Date.now }
});

templateSchema.pre('save', function(next) {
    this.updatedAt = Date.now();
    next();
});

const Template = mongoose.model('Template', templateSchema);

// Shared Wish Schema
const sharedWishSchema = new mongoose.Schema({
    shortCode: { type: String, required: true },
    templateId: { type: mongoose.Schema.Types.ObjectId, ref: 'Template', required: true },
    recipientName: { type: String, required: true },
    senderName: { type: String, required: true },
    customizedHtml: { type: String, required: true },
    views: { type: Number, default: 0 },
    lastViewedAt: { type: Date },
    createdAt: { type: Date, default: Date.now }
});

const SharedWish = mongoose.model('SharedWish', sharedWishSchema);

// API Routes
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

app.post('/api/templates', async (req, res) => {
    const template = new Template({
        title: req.body.title,
        category: req.body.category,
        htmlContent: req.body.htmlContent,
        previewUrl: req.body.previewUrl
    });

    try {
        const newTemplate = await template.save();
        res.status(201).json(newTemplate);
    } catch (error) {
        console.error('Error creating template:', error);
        res.status(400).json({ message: error.message });
    }
});

app.put('/api/templates/:id', async (req, res) => {
    try {
        const template = await Template.findByIdAndUpdate(
            req.params.id,
            {
                ...req.body,
                updatedAt: Date.now()
            },
            { new: true }
        );
        
        if (template) {
            res.json(template);
        } else {
            res.status(404).json({ message: 'Template not found' });
        }
    } catch (error) {
        console.error('Error updating template:', error);
        res.status(400).json({ message: error.message });
    }
});

app.delete('/api/templates/:id', async (req, res) => {
    try {
        const template = await Template.findByIdAndDelete(req.params.id);
        if (template) {
            res.json({ message: 'Template deleted successfully' });
        } else {
            res.status(404).json({ message: 'Template not found' });
        }
    } catch (error) {
        console.error('Error deleting template:', error);
        res.status(500).json({ message: error.message });
    }
});

// Share endpoint
app.post('/api/share', async (req, res) => {
    try {
        const { templateId, recipientName, senderName } = req.body;
        
        // Input validation
        if (!templateId || !recipientName || !senderName) {
            return res.status(400).json({ message: 'Missing required fields' });
        }

        // Get the template
        const template = await Template.findById(templateId);
        if (!template) {
            return res.status(404).json({ message: 'Template not found' });
        }

        // Generate unique short code
        const shortCode = shortid.generate();

        // Replace placeholders in HTML content
        const customizedHtml = template.htmlContent
            .replace(/\{\{recipientName\}\}/g, recipientName)
            .replace(/\{\{senderName\}\}/g, senderName);

        // Generate social media preview HTML
        const socialPreviewHtml = `
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta property="og:title" content="${template.title} - From ${senderName} to ${recipientName}">
    <meta property="og:description" content="A special wish from ${senderName} to ${recipientName}">
    <meta property="og:image" content="${template.previewUrl}">
    <meta property="og:url" content="${process.env.PRODUCTION_URL || 'https://eventwishes.onrender.com'}/share/${shortCode}">
    <meta property="og:type" content="website">
    <meta name="twitter:card" content="summary_large_image">
    <title>${template.title} - From ${senderName} to ${recipientName}</title>
    <style>
        body { 
            margin: 0; 
            font-family: Arial, sans-serif;
            background: linear-gradient(135deg, #6e8efb, #ff6b6b);
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
        }
        .wish-container {
            background: white;
            border-radius: 20px;
            padding: 30px;
            box-shadow: 0 10px 25px rgba(0,0,0,0.1);
            max-width: 600px;
            width: 90%;
            text-align: center;
        }
        .preview-image {
            max-width: 100%;
            border-radius: 10px;
            margin-bottom: 20px;
        }
        .names {
            font-size: 1.2em;
            color: #666;
            margin: 15px 0;
        }
    </style>
</head>
<body>
    <div class="wish-container">
        <img class="preview-image" src="${template.previewUrl}" alt="Wish Preview">
        ${customizedHtml}
        <div class="names">
            From: ${senderName}<br>
            To: ${recipientName}
        </div>
    </div>
</body>
</html>`;

        // Create shared wish record
        const sharedWish = new SharedWish({
            shortCode,
            templateId,
            recipientName,
            senderName,
            customizedHtml: socialPreviewHtml
        });
        
        await sharedWish.save();

        // Production base URL
        const baseUrl = process.env.PRODUCTION_URL || 'https://eventwishes.onrender.com';
        const shareUrl = `${baseUrl}/share/${shortCode}`;
        
        res.json({
            shareUrl,
            shortUrl: shareUrl,
            previewContent: customizedHtml,
            socialPreviewHtml
        });
    } catch (error) {
        console.error('Error creating share link:', error);
        res.status(500).json({ message: error.message });
    }
});

// Get shared wish endpoint
app.get('/api/share/:shortCode', async (req, res) => {
    try {
        const { shortCode } = req.params;
        
        const sharedWish = await SharedWish.findOne({ shortCode })
            .populate('templateId', 'title category');

        if (!sharedWish) {
            return res.status(404).json({ message: 'Shared wish not found or expired' });
        }

        // Update view count and last viewed timestamp
        sharedWish.views += 1;
        sharedWish.lastViewedAt = new Date();
        await sharedWish.save();

        res.json({
            id: sharedWish._id,
            shortCode: sharedWish.shortCode,
            template: sharedWish.templateId,
            recipientName: sharedWish.recipientName,
            senderName: sharedWish.senderName,
            customizedHtml: sharedWish.customizedHtml,
            views: sharedWish.views,
            createdAt: sharedWish.createdAt,
            lastViewedAt: sharedWish.lastViewedAt
        });
    } catch (error) {
        console.error('Error fetching shared wish:', error);
        res.status(500).json({ message: error.message });
    }
});

// Serve shared wish page
app.get('/share/:shortCode', async (req, res) => {
    try {
        const { shortCode } = req.params;
        
        const sharedWish = await SharedWish.findOne({ shortCode })
            .populate('templateId');

        if (!sharedWish) {
            return res.status(404).send('Wish not found or has expired');
        }

        // Update view count
        sharedWish.views += 1;
        sharedWish.lastViewedAt = new Date();
        await sharedWish.save();

        // Send the full HTML page
        res.send(sharedWish.customizedHtml);
    } catch (error) {
        console.error('Error serving shared wish:', error);
        res.status(500).send('Error loading the wish');
    }
});

// Error handling middleware
app.use((err, req, res, next) => {
    console.error(err.stack);
    res.status(500).json({ message: 'Something went wrong!' });
});

// Start server
app.listen(port, () => {
    console.log(`Server is running on port ${port}`);
});
