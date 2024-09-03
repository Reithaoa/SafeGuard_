package com.example.myapplication.firestorecontroller;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class UploadContact extends FirestoreLoader {

    private final String user;
    private final Contact contact;
    private final FirebaseFirestore db;


    /**
     * This module will be used to instantiate the data and prepare it for pushing into
     * our firestore db
     * @param user the name of the user
     * @param contact the contact to add to the db. It consists of the user's name and number
     */
    public UploadContact(String user, Contact contact){
        super();
        this.db = super.getDb();
        this.user = user;
        this.contact = contact;
    }

    public void pushContactToFS(){
        Map<String, Object> data = new HashMap<>();
        data.put("name",contact.getName());
        data.put("number",contact.getNumber());

        // getting the document that belongs to the user currently using the app
        DocumentReference userInfo = this.db.collection("users").document(user);
        // All emergency contacts will be saved under friends collection
        CollectionReference friends = userInfo.collection("friends");

        // Each emergency contact will have a document of their own.
        DocumentReference newContact = friends.document(contact.getName()); // the name of the emergency contact will be the name of the doc
        newContact.set(data); // adding the new user to our friends list in the fire store


    }

}
