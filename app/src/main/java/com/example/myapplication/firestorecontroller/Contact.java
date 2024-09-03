package com.example.myapplication.firestorecontroller;

import com.example.myapplication.util.UserPhoneNumber;

public class Contact {
    private final String name;
    private final String number;

    public Contact(String name, String number, String country){
        this.name = name;
        String region = new UserPhoneNumber().getRegionCodeByCountry(country);
        UserPhoneNumber correctNumber = new UserPhoneNumber(number, region);
        this.number = correctNumber.toString();
    }

    public Contact(String number, String country, boolean countryOnly){
       this("",number,country);
    }

    public Contact(String name, String number){
        this.name = name;
        this.number = number;
    }

    public Contact getContact(){
        return this;
    }

    public String getName() {
        return name;
    }

    public String getNumber(){return number;}

}
