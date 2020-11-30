package com.example.vivlio;

import android.widget.EditText;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import com.example.vivlio.Activities.CreateAccountActivity;
import com.example.vivlio.Activities.LoginActivity;
import com.example.vivlio.Activities.MainActivity;
import com.robotium.solo.Solo;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class LoginActivityTest {
    private Solo solo;
    @Rule
    public ActivityTestRule<LoginActivity> rule =
            new ActivityTestRule<>(LoginActivity.class, true, true);

    @Before
    public void setUp() throws Exception{
        solo = new Solo(InstrumentationRegistry.getInstrumentation(),rule.getActivity());
        solo.assertCurrentActivity("Didnt open to Login", LoginActivity.class);

    }

    @After
    public void tearDown() throws Exception{
        solo.finishOpenedActivities();
    }

    @Test
    public void testSuccessfulLogin(){
        solo.enterText((EditText) solo.getView(R.id.LOGIN_ETusername), "");
        solo.enterText((EditText) solo.getView(R.id.LOGIN_ETpassword), "");

        solo.enterText((EditText) solo.getView(R.id.LOGIN_ETusername), "vanmaren@ualberta.ca");
        solo.enterText((EditText) solo.getView(R.id.LOGIN_ETpassword), "timvm1234");

        solo.clickOnImageButton(0);

        solo.assertCurrentActivity("Login failed", MainActivity.class);
    }

    @Test
    public void testFailedLogin(){
        solo.enterText((EditText) solo.getView(R.id.LOGIN_ETusername), "");
        solo.enterText((EditText) solo.getView(R.id.LOGIN_ETpassword), "");

        solo.enterText((EditText) solo.getView(R.id.LOGIN_ETusername), "vanmaren@ualberta.ca");
        solo.enterText((EditText) solo.getView(R.id.LOGIN_ETpassword), "pass12");

        solo.clickOnImageButton(0);

        solo.assertCurrentActivity("Login succeeded", LoginActivity.class);
    }

    @Test
    public void testEmptyFields(){

        //Empty fields
        solo.enterText((EditText) solo.getView(R.id.LOGIN_ETusername), "");
        solo.enterText((EditText) solo.getView(R.id.LOGIN_ETpassword), "");
        solo.clickOnImageButton(0);
        solo.assertCurrentActivity("Login succeeded", LoginActivity.class);

        //empty password
        solo.enterText((EditText) solo.getView(R.id.LOGIN_ETusername), "vanmaren@ualberta.ca");
        solo.clickOnImageButton(0);
        solo.assertCurrentActivity("Login succeeded", LoginActivity.class);

        //empty username
        solo.enterText((EditText) solo.getView(R.id.LOGIN_ETusername), "");
        solo.enterText((EditText) solo.getView(R.id.LOGIN_ETpassword), "timvm1234");
        solo.clickOnImageButton(0);
        solo.assertCurrentActivity("Login succeeded", LoginActivity.class);

        //login
        solo.enterText((EditText) solo.getView(R.id.LOGIN_ETusername), "vanmaren@ualberta.ca");
        solo.clickOnImageButton(0);
        solo.assertCurrentActivity("Login failed", MainActivity.class);
    }

    @Test
    public void openCreateAccount(){
        solo.clickOnText("new to Vivlio?");

        solo.assertCurrentActivity("Didnt open to Create account", CreateAccountActivity.class);
    }
}
