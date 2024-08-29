package FirestoreController;

import util.UserPhoneNumber;

public class Contact {
    private final String name;
    private final String number;

    public Contact(String name, String number, String country){
        this.name = name;
        UserPhoneNumber correctNumber = new UserPhoneNumber(number, country);
        this.number = correctNumber.toString();
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
