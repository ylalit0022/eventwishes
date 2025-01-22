const express = require('express');
const router = express.Router();
const Template = require('../models/template');
const mongoose = require('mongoose');

// Debug route to check database connection and template count
router.get('/debug/status', async (req, res) => {
    try {
        // Check MongoDB connection status
        const dbState = mongoose.connection.readyState;
        const dbStatus = {
            0: 'disconnected',
            1: 'connected',
            2: 'connecting',
            3: 'disconnecting'
        };

        // Get template count
        const count = await Template.countDocuments();
        
        // Get sample of templates (first 5)
        const templates = await Template.find({}).limit(5).select('_id title category');

        res.json({
            databaseStatus: {
                status: dbStatus[dbState],
                readyState: dbState,
                host: mongoose.connection.host,
                name: mongoose.connection.name
            },
            templateStats: {
                totalCount: count,
                sampleTemplates: templates
            }
        });
    } catch (error) {
        console.error('Error getting debug status:', error);
        res.status(500).json({ error: 'Internal server error', details: error.message });
    }
});

// Get all templates with pagination
router.get('/', async (req, res) => {
    try {
        const page = parseInt(req.query.page) || 1;
        const limit = parseInt(req.query.limit) || 20;
        const skip = (page - 1) * limit;

        console.log(`Fetching templates with pagination: page=${page}, limit=${limit}, skip=${skip}`);

        // Get total count for pagination
        const totalItems = await Template.countDocuments();
        const totalPages = Math.ceil(totalItems / limit);

        // Find templates with pagination
        const templates = await Template.find({})
            .sort({ createdAt: -1 }) // Sort by newest first
            .skip(skip)
            .limit(limit)
            .lean(); // Convert to plain JavaScript objects

        console.log(`Found ${templates ? templates.length : 0} templates for page ${page}`);
        
        // Always return a paginated response object
        return res.json({
            data: templates || [],
            page: page,
            totalPages: totalPages,
            totalItems: totalItems,
            hasMore: page < totalPages
        });
    } catch (error) {
        console.error('Error getting templates:', error);
        return res.status(500).json({
            data: [],
            page: 1,
            totalPages: 0,
            totalItems: 0,
            hasMore: false,
            error: 'Internal server error'
        });
    }
});

// Get all templates CSS
router.get('/css/all', async (req, res) => {
    try {
        // Find all templates and select only necessary fields
        const templates = await Template.find({}, 'title cssContent category _id');
        
        if (!templates || templates.length === 0) {
            return res.status(404).json({ error: 'No templates found' });
        }

        // Return array of templates with their CSS
        res.json({
            templates: templates.map(template => ({
                cssContent: template.cssContent,
                templateId: template._id,
                title: template.title,
                category: template.category
            }))
        });
    } catch (error) {
        console.error('Error getting templates CSS:', error);
        res.status(500).json({ error: 'Internal server error' });
    }
});

// Get template CSS
router.get('/:templateId/css', async (req, res) => {
    try {
        const { templateId } = req.params;

        // Find template
        const template = await Template.findById(templateId);
        if (!template) {
            return res.status(404).json({ error: 'Template not found' });
        }

        // Return the template's CSS content
        res.json({
            cssContent: template.cssContent,
            templateId: template._id,
            title: template.title,
            category: template.category
        });
    } catch (error) {
        console.error('Error getting template CSS:', error);
        res.status(500).json({ error: 'Internal server error' });
    }
});

module.exports = router;
