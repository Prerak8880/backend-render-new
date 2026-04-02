package com.example.demo.service;

import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.CreateUserRequest;
import com.example.demo.usermodel.User;
import com.google.api.core.ApiFuture;
import com.google.api.services.storage.Storage.Projects.HmacKeys.Create;
import com.google.cloud.firestore.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class UserService {

    @Autowired
    Firestore firestore;

    private static final String COLLECTION_NAME = "users";

    public String createUser(CreateUserRequest request) throws Exception {
        UserRecord.CreateRequest createRequest = new UserRecord.CreateRequest()
                .setEmail(request.getEmail())
                .setPassword(request.getPassword());

        UserRecord userRecord = FirebaseAuth.getInstance().createUser(createRequest);
        String uid = userRecord.getUid();

        User user = new User(
                uid,
                request.getFirstName(),
                request.getLastName(),
                request.getEmail(),
                request.getPhone()
        );

        ApiFuture<WriteResult> future = firestore.collection(COLLECTION_NAME).document(uid).set(user);
        return "User Created: " + future.get().getUpdateTime();
    }

    public List<User> getAllUsers() throws Exception {
        Iterable<DocumentReference> documents = firestore.collection(COLLECTION_NAME).listDocuments();
        List<User> users = new ArrayList<>();

        for (DocumentReference doc : documents) {
            DocumentSnapshot snapshot = doc.get().get();
            User user = snapshot.toObject(User.class);
            if (user != null) {
                users.add(user);
            }
        }
        return users;
    }

    public boolean loginUser(LoginRequest request) {
        try {
            // Verify user with Firebase Auth
            UserRecord userRecord = FirebaseAuth.getInstance().getUserByEmail(request.getEmail());
            
            // Note: Firebase Admin SDK doesn't verify passwords directly
            // You need to use Firebase Authentication REST API or Firebase Client SDK
            // Here's a workaround - check if user exists
            if (userRecord != null && userRecord.getEmail().equals(request.getEmail())) {
                // For production, implement proper password verification
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public User getUser(String id) throws Exception {
        DocumentReference doc = firestore.collection(COLLECTION_NAME).document(id);
        DocumentSnapshot snapshot = doc.get().get();
        return snapshot.toObject(User.class);
    }

    public String updateUser(String id, User user) {
        firestore.collection(COLLECTION_NAME).document(id).set(user);
        return "User Updated";
    }

    public String deleteUser(String id) throws Exception {
        firestore.collection(COLLECTION_NAME).document(id).delete();
        FirebaseAuth.getInstance().deleteUser(id);
        return "User Deleted";
    }
}