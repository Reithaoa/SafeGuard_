package util;
import androidx.annotation.NonNull;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import com.google.i18n.phonenumbers.NumberParseException;
import java.util.*;


public class UserPhoneNumber{
    private final PhoneNumber phoneNumber;
    private final String countryCode;
    private final String number;
    private final PhoneNumberUtil util = PhoneNumberUtil.getInstance();
    private final Map<String,String> countries = new HashMap<>();


    /**
     * Formats the phone number appropriately. A given number will be formated according to the country
     * selected by the user. All numbers will be saved as : + ctrCode-number
     * @param number the users number
     * @param country the country the number belongs to
     * @throws IllegalArgumentException
     */
    public UserPhoneNumber(String number, String country) throws IllegalArgumentException {
        loadRegions();
        this.phoneNumber = setPhoneNumber(number, country);
        if (this.phoneNumber != null) {
            this.countryCode = String.valueOf(this.phoneNumber.getCountryCode());
            this.number = String.valueOf(this.phoneNumber.getNationalNumber());
        } else {
            throw new IllegalArgumentException("Invalid phone number or country code");
        }
    }

    // no arg constructor used for getting phone number examples to display to user
    public UserPhoneNumber(){
        this.phoneNumber = null;
        this.countryCode = null;
        this.number = null;
        loadRegions();
    }

    public Long UserPhoneNumberExample(String country){
        return this.util.getExampleNumber(getRegion(country)).getNationalNumber();
    }


    public String getRegionCode(String region){
        // retrieves the regional code for the country
        return String.valueOf(util.getCountryCodeForRegion(getRegion(region)));
    }

    public String getRegionCodeByCountry(String country){
        // retrieves the regional code for the country
        String region = getRegion(country);
        return String.valueOf(util.getCountryCodeForRegion(getRegion(region)));
    }

    public String getRegion(int code){
        return util.getRegionCodeForCountryCode(code);
    }

    public String getCountry(String code){
        int codeInt = Integer.parseInt(code);

        if(validateRegion(codeInt)){
            String region = getRegion(codeInt);
            return countries.get(region);
        }
        return "";
    }

    public boolean validateRegion(int code){
        String region = getRegion(code);
        return (region != null) && !region.isEmpty();
    }

    public String getCountryCode(){return this.countryCode;}
    public String getNumber(){return this.number;}
    public PhoneNumber getPhoneNumber(){
        return this.phoneNumber;
    }

    private PhoneNumber setPhoneNumber(String number, String country){
        try {
            return util.parse(number, getRegion(Integer.parseInt(country)));
        }catch (NumberParseException e){
            e.getCause();
            return null;
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

    public Map<String,String> getRegions(){
        return this.countries;
    }

    public List<String> getCountries(){
        ArrayList<String> data = new ArrayList<>(this.countries.values());
        Collections.sort(data);
        return data;
    }

    public String getRegion(String country){
        // the key we want to return

        if (countries.containsKey(country)) return country;

        // checking whether we have the given country in our map
        for (Map.Entry<String, String> entry : countries.entrySet()) {
            if (entry.getValue().equals(country)) {
                return entry.getKey(); // return the region (key)
            }
        }


        return "";
    }

    @NonNull
    public Long getUsersPhoneNumber(){
        return Objects.requireNonNull(Long.getLong(this.toString()));
    }

    @NonNull
    @Override
    public String toString(){

        return (this.countryCode == null || this.number == null)
                ? "Error type: INVALID_COUNTRY_CODE. Missing or invalid region."
                : this.countryCode +this.number;
    }

}
