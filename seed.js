const mongoose = require('mongoose');
const Template = require('./models/template');

// MongoDB connection string (same as server.js)
const MONGODB_URI = 'mongodb+srv://ylalit0022:jBRgqv6BBfj2lYaG@cluster0.3d1qt.mongodb.net/eventwishes?retryWrites=true&w=majority';

mongoose.set('strictQuery', false); // Add this line to handle deprecation warning

// Sample template data
const sampleTemplates = [
    {
        title: "Birthday Wishes",
        category: "Birthday",
        htmlContent: `
            <div style="text-align: center; padding: 20px;">
                <h1>Happy Birthday! 🎉</h1>
                <p>Wishing you a day filled with joy and laughter!</p>
                <p>May all your dreams come true.</p>
                <p>🎂 🎈 🎁</p>
            </div>
        `,
        previewUrl: "https://example.com/birthday-preview.jpg"
    },
    {
        title: "Wedding Anniversary",
        category: "Anniversary",
        htmlContent: `
            <div style="text-align: center; padding: 20px;">
                <h1>Happy Anniversary! 💑</h1>
                <p>Celebrating your special day with love and joy!</p>
                <p>Here's to many more years of happiness together.</p>
                <p>💝 💐 🥂</p>
            </div>
        `,
        previewUrl: "https://example.com/anniversary-preview.jpg"
    },
    {
        title: "Graduation Congratulations",
        category: "Graduation",
        htmlContent: `
            <div style="text-align: center; padding: 20px;">
                <h1>Congratulations Graduate! 🎓</h1>
                <p>You've worked hard and achieved your goals!</p>
                <p>Wishing you success in all your future endeavors.</p>
                <p>📚 🎊 ⭐</p>
            </div>
        `,
        previewUrl: "https://example.com/graduation-preview.jpg"
    },
    {
        title: "New Year Greetings",
        category: "New Year",
        htmlContent: `
            <div style="text-align: center; padding: 20px;">
                <h1>Happy New Year! 🎆</h1>
                <p>May the coming year bring you joy, success, and prosperity!</p>
                <p>Here's to new beginnings and amazing adventures.</p>
                <p>✨ 🎉 🥳</p>
            </div>
        `,
        previewUrl: "https://example.com/newyear-preview.jpg"
    },
    {
        title: "Get Well Soon",
        category: "Get Well",
        htmlContent: `
            <div style="text-align: center; padding: 20px;">
                <h1>Get Well Soon! 💐</h1>
                <p>Sending you healing thoughts and warm wishes.</p>
                <p>Hope you feel better soon!</p>
                <p>🌟 💝 🌺</p>
            </div>
        `,
        previewUrl: "https://example.com/getwell-preview.jpg"
    }
];

// Connect to MongoDB
mongoose.connect(MONGODB_URI)
    .then(() => {
        console.log('Connected to MongoDB');
        return Template.deleteMany({}); // Clear existing templates
    })
    .then(() => {
        console.log('Existing templates cleared');
        return Template.insertMany(sampleTemplates);
    })
    .then((result) => {
        console.log(`${result.length} templates inserted successfully`);
        mongoose.connection.close();
    })
    .catch((error) => {
        console.error('Error seeding data:', error);
        mongoose.connection.close();
    });
