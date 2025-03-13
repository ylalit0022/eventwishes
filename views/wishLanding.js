/**
 * Generate an enhanced landing page for shared wishes
 * @param {Object} wish The shared wish object
 * @param {String} shortCode The wish short code
 * @returns {String} HTML for the landing page
 */
function generateWishLandingPage(wish, shortCode) {
    // Default values
    let wishTitle = 'Special Wish';
    let senderName = 'Someone';
    let recipientName = 'you';
    let previewImage = '/images/default-preview.jpg';
    
    // Use wish data if available
    if (wish) {
        senderName = wish.senderName || 'Someone';
        recipientName = wish.recipientName || 'you';
        wishTitle = `A special wish from ${senderName} to ${recipientName}`;
        
        if (wish.templateId && wish.templateId.thumbnailUrl) {
            previewImage = wish.templateId.thumbnailUrl;
        }
    }
    
    return `
        <!DOCTYPE html>
        <html>
        <head>
            <title>${wishTitle} | EventWish</title>
            <meta name="viewport" content="width=device-width, initial-scale=1">
            <meta property="og:title" content="${wishTitle}" />
            <meta property="og:description" content="Open this special wish from ${senderName}" />
            <meta property="og:image" content="${previewImage}" />
            <meta property="og:url" content="https://eventwishes.onrender.com/wish/${shortCode}" />
            <meta name="twitter:card" content="summary_large_image" />
            <style>
                body { 
                    font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; 
                    text-align: center; 
                    padding: 0; 
                    margin: 0;
                    background-color: #f8f9fa;
                    color: #333;
                }
                .container {
                    max-width: 600px;
                    margin: 0 auto;
                    padding: 20px;
                }
                .header {
                    background-color: #6200ee;
                    color: white;
                    padding: 20px;
                    margin-bottom: 20px;
                    border-radius: 0 0 15px 15px;
                    box-shadow: 0 4px 6px rgba(0,0,0,0.1);
                }
                h1 {
                    margin: 0;
                    font-size: 24px;
                }
                .preview {
                    background-color: white;
                    border-radius: 15px;
                    padding: 20px;
                    margin-bottom: 20px;
                    box-shadow: 0 4px 6px rgba(0,0,0,0.1);
                }
                .preview img {
                    max-width: 100%;
                    border-radius: 10px;
                    margin-bottom: 15px;
                }
                .message {
                    font-size: 18px;
                    margin-bottom: 20px;
                }
                .button { 
                    display: inline-block;
                    padding: 12px 24px;
                    margin: 10px;
                    background-color: #6200ee;
                    color: white;
                    text-decoration: none;
                    border-radius: 50px;
                    font-weight: bold;
                    box-shadow: 0 4px 6px rgba(0,0,0,0.1);
                    transition: all 0.3s ease;
                }
                .button:hover {
                    transform: translateY(-2px);
                    box-shadow: 0 6px 8px rgba(0,0,0,0.15);
                }
                .button.secondary {
                    background-color: #03DAC6;
                }
                .footer {
                    margin-top: 30px;
                    font-size: 14px;
                    color: #666;
                }
                .hidden {
                    opacity: 0;
                    transition: opacity 0.5s ease;
                }
                .visible {
                    opacity: 1;
                }
                .social-share {
                    margin: 20px 0;
                }
                .social-button {
                    display: inline-block;
                    margin: 0 5px;
                    width: 40px;
                    height: 40px;
                    border-radius: 50%;
                    background-color: #f1f1f1;
                    line-height: 40px;
                    text-align: center;
                    text-decoration: none;
                    color: #333;
                    font-size: 18px;
                    transition: all 0.3s ease;
                }
                .social-button:hover {
                    transform: scale(1.1);
                }
            </style>
            <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.3/css/all.min.css">
        </head>
        <body>
            <div class="header">
                <h1>EventWish</h1>
            </div>
            <div class="container hidden" id="content">
                <div class="preview">
                    <img src="${previewImage}" alt="Wish Preview">
                    <div class="message">
                        <p><strong>${senderName}</strong> sent you a special wish!</p>
                        <p>Open it in the app for the best experience.</p>
                    </div>
                </div>
                
                <a href="eventwish://wish/${shortCode}" class="button" id="openAppButton">Open in App</a>
                <a href="https://play.google.com/store/apps/details?id=com.ds.eventwish" class="button secondary" id="getAppButton">Get the App</a>
                
                <div class="social-share">
                    <p>Share this wish:</p>
                    <a href="https://wa.me/?text=${encodeURIComponent(`Check out this special wish: https://eventwishes.onrender.com/wish/${shortCode}`)}" class="social-button" target="_blank">
                        <i class="fab fa-whatsapp"></i>
                    </a>
                    <a href="https://www.facebook.com/sharer/sharer.php?u=${encodeURIComponent(`https://eventwishes.onrender.com/wish/${shortCode}`)}" class="social-button" target="_blank">
                        <i class="fab fa-facebook-f"></i>
                    </a>
                    <a href="https://twitter.com/intent/tweet?text=${encodeURIComponent(`Check out this special wish: https://eventwishes.onrender.com/wish/${shortCode}`)}" class="social-button" target="_blank">
                        <i class="fab fa-twitter"></i>
                    </a>
                </div>
                
                <div class="footer">
                    <p>Create your own special wishes with EventWish</p>
                </div>
            </div>
            
            <script>
                // Attempt to open the app
                let hasApp = false;
                
                // Show content after a short delay
                setTimeout(function() {
                    document.getElementById('content').classList.add('visible');
                }, 300);
                
                // Attempt to open the app
                document.getElementById('openAppButton').addEventListener('click', function(e) {
                    // Record the current time
                    const start = Date.now();
                    
                    // Set up a timeout to check if app opened
                    setTimeout(function() {
                        if (!hasApp && (Date.now() - start < 1500)) {
                            // If we're still here after 1.5 seconds, the app probably doesn't exist
                            window.location.href = 'https://play.google.com/store/apps/details?id=com.ds.eventwish';
                        }
                    }, 1000);
                });
                
                // Track app installation
                document.getElementById('getAppButton').addEventListener('click', function() {
                    // Send analytics event
                    fetch('/api/wishes/${shortCode}/analytics/install', {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json'
                        }
                    }).catch(err => console.error('Error tracking installation:', err));
                });
                
                // Handle visibility change to detect if user returned from app
                document.addEventListener('visibilitychange', function() {
                    if (!document.hidden) {
                        hasApp = true;
                    }
                });
                
                // Attempt to open the app immediately
                window.location.href = 'eventwish://wish/${shortCode}';
            </script>
        </body>
        </html>
    `;
}

/**
 * Generate a fallback landing page for shared wishes
 * @param {String} shortCode The wish short code
 * @returns {String} HTML for the fallback landing page
 */
function generateFallbackLandingPage(shortCode) {
    return `
        <!DOCTYPE html>
        <html>
        <head>
            <title>EventWish</title>
            <meta name="viewport" content="width=device-width, initial-scale=1">
            <style>
                body { font-family: Arial, sans-serif; text-align: center; padding: 20px; }
                .button { 
                    display: inline-block;
                    padding: 10px 20px;
                    margin: 10px;
                    background-color: #4CAF50;
                    color: white;
                    text-decoration: none;
                    border-radius: 5px;
                }
            </style>
        </head>
        <body>
            <h1>EventWish</h1>
            <p>View this wish in our app!</p>
            <a href="eventwish://wish/${shortCode}" class="button">Open in App</a>
            <a href="https://play.google.com/store/apps/details?id=com.ds.eventwish" class="button">Get the App</a>
            <script>
                // Attempt to open the app
                window.location.href = 'eventwish://wish/${shortCode}';
                
                // After a delay, if the app hasn't opened, show the buttons
                setTimeout(function() {
                    document.body.style.opacity = '1';
                }, 1000);
            </script>
        </body>
        </html>
    `;
}

module.exports = {
    generateWishLandingPage,
    generateFallbackLandingPage
}; 