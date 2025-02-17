const mongoose = require('mongoose');
const Festival = require('../models/Festival');
require('dotenv').config();

const festivals = [
    {
        name: "Christmas",
        date: new Date("2024-12-25"),
        description: "Annual Christmas celebration",
        category: "Holiday",
        imageUrl: "https://example.com/christmas.jpg",
        isActive: true
    },
    {
        name: "New Year",
        date: new Date("2024-01-01"),
        description: "New Year celebration",
        category: "Holiday",
        imageUrl: "https://example.com/newyear.jpg",
        isActive: true
    },
    {
        name: "Today's Festival",
        date: new Date(), // Today's date
        description: "Test festival for today",
        category: "Holiday",
        imageUrl: "https://example.com/today.jpg",
        isActive: true
    },
    {
        name: "Tomorrow's Festival",
        date: new Date(Date.now() + 24 * 60 * 60 * 1000), // Tomorrow
        description: "Test festival for tomorrow",
        category: "Holiday",
        imageUrl: "https://example.com/tomorrow.jpg",
        isActive: true
    }
];

const seedFestivals = async () => {
    try {
        console.log('Connecting to MongoDB...');
        await mongoose.connect(process.env.MONGODB_URI);
        console.log('Connected to MongoDB');

        console.log('Clearing existing festivals...');
        await Festival.deleteMany({});
        
        console.log('Inserting new festivals...');
        const insertedFestivals = await Festival.insertMany(festivals);
        console.log('Festivals seeded successfully:', insertedFestivals.length);

        // Verify the data
        const count = await Festival.countDocuments();
        console.log('Total festivals in database:', count);

        const holidayCount = await Festival.countDocuments({ category: 'Holiday' });
        console.log('Holiday category festivals:', holidayCount);

        process.exit(0);
    } catch (error) {
        console.error('Error seeding festivals:', error);
        process.exit(1);
    }
};

seedFestivals();