package com.example.vivlio.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;

import com.example.vivlio.Activities.SearchUserDetailActivity;
import com.example.vivlio.Models.Book;
import com.example.vivlio.CustomLists.SearchBookCustomList;
import com.example.vivlio.CustomLists.SearchUserCustomList;
import com.example.vivlio.R;
import com.example.vivlio.Activities.SearchDetailActivity;
import com.example.vivlio.Models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SearchFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SearchFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private EditText searchEditText;
    private Switch searchSwitch;
    private FloatingActionButton searchButton;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private ArrayAdapter<Book> resultAdapter;
    private ArrayList<Book> resultDataList;
    private ListView resultList;
    private ArrayAdapter<User> userAdapter;
    private ArrayList<User> userDataList;
    private CollectionReference bookCollection;
    private CollectionReference userCollection;
    private Boolean available = false;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public SearchFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SearchFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SearchFragment newInstance(String param1, String param2) {
        SearchFragment fragment = new SearchFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * General onCreate method to display the fragment
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    /**
     * Runs once the view has been created and inflates the view into the fragment space provided in the center of the screen.
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    /**
     * Runs once the view has been created and populates the page with necessary components. Allows the user to click search and query the database for a book or user with the specified keyword(s).
     * @param view
     * @param savedInstanceState
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        searchEditText = view.findViewById(R.id.searchTermEditText);
        searchSwitch = view.findViewById(R.id.search_switch);
        searchButton = view.findViewById(R.id.search_button);
        resultList = view.findViewById(R.id.result_list_view);

        resultDataList = new ArrayList<>();
        resultAdapter = new SearchBookCustomList(getActivity(), resultDataList);

        userDataList = new ArrayList<>();
        userAdapter = new SearchUserCustomList(getActivity(), userDataList);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        final FirebaseUser user = mAuth.getCurrentUser();

        bookCollection = db.collection("books/");
        userCollection = db.collection("users/");


        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                boolean switchState = searchSwitch.isChecked(); //Checked = books, unchecked = users
                final String[] searchTerms = searchEditText.getText().toString().split("\\s+");
                Log.d("SEARCHING", "Initiated Search with Term: " + searchTerms[0]);
                userDataList.clear();
                userAdapter.notifyDataSetChanged();
                resultDataList.clear();
                resultAdapter.notifyDataSetChanged();

                if(switchState) {
                    bookCollection.addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                            resultDataList.clear();
                            userDataList.clear();
                            resultList.setAdapter(resultAdapter);
                            final ArrayList<String> ISBNList =  new ArrayList<>();
                            for(final QueryDocumentSnapshot doc: value) {
                                final String title = doc.getData().get("title").toString();
                                final String author = doc.getData().get("author").toString();
                                final ArrayList<String> bookCollectionOwners = (ArrayList<String>) doc.getData().get("owners");
                                Log.d("INFO", "Current title and author: " + title + author + " " + !bookCollectionOwners.contains(mAuth.getUid()));
                                for(String term : searchTerms) {
                                    if ((title.toLowerCase().contains(term.toLowerCase()) || author.toLowerCase().contains(term.toLowerCase()))) {
                                        final ArrayList<String> owners = (ArrayList<String>) doc.getData().get("owners");

                                        for(String owner : owners) {
                                            if(!owner.equals(mAuth.getUid())) {
                                                Task<DocumentSnapshot> userDoc = userCollection.document(owner).get();
                                                CollectionReference ownedCollection = db.collection("users/" + owner + "/owned");
                                                Log.d("TASK COLLECTION", ownedCollection.getPath() + doc.getId());
                                                ownedCollection.document(doc.getId().toString()).get()
                                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                DocumentSnapshot document = task.getResult();
                                                                if (document.exists()) {
                                                                    if (document.get("status").toString().equals("available") || document.get("status").toString().equals("pending")) {
                                                                        available = true;
                                                                        Book resultBook;
                                                                        resultBook = new Book(title, author, doc.getId().toString(), owners);
                                                                        Log.d("POS_RESULT", resultBook.getTitle() + ", " + resultBook.getAuthor());
                                                                        if ((ISBNList.isEmpty() || !ISBNList.contains(doc.getId().toString())) && available) {
                                                                            resultDataList.add(resultBook);
                                                                            ISBNList.add(doc.getId().toString());
                                                                            available = false;
                                                                        }
                                                                        Log.d("TASK RESULT", document.get("status").toString() + available.toString());
                                                                        resultAdapter.notifyDataSetChanged();
                                                                    }
                                                                }

                                                            }
                                                        });
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    });
                }
                else {
                    userCollection.addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                            resultDataList.clear();
                            userDataList.clear();
                            resultList.setAdapter(userAdapter);
                            ArrayList<String> userIDList = new ArrayList<>();
                            for(QueryDocumentSnapshot doc: value) {
                                String username = doc.getData().get("username").toString();
                                for(String term : searchTerms) {
                                    if(username.toLowerCase().contains(term.toLowerCase())) {
                                        User resultUser;
                                        resultUser = new User(doc.getData().get("fname").toString() + " " + doc.getData().get("lname").toString(), username, doc.getData().get("email").toString(), doc.getData().get("phone").toString());
                                        if(userIDList.isEmpty() || !userIDList.contains(doc.getId().toString())) {
                                            userDataList.add(resultUser);
                                            userIDList.add(doc.getId().toString());
                                        }
                                    }
                                }
                            }
                            userAdapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        });

        // On click listener for the books search result sos that the user can then request the book
        resultList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(searchSwitch.isChecked()) {
                    Intent intent = new Intent(SearchFragment.this.getActivity(), SearchDetailActivity.class);
                    intent.putExtra("selected book", (Book) adapterView.getItemAtPosition(i));
                    startActivity(intent);
                }
                else {
                    Intent intent = new Intent(SearchFragment.this.getActivity(), SearchUserDetailActivity.class);
                    intent.putExtra("selected user", (User) adapterView.getItemAtPosition(i));
                    startActivity(intent);
                }
            }
        });

        searchSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userDataList.clear();
                userAdapter.notifyDataSetChanged();
                resultDataList.clear();
                resultAdapter.notifyDataSetChanged();
            }
        });

    }
}