const express = require('express');
const router = express.Router();
const mongoose = require('mongoose');
const SharedWish = require('../models/sharedWish');
const Template = require('../models/template');
const shortid = require('shortid');

// Create share link
router.post('/', async (req, res) => {
    try {
        console.log('[DEBUG] Share request body:', JSON.stringify(req.body, null, 2));
        
        const { templateId, recipientName, senderName, htmlContent } = req.body;
        
        console.log('[DEBUG] Extracted fields:', {
            templateId: templateId || 'missing',
            recipientName: recipientName || 'missing',
            senderName: senderName || 'missing',
            htmlContent: htmlContent ? 'present' : 'missing',
            htmlContentLength: htmlContent ? htmlContent.length : 0
        });

        // Validate required fields
        if (!templateId || !recipientName || !senderName || !htmlContent) {
            const missingFields = {
                templateId: !templateId ? 'Missing template ID' : undefined,
                recipientName: !recipientName ? 'Missing recipientName' : undefined,
                senderName: !senderName ? 'Missing senderName' : undefined,
                htmlContent: !htmlContent ? 'Missing htmlContent' : undefined
            };
            
            console.log('[DEBUG] Validation failed - missing fields:', missingFields);
            
            return res.status(400).json({ 
                error: 'Missing required fields',
                missingFields: Object.keys(missingFields).filter(key => missingFields[key]),
                received: req.body
            });
        }

        // Convert templateId to ObjectId
        let objectId;
        try {
            objectId = new mongoose.Types.ObjectId(templateId);
        } catch (err) {
            console.log('[DEBUG] Invalid template ID format:', templateId);
            return res.status(400).json({ 
                error: 'Invalid template ID format',
                details: err.message
            });
        }

        // Get template
        const template = await Template.findById(objectId);
        if (!template) {
            console.log('[DEBUG] Template not found:', templateId);
            return res.status(404).json({ error: 'Template not found' });
        }
        console.log('[DEBUG] Found template:', template._id);

        // Check if a wish with same template and names already exists
        const existingWish = await SharedWish.findOne({
            templateId: objectId,
            recipientName: recipientName.trim(),
            senderName: senderName.trim()
        });

        if (existingWish) {
            console.log('[DEBUG] Found existing wish:', existingWish._id);
            return res.json({
                success: true,
                data: {
                    shortCode: existingWish.shortCode,
                    shareUrl: process.env.BASE_URL + '/w/' + existingWish.shortCode,
                    previewImageUrl: template.previewImageUrl
                }
            });
        }

        // Generate short code
        const shortCode = shortid.generate();
        console.log('[DEBUG] Generated short code:', shortCode);

        // Create new shared wish
        const sharedWish = new SharedWish({
            shortCode,
            templateId: objectId,
            recipientName: recipientName.trim(),
            senderName: senderName.trim(),
            htmlContent: htmlContent
        });

        console.log('[DEBUG] Creating new wish:', {
            shortCode,
            templateId: objectId,
            recipientName: recipientName.trim(),
            senderName: senderName.trim(),
            htmlContentLength: htmlContent ? htmlContent.length : 0
        });

        await sharedWish.save();
        console.log('[DEBUG] Saved new wish:', sharedWish._id);

        // Return success response
        return res.json({
            success: true,
            data: {
                shortCode: shortCode,
                shareUrl: process.env.BASE_URL + '/w/' + shortCode,
                previewImageUrl: template.previewImageUrl
            }
        });

    } catch (error) {
        console.error('[ERROR] Share error:', error);
        console.error('[ERROR] Stack trace:', error.stack);
        
        // Better error handling
        let errorMessage = 'Internal server error';
        let statusCode = 500;
        
        if (error.name === 'ValidationError') {
            statusCode = 400;
            errorMessage = 'Validation failed';
            const errors = {};
            
            // Extract validation errors
            for (let field in error.errors) {
                errors[field] = error.errors[field].message;
            }
            
            return res.status(statusCode).json({
                error: errorMessage,
                details: errors
            });
        }
        
        return res.status(statusCode).json({ error: errorMessage });
    }
});

// Get shared wish by short code
router.get('/:shortCode', async (req, res) => {
    try {
        const shortCode = req.params.shortCode;
        console.log('[DEBUG] Fetching wish with shortCode:', shortCode);
        
        const wish = await SharedWish.findOne({ shortCode });
        if (!wish) {
            console.log('[DEBUG] Wish not found:', shortCode);
            return res.status(404).json({ error: 'Wish not found' });
        }

        console.log('[DEBUG] Found wish:', wish._id);
        return res.json({
            success: true,
            data: wish
        });

    } catch (error) {
        console.error('[ERROR] Wish fetch failed:', error);
        return res.status(500).json({ error: 'Internal server error' });
    }
});

module.exports = router;
