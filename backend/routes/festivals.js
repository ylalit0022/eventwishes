const express = require('express');
const router = express.Router();
const { 
    getUpcomingFestivals,
    getFestivalsByCategory
} = require('../controllers/festivalController');

// Debug middleware
router.use((req, res, next) => {
    console.log('Festival Route:', req.method, req.originalUrl);
    next();
});

router.get('/upcoming', getUpcomingFestivals);
router.get('/category/:category', getFestivalsByCategory);

// Catch-all for undefined routes
router.use('*', (req, res) => {
    console.log('Invalid festival route:', req.originalUrl);
    res.status(404).json({ 
        message: 'Festival route not found',
        requestedUrl: req.originalUrl 
    });
});

module.exports = router; 