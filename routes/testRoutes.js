const express = require('express');
const router = express.Router();
const testController = require('../controllers/testController');

// Get server time
router.get('/time', testController.getServerTime);

// Test notification
router.get('/notification', testController.testNotification);

module.exports = router; 