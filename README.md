# Event Wishes App

A mobile application for creating and sharing event wishes and greetings.

## Backend Deployment Guide

### Prerequisites
- Node.js and npm installed
- MongoDB Atlas account
- Render.com account
- Git repository

### Deployment Steps to Render

1. Fork or push this repository to your GitHub account

2. Create a new Web Service on Render:
   - Go to [Render Dashboard](https://dashboard.render.com)
   - Click "New +" and select "Web Service"
   - Connect your GitHub repository
   - Configure the service:
     ```
     Name: eventwishes-backend
     Environment: Node
     Build Command: npm install
     Start Command: node server.js
     ```

3. Add Environment Variables on Render:
   - Click on "Environment" tab
   - Add the following variables:
     ```
     MONGODB_URI=your_mongodb_atlas_connection_string
     NODE_ENV=production
     PORT=3000
     ```

4. Deploy:
   - Click "Create Web Service"
   - Wait for the deployment to complete

### MongoDB Atlas Setup

1. Create a MongoDB Atlas Account:
   - Go to [MongoDB Atlas](https://www.mongodb.com/cloud/atlas)
   - Sign up or log in

2. Create a Cluster:
   - Click "Build a Database"
   - Choose the free tier option
   - Select your preferred region
   - Click "Create"

3. Configure Network Access:
   - Go to Network Access in sidebar
   - Click "Add IP Address"
   - Add `0.0.0.0/0` for all IPs (or specific Render IPs)
   - Click "Confirm"

4. Create Database User:
   - Go to Database Access
   - Click "Add New Database User"
   - Create username and password
   - Add user with read/write permissions

5. Get Connection String:
   - Click "Connect" on your cluster
   - Choose "Connect your application"
   - Copy the connection string
   - Replace `<password>` with your database user password

## Android App Configuration

### Debug Build
- Uses `http://10.0.2.2:3000` for local development
- Points to local Node.js server

### Release Build
- Uses `https://eventwishes-backend.onrender.com`
- Points to production server on Render

## Testing the Deployment

1. After deployment, test the API:
   ```
   curl https://your-render-url/api/templates
   ```

2. Update Android app's BASE_URL in build.gradle
3. Build and test the Android app

## Troubleshooting

1. If the app shows "Error loading templates":
   - Check if the backend is running
   - Verify MongoDB connection
   - Check network permissions

2. If deployment fails:
   - Check Render logs
   - Verify environment variables
   - Check MongoDB Atlas network access

## Support

For issues and support:
- Create an issue in the GitHub repository
- Check Render logs for backend issues
- Check MongoDB Atlas logs for database issues
