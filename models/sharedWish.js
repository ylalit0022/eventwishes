const mongoose = require('mongoose');

const sharedWishSchema = new mongoose.Schema({
    shortCode: {
        type: String,
        required: true,
        unique: true
    },
    templateId: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'Template',
        required: true
    },
    recipientName: {
        type: String,
        required: true
    },
    senderName: {
        type: String,
        required: true
    },
    customizedHtml: {
        type: String,
        required: true
    },
    cssContent: {
        type: String,
        default: ''
    },
    jsContent: {
        type: String,
        default: ''
    },
    sharedVia: {
        type: String,
        enum: ['LINK', 'WHATSAPP', 'FACEBOOK', 'TWITTER', 'INSTAGRAM', 'EMAIL', 'SMS', 'OTHER'],
        default: 'LINK'
    },
    views: {
        type: Number,
        default: 0
    },
    uniqueViews: {
        type: Number,
        default: 0
    },
    viewerIps: {
        type: [String],
        default: []
    },
    shareCount: {
        type: Number,
        default: 0
    },
    shareHistory: [{
        platform: {
            type: String,
            enum: ['LINK', 'WHATSAPP', 'FACEBOOK', 'TWITTER', 'INSTAGRAM', 'EMAIL', 'SMS', 'OTHER']
        },
        timestamp: {
            type: Date,
            default: Date.now
        }
    }],
    lastSharedAt: {
        type: Date,
        default: Date.now
    },
    conversionSource: {
        type: String,
        default: null
    },
    referrer: {
        type: String,
        default: null
    },
    deviceInfo: {
        type: String,
        default: null
    }
}, {
    timestamps: true
});

module.exports = mongoose.model('SharedWish', sharedWishSchema, 'sharedwishes');
