const express = require("express");
const { Users } = require("../models");
const router = express.Router();
const path = require("path");

//Used for default registration
router.post("/register", async (req, res) => {
  const result = await Users.findOne({ 
    where: { email: req.body.email },
  });
  if (result === null) {
 
    let uploadPath; 
    console.log(req.files)
    const file = req.files.file; 
    console.log(file.name)
    uploadPath = path.join(__dirname, "..");
    uploadPath +=
      "/images/" + req.body.email + "." + file.name.split(".").pop();
    file.mv(uploadPath, function (err) {
      if (err) {
        return res.json({
          data: "",
          error: "File Upload Error",
        });
      }
    });
    const user = await Users.create({
        email: req.body.email,
        photo:  req.body.email + "." + file.name.split(".").pop(), 
    })
    console.log(user)
    res.json({
      data: user.userUNID,
      error: "",
    });

  } else {
    res.json({
      data: "",
      error: "Email Registered Already",
    }); 
  }
});

router.get("/get-all-users",async (req, res) => {
    const result = await Users.findAll()
    let uploadPath = path.join(__dirname, "..");
    console.log(uploadPath)
    res.json({
        data: result,
        error: "",
      });
})

module.exports = router;
