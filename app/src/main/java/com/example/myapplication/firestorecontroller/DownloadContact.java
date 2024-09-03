package com.example.myapplication.firestorecontroller;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class DownloadContact extends FirestoreLoader {

    private final FirebaseFirestore db;
    private final String user;
    private final String friend;

    /**
     * This module will be used to retrieve a contact from the firestore.<br>
     * The contact can belong to the currently logged in usr or an emergency contact<br>
     * @param user the current / active user that is logged in on the app.
     * @param friend the name of the person who's contact details we want
     */
    public DownloadContact(String user, String friend){
        this.db = super.getDb();
        this.user = user;
        this.friend =friend;
    }

    public Contact downloadOneContact(){

        CollectionReference friends = getFriendsCollection();
        // retrieving the contact belonging to the friend
        DocumentReference friend =  friends.document(this.friend);
        Task<DocumentSnapshot> data = friend.get();

        if (data.isSuccessful()){
            DocumentSnapshot user = data.getResult();
            return genContact(user);
        }

        return new Contact("","");

    }

    public ArrayList<Contact> downloadAll(){
        CollectionReference friends = getFriendsCollection();
        // attempting to retrieve all the friends our user has
        Task<QuerySnapshot> data = friends.get();

        if(data.isSuccessful()){
            QuerySnapshot userFriends = data.getResult();
            return genContacts(userFriends);
        }

        return new ArrayList<>();
    }

    private ArrayList<Contact> genContacts(QuerySnapshot friends){
        ArrayList<Contact> contacts = new ArrayList<>();

        for (QueryDocumentSnapshot friend : friends){
            // if the friend exists than we adding the friend to the list
            if(friend.contains("name") && friend.contains("number")){
                contacts.add( genContact(friend));
            }
        }
        return contacts;
    }

    private Contact genContact(DocumentSnapshot user){
        return new Contact((String) user.get("name"), (String) user.get("number"));
    }
    private CollectionReference getFriendsCollection(){
        // getting the document that belongs to our user
        DocumentReference usersDoc = db.collection("users").document(this.user);
        return usersDoc.collection("friends");
    }
}
