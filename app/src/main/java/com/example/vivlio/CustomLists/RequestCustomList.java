package com.example.vivlio.CustomLists;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.vivlio.Models.Book;
import com.example.vivlio.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * RequestCustomList.java
 *
 * Custom List that handles the content for the MyRequestListFragment. Extends ArrayAdapter and is
 * used in the ListView for MyRequestListFragment.
 *
 * Displays the title, author, owner (by their username) and status of the requested book.
 *
 * Book owner is stored as their unique UID so Firebase access is needed to access the associated
 * username.
 *
 * Issues:
 * Loading the owner name is a little slow since it relies on database access, the user can see the
 * TextViews say "Loading..." for a brief second. Not sure if there's a remedy for this, maybe
 * caching the usernames on the first run?
 *
 */

public class RequestCustomList extends ArrayAdapter {

    private ArrayList<Book> books;
    private Context context;

    private FirebaseFirestore db;
    private String userName;

    /**
     * Constructor to set the context and the ArrayList with the data.
     * @param context
     * @param books
     */
    public RequestCustomList( Context context, ArrayList<Book> books) {
        super(context,0,books);
        this.context = context;
        this.books = books;
    }


    /**
     * Gets the view to be displayed in the ListView. Inflates content from content_request_list.xml
     * and sets the TextViews to the relevant information. Firebase access is used to get the
     * username from the stored UID in the book object.
     *
     * @param position
     * @param convertView
     * @param parent
     * @return the constructed view
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        DecimalFormat priceFormat = new DecimalFormat("0.00");
        View view = convertView;
        //if the view is null create and new view and inflate it
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.content_request_list, parent, false);
        }

        final Book book = books.get(position);
        final View finalView = view;

        // set TextViews to the appropriate information
        TextView titleView = finalView.findViewById(R.id.tv_title);
        TextView authorView = finalView.findViewById(R.id.tv_author);
        TextView statusView = finalView.findViewById(R.id.tv_status);

        titleView.setText(book.getTitle());
        authorView.setText(book.getAuthor());
        statusView.setText(book.getStatus());

        // turn owner code into the username to display
        // update owner TextView
        db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.document("users/" + book.getOwner());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot document = task.getResult();

                // make sure that document for that user isn't null (crashes the app if it is)
                if (document.getData() != null) {
                    userName = document.getData().get("username").toString();
                    // Log.d("username", userName);
                    String ownerText = "Owner: " + userName;

                    TextView ownerView = finalView.findViewById(R.id.tv_owner);
                    ownerView.setText(ownerText);

                }


            }
        });

        //return the view
        return view;
    }
}
