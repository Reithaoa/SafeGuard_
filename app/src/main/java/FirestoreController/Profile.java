package FirestoreController;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Profile extends FirestoreLoader{

    private FirebaseFirestore db;
    private String name;
    private String surname;
    private int age;
    private String height;
    private String race;
    private Contact contact1;
    private Contact  contact2;

    public Profile(String name,String surname,String height, int age,String race, Contact contact1, Contact contact2){
        super();
        this.db = getDb();
        this.name=name;
        this.age=age;
        this.surname=surname;
        this.height = height;
        this.race = race;
        this.contact1 = contact1;
        this.contact2 = contact2;
    }

    public void createProfile(){
        Map<String, Object> userData = getUserData();

        // creating the docRef belonging to our user
        DocumentReference userInfo = this.db.collection("users").document(name);
        // here is where we store our friends
        CollectionReference friends = userInfo.collection("friends");

        // commiting the users information. This includes
        userInfo.set(userData);

        List<Contact> contacts = List.of(contact1,contact2);

        for (Contact contact : contacts){
            String docEntry = ("".equals(contact.getName()))
                    ? "Contact_" + String.valueOf(contacts.indexOf(contact) + 1)
                    : contact.getName();

            // adding the users contact to the db
            friends.document(docEntry).set(getContacts(contact));
        }


    }

    private Map<String, Object> getUserData(){
        Map<String,Object> data = new HashMap<>();
        data.put("name",name);
        data.put("age",age);
        data.put("height",height);
        data.put("race",race);

        return data;
    }

    private Map<String, Object> getContacts(Contact contact){
        Map<String,Object> contacts = new HashMap<>();
        contacts.put("name",contact.getName());
        contacts.put("number",contact.getNumber());
        return contacts;
    }
}
