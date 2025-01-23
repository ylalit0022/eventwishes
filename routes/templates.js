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

// Helper function to get icon for category
function getCategoryIcon(category) {
    // Convert to lowercase for case-insensitive matching
    const lowerCategory = category.toLowerCase();
    
    // Map of category keywords to icons
    const iconMap = {
        'birthday': 'ic_birthday',
        'anniversary': 'ic_anniversary',
        'wedding': 'ic_wedding',
        'graduation': 'ic_graduation',
        'holi': 'ic_holi',
        'all': 'ic_all'
    };

    // Check if category contains any of the keywords
    for (const [keyword, icon] of Object.entries(iconMap)) {
        if (lowerCategory.includes(keyword)) {
            return icon;
        }
    }

    // Default icon
    return 'ic_other';
}

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

        // Get all unique categories and their counts with icons
        const categoryCounts = await Template.aggregate([
            {
                $group: {
                    _id: "$category",  // Keep original case
                    count: { $sum: 1 },
                    iconUrl: { $first: "$categoryIconUrl" }  // Get the most common icon URL for this category
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
        
        // Format category counts and icons
        const categories = {};
        categoryCounts.forEach(cat => {
            if (cat._id && cat._id.trim()) {
                categories[cat._id.trim()] = {
                    count: cat.count,
                    icon: cat.iconUrl || 'https://raw.githubusercontent.com/ylalit0022/eventwishes/main/assets/icons/ic_other.png'  // Use default if no icon
                };
            }
        });

        // Always return a paginated response object
        return res.json({
            data: templates || [],
            page: page,
            totalPages: totalPages,
            totalItems: category && category.toLowerCase() !== 'all' ? totalItems : totalTemplates,
            hasMore: page < totalPages,
            categories: categories,
            totalTemplates: totalTemplates
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
