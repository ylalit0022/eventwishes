const Festival = require('../models/Festival');

/**
 * Get the current server time
 * @param {Object} req - Express request object
 * @param {Object} res - Express response object
 */
exports.getServerTime = async (req, res) => {
    try {
        // Return the current server timestamp
        res.json({
            success: true,
            timestamp: Date.now(),
            date: new Date().toISOString()
        });
    } catch (error) {
        console.error('Error getting server time:', error);
        res.status(500).json({
            success: false,
            message: 'Error getting server time',
            error: error.message
        });
    }
};

/**
 * Test a notification for a specific date and festival
 * @param {Object} req - Express request object
 * @param {Object} res - Express response object
 */
exports.testNotification = async (req, res) => {
    try {
        const { date, festivalId } = req.query;
        
        if (!date) {
            return res.status(400).json({
                success: false,
                message: 'Date is required'
            });
        }
        
        // Parse the date
        const testDate = new Date(date);
        
        // Validate the date
        if (isNaN(testDate.getTime())) {
            return res.status(400).json({
                success: false,
                message: 'Invalid date format. Use YYYY-MM-DD'
            });
        }
        
        // If festivalId is provided, get the festival
        let festival = null;
        if (festivalId) {
            festival = await Festival.findById(festivalId);
            
            if (!festival) {
                return res.status(404).json({
                    success: false,
                    message: 'Festival not found'
                });
            }
        } else {
            // If no festivalId is provided, find festivals on the test date
            const festivals = await Festival.find({
                date: {
                    $gte: new Date(testDate.setHours(0, 0, 0, 0)),
                    $lt: new Date(testDate.setHours(23, 59, 59, 999))
                }
            });
            
            if (festivals.length === 0) {
                return res.status(404).json({
                    success: false,
                    message: 'No festivals found on the specified date'
                });
            }
            
            // Use the first festival found
            festival = festivals[0];
        }
        
        // Simulate the notification
        const notificationData = {
            title: `Test: ${festival.name}`,
            body: `This is a test notification for ${festival.name} on ${testDate.toDateString()}`,
            festivalId: festival._id.toString(),
            imageUrl: festival.imageUrl
        };
        
        // Log the test notification
        console.log('Test notification:', notificationData);
        
        // Return success
        res.json({
            success: true,
            message: `Test notification sent for ${festival.name} on ${testDate.toDateString()}`,
            notification: notificationData
        });
    } catch (error) {
        console.error('Error testing notification:', error);
        res.status(500).json({
            success: false,
            message: 'Error testing notification',
            error: error.message
        });
    }
}; 