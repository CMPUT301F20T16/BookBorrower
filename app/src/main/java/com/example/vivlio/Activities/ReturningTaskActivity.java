package com.example.vivlio.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.vivlio.Models.Book;
import com.example.vivlio.CustomLists.ReturningTaskCustomList;
import com.example.vivlio.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class ReturningTaskActivity extends AppCompatActivity {
    ArrayAdapter<Book> bookAdapter;
    ArrayList<Book> bookDataList;
    String selectedISBN;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private CollectionReference collectionReference;
    private static final int RESULT_INVALID = 30000;
    private static final int RESULT_INCOMPLETE = 30001;
    String otherUID;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_returntask);

        ListView BookListLV;

        BookListLV = findViewById(R.id.RETURNT_LVbooks);
        bookDataList = new ArrayList<>();
        bookAdapter = new ReturningTaskCustomList(this, bookDataList);
        BookListLV.setAdapter(bookAdapter);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        String currentUID = mAuth.getCurrentUser().getUid();
        collectionReference = db.collection("users" + "/" + currentUID + "/requested");

        collectionReference.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable
                    FirebaseFirestoreException error) {
                bookDataList.clear();
                for(QueryDocumentSnapshot doc: queryDocumentSnapshots)
                {
                    if (!doc.getData().containsKey("blank")) {

                        if (doc.getData().get("status").toString().equals("borrowed")) {
                            ArrayList<String> owner = new ArrayList<>();
                            owner.add(doc.getData().get("owners").toString());

                            Book book = new Book(doc.getData().get("title").toString(),
                                    doc.getData().get("author").toString(),
                                    owner.get(0), doc.getId().replace("-", ""));
                            //Log.e("ISB", doc.getId());
                            //Log.e("title", doc.getData().get("title").toString());
                            bookDataList.add(book);
                        }
                    }
                }
                bookAdapter.notifyDataSetChanged();
            }
        });

        BookListLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                selectedISBN = bookDataList.get(i).getISBN();
                otherUID = bookDataList.get(i).getOwner();
                Log.e("SELECTED BOOK", bookDataList.get(i).getTitle());
                Log.e("SELECTED BOOK ISBN", bookDataList.get(i).getISBN());
                openScanner();

            }
        });

    }

    public void openScanner(){
        Intent intent = new Intent(ReturningTaskActivity.this, BarcodeScannerActivity.class);
        startActivityForResult(intent, 0);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {
//            Log.e("scanned isbn in task", result);

            if (resultCode == Activity.RESULT_OK || resultCode == RESULT_INCOMPLETE) {
                String result = data.getStringExtra("isbn");

                if (selectedISBN.equals(result)) {
                    Intent intent = new Intent(ReturningTaskActivity.this, SuccessExchangeActivity.class);
                    intent.putExtra("RETURNER", result);
                    intent.putExtra("OTHER_UID", otherUID);
                    startActivity(intent);
                } else {
                    Toast.makeText(ReturningTaskActivity.this, "ISBN did not match selected book!",
                            Toast.LENGTH_SHORT).show();
                }
            } else if (resultCode == RESULT_INVALID) {
                Toast.makeText(ReturningTaskActivity.this, "ISBN is not valid",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}
