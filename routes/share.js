const express = require('express');
const router = express.Router();
const SharedWish = require('../models/sharedWish');
const Template = require('../models/template');

// Create share link
router.post('/', async (req, res) => {
    try {
        const { templateId, recipientName, senderName } = req.body;

        // Validate required fields
        if (!templateId || !recipientName || !senderName) {
            return res.status(400).json({ error: 'Missing required fields' });
        }

        // Get template
        const template = await Template.findById(templateId);
        if (!template) {
            return res.status(404).json({ error: 'Template not found' });
        }

        // Create shared wish
        const sharedWish = new SharedWish({
            templateId,
            recipientName,
            senderName,
            htmlContent: template.htmlContent,
            createdAt: new Date()
        });

        await sharedWish.save();

        // Generate share URL
        const shareUrl = `${process.env.BASE_URL || 'https://eventwishes.onrender.com'}/wish/${sharedWish._id}`;

        res.json({
            shareUrl,
            message: 'Wish shared successfully'
        });
    } catch (error) {
        console.error('Share error:', error);
        res.status(500).json({ error: 'Failed to create share link' });
    }
});

// Get shared wish by ID - supports both /share/:id and /wish/:id
router.get('/:id', async (req, res) => {
    try {
        const sharedWish = await SharedWish.findById(req.params.id)
            .populate('templateId', 'name description');

        if (!sharedWish) {
            return res.status(404).json({ error: 'Shared wish not found' });
        }

        // Update view count and last viewed timestamp
        sharedWish.views = (sharedWish.views || 0) + 1;
        sharedWish.lastViewedAt = new Date();
        await sharedWish.save();

        // Format response for mobile app
        const response = {
            id: sharedWish._id,
            shortCode: sharedWish._id, // Using _id as shortCode for simplicity
            recipientName: sharedWish.recipientName,
            senderName: sharedWish.senderName,
            customizedHtml: sharedWish.htmlContent,
            views: sharedWish.views,
            createdAt: sharedWish.createdAt,
            lastViewedAt: sharedWish.lastViewedAt,
            template: sharedWish.templateId
        };

        res.json(response);
    } catch (error) {
        console.error('Get shared wish error:', error);
        res.status(500).json({ error: 'Failed to get shared wish' });
    }
});

module.exports = router;
