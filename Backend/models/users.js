//To keep track of all User

module.exports = (sequelize, DataTypes) => {
    const Users = sequelize.define("Users", { 
  
      // Unique UserID for all user
      userUNID: {
        type: DataTypes.INTEGER,
        primaryKey: true,
        autoIncrement: true
      },
      email: DataTypes.STRING,
      photo: DataTypes.STRING,
       
    });
  
    
  
    return Users;
  };
  