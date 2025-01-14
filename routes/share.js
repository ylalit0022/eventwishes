const express = require('express');
const router = express.Router();
const SharedWish = require('../models/sharedWish');
const Template = require('../models/template');
const shortid = require('shortid');

// Create share link
router.post('/', async (req, res) => {
    try {
        const { templateId, recipientName, senderName, htmlContent } = req.body;

        // Validate required fields
        if (!templateId || !recipientName || !senderName || !htmlContent) {
            return res.status(400).json({ 
                error: 'Missing required fields',
                details: {
                    templateId: !templateId ? 'Missing templateId' : undefined,
                    recipientName: !recipientName ? 'Missing recipientName' : undefined,
                    senderName: !senderName ? 'Missing senderName' : undefined,
                    htmlContent: !htmlContent ? 'Missing htmlContent' : undefined
                }
            });
        }

        // Get template
        const template = await Template.findById(templateId);
        if (!template) {
            return res.status(404).json({ error: 'Template not found' });
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
        const shareUrl = `${process.env.BASE_URL || 'https://eventwishes.onrender.com'}/wish/${shortCode}`;

        res.json({
            shareUrl,
            shortCode,
            message: 'Wish shared successfully'
        });
    } catch (error) {
        console.error('Share error:', error);
        
        // Better error handling
        if (error.name === 'ValidationError') {
            return res.status(400).json({
                error: 'Validation Error',
                details: Object.keys(error.errors).reduce((acc, key) => {
                    acc[key] = error.errors[key].message;
                    return acc;
                }, {})
            });
        }
        
        res.status(500).json({ 
            error: 'Failed to create share link',
            message: error.message
        });
    }
});

// Get shared wish by shortCode
router.get('/:shortCode', async (req, res) => {
    try {
        const sharedWish = await SharedWish.findOne({ shortCode: req.params.shortCode })
            .populate('templateId', 'name description');

        if (!sharedWish) {
            return res.status(404).json({ error: 'Shared wish not found' });
        }

        // Update view count if needed
        if (!sharedWish.views) sharedWish.views = 0;
        sharedWish.views += 1;
        sharedWish.lastViewedAt = new Date();
        await sharedWish.save();

        res.json(sharedWish);
    } catch (error) {
        console.error('Error fetching shared wish:', error);
        res.status(500).json({ error: 'Failed to get shared wish' });
    }
});

module.exports = router;
