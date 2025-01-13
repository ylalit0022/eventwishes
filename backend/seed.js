const mongoose = require('mongoose');
const Template = require('./models/template');

// MongoDB connection string
const MONGODB_URI = process.env.MONGODB_URI || 'mongodb+srv://ylalit0022:jBRgqv6BBfj2lYaG@eventwishes.3d1qt.mongodb.net/eventwishes?retryWrites=true&w=majority';

// Sample template data with modern design
const sampleTemplates = [
    {
        title: "Birthday Celebration",
        category: "Birthday",
        htmlContent: `
            <div style="text-align: center; padding: 20px; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;">
                <h1 style="color: #FF6B6B; font-size: 2.5em; margin-bottom: 20px;">🎉 Happy Birthday! 🎂</h1>
                <p style="font-size: 1.2em; color: #4A4A4A; margin: 15px 0;">Dear {{recipientName}},</p>
                <p style="font-size: 1.1em; color: #666; line-height: 1.6;">
                    May your special day be filled with beautiful moments,<br>
                    happy smiles, and endless joy!<br>
                    Wishing you a fantastic birthday celebration!
                </p>
                <div style="margin: 25px 0; font-size: 1.5em;">🎈 🎁 🎊</div>
                <p style="font-size: 1.1em; color: #666;">With love,<br>{{senderName}}</p>
            </div>
        `,
        previewUrl: "https://i.imgur.com/birthday_preview.jpg"
    },
    {
        title: "Wedding Anniversary",
        category: "Anniversary",
        htmlContent: `
            <div style="text-align: center; padding: 20px; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;">
                <h1 style="color: #6E8EFB; font-size: 2.5em; margin-bottom: 20px;">💑 Happy Anniversary! 💝</h1>
                <p style="font-size: 1.2em; color: #4A4A4A; margin: 15px 0;">Dear {{recipientName}},</p>
                <p style="font-size: 1.1em; color: #666; line-height: 1.6;">
                    Celebrating the beautiful journey of your love!<br>
                    May your bond grow stronger with each passing day.<br>
                    Here's to many more years of happiness together!
                </p>
                <div style="margin: 25px 0; font-size: 1.5em;">💐 💍 🥂</div>
                <p style="font-size: 1.1em; color: #666;">Best wishes,<br>{{senderName}}</p>
            </div>
        `,
        previewUrl: "https://i.imgur.com/anniversary_preview.jpg"
    },
    {
        title: "New Year Celebration",
        category: "New Year",
        htmlContent: `
            <div style="text-align: center; padding: 20px; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;">
                <h1 style="color: #FFD93D; font-size: 2.5em; margin-bottom: 20px;">✨ Happy New Year! 🎊</h1>
                <p style="font-size: 1.2em; color: #4A4A4A; margin: 15px 0;">Dear {{recipientName}},</p>
                <p style="font-size: 1.1em; color: #666; line-height: 1.6;">
                    As we welcome the new year,<br>
                    may it bring you joy, success, and prosperity!<br>
                    Here's to new beginnings and amazing adventures!
                </p>
                <div style="margin: 25px 0; font-size: 1.5em;">🎆 🥳 ⭐</div>
                <p style="font-size: 1.1em; color: #666;">Warm wishes,<br>{{senderName}}</p>
            </div>
        `,
        previewUrl: "https://i.imgur.com/newyear_preview.jpg"
    },
    {
        title: "Graduation Success",
        category: "Graduation",
        htmlContent: `
            <div style="text-align: center; padding: 20px; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;">
                <h1 style="color: #6ECB63; font-size: 2.5em; margin-bottom: 20px;">🎓 Congratulations Graduate! 📚</h1>
                <p style="font-size: 1.2em; color: #4A4A4A; margin: 15px 0;">Dear {{recipientName}},</p>
                <p style="font-size: 1.1em; color: #666; line-height: 1.6;">
                    Your hard work and dedication have paid off!<br>
                    This is just the beginning of your amazing journey.<br>
                    Wishing you success in all your future endeavors!
                </p>
                <div style="margin: 25px 0; font-size: 1.5em;">🎊 📜 ⭐</div>
                <p style="font-size: 1.1em; color: #666;">Proud of you!<br>{{senderName}}</p>
            </div>
        `,
        previewUrl: "https://i.imgur.com/graduation_preview.jpg"
    },
    {
        title: "Get Well Soon",
        category: "Get Well",
        htmlContent: `
            <div style="text-align: center; padding: 20px; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;">
                <h1 style="color: #4CACBC; font-size: 2.5em; margin-bottom: 20px;">💐 Get Well Soon! 🌟</h1>
                <p style="font-size: 1.2em; color: #4A4A4A; margin: 15px 0;">Dear {{recipientName}},</p>
                <p style="font-size: 1.1em; color: #666; line-height: 1.6;">
                    Sending you healing thoughts and warm wishes.<br>
                    May each day bring you renewed strength,<br>
                    brighter moments, and restored health.
                </p>
                <div style="margin: 25px 0; font-size: 1.5em;">🌺 ❤️ 🌈</div>
                <p style="font-size: 1.1em; color: #666;">Take care,<br>{{senderName}}</p>
            </div>
        `,
        previewUrl: "https://i.imgur.com/getwell_preview.jpg"
    },
    {
        title: "Valentine's Day Love",
        category: "Valentine",
        htmlContent: `
            <div style="text-align: center; padding: 20px; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background: linear-gradient(135deg, #FF9A9E, #FAD0C4);">
                <div style="background: rgba(255,255,255,0.95); padding: 30px; border-radius: 20px; box-shadow: 0 10px 30px rgba(0,0,0,0.1);">
                    <h1 style="color: #FF4B6E; font-size: 2.5em; margin-bottom: 20px;">💝 Happy Valentine's Day! 💖</h1>
                    <p style="font-size: 1.2em; color: #4A4A4A; margin: 15px 0;">Dearest {{recipientName}},</p>
                    <p style="font-size: 1.1em; color: #666; line-height: 1.8;">
                        On this special day of love,<br>
                        I want you to know how much you mean to me.<br>
                        Every moment with you is a gift I cherish deeply.
                    </p>
                    <div style="margin: 25px 0; font-size: 1.5em;">💘 🌹 💑</div>
                    <p style="font-size: 1.1em; color: #666;">With all my love,<br>{{senderName}}</p>
                </div>
            </div>
        `,
        previewUrl: "https://i.imgur.com/valentine_preview.jpg"
    },
    {
        title: "Christmas Joy",
        category: "Christmas",
        htmlContent: `
            <div style="text-align: center; padding: 20px; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background: linear-gradient(135deg, #73C8A9, #373B44);">
                <div style="background: rgba(255,255,255,0.95); padding: 30px; border-radius: 20px; box-shadow: 0 10px 30px rgba(0,0,0,0.1);">
                    <h1 style="color: #D4343F; font-size: 2.5em; margin-bottom: 20px;">🎄 Merry Christmas! ⛄</h1>
                    <p style="font-size: 1.2em; color: #4A4A4A; margin: 15px 0;">Dear {{recipientName}},</p>
                    <p style="font-size: 1.1em; color: #666; line-height: 1.8;">
                        May your holidays be filled with warmth and cheer,<br>
                        bringing joy, peace, and wonderful memories.<br>
                        Wishing you a magical Christmas season!
                    </p>
                    <div style="margin: 25px 0; font-size: 1.5em;">🎅 🎁 ❄️</div>
                    <p style="font-size: 1.1em; color: #666;">Season's Greetings,<br>{{senderName}}</p>
                </div>
            </div>
        `,
        previewUrl: "https://i.imgur.com/christmas_preview.jpg"
    },
    {
        title: "Diwali Wishes",
        category: "Diwali",
        htmlContent: `
            <div style="text-align: center; padding: 20px; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background: linear-gradient(135deg, #FFD700, #FF8C00);">
                <div style="background: rgba(255,255,255,0.95); padding: 30px; border-radius: 20px; box-shadow: 0 10px 30px rgba(0,0,0,0.1);">
                    <h1 style="color: #B8860B; font-size: 2.5em; margin-bottom: 20px;">✨ Happy Diwali! 🪔</h1>
                    <p style="font-size: 1.2em; color: #4A4A4A; margin: 15px 0;">Dear {{recipientName}},</p>
                    <p style="font-size: 1.1em; color: #666; line-height: 1.8;">
                        May the festival of lights brighten your life,<br>
                        bring prosperity, joy, and success.<br>
                        Let's celebrate this auspicious occasion together!
                    </p>
                    <div style="margin: 25px 0; font-size: 1.5em;">🎆 💫 🎊</div>
                    <p style="font-size: 1.1em; color: #666;">Warm wishes,<br>{{senderName}}</p>
                </div>
            </div>
        `,
        previewUrl: "https://i.imgur.com/diwali_preview.jpg"
    },
    {
        title: "Baby Shower Celebration",
        category: "Baby Shower",
        htmlContent: `
            <div style="text-align: center; padding: 20px; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background: linear-gradient(135deg, #E0C3FC, #8EC5FC);">
                <div style="background: rgba(255,255,255,0.95); padding: 30px; border-radius: 20px; box-shadow: 0 10px 30px rgba(0,0,0,0.1);">
                    <h1 style="color: #9B6B9E; font-size: 2.5em; margin-bottom: 20px;">👶 Baby Shower Wishes! 🎀</h1>
                    <p style="font-size: 1.2em; color: #4A4A4A; margin: 15px 0;">Dear {{recipientName}},</p>
                    <p style="font-size: 1.1em; color: #666; line-height: 1.8;">
                        Congratulations on your upcoming bundle of joy!<br>
                        May this special time be filled with happiness<br>
                        and beautiful moments to cherish forever.
                    </p>
                    <div style="margin: 25px 0; font-size: 1.5em;">🍼 🎈 🧸</div>
                    <p style="font-size: 1.1em; color: #666;">With love and excitement,<br>{{senderName}}</p>
                </div>
            </div>
        `,
        previewUrl: "https://i.imgur.com/babyshower_preview.jpg"
    },
    {
        title: "Job Promotion",
        category: "Career",
        htmlContent: `
            <div style="text-align: center; padding: 20px; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background: linear-gradient(135deg, #84FAB0, #8FD3F4);">
                <div style="background: rgba(255,255,255,0.95); padding: 30px; border-radius: 20px; box-shadow: 0 10px 30px rgba(0,0,0,0.1);">
                    <h1 style="color: #2E8B57; font-size: 2.5em; margin-bottom: 20px;">🌟 Congratulations on Your Promotion! 📈</h1>
                    <p style="font-size: 1.2em; color: #4A4A4A; margin: 15px 0;">Dear {{recipientName}},</p>
                    <p style="font-size: 1.1em; color: #666; line-height: 1.8;">
                        Your hard work and dedication have paid off!<br>
                        This promotion is well-deserved, and we're so proud of you.<br>
                        Wishing you continued success in your new role!
                    </p>
                    <div style="margin: 25px 0; font-size: 1.5em;">🎯 💼 🚀</div>
                    <p style="font-size: 1.1em; color: #666;">Congratulations!<br>{{senderName}}</p>
                </div>
            </div>
        `,
        previewUrl: "https://i.imgur.com/promotion_preview.jpg"
    },
    {
        title: "House Warming",
        category: "Home",
        htmlContent: `
            <div style="text-align: center; padding: 20px; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background: linear-gradient(135deg, #A8E6CF, #DCEDC1);">
                <div style="background: rgba(255,255,255,0.95); padding: 30px; border-radius: 20px; box-shadow: 0 10px 30px rgba(0,0,0,0.1);">
                    <h1 style="color: #3CB371; font-size: 2.5em; margin-bottom: 20px;">🏠 Happy House Warming! 🎊</h1>
                    <p style="font-size: 1.2em; color: #4A4A4A; margin: 15px 0;">Dear {{recipientName}},</p>
                    <p style="font-size: 1.1em; color: #666; line-height: 1.8;">
                        Congratulations on your new home!<br>
                        May it be filled with love, laughter, and countless happy memories.<br>
                        Wishing you all the best in your new chapter!
                    </p>
                    <div style="margin: 25px 0; font-size: 1.5em;">🔑 🌿 ✨</div>
                    <p style="font-size: 1.1em; color: #666;">Best wishes,<br>{{senderName}}</p>
                </div>
            </div>
        `,
        previewUrl: "https://i.imgur.com/housewarming_preview.jpg"
    },
    {
        title: "Eid Mubarak",
        category: "Religious",
        htmlContent: `
            <div style="text-align: center; padding: 20px; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background: linear-gradient(135deg, #91EAE4, #86A8E7);">
                <div style="background: rgba(255,255,255,0.95); padding: 30px; border-radius: 20px; box-shadow: 0 10px 30px rgba(0,0,0,0.1); animation: fadeIn 1s ease-in;">
                    <h1 style="color: #5C7AEA; font-size: 2.5em; margin-bottom: 20px;">🌙 Eid Mubarak! ✨</h1>
                    <p style="font-size: 1.2em; color: #4A4A4A; margin: 15px 0;">Dear {{recipientName}},</p>
                    <p style="font-size: 1.1em; color: #666; line-height: 1.8;">
                        May this blessed occasion bring you<br>
                        peace, happiness, and prosperity.<br>
                        Wishing you and your family a joyous celebration!
                    </p>
                    <div style="margin: 25px 0; font-size: 1.5em;">🕌 ⭐ 🎊</div>
                    <p style="font-size: 1.1em; color: #666;">Warm regards,<br>{{senderName}}</p>
                </div>
            </div>
            <style>
                @keyframes fadeIn {
                    from { opacity: 0; transform: translateY(20px); }
                    to { opacity: 1; transform: translateY(0); }
                }
            </style>
        `,
        previewUrl: "https://i.imgur.com/eid_preview.jpg"
    },
    {
        title: "Thank You Note",
        category: "Gratitude",
        htmlContent: `
            <div style="text-align: center; padding: 20px; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background: linear-gradient(135deg, #F6D365, #FDA085);">
                <div style="background: rgba(255,255,255,0.95); padding: 30px; border-radius: 20px; box-shadow: 0 10px 30px rgba(0,0,0,0.1); animation: slideIn 1s ease-out;">
                    <h1 style="color: #FF7F50; font-size: 2.5em; margin-bottom: 20px;">💝 Thank You! 🙏</h1>
                    <p style="font-size: 1.2em; color: #4A4A4A; margin: 15px 0;">Dear {{recipientName}},</p>
                    <p style="font-size: 1.1em; color: #666; line-height: 1.8;">
                        Words cannot express how grateful I am<br>
                        for your kindness and support.<br>
                        You've made such a difference!
                    </p>
                    <div style="margin: 25px 0; font-size: 1.5em;">🌟 💖 ✨</div>
                    <p style="font-size: 1.1em; color: #666;">With gratitude,<br>{{senderName}}</p>
                </div>
            </div>
            <style>
                @keyframes slideIn {
                    from { transform: translateX(-100%); }
                    to { transform: translateX(0); }
                }
            </style>
        `,
        previewUrl: "https://i.imgur.com/thankyou_preview.jpg"
    },
    {
        title: "Wedding Day",
        category: "Wedding",
        htmlContent: `
            <div style="text-align: center; padding: 20px; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background: linear-gradient(135deg, #E3FDF5, #FFE6FA);">
                <div style="background: rgba(255,255,255,0.95); padding: 30px; border-radius: 20px; box-shadow: 0 10px 30px rgba(0,0,0,0.1); animation: scaleIn 1s ease-in-out;">
                    <h1 style="color: #FF69B4; font-size: 2.5em; margin-bottom: 20px;">💒 Happy Wedding Day! 💍</h1>
                    <p style="font-size: 1.2em; color: #4A4A4A; margin: 15px 0;">Dear {{recipientName}},</p>
                    <p style="font-size: 1.1em; color: #666; line-height: 1.8;">
                        Congratulations on your special day!<br>
                        May your love story continue to grow<br>
                        and inspire everyone around you.
                    </p>
                    <div style="margin: 25px 0; font-size: 1.5em;">👰 🤵 💑</div>
                    <p style="font-size: 1.1em; color: #666;">Best wishes,<br>{{senderName}}</p>
                </div>
            </div>
            <style>
                @keyframes scaleIn {
                    from { transform: scale(0); }
                    to { transform: scale(1); }
                }
            </style>
        `,
        previewUrl: "https://i.imgur.com/wedding_preview.jpg"
    },
    {
        title: "Get Well Soon Premium",
        category: "Health",
        htmlContent: `
            <div style="text-align: center; padding: 20px; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background: linear-gradient(135deg, #A8EDEA, #FED6E3);">
                <div style="background: rgba(255,255,255,0.95); padding: 30px; border-radius: 20px; box-shadow: 0 10px 30px rgba(0,0,0,0.1); animation: bounce 1s ease-in-out;">
                    <h1 style="color: #5F9EA0; font-size: 2.5em; margin-bottom: 20px;">🌺 Get Well Soon! 💐</h1>
                    <p style="font-size: 1.2em; color: #4A4A4A; margin: 15px 0;">Dear {{recipientName}},</p>
                    <p style="font-size: 1.1em; color: #666; line-height: 1.8;">
                        Sending you healing thoughts and prayers<br>
                        for a speedy recovery.<br>
                        Take good care and rest well!
                    </p>
                    <div style="margin: 25px 0; font-size: 1.5em;">🌈 ❤️ ✨</div>
                    <p style="font-size: 1.1em; color: #666;">Thinking of you,<br>{{senderName}}</p>
                </div>
            </div>
            <style>
                @keyframes bounce {
                    0% { transform: translateY(-500px); }
                    60% { transform: translateY(30px); }
                    80% { transform: translateY(-10px); }
                    100% { transform: translateY(0); }
                }
            </style>
        `,
        previewUrl: "https://i.imgur.com/getwell_premium_preview.jpg"
    },
    {
        title: "Retirement Wishes",
        category: "Career",
        htmlContent: `
            <div style="text-align: center; padding: 20px; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background: linear-gradient(135deg, #D4FC79, #96E6A1);">
                <div style="background: rgba(255,255,255,0.95); padding: 30px; border-radius: 20px; box-shadow: 0 10px 30px rgba(0,0,0,0.1); animation: rotateIn 1s ease-out;">
                    <h1 style="color: #3CB371; font-size: 2.5em; margin-bottom: 20px;">🌴 Happy Retirement! 🎉</h1>
                    <p style="font-size: 1.2em; color: #4A4A4A; margin: 15px 0;">Dear {{recipientName}},</p>
                    <p style="font-size: 1.1em; color: #666; line-height: 1.8;">
                        Congratulations on your well-deserved retirement!<br>
                        Thank you for your years of dedicated service.<br>
                        May this new chapter bring you joy and relaxation!
                    </p>
                    <div style="margin: 25px 0; font-size: 1.5em;">⛱️ 🎨 🎣</div>
                    <p style="font-size: 1.1em; color: #666;">Best wishes,<br>{{senderName}}</p>
                </div>
            </div>
            <style>
                @keyframes rotateIn {
                    from { transform: rotate(-180deg) scale(0); }
                    to { transform: rotate(0) scale(1); }
                }
            </style>
        `,
        previewUrl: "https://i.imgur.com/retirement_preview.jpg"
    }
];

async function seedDatabase() {
    try {
        // Connect to MongoDB
        await mongoose.connect(MONGODB_URI, {
            useNewUrlParser: true,
            useUnifiedTopology: true
        });
        console.log('Connected to MongoDB successfully');

        // Clear existing templates
        await Template.deleteMany({});
        console.log('Cleared existing templates');

        // Insert new templates
        const result = await Template.insertMany(sampleTemplates);
        console.log(`Successfully seeded ${result.length} templates`);

        console.log('Database seeding completed');
        process.exit(0);
    } catch (error) {
        console.error('Error seeding database:', error);
        process.exit(1);
    }
}

// Run the seeding function
seedDatabase();
