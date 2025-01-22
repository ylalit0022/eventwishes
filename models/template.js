const mongoose = require('mongoose');

const templateSchema = new mongoose.Schema({
    title: {
        type: String,
        required: true,
        trim: true
    },
    category: {
        type: String,
        required: true,
        trim: true,
        enum: ['birthday', 'wedding', 'professional', 'general', 'holiday', 'congratulation']
    },
    htmlContent: {
        type: String,
        required: true
    },
    cssContent: {
        type: String,
        required: false,
        default: `
            .content { 
                background: #ffffff; 
                padding: 20px; 
            }
            h1 { 
                color: #333; 
                text-align: center; 
            }
            p { 
                color: #666; 
                line-height: 1.5; 
            }
            .recipient { 
                font-weight: bold; 
            }
            .sender { 
                font-style: italic; 
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

// Update the updatedAt timestamp before saving
templateSchema.pre('save', function(next) {
    this.updatedAt = Date.now();
    next();
});

module.exports = mongoose.model('Template', templateSchema);
