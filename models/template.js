const mongoose = require('mongoose');

const templateSchema = new mongoose.Schema({
    title: {
        type: String,
        required: true,
        trim: true
    },
    content: {
        type: String,
        required: true
    },
    category: {
        type: String,
        required: true,
        trim: true,
        enum: ['birthday', 'wedding', 'professional', 'general', 'holiday', 'congratulation']
    },
    categoryIconUrl: {
        type: String,
        required: false,  // Optional, will use default if not specified
        trim: true,
        default: 'https://raw.githubusercontent.com/ylalit0022/eventwishes/main/assets/icons/ic_other.png'  // Default icon URL
    },
    css: {
        type: String,
        required: false,
        default: `
            .template-container {
                padding: 20px;
                text-align: center;
            }
        `
    },
    previewUrl: {
        type: String,
        required: false
    },
    createdAt: {
        type: Date,
        default: Date.now
    },
    updatedAt: {
        type: Date,
        default: Date.now
    }
});

// Update timestamps on save
templateSchema.pre('save', function(next) {
    this.updatedAt = Date.now();
    next();
});

module.exports = mongoose.model('Template', templateSchema);
