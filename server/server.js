const express = require('express');
const cors = require('cors');
const bodyParser = require('body-parser');
const { v4: uuidv4 } = require('uuid');

const app = express();
const port = process.env.PORT || 3000;

// Middleware
app.use(cors());
app.use(bodyParser.json());

// In-memory storage
let templates = [
    {
        id: uuidv4(),
        title: "Birthday Wishes",
        content: "Wishing you a day filled with joy and laughter!",
        category: "Birthday",
        imageUrl: "https://example.com/birthday.jpg",
        createdAt: new Date().toISOString()
    },
    {
        id: uuidv4(),
        title: "Anniversary Celebration",
        content: "Congratulations on another year of love and happiness!",
        category: "Anniversary",
        imageUrl: "https://example.com/anniversary.jpg",
        createdAt: new Date().toISOString()
    },
    {
        id: uuidv4(),
        title: "Wedding Wishes",
        content: "May your love grow stronger with each passing day!",
        category: "Wedding",
        imageUrl: "https://example.com/wedding.jpg",
        createdAt: new Date().toISOString()
    }
];

// GET templates with pagination
app.get('/api/templates', (req, res) => {
    const page = parseInt(req.query.page) || 1;
    const limit = parseInt(req.query.limit) || 20;
    const startIndex = (page - 1) * limit;
    const endIndex = startIndex + limit;

    const paginatedTemplates = templates.slice(startIndex, endIndex);
    const totalTemplates = templates.length;
    const totalPages = Math.ceil(totalTemplates / limit);

    res.json({
        data: paginatedTemplates,
        page: page,
        totalPages: totalPages,
        totalItems: totalTemplates,
        hasMore: page < totalPages
    });
});

// GET single template
app.get('/api/templates/:id', (req, res) => {
    const template = templates.find(t => t.id === req.params.id);
    if (!template) {
        return res.status(404).json({ error: 'Template not found' });
    }
    res.json(template);
});

// POST new template
app.post('/api/templates', (req, res) => {
    const template = {
        id: uuidv4(),
        ...req.body,
        createdAt: new Date().toISOString()
    };
    templates.unshift(template); // Add to beginning of array
    res.status(201).json(template);
});

// PUT update template
app.put('/api/templates/:id', (req, res) => {
    const index = templates.findIndex(t => t.id === req.params.id);
    if (index === -1) {
        return res.status(404).json({ error: 'Template not found' });
    }
    templates[index] = { ...templates[index], ...req.body };
    res.json(templates[index]);
});

// DELETE template
app.delete('/api/templates/:id', (req, res) => {
    const index = templates.findIndex(t => t.id === req.params.id);
    if (index === -1) {
        return res.status(404).json({ error: 'Template not found' });
    }
    templates.splice(index, 1);
    res.status(204).send();
});

// Start server
app.listen(port, () => {
    console.log(`Server is running on port ${port}`);
});
