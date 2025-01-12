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
        const shortCode = shortid.generate();
        
        // In a production environment, you'd want to store this information
        // in a separate collection for shared templates
        
        const shareUrl = `${process.env.APP_URL || 'http://localhost:3000'}/share/${shortCode}`;
        
        res.json({
            shareUrl,
            shortUrl: shareUrl
        });
    } catch (error) {
        console.error('Error creating share link:', error);
        res.status(500).json({ message: error.message });
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
