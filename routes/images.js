const express = require('express');
const router = express.Router();
const imageController = require('../controllers/imageController');

// Get image by URL
router.get('/fetch', imageController.fetchImage);

module.exports = router; 