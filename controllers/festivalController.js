const Festival = require('../models/Festival');
const { startOfDay, endOfDay, addDays } = require('date-fns');

exports.getUpcomingFestivals = async (req, res) => {
    console.log('Getting upcoming festivals...');
    try {
        const today = startOfDay(new Date());
        const threeDaysLater = endOfDay(addDays(today, 3));

        console.log('Date range:', { today, threeDaysLater });

        const festivals = await Festival.find({
            date: {
                $gte: today,
                $lte: threeDaysLater
            },
            isActive: true
        })
        .populate('templates')
        .sort({ date: 1 });

        console.log('Found festivals:', festivals.length);
        res.json(festivals);
    } catch (error) {
        console.error('Error in getUpcomingFestivals:', error);
        res.status(500).json({ 
            message: error.message,
            stack: process.env.NODE_ENV === 'development' ? error.stack : undefined
        });
    }
};

exports.getFestivalsByCategory = async (req, res) => {
    const { category } = req.params;
    console.log('Getting festivals by category:', category);

    try {
        if (!category) {
            throw new Error('Category parameter is required');
        }

        const festivals = await Festival.find({ 
            category,
            isActive: true 
        })
        .populate('templates')
        .sort({ date: 1 });

        console.log('Found festivals for category:', festivals.length);

        if (festivals.length === 0) {
            return res.status(404).json({ 
                message: `No festivals found for category: ${category}`,
                category 
            });
        }

        res.json(festivals);
    } catch (error) {
        console.error('Error in getFestivalsByCategory:', error);
        res.status(500).json({ 
            message: error.message,
            category,
            stack: process.env.NODE_ENV === 'development' ? error.stack : undefined
        });
    }
}; 