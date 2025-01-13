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
const MONGODB_URI = process.env.MONGODB_URI || 'mongodb+srv://ylalit0022:jBRgqv6BBfj2lYaG@eventwishes.3d1qt.mongodb.net/eventwishes?retryWrites=true&w=majority';

mongoose.connect(MONGODB_URI)
    .then(() => console.log('Successfully connected to MongoDB Atlas'))
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

// Import models
const Template = require('./models/template');
const SharedWish = require('./models/sharedWish');

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

        // Validate required fields
        if (!templateId || !recipientName || !senderName) {
            return res.status(400).json({ 
                error: 'Missing required fields',
                required: ['templateId', 'recipientName', 'senderName']
            });
        }

        // Generate unique short code
        const shortCode = shortid.generate();

        // Create shared wish
        const sharedWish = new SharedWish({
            shortCode,
            templateId,
            recipientName,
            senderName,
            customizedHtml: htmlContent || '',
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
            details: error.message 
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
});
