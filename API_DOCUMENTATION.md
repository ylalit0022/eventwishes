# EventWishes API Documentation

## Base URL
```
https://eventwishes.onrender.com
```

## Authentication
All API requests require authentication using an API key in the request header:
```
Authorization: Bearer YOUR_API_KEY
```

## API Endpoints

### 1. Template Endpoints

#### Get All Templates
```http
GET /api/templates
```
**Response:**
```json
[
  {
    "id": "string",
    "name": "string",
    "description": "string",
    "htmlContent": "string",
    "cssContent": "string",
    "category": "string",
    "thumbnailUrl": "string",
    "isActive": true
  }
]
```

#### Get Template by ID
```http
GET /api/templates/{id}
```
**Response:**
```json
{
  "id": "string",
  "name": "string",
  "description": "string",
  "htmlContent": "string",
  "cssContent": "string",
  "category": "string",
  "thumbnailUrl": "string",
  "isActive": true
}
```

#### Create Template
```http
POST /api/templates
```
**Request Body:**
```json
{
  "name": "string",
  "description": "string",
  "htmlContent": "string",
  "cssContent": "string",
  "category": "string",
  "thumbnailUrl": "string",
  "isActive": true
}
```

#### Update Template
```http
PUT /api/templates/{id}
```
**Request Body:** Same as Create Template

#### Delete Template
```http
DELETE /api/templates/{id}
```

### 2. Share Endpoints

#### Create Share
```http
POST /api/share
```
**Request Body:**
```json
{
  "templateId": "string",
  "recipientName": "string",
  "senderName": "string",
  "cssContent": "string"
}
```
**Response:**
```json
{
  "shortCode": "string",
  "shareUrl": "string"
}
```

#### Get Shared Wish
```http
GET /api/share/{shortCode}
```
**Response:**
```json
{
  "id": "string",
  "shortCode": "string",
  "templateId": "string",
  "recipientName": "string",
  "senderName": "string",
  "customizedHtml": "string",
  "cssContent": "string",
  "views": 0,
  "createdAt": "string",
  "lastViewedAt": "string",
  "sharedVia": "string"
}
```

### 3. CSS Endpoints

#### Get Template CSS
```http
GET /api/templates/{templateId}/css
```
**Response:**
```json
{
  "cssContent": "string",
  "templateType": "string",
  "customProperties": {
    "primaryColor": "string",
    "fontFamily": "string",
    "fontSize": "string",
    "spacing": "string"
  }
}
```

### 4. Analytics Endpoints

#### Record View
```http
POST /api/analytics/view
```
**Request Body:**
```json
{
  "shortCode": "string"
}
```
**Response:**
```json
{
  "views": 0,
  "lastViewedAt": "string"
}
```

#### Record Share
```http
POST /api/analytics/share
```
**Request Body:**
```json
{
  "shortCode": "string",
  "platform": "string"
}
```
**Response:**
```json
{
  "shares": {
    "whatsapp": 0,
    "facebook": 0,
    "twitter": 0,
    "email": 0,
    "link": 0
  }
}
```

### 5. Category Endpoints

#### Get All Categories
```http
GET /api/categories
```
**Response:**
```json
[
  {
    "id": "string",
    "name": "string",
    "description": "string",
    "thumbnailUrl": "string"
  }
]
```

#### Get Templates by Category
```http
GET /api/categories/{id}/templates
```
**Response:** Array of Template objects

## Error Responses

All endpoints may return the following error responses:

### 400 Bad Request
```json
{
  "error": "Bad Request",
  "message": "Invalid request parameters",
  "details": {}
}
```

### 401 Unauthorized
```json
{
  "error": "Unauthorized",
  "message": "Invalid or missing API key"
}
```

### 404 Not Found
```json
{
  "error": "Not Found",
  "message": "Resource not found"
}
```

### 500 Internal Server Error
```json
{
  "error": "Internal Server Error",
  "message": "An unexpected error occurred"
}
```

## Rate Limiting

- Rate limit: 100 requests per minute per API key
- Rate limit header included in responses:
  ```
  X-RateLimit-Limit: 100
  X-RateLimit-Remaining: 99
  X-RateLimit-Reset: 1516131940
  ```

## Data Types

### Template Types
- birthday
- wedding
- professional
- holiday
- congratulation
- general

### Share Platforms
- WHATSAPP
- FACEBOOK
- TWITTER
- EMAIL
- LINK

## Best Practices

1. Always include error handling for API responses
2. Cache template data when appropriate
3. Use proper HTTP methods for CRUD operations
4. Include proper content-type headers
5. Validate input before making API calls
