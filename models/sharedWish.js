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
        required: true
    },
    recipientName: {
        type: String,
        required: true,
        trim: true
    },
    senderName: {
        type: String,
        required: true,
        trim: true
    },
    customizedHtml: {
        type: String,
        required: true
    },
    createdAt: {
        type: Date,
        default: Date.now,
        index: { expires: 7776000 } // 90 days in seconds
    },
    views: {
        type: Number,
        default: 0
    },
    lastViewedAt: {
        type: Date
    }
});

module.exports = mongoose.model('SharedWish', sharedWishSchema);
