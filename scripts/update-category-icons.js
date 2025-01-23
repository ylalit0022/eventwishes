const mongoose = require('mongoose');
const Template = require('../models/template');

const MONGODB_URI = 'mongodb+srv://ylalit0022:jBRgqv6BBfj2lYaG@cluster0.3d1qt.mongodb.net/eventwishes?retryWrites=true&w=majority';

// Default icon mapping (for initial data)
const defaultIcons = {
    'birthday': 'https://raw.githubusercontent.com/ylalit0022/eventwishes/main/assets/icons/ic_birthday.png',
    'wedding': 'https://raw.githubusercontent.com/ylalit0022/eventwishes/main/assets/icons/ic_wedding.png',
    'anniversary': 'https://raw.githubusercontent.com/ylalit0022/eventwishes/main/assets/icons/ic_anniversary.png',
    'graduation': 'https://raw.githubusercontent.com/ylalit0022/eventwishes/main/assets/icons/ic_graduation.png',
    'holi': 'https://raw.githubusercontent.com/ylalit0022/eventwishes/main/assets/icons/ic_holi.png',
    'professional': 'https://raw.githubusercontent.com/ylalit0022/eventwishes/main/assets/icons/ic_other.png',
    'general': 'https://raw.githubusercontent.com/ylalit0022/eventwishes/main/assets/icons/ic_other.png',
    'holiday': 'https://raw.githubusercontent.com/ylalit0022/eventwishes/main/assets/icons/ic_other.png',
    'congratulation': 'https://raw.githubusercontent.com/ylalit0022/eventwishes/main/assets/icons/ic_other.png'
};

async function updateCategoryIcons() {
    try {
        // Connect to MongoDB
        console.log('Connecting to MongoDB...');
        await mongoose.connect(MONGODB_URI, {
            useNewUrlParser: true,
            useUnifiedTopology: true,
            serverSelectionTimeoutMS: 30000, // Increase timeout to 30 seconds
            socketTimeoutMS: 45000, // Increase socket timeout to 45 seconds
        });
        console.log('Connected to MongoDB successfully');

        // Get all templates
        console.log('Fetching templates...');
        const templates = await Template.find({});
        console.log(`Found ${templates.length} templates to update`);

        // Update each template
        let updatedCount = 0;
        for (const template of templates) {
            const category = template.category.toLowerCase();
            const iconUrl = defaultIcons[category] || 'https://raw.githubusercontent.com/ylalit0022/eventwishes/main/assets/icons/ic_other.png';
            
            try {
                await Template.updateOne(
                    { _id: template._id },
                    { $set: { categoryIconUrl: iconUrl } }
                );
                updatedCount++;
                console.log(`Updated template ${template._id} with icon URL ${iconUrl} (${updatedCount}/${templates.length})`);
            } catch (updateError) {
                console.error(`Error updating template ${template._id}:`, updateError);
            }
        }

        console.log(`Category icon update complete. Updated ${updatedCount} templates.`);
    } catch (error) {
        console.error('Error updating category icons:', error);
    } finally {
        try {
            await mongoose.disconnect();
            console.log('Disconnected from MongoDB');
        } catch (disconnectError) {
            console.error('Error disconnecting from MongoDB:', disconnectError);
        }
        process.exit(0);
    }
}

// Run the migration
console.log('Starting category icon update...');
updateCategoryIcons();
