package com.devsoul.dima.kindergarten.model;

import java.io.Serializable;

/**
 * This is the parent class
 * @params ID, First Name, Last Name, Phone, Address, Email, Password, Created at
 */
public class Parent implements Serializable
{
    // Variables
    private String ID;
    private String First_Name;
    private String Last_Name;
    private String Phone;
    private String Address;
    private String Email;
    private String Password;
    private String Created_at;

    // Default Constructor
    public Parent() {}

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

    // Address
    public void SetAddress(String Address)
    {
        this.Address = Address;
    }
    public String GetAddress()
    {
        return this.Address;
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
