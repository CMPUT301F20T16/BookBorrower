package com.example.vivlio;

import android.app.Activity;
import android.view.SurfaceView;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import com.example.vivlio.Activities.AddBook;
import com.example.vivlio.Activities.BarcodeScannerActivity;
import com.example.vivlio.Activities.LoginActivity;
import com.example.vivlio.Activities.MainActivity;
import com.example.vivlio.Activities.Mybook_Accepted;
import com.example.vivlio.Activities.Mybook_Avalible;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.robotium.solo.Solo;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import static junit.framework.TestCase.assertTrue;


/**
 * tests to see if the user can open the barcode scanner
 */
public class BarcodeScannerTest {
    private Solo solo;

    @Rule
    public ActivityTestRule<LoginActivity> rule =
            new ActivityTestRule<>(LoginActivity.class, true, true);

    /**
     * logs the user in
     * @throws Exception
     */

    @Before
    public void SetUp() throws Exception {
        solo = new Solo(InstrumentationRegistry.getInstrumentation(), rule.getActivity());
        Activity activity = rule.getActivity();
        solo.enterText((EditText) solo.getView(R.id.LOGIN_ETusername), "vanmaren@ualberta.ca");
        solo.enterText((EditText) solo.getView(R.id.LOGIN_ETpassword), "timvm1234");
        solo.clickOnView(solo.getView(R.id.LOGIN_TVlogin));


    }

    /**
     * tests to see if you can click on scan
     */
    @Test
    public void successAdd(){
        solo.clickOnView(solo.getView(R.id.navigation_my_book_list));
        solo.clickOnView(solo.getView(R.id.addBtn));
        solo.clickOnView(solo.getView(R.id.button_scan));


    }



    @After
    public void tearDown() throws Exception {
        solo.finishOpenedActivities();
    }
}