const express = require('express');
const router = express.Router();
const CategoryIcon = require('../models/categoryIcon');

// Get all category icons
router.get('/', async (req, res) => {
    try {
        const icons = await CategoryIcon.find({ isActive: true })
            .sort({ displayOrder: 1, title: 1 });
        res.json(icons);
    } catch (error) {
        console.error('Error fetching category icons:', error);
        res.status(500).json({ error: 'Failed to fetch category icons' });
    }
});

// Get a specific category icon by title
router.get('/:title', async (req, res) => {
    try {
        const icon = await CategoryIcon.findOne({ 
            title: req.params.title,
            isActive: true 
        });
        
        if (!icon) {
            return res.status(404).json({ error: 'Category icon not found' });
        }
        
        res.json(icon);
    } catch (error) {
        console.error('Error fetching category icon:', error);
        res.status(500).json({ error: 'Failed to fetch category icon' });
    }
});

// Create a new category icon (admin only)
router.post('/', async (req, res) => {
    try {
        const { title, iconUrl, displayOrder } = req.body;
        
        // Validate required fields
        if (!title || !iconUrl) {
            return res.status(400).json({
                error: 'Missing required fields',
                details: {
                    title: !title ? 'Missing title' : undefined,
                    iconUrl: !iconUrl ? 'Missing iconUrl' : undefined
                }
            });
        }
        
        // Check for duplicate title
        const existing = await CategoryIcon.findOne({ title });
        if (existing) {
            return res.status(400).json({ error: 'Category with this title already exists' });
        }
        
        const icon = new CategoryIcon({
            title,
            iconUrl,
            displayOrder: displayOrder || 0
        });
        
        await icon.save();
        res.status(201).json(icon);
    } catch (error) {
        console.error('Error creating category icon:', error);
        res.status(500).json({ error: 'Failed to create category icon' });
    }
});

// Update a category icon (admin only)
router.put('/:title', async (req, res) => {
    try {
        const { iconUrl, displayOrder, isActive } = req.body;
        const title = req.params.title;
        
        const icon = await CategoryIcon.findOne({ title });
        if (!icon) {
            return res.status(404).json({ error: 'Category icon not found' });
        }
        
        if (iconUrl) icon.iconUrl = iconUrl;
        if (displayOrder !== undefined) icon.displayOrder = displayOrder;
        if (isActive !== undefined) icon.isActive = isActive;
        
        await icon.save();
        res.json(icon);
    } catch (error) {
        console.error('Error updating category icon:', error);
        res.status(500).json({ error: 'Failed to update category icon' });
    }
});

// Delete a category icon (admin only)
router.delete('/:title', async (req, res) => {
    try {
        const result = await CategoryIcon.deleteOne({ title: req.params.title });
        if (result.deletedCount === 0) {
            return res.status(404).json({ error: 'Category icon not found' });
        }
        res.json({ message: 'Category icon deleted successfully' });
    } catch (error) {
        console.error('Error deleting category icon:', error);
        res.status(500).json({ error: 'Failed to delete category icon' });
    }
});

module.exports = router;
