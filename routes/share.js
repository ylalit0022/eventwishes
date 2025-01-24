const express = require('express');
const router = express.Router();
const SharedWish = require('../models/sharedWish');
const Template = require('../models/template');
const shortid = require('shortid');

// Create share link
router.post('/', async (req, res) => {
    try {
        console.log('[DEBUG] Share request body:', JSON.stringify(req.body, null, 2));
        
        const { _id, recipientName, senderName, htmlContent } = req.body;
        
        console.log('[DEBUG] Extracted fields:', {
            _id: _id || 'missing',
            recipientName: recipientName || 'missing',
            senderName: senderName || 'missing',
            htmlContent: htmlContent ? 'present' : 'missing',
            htmlContentLength: htmlContent ? htmlContent.length : 0
        });

        // Validate required fields
        if (!_id || !recipientName || !senderName || !htmlContent) {
            const missingFields = {
                _id: !_id ? 'Missing template ID' : undefined,
                recipientName: !recipientName ? 'Missing recipientName' : undefined,
                senderName: !senderName ? 'Missing senderName' : undefined,
                htmlContent: !htmlContent ? 'Missing htmlContent' : undefined
            };
            
            console.log('[DEBUG] Validation failed - missing fields:', missingFields);
            
            return res.status(400).json({ 
                error: 'Missing required fields',
                details: missingFields
            });
        }

        // Get template
        const template = await Template.findById(_id);
        if (!template) {
            console.log('[DEBUG] Template not found:', _id);
            return res.status(404).json({ error: 'Template not found' });
        }
        console.log('[DEBUG] Found template:', template._id);

        // Check if a wish with same template and names already exists
        const existingWish = await SharedWish.findOne({
            templateId: _id,
            recipientName: recipientName.trim(),
            senderName: senderName.trim()
        });

        let shortCode;
        let sharedWish;

        if (existingWish) {
            console.log('[DEBUG] Updating existing wish:', existingWish._id);
            // Use existing wish but update htmlContent
            shortCode = existingWish.shortCode;
            sharedWish = existingWish;
            
            // Update htmlContent and timestamp
            existingWish.htmlContent = htmlContent;
            existingWish.lastSharedAt = new Date();
            await existingWish.save();
            
            console.log('[DEBUG] Updated existing wish:', shortCode);
        } else {
            // Generate unique short code
            shortCode = shortid.generate();
            
            // Ensure shortCode is unique
            while (await SharedWish.findOne({ shortCode })) {
                shortCode = shortid.generate();
            }

            console.log('[DEBUG] Creating new wish with shortCode:', shortCode);
            
            // Create new shared wish
            sharedWish = new SharedWish({
                shortCode,
                templateId: _id,
                recipientName: recipientName.trim(),
                senderName: senderName.trim(),
                htmlContent,
                createdAt: new Date(),
                lastSharedAt: new Date()
            });

            await sharedWish.save();
            console.log('[DEBUG] Created new wish:', sharedWish._id);
        }

        // Generate share URL
        const shareUrl = `${process.env.BASE_URL || 'https://eventwishes.onrender.com'}/wish/${shortCode}`;
        console.log('[DEBUG] Generated share URL:', shareUrl);

        res.json({
            shareUrl,
            shortCode,
            message: 'Wish shared successfully',
            isExisting: !!existingWish
        });
    } catch (error) {
        console.error('[ERROR] Share error:', error);
        console.error('[ERROR] Stack trace:', error.stack);
        
        // Better error handling
        if (error.name === 'ValidationError') {
            const details = Object.keys(error.errors).reduce((acc, key) => {
                acc[key] = error.errors[key].message;
                return acc;
            }, {});
            
            console.log('[DEBUG] Validation error details:', details);
            
            return res.status(400).json({
                error: 'Validation Error',
                details
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
        console.log('[DEBUG] Fetching wish with shortCode:', shortCode);
        
        // Find the shared wish
        const wish = await SharedWish.findOne({ shortCode });
        if (!wish) {
            console.log('[DEBUG] Wish not found:', shortCode);
            return res.status(404).json({ error: 'Wish not found' });
        }

        console.log('[DEBUG] Found wish:', wish._id);

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
            htmlContent: wish.htmlContent,
            views: wish.views,
            createdAt: wish.createdAt,
            lastViewedAt: wish.lastViewedAt
        });
    } catch (error) {
        console.error('[ERROR] Error fetching shared wish:', error);
        console.error('[ERROR] Stack trace:', error.stack);
        res.status(500).json({ error: 'Failed to fetch shared wish' });
    }
});

module.exports = router;
