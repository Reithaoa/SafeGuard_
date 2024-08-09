package util;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import com.google.i18n.phonenumbers.NumberParseException;
import java.util.*;


public class UserPhoneNumber{
    private PhoneNumber phoneNumber;
    private String countryCode;
    private String number;
    private final PhoneNumberUtil util = PhoneNumberUtil.getInstance();
    private final Map<String,String> countries = new HashMap<>();


    public UserPhoneNumber(String number,String country){
        loadRegions();
        setPhoneNumber(number,country);

        try {
            this.countryCode = String.valueOf(this.phoneNumber.getCountryCode());
            this.number = String.valueOf(this.phoneNumber.getNationalNumber());
        }catch (NullPointerException e){
            e.getCause();
        }
    }

    // no arg constructor used for getting phone number examples to display to user
    public UserPhoneNumber(){
        loadRegions();
    }

    public PhoneNumber UserPhoneNumberExample(String country){
        return this.util.getExampleNumber(getRegion(country));
    }

    public String getCountryCode(){return this.countryCode;}
    public String getNumber(){return this.number;}
    public PhoneNumber getPhoneNumber(){
        return this.phoneNumber;
    }

    private void setPhoneNumber(String number, String country){
        try {
            phoneNumber = util.parse(number, getRegion(country));
        }catch (NumberParseException e){
            e.getCause();
        }
    }
    private void loadRegions(){
        // Getting all the available regions. The regions are returned as a set.
        List<String> regions = new ArrayList<String>(util.getSupportedRegions());
        Collections.sort(regions); // sorting regions alphabetically

        /*
         creating a map of all the available region abbreviation and country
         Example - US: United States, ZA: South Africa
        */
        for (String region : regions){
            countries.put(region,new Locale("",region).getDisplayCountry());
        }
    }

    private String getRegion(String country){
        // the key we want to return

        if (countries.containsKey(country)) return country;

        // checking whether we have the given country in our map
        if (countries.containsValue(country)) {
            // retrieving the index of the country in the list
            int countryIndex = countries.values().stream().toList().indexOf(country);
            // retrieving the country region
            return countries.keySet().stream().toList().get(countryIndex);
        }

        return "";
    }

    @Override
    public String toString(){

        return (this.countryCode == null || this.number == null)
                ? "Error type: INVALID_COUNTRY_CODE. Missing or invalid region."
                : this.countryCode +this.number;
    }

}
