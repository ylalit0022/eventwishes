const https = require('https');
const http = require('http');

/**
 * Fetch an image from a URL and stream it to the response
 */
exports.fetchImage = async (req, res) => {
    const { url } = req.query;

    if (!url) {
        return res.status(400).json({
            success: false,
            message: 'URL parameter is required'
        });
    }

    try {
        // Determine if URL is HTTP or HTTPS
        const client = url.startsWith('https') ? https : http;

        // Create request options
        const options = {
            headers: {
                'User-Agent': 'EventWish/1.0',
                'Accept': 'image/*'
            },
            timeout: 10000 // 10 seconds timeout
        };

        // Make the request
        const request = client.get(url, options, (response) => {
            // Check if response is an image
            const contentType = response.headers['content-type'];
            if (!contentType || !contentType.startsWith('image/')) {
                return res.status(400).json({
                    success: false,
                    message: 'URL does not point to a valid image'
                });
            }

            // Set appropriate headers
            res.setHeader('Content-Type', contentType);
            res.setHeader('Cache-Control', 'public, max-age=31536000'); // Cache for 1 year
            res.setHeader('Access-Control-Allow-Origin', '*');

            // Pipe the image data to response
            response.pipe(res);
        });

        // Handle request errors
        request.on('error', (error) => {
            console.error('Error fetching image:', error);
            res.status(500).json({
                success: false,
                message: 'Failed to fetch image',
                error: error.message
            });
        });

        // Handle timeout
        request.on('timeout', () => {
            request.destroy();
            res.status(504).json({
                success: false,
                message: 'Request timeout while fetching image'
            });
        });

    } catch (error) {
        console.error('Error in image controller:', error);
        res.status(500).json({
            success: false,
            message: 'Internal server error',
            error: error.message
        });
    }
}; 