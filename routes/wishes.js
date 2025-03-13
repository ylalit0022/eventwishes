const express = require('express');
const router = express.Router();
const { 
    createSharedWish, 
    getSharedWish,
    updateSharedWishPlatform,
    getWishAnalytics
} = require('../controllers/wishController');
const SharedWish = require('../models/SharedWish');

// Create shared wish
router.post('/create', createSharedWish);

// Get shared wish by short code
router.get('/:shortCode', getSharedWish);

// Update shared wish platform
router.post('/:shortCode/share', async (req, res) => {
    try {
        const { shortCode } = req.params;
        const { platform } = req.body;
        
        if (!platform) {
            return res.status(400).json({ message: 'Platform is required' });
        }
        
        // Find the shared wish
        const wish = await SharedWish.findOne({ shortCode });
        
        if (!wish) {
            return res.status(404).json({ message: 'Shared wish not found' });
        }
        
        // Update the share history
        wish.shareHistory.push({
            platform,
            timestamp: new Date()
        });
        
        // Update share count and last shared time
        wish.shareCount = (wish.shareCount || 0) + 1;
        wish.lastSharedAt = new Date();
        wish.sharedVia = platform;
        
        await wish.save();
        
        return res.status(200).json({ 
            message: 'Share platform updated',
            shareCount: wish.shareCount
        });
    } catch (error) {
        console.error('Error updating share platform:', error);
        return res.status(500).json({ message: 'Error updating share platform' });
    }
});

// Get wish analytics
router.get('/:shortCode/analytics', async (req, res) => {
    try {
        const { shortCode } = req.params;
        
        // Find the shared wish
        const wish = await SharedWish.findOne({ shortCode });
        
        if (!wish) {
            return res.status(404).json({ message: 'Shared wish not found' });
        }
        
        // Prepare analytics data
        const analytics = {
            views: wish.views || 0,
            uniqueViews: wish.uniqueViews || 0,
            shareCount: wish.shareCount || 0,
            shareHistory: wish.shareHistory || [],
            lastSharedAt: wish.lastSharedAt,
            conversionSource: wish.conversionSource,
            referrer: wish.referrer,
            deviceInfo: wish.deviceInfo
        };
        
        return res.status(200).json(analytics);
    } catch (error) {
        console.error('Error getting wish analytics:', error);
        return res.status(500).json({ message: 'Error getting wish analytics' });
    }
});

// Track app installation from landing page
router.post('/:shortCode/analytics/install', async (req, res) => {
    try {
        const { shortCode } = req.params;
        const SharedWish = require('../models/SharedWish');
        
        const sharedWish = await SharedWish.findOne({ shortCode });
        
        if (!sharedWish) {
            return res.status(404).json({ message: 'Shared wish not found' });
        }
        
        // Update conversion source
        sharedWish.conversionSource = 'LANDING_PAGE';
        await sharedWish.save();
        
        res.status(200).json({ message: 'Installation tracked successfully' });
    } catch (error) {
        console.error('Error tracking installation:', error);
        res.status(500).json({ message: 'Error tracking installation' });
    }
});

module.exports = router;
