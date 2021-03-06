package com.example.vivlio;
import android.app.Activity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import com.example.vivlio.Activities.LocationActivity;
import com.example.vivlio.Activities.LoginActivity;
import com.example.vivlio.Activities.MainActivity;
import com.example.vivlio.Activities.Mybook_Accepted;
import com.example.vivlio.Activities.Mybook_Avalible;
import com.example.vivlio.Activities.Mybook_Borrowed;
import com.example.vivlio.Activities.Mybook_Pending;
import com.example.vivlio.Activities.RequestDetailActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.robotium.solo.Solo;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static junit.framework.TestCase.assertTrue;


/**
 * Test class for the MyBookList fragment and switching between the different book status
 * Tests adding book aswell
 */
@RunWith(AndroidJUnit4.class)
public class MyBookListTesting {

    private Solo solo;

    @Rule
    public ActivityTestRule<LoginActivity> rule =
            new ActivityTestRule<>(LoginActivity.class, true, true);

    @Before
    public void SetUp() throws Exception {
        solo = new Solo(InstrumentationRegistry.getInstrumentation(), rule.getActivity());
    }

    /**
     * Gets the activity
     * @throws Exception
     */
    @Test
    public void start() throws Exception{
        Activity activity = rule.getActivity();
    }


    /**
     * Logs into the user using the provided username and password
     * Tests to make sure it is in the correct activity
     * Clicks on the MyBookList fragment
     * Adds the information for a new book and clicks confirm to add the book
     * Clicks on the different tabs to make sure the navigation between tabs works
     */
    @Test
    public void checkShow() {
        solo.assertCurrentActivity("Didnt open to Login", LoginActivity.class);
        solo.enterText((EditText) solo.getView(R.id.LOGIN_ETusername), "vanmaren@ualberta.ca");
        solo.enterText((EditText) solo.getView(R.id.LOGIN_ETpassword), "timvm1234");
        solo.clickOnImageButton(0);
        solo.assertCurrentActivity("Login failed", MainActivity.class);

        solo.clickOnView(solo.getView(R.id.navigation_my_book_list));
        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);

        FloatingActionButton add = (FloatingActionButton)solo.getView(R.id.addBtn);
        solo.clickOnView(add);

        solo.enterText((EditText) solo.getView(R.id.edit_author), "testingAuthor");
        solo.enterText((EditText) solo.getView(R.id.edit_title), "testingTitle");
        solo.enterText((EditText) solo.getView(R.id.edit_isbn), "testingIsbn");

        ImageButton confirm = (ImageButton) solo.getView(R.id.button_upload);
        solo.clickOnView(confirm);

        solo.clickOnText("Accepted");
        solo.clickOnText("Available");
        solo.clickOnText("Pending");
        solo.clickOnText("Borrowed");
        solo.clickOnText("All");


        // test details for avalible
        solo.clickOnText("Available");
        solo.sleep(1000);
        solo.clickInList(0);
        solo.assertCurrentActivity("Didn't click", Mybook_Avalible.class);
        solo.sleep(1000);
        solo.goBack();

        // test details for accepted
        solo.clickOnText("Accepted");
        solo.sleep(1000);
        solo.clickInList(0);
        solo.assertCurrentActivity("Didn't click", Mybook_Accepted.class);
        solo.sleep(1000);

        solo.clickOnView(solo.getView(R.id.mapsBtn));
        solo.assertCurrentActivity("Location activity didn't launch", LocationActivity.class);
        solo.sleep(1000);

        solo.clickOnView(solo.getView(R.id.done_button));
        solo.assertCurrentActivity("Didn't return from location", Mybook_Accepted.class);
        solo.sleep(1000);

        solo.goBack();

        // test details for pending
        solo.clickOnText("Pending");
        solo.sleep(1000);
        solo.clickInList(0);
        solo.assertCurrentActivity("Didn't click", Mybook_Pending.class);
        solo.sleep(1000);
        solo.goBack();

        // test details for borrowed
        solo.clickOnText("Borrowed");
        solo.sleep(1000);
        solo.clickInList(0);
        solo.assertCurrentActivity("Didn't click", Mybook_Borrowed.class);
        solo.sleep(1000);
    }

    /**
     * closes the activities
     * @throws Exception
     */
    @After
    public void tearDown() throws Exception{
        solo.finishOpenedActivities();
    }
}













