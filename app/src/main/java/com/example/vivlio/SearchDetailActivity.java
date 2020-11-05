package com.example.vivlio;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class SearchDetailActivity extends AppCompatActivity {
    private TextView titleEditText;
    private TextView authorEditText;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private CollectionReference userCollection;
    private ArrayAdapter<User> ownerAdapter;
    private ArrayList<User> ownerDataList;
    private ListView resultList;
    private Boolean available = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_detail);

        titleEditText = findViewById(R.id.search_detail_title);
        authorEditText = findViewById(R.id.search_detail_author);
        resultList = findViewById(R.id.searchOwnersList);

        ownerDataList = new ArrayList<>();
        ownerAdapter = new SearchDetailCustomList(this, ownerDataList);
        resultList.setAdapter(ownerAdapter);

        Intent intent = getIntent();
        final Book searchDetailBook = (Book) intent.getSerializableExtra("selected book");

        titleEditText.setText(searchDetailBook.getTitle());
        authorEditText.setText(searchDetailBook.getAuthor());

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        final FirebaseUser user = mAuth.getCurrentUser();

        userCollection = db.collection("users/");

        Log.e("YOYO", searchDetailBook.getCurrentOwners().get(0));

        for(final String owner : searchDetailBook.getCurrentOwners()) {
            Task<DocumentSnapshot> userDoc = userCollection.document(owner).get();
            CollectionReference ownedCollection = db.collection("users/" + owner + "/owned");
            Log.e("TASK COLLECTION", ownedCollection.getPath() + searchDetailBook.getISBN());
            ownedCollection.document(searchDetailBook.getISBN()).get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            final DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                if (document.get("status").toString().equals("available") || document.get("status").toString().equals("pending")) {
                                    Log.e("TASK RESULT", document.get("status").toString() + available.toString());
                                    Log.e("OWNER", owner);
                                    userCollection.document(owner).get()
                                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                    final DocumentSnapshot goodUser = task.getResult();
                                                    if(goodUser.exists()) {
                                                        Log.e("GOOD USER", goodUser.getData().get("lname").toString() + document.getData().get("status").toString());
                                                        //ownerDataList.add(new User(goodUser.getData().get("lname").toString(), goodUser.getData().get("username").toString(), document.getData().get("status").toString()));
                                                        Log.e("SIZE", String.valueOf(ownerDataList.size()));
                                                        CollectionReference requester = db.collection("users/" + mAuth.getUid() + "/requested");
                                                        Log.e("PATH", searchDetailBook.getISBN());
                                                        requester.document(searchDetailBook.getISBN()).get()
                                                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                        final DocumentSnapshot requestedSnapshot = task.getResult();
                                                                        if(requestedSnapshot.exists()) {
                                                                            ArrayList<String> checkUser = (ArrayList<String>) requestedSnapshot.get("owners");
                                                                            if (checkUser.contains(goodUser.getId())) {
                                                                                ownerDataList.add(new User(goodUser.getId(), goodUser.getData().get("lname").toString(), goodUser.getData().get("username").toString(), "pending", ""));
                                                                                Log.e("AVAILABLE_USER", goodUser.getData().get("lname").toString());
                                                                            }
                                                                            else {
                                                                                ownerDataList.add(new User(goodUser.getId(), goodUser.getData().get("lname").toString(), goodUser.getData().get("username").toString(), "available", ""));
                                                                                Log.e("REQUESTED_USER", goodUser.getData().get("lname").toString());
                                                                            }
                                                                        }
                                                                        else {
                                                                            ownerDataList.add(new User(goodUser.getId(), goodUser.getData().get("lname").toString(), goodUser.getData().get("username").toString(), "available", ""));
                                                                            Log.e("REQUESTED_USER", goodUser.getData().get("lname").toString());
                                                                        }
                                                                        ownerAdapter.notifyDataSetChanged();
                                                                    }
                                                                });
                                                    }
                                                }
                                            });
                                }
                            }

                        }
                    });
        }

        resultList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> adapterView, View view, final int i, long l) {
                /*
                User clicks on unrequested owner
                if the book is not in requested:
                    create book in requested book collection with title, author, location, ISBN, photoLink,

                Go to mAuth's request and make status pending
                In mAuth's requested add, the user ID of the person we are requesting from under the owner array

                Go to owner of book and add mAuth to borrower array
                 */
                final User chosenOne = (User) adapterView.getItemAtPosition(i);
                final CollectionReference requestedCollection = db.collection("users/" + mAuth.getUid() + "/requested/");
                final CollectionReference ownerCollection = db.collection("users/" + chosenOne.getUid() + "/owned/");

                requestedCollection.document(searchDetailBook.getISBN()).get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                DocumentSnapshot doc = task.getResult();
                                if(!doc.exists()) {

                                    HashMap<String, Object> newRequestedBook = new HashMap<>();
                                    newRequestedBook.put("title", searchDetailBook.getTitle());
                                    newRequestedBook.put("author", searchDetailBook.getAuthor());
                                    newRequestedBook.put("status", "pending");
                                    GeoPoint location = new GeoPoint(0,0);
                                    newRequestedBook.put("location", location);
                                    newRequestedBook.put("path", "link");
                                    newRequestedBook.put("owners", new ArrayList<String>());
                                    requestedCollection.document(searchDetailBook.getISBN()).set(newRequestedBook);
                                }
                                requestedCollection.document(searchDetailBook.getISBN()).get()
                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                DocumentSnapshot addOwnerDoc = task.getResult();
                                                ArrayList<String> requestOwners = new ArrayList<>();
                                                requestOwners = (ArrayList<String>) addOwnerDoc.get("owners");
                                                requestOwners.add(chosenOne.getUid());
                                                requestedCollection.document(searchDetailBook.getISBN()).update("owners", requestOwners);
                                                ownerCollection.document(searchDetailBook.getISBN()).get()
                                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                DocumentSnapshot addBorrowerdoc = task.getResult();
                                                                ArrayList<String> requestBorrowers = (ArrayList<String>) addBorrowerdoc.get("borrowers");
                                                                requestBorrowers.add(mAuth.getUid());
                                                                ownerCollection.document(searchDetailBook.getISBN()).update("borrowers", requestBorrowers);
                                                            }
                                                        });
                                            }
                                        });

                            }
                        });
                chosenOne.setOwnedBookStatus("pending");
                ownerDataList.set(i, chosenOne);
                ownerAdapter.notifyDataSetChanged();
            }
        });
    }

}
