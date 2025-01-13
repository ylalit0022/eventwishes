const mongoose = require('mongoose');

const uri = 'mongodb+srv://ylalit0022:jBRgqv6BBfj2lYaG@cluster0.3d1qt.mongodb.net/eventwishes?retryWrites=true&w=majority';

console.log('Attempting to connect to MongoDB...');

mongoose.connect(uri)
  .then(() => {
    console.log('Successfully connected to MongoDB Atlas!');
    process.exit(0);
  })
  .catch(err => {
    console.error('Connection error:', err);
    process.exit(1);
  });
