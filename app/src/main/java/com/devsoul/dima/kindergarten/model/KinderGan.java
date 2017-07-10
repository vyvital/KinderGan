package com.devsoul.dima.kindergarten.model;

import java.io.Serializable;

/**
 * This is the KinderGan class
 * @params ID, Name, Address, Classes, City, Phone, Schedule
 */
public class KinderGan implements Serializable
{
    // Variables
    private String ID;
    private String Name;
    private String Address;
    private int Classes;
    private String City;
    private String Phone;
    private String Schedule_Path;

    // Constructor
    public KinderGan() {}

    public KinderGan(String Name)
    {
        this.Name = Name;
    }

    public KinderGan(String Name, String City)
    {
        this.Name = Name;
        this.City = City;
    }

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

    // Name
    public void SetName(String Name)
    {
        this.Name = Name;
    }
    public String GetName()
    {
        return this.Name;
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

    // Classes
    public void SetClasses(int Classes)
    {
        this.Classes = Classes;
    }
    public int GetClasses()
    {
        return this.Classes;
    }

    // City
    public void SetCity(String City)
    {
        this.City = City;
    }
    public String GetCity()
    {
        return this.City;
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

    // Schedule
    public void SetSchedule(String Schedule)
    {
        this.Schedule_Path = Schedule;
    }
    public String GetSchedule()
    {
        return this.Schedule_Path;
    }
}
