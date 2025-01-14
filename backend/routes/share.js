const express = require('express');
const router = express.Router();
const SharedWish = require('../models/sharedWish');
const Template = require('../models/template');
const shortid = require('shortid');

// Create share link
router.post('/', async (req, res) => {
    try {
        const { templateId, recipientName, senderName } = req.body;

        // Validate required fields
        if (!templateId || !recipientName || !senderName) {
            return res.status(400).json({ 
                error: 'Missing required fields',
                details: {
                    templateId: !templateId ? 'Missing templateId' : undefined,
                    recipientName: !recipientName ? 'Missing recipientName' : undefined,
                    senderName: !senderName ? 'Missing senderName' : undefined
                }
            });
        }

        // Get template
        const template = await Template.findById(templateId);
        if (!template) {
            return res.status(404).json({ error: 'Template not found' });
        }

        // Check if a wish with same template and names already exists
        const existingWish = await SharedWish.findOne({
            templateId,
            recipientName: recipientName.trim(),
            senderName: senderName.trim()
        });

        let shortCode;
        let sharedWish;

        if (existingWish) {
            // Use existing wish
            shortCode = existingWish.shortCode;
            sharedWish = existingWish;
            
            // Update timestamp to show it was shared again
            existingWish.lastSharedAt = new Date();
            await existingWish.save();
            
            console.log('Using existing wish:', shortCode);
        } else {
            // Generate unique short code
            shortCode = shortid.generate();
            
            // Ensure shortCode is unique
            while (await SharedWish.findOne({ shortCode })) {
                shortCode = shortid.generate();
            }

            // Create new shared wish
            sharedWish = new SharedWish({
                shortCode,
                templateId,
                recipientName: recipientName.trim(),
                senderName: senderName.trim(),
                customizedHtml: template.htmlContent,
                createdAt: new Date(),
                lastSharedAt: new Date()
            });

            await sharedWish.save();
            console.log('Created new wish:', shortCode);
        }

        // Generate share URL
        const shareUrl = `${process.env.BASE_URL || 'https://eventwishes.onrender.com'}/wish/${shortCode}`;

        res.json({
            shareUrl,
            shortCode,
            message: 'Wish shared successfully',
            isExisting: !!existingWish
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
        const shortCode = req.params.shortCode;
        
        // Find the shared wish
        const wish = await SharedWish.findOne({ shortCode });
        if (!wish) {
            return res.status(404).json({ error: 'Wish not found' });
        }

        // Update view count and last viewed time
        wish.views = (wish.views || 0) + 1;
        wish.lastViewedAt = new Date();
        await wish.save();

        // Return wish data
        res.json({
            id: wish._id,
            shortCode: wish.shortCode,
            templateId: wish.templateId,
            recipientName: wish.recipientName,
            senderName: wish.senderName,
            customizedHtml: wish.customizedHtml,
            views: wish.views,
            createdAt: wish.createdAt,
            lastViewedAt: wish.lastViewedAt
        });
    } catch (error) {
        console.error('Error fetching shared wish:', error);
        res.status(500).json({ error: 'Failed to fetch shared wish' });
    }
});

module.exports = router;
