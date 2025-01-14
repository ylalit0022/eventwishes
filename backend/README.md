# EventWishes Backend

Backend server for the EventWishes Android app, providing API endpoints for wish templates, sharing wishes, and handling App Links/Deep Links.

## Features

- 🎯 Template Management
- 🔗 Deep Linking Support
- 🌐 Wish Sharing
- 📱 Android App Integration
- 🔒 Secure Asset Links
- 🚀 Express.js & MongoDB

## Quick Start

1. **Install Dependencies**
   ```bash
   npm install
   ```

2. **Environment Setup**
   - Copy `.env.example` to `.env`
   - Update the environment variables:
     ```
     PORT=3000
     MONGODB_URI=your_mongodb_connection_string
     NODE_ENV=development
     APP_URL=http://localhost:3000
     ```

3. **Run the Server**
   - Development mode (with auto-reload):
     ```bash
     npm run dev
     ```
   - Production mode:
     ```bash
     npm start
     ```

## API Endpoints

### Asset Links
- `GET /.well-known/assetlinks.json` - Android App Links verification
- `GET /assetlinks.json` - Alternative path for App Links

### Wishes
- `POST /api/share` - Share a new wish
- `GET /wish/:shortCode` - View a shared wish

### Health Check
- `GET /health` - Server health status

## Project Structure

```
backend/
├── models/          # MongoDB models
│   ├── template.js
│   └── sharedWish.js
├── public/          # Static files
│   ├── .well-known/
│   │   └── assetlinks.json
│   └── test-deep-link.html
├── routes/          # API routes
├── server.js        # Main server file
├── .env             # Environment variables
└── package.json     # Dependencies
```

## Development

### Prerequisites
- Node.js 14+
- MongoDB Atlas account or local MongoDB
- npm or yarn

### Testing Deep Links
1. Start the server
2. Visit `http://localhost:3000/test-deep-link.html`
3. Test both direct and intent URLs

### Error Handling
- Proper error responses with status codes
- Request validation
- MongoDB connection monitoring
- Graceful shutdown support

## Deployment

### Render.com
1. Connect your GitHub repository
2. Add environment variables
3. Deploy the backend

### Environment Variables
- `PORT`: Server port (default: 3000)
- `MONGODB_URI`: MongoDB connection string
- `NODE_ENV`: development/production
- `APP_URL`: Backend URL

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.
