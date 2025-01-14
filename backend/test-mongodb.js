const mongoose = require('mongoose');
require('dotenv').config();

// Test connection URLs
const connectionURLs = [
    // Atlas connection with correct cluster identifier
    'mongodb+srv://ylalit0022:jBRgqv6BBfj2lYaG@cluster0.3d1qt.mongodb.net/eventwishes?retryWrites=true&w=majority',
    
    // Backup direct connection
    'mongodb://ylalit0022:jBRgqv6BBfj2lYaG@cluster0.3d1qt.mongodb.net:27017/eventwishes',
    
    // Local fallback
    'mongodb://localhost:27017/eventwishes'
];

// Test schema
const TestSchema = new mongoose.Schema({
    name: String,
    createdAt: { type: Date, default: Date.now }
});

async function testConnection(uri) {
    try {
        console.log(`\nTesting connection to: ${uri}`);
        
        // Try to connect
        await mongoose.connect(uri, {
            serverSelectionTimeoutMS: 5000,
            heartbeatFrequencyMS: 2000,
            maxPoolSize: 10,
            minPoolSize: 1,
            maxIdleTimeMS: 30000
        });
        
        console.log('✅ Connected successfully!');

        // Try to create a model and perform a simple operation
        const Test = mongoose.model('Test', TestSchema);
        const testDoc = new Test({ name: 'Test Document' });
        await testDoc.save();
        console.log('✅ Successfully created test document');

        const docs = await Test.find();
        console.log(`✅ Successfully retrieved ${docs.length} documents`);

        // Clean up
        await Test.deleteMany({});
        console.log('✅ Successfully cleaned up test documents');

        await mongoose.connection.close();
        console.log('✅ Connection closed successfully');
        return true;
    } catch (error) {
        console.error('❌ Connection failed:', error.message);
        try {
            await mongoose.connection.close();
        } catch (e) {
            // Ignore close errors
        }
        return false;
    }
}

async function testAllConnections() {
    console.log('🔍 Starting MongoDB connection tests...');
    
    for (const url of connectionURLs) {
        if (!url) {
            console.log('\n⚠️ Skipping empty connection URL');
            continue;
        }
        
        const success = await testConnection(url);
        if (success) {
            console.log('\n✨ Found working connection! Use this URL in your .env file:');
            console.log(url);
            break;
        }
    }
    
    console.log('\n🏁 Connection tests completed');
    process.exit(0);
}

// Run tests
testAllConnections().catch(err => {
    console.error('Fatal error:', err);
    process.exit(1);
});
