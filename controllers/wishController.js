const SharedWish = require('./models/SharedWish');
const crypto = require('crypto');

// Generate short code
const generateShortCode = () => {
    return crypto.randomBytes(4).toString('base64')
        .replace(/[+/=]/g, '')  // remove non-url-safe chars
        + crypto.randomBytes(2).toString('hex'); // add some hex chars
};

// Create shared wish
exports.createSharedWish = async (req, res) => {
    try {
        const { templateId, recipientName, senderName, customizedHtml } = req.body;
        
        const shortCode = generateShortCode();
        
        const sharedWish = new SharedWish({
            shortCode,
            templateId,
            recipientName,
            senderName,
            customizedHtml
        });

        await sharedWish.save();
        res.status(201).json(sharedWish);
    } catch (error) {
        res.status(500).json({ message: error.message });
    }
};

// Get shared wish by short code
exports.getSharedWish = async (req, res) => {
    try {
        const { shortCode } = req.params;
        const sharedWish = await SharedWish.findOne({ shortCode })
            .populate('templateId');

        if (!sharedWish) {
            return res.status(404).json({ message: 'Shared wish not found' });
        }

        // Increment views
        sharedWish.views += 1;
        
        // Track unique views by IP
        const clientIp = req.headers['x-forwarded-for'] || req.connection.remoteAddress;
        if (clientIp && !sharedWish.viewerIps.includes(clientIp)) {
            sharedWish.viewerIps.push(clientIp);
            sharedWish.uniqueViews += 1;
        }
        
        // Track referrer if available
        if (req.headers.referer && !sharedWish.referrer) {
            sharedWish.referrer = req.headers.referer;
        }
        
        // Track device info if available
        if (req.headers['user-agent'] && !sharedWish.deviceInfo) {
            sharedWish.deviceInfo = req.headers['user-agent'];
        }
        
        await sharedWish.save();

        res.json(sharedWish);
    } catch (error) {
        res.status(500).json({ message: error.message });
    }
};

// Update shared wish with sharing platform
exports.updateSharedWishPlatform = async (req, res) => {
    try {
        const { shortCode } = req.params;
        const { platform } = req.body;
        
        if (!platform) {
            return res.status(400).json({ message: 'Platform is required' });
        }
        
        const sharedWish = await SharedWish.findOne({ shortCode });
        
        if (!sharedWish) {
            return res.status(404).json({ message: 'Shared wish not found' });
        }
        
        // Update sharing platform
        sharedWish.sharedVia = platform;
        sharedWish.lastSharedAt = new Date();
        sharedWish.shareCount += 1;
        
        // Add to share history
        sharedWish.shareHistory.push({
            platform,
            timestamp: new Date()
        });
        
        await sharedWish.save();
        
        res.json({ message: 'Shared wish updated successfully' });
    } catch (error) {
        res.status(500).json({ message: error.message });
    }
};

// Get sharing analytics for a wish
exports.getWishAnalytics = async (req, res) => {
    try {
        const { shortCode } = req.params;
        
        const sharedWish = await SharedWish.findOne({ shortCode });
        
        if (!sharedWish) {
            return res.status(404).json({ message: 'Shared wish not found' });
        }
        
        // Prepare analytics data
        const analytics = {
            views: sharedWish.views,
            uniqueViews: sharedWish.uniqueViews,
            shareCount: sharedWish.shareCount,
            lastSharedAt: sharedWish.lastSharedAt,
            shareHistory: sharedWish.shareHistory,
            conversionRate: sharedWish.uniqueViews > 0 ? 
                (sharedWish.shareCount / sharedWish.uniqueViews) : 0,
            platformBreakdown: {}
        };
        
        // Calculate platform breakdown
        if (sharedWish.shareHistory && sharedWish.shareHistory.length > 0) {
            sharedWish.shareHistory.forEach(share => {
                if (!analytics.platformBreakdown[share.platform]) {
                    analytics.platformBreakdown[share.platform] = 0;
                }
                analytics.platformBreakdown[share.platform] += 1;
            });
        }
        
        res.json(analytics);
    } catch (error) {
        res.status(500).json({ message: error.message });
    }
};
