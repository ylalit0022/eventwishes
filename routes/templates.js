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
        const category = req.query.category ? req.query.category.trim() : null;

        console.log(`Fetching templates with pagination: page=${page}, limit=${limit}, skip=${skip}, category=${category}`);

        // Build query
        const query = {};
        if (category && category.toLowerCase() !== 'all') {
            // Case-insensitive exact match for category
            query.category = { $regex: new RegExp(`^${category}$`, 'i') };
        }

        // Get total count for the current query
        const totalItems = await Template.countDocuments(query);
        
        // Get total count across all categories
        const totalTemplates = await Template.countDocuments({});

        // Get all unique categories and their counts
        const categoryCounts = await Template.aggregate([
            {
                $group: {
                    _id: "$category",  // Keep original case
                    count: { $sum: 1 }
                }
            },
            {
                $match: {
                    _id: { $nin: [null, ""] }  // Exclude null and empty categories
                }
            },
            {
                $sort: { _id: 1 }
            }
        ]);

        const totalPages = Math.ceil(totalItems / limit);

        // Find templates with pagination
        const templates = await Template.find(query)
            .sort({ createdAt: -1 }) // Sort by newest first
            .skip(skip)
            .limit(limit)
            .lean(); // Convert to plain JavaScript objects

        console.log(`Found ${templates ? templates.length : 0} templates for category: ${category}`);
        console.log('Category counts:', categoryCounts);
        console.log(`Total templates across all categories: ${totalTemplates}`);
        
        // Format category counts - keep original case
        const categories = categoryCounts.reduce((acc, curr) => {
            if (curr._id && curr._id.trim()) {
                acc[curr._id.trim()] = curr.count;
            }
            return acc;
        }, {});

        // Add total count to response
        categories['All'] = totalTemplates;

        // Always return a paginated response object
        return res.json({
            data: templates || [],
            page: page,
            totalPages: totalPages,
            totalItems: totalItems,
            hasMore: page < totalPages,
            categories: categories,
            totalTemplates: totalTemplates  // Add total templates count
        });
    } catch (error) {
        console.error('Error getting templates:', error);
        return res.status(500).json({
            data: [],
            page: 1,
            totalPages: 0,
            totalItems: 0,
            hasMore: false,
            categories: {},
            totalTemplates: 0,
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
