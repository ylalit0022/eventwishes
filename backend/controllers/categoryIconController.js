const CategoryIcon = require('../models/CategoryIcon');

// Get all category icons
exports.getAllCategoryIcons = async (req, res) => {
    try {
        const categoryIcons = await CategoryIcon.find();
        res.status(200).json(categoryIcons);
    } catch (error) {
        res.status(500).json({ message: error.message });
    }
};

// Create a new category icon
exports.createCategoryIcon = async (req, res) => {
    try {
        const categoryIcon = new CategoryIcon({
            title: req.body.title,
            categoryIcon: req.body.categoryIcon
        });
        const newCategoryIcon = await categoryIcon.save();
        res.status(201).json(newCategoryIcon);
    } catch (error) {
        res.status(400).json({ message: error.message });
    }
};