package com.devsoul.dima.kindergarten.model;

import java.io.Serializable;

/**
 * This is the teacher class
 * @params ID, First Name, Last Name, Phone, Picture, Class, Email, Password, Notification Time, Created at
 */
public class Teacher implements Serializable
{
    // Variables
    private String ID;
    private String First_Name;
    private String Last_Name;
    private String Phone;
    private String Picture;
    private String Class;
    private String Email;
    private String Password;
    private String Notification_Time;
    private String Created_at;

    // Default Constructor
    public Teacher() {}

    // Getters and Setters
    // ID
    public void SetID(String ID)
    {
        this.ID = ID;
    }
    public String GetID()
    {
        return this.ID;
    }

    // First Name
    public void SetFirstName(String FName)
    {
        this.First_Name = FName;
    }
    public String GetFirstName()
    {
        return this.First_Name;
    }

    // Last Name
    public void SetLastName(String LName)
    {
        this.Last_Name = LName;
    }
    public String GetLastName()
    {
        return this.Last_Name;
    }

    // Phone
    public void SetPhone(String Phone)
    {
        this.Phone = Phone;
    }
    public String GetPhone()
    {
        return this.Phone;
    }

    // Picture
    public void SetPicture(String Pic)
    {
        this.Picture = Pic;
    }
    public String GetPicture()
    {
        return this.Picture;
    }

    // Class
    public void SetClass(String Class)
    {
        this.Class = Class;
    }
    public String GetClass()
    {
        return this.Class;
    }

    // Email
    public void SetEmail(String Email)
    {
        this.Email = Email;
    }
    public String GetEmail()
    {
        return this.Email;
    }

    // Password
    public void SetPassword(String Pass)
    {
        this.Password = Pass;
    }
    public String GetPassword()
    {
        return this.Password;
    }

    // Notification Time
    public void SetNotificationTime(String Notification_Time)
    {
        this.Notification_Time = Notification_Time;
    }
    public String GetNotificationTime()
    {
        return this.Notification_Time;
    }

    // Created_at
    public void SetCreatedAt(String Created_at)
    {
        this.Created_at = Created_at;
    }
    public String GetCreatedAt()
    {
        return this.Created_at;
    }
}
