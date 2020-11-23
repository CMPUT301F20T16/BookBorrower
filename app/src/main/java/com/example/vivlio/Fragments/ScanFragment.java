package com.example.vivlio.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.vivlio.Activities.BorrowTaskActivity;
import com.example.vivlio.Activities.LendTaskActivity;
import com.example.vivlio.R;
import com.example.vivlio.Activities.RecievingTaskActivity;
import com.example.vivlio.Activities.ReturningTaskActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;


public class ScanFragment extends Fragment {

    /**
     * Required empty public constructor
     */
    public ScanFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * inflates view with fragment_scan
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_scan, container, false);

    }

    /**
     * opens Borrowing or Lending views depending on which button the user taps on
     * @param view
     * @param savedInstanceState
     */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        FloatingActionButton borrowFAB = view.findViewById(R.id.EXCHANGE_FABborrow);
        FloatingActionButton lendFAB = view.findViewById(R.id.EXCHANGE_FABlend);
        FloatingActionButton returnFAB = view.findViewById(R.id.EXCHANGE_FABreturning);
        FloatingActionButton recieveFAB = view.findViewById(R.id.EXCHANGE_FABrecieving);

        borrowFAB.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                openBorrowing();
            }
        });

        lendFAB.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                openLending();
            }
        });

        returnFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openReturn();
            }
        });

        recieveFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                openRecieve();
            }
        });
    }

    /**
     * opens List of books that the current user has accepted the requests of
     */
    public void openLending(){
        Intent intent = new Intent(ScanFragment.this.getActivity(), LendTaskActivity.class);
        startActivity(intent);
    }

    /**
     * opens a list of books that the user has requested that has been accepted
     */
    public void openBorrowing(){
        Intent intent = new Intent(ScanFragment.this.getActivity(), BorrowTaskActivity.class);
        startActivity(intent);
    }

    /**
     * opens a list of books that the user has borrowed
     */
    public void openReturn(){
        Intent intent = new Intent(ScanFragment.this.getActivity(), ReturningTaskActivity.class);
        startActivity(intent);
    }

    /**
     * opens a list of books that the user has lent
     */
    public void openRecieve(){
        Intent intent = new Intent(ScanFragment.this.getActivity(), RecievingTaskActivity.class);
        startActivity(intent);
    }

}