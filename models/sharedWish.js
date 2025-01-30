const mongoose = require('mongoose');

const sharedWishSchema = new mongoose.Schema({
    shortCode: {
        type: String,
        required: true,
        unique: true,
        index: true
    },
    templateId: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'Template',
        required: true,
        index: true
    },
    recipientName: {
        type: String,
        required: true,
        trim: true,
        index: true
    },
    senderName: {
        type: String,
        required: true,
        trim: true,
        index: true
    },
    customizedHtml: {  
        type: String,
        required: false
    },
    createdAt: {
        type: Date,
        default: Date.now,
        index: { expires: 7776000 } // 90 days in seconds
    },
    lastSharedAt: {
        type: Date,
        default: Date.now,
        index: true
    },
    views: {
        type: Number,
        default: 0
    },
    lastViewedAt: {
        type: Date
    }
}, {
    timestamps: true
});

// Compound index for finding duplicate wishes
sharedWishSchema.index({ templateId: 1, recipientName: 1, senderName: 1 });

module.exports = mongoose.model('SharedWish', sharedWishSchema);
