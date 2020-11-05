package com.example.vivlio;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

/**
 * This model provides the format of the list of results when searching.
 */
public class SearchUserCustomList extends ArrayAdapter<User> {

    public ArrayList<User> users;
    public Context context;

    public SearchUserCustomList(Context context, ArrayList<User> users) {
        super(context,0,  users);
        this.users = users;
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;

        if(view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.content, parent, false);
        }

        User user = users.get(position);

        TextView username = view.findViewById(R.id.bookName_View);
        TextView name = view.findViewById(R.id.maker_View);

        username.setText(user.getUsername());
        name.setText(user.getName());

        return view;
    }

}
