package com.example.vivlio;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;


/**
 * Activity that handles available books that the user owns.
 * Loads the books information and allows the user to edit the information of the book.
 * If user edits the book the activity loads the new information in, also if user deletes the book then
 * it deletes the book from the database
 */

public class mybook_avalible extends AppCompatActivity {
    private TextView titleView;
    private TextView authorView;
    private TextView isbnView;
    private Button editBtn;
    private Book book;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private Book updatedBook;
    private Boolean trigger = false;


    /**
     * General OnCreate method that loads the books information and sets the information to be displayed
     * editBtn on click listener will lead to a new activity that allows the user to edit the books information. It
     * will also return the updated information if their was a change
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mybook_avalible);

        Intent intent = getIntent();
        book = (Book) intent.getSerializableExtra("book");

        titleView = findViewById(R.id.titleView);
        authorView = findViewById(R.id.authorView);
        isbnView = findViewById(R.id.isbnView);
        editBtn = findViewById(R.id.editBtn);

        titleView.setText(book.getTitle());
        authorView.setText(book.getAuthor());
        isbnView.setText(book.getISBN());


        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent editIntent = new Intent(mybook_avalible.this, editBook.class);
                if (trigger == false) {
                    editIntent.putExtra("book", book);
                }
                else {
                    editIntent.putExtra("book", updatedBook);
                }
                startActivityForResult(editIntent, 0);

            }
        });

    }


    /**
     * OnActivityResult method that handles the information the editBook book returns
     * If the requestCode is 0 and is equal to Activity.RESULT_OK then it means
     * The books information was updated. So then we update the books information
     * In the database. If the resultCode is equal to Activity.RESULT_CANCELED then
     * no changes were made so we do nothing. If the resultCode is equal to 5, then
     * the user decided that they wanted to delete the book, so we delete the book
     * from the database
     * @param requestCode
     * @param resultCode
     * @param data
     */

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == 0) {
            if (resultCode == Activity.RESULT_OK) {
                trigger = true;
                updatedBook  = (Book) data.getSerializableExtra("book");

                titleView.setText(updatedBook.getTitle());
                authorView.setText(updatedBook.getAuthor());
                isbnView.setText(updatedBook.getISBN());

                db = FirebaseFirestore.getInstance();
                mAuth = FirebaseAuth.getInstance();
                final FirebaseUser Firebaseuser = mAuth.getCurrentUser();

                String uid = Firebaseuser.getUid();

                Log.i("uid", uid);
                db.collection("users").document(uid + "/owned/" + updatedBook.getISBN())
                        .update(
                                "author", updatedBook.getAuthor(),
                                "title", updatedBook.getTitle()
                        );


                /*
                db.collection("users").document(uid + "/owned/")
                        .update(
                                book.getISBN(), updatedBook.getISBN()
                        );

                 */
            }

            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }


            if (resultCode == 5) {

                db = FirebaseFirestore.getInstance();
                mAuth = FirebaseAuth.getInstance();
                final FirebaseUser Firebaseuser = mAuth.getCurrentUser();

                String uid = Firebaseuser.getUid();

                db.collection("users").document(uid + "/owned/" + book.getISBN())
                        .delete();
                finish();

            }
        }
    }





}