package com.example.myapplication.firestorecontroller;

import com.example.myapplication.MainActivity;
import com.google.firebase.firestore.FirebaseFirestore;

class FirestoreLoader extends MainActivity {

    private final FirebaseFirestore db;

    /**
     * Just creating an instance our firestore
     */
    public FirestoreLoader(){
        this.db = FirebaseFirestore.getInstance();
    }

    public FirebaseFirestore getDb(){
        return this.db;
    }
}
