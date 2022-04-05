package com.example.qrhunter;

import static org.junit.Assert.assertTrue;

import android.app.Activity;
import android.view.View;
import android.widget.EditText;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;
import com.robotium.solo.Solo;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class UserSearchActivityTest {

    private View view;
    private Solo solo;
    @Rule
    public ActivityTestRule<MainActivity> rule =
            new ActivityTestRule<>(MainActivity.class, true, true);

    @Before
    public void setUp() throws Exception{
        solo = new Solo(InstrumentationRegistry.getInstrumentation(),rule.getActivity());
    }
    /**
     * Gets the Activity
     * @throws Exception
     */
    @Test
    public void start() throws Exception{
        Activity activity = rule.getActivity();
    }

    @Test
    public void checkUserProfileViewerOpen(){
        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);
        view = solo.getView(R.id.MainMenuSearchButton);
        solo.clickOnView(view);
        solo.sleep(1000);
        solo.assertCurrentActivity("Wrong Activity", userSearchActivity.class);

        view = solo.getView(R.id.imageButton3);
        solo.clickOnView(view);
        solo.sleep(1000);
        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);
    }

    @Test
    public void checkSearchByUsername(){

        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);
        view = solo.getView(R.id.MainMenuSearchButton);
        solo.clickOnView(view);
        solo.sleep(1000);
        solo.assertCurrentActivity("Wrong Activity", userSearchActivity.class);

        view = solo.getView(R.id.usernameSearchEditText);
        solo.enterText((EditText)view, "!");
        view = solo.getView(R.id.usernameSearchButton);
        solo.clickOnView(view);
        solo.sleep(1000);
        assertTrue(solo.waitForText("No user with the username: ! exists.\nPlease try a different username",
                1, 1000));
        view = solo.getView(R.id.usernameSearchEditText);
        solo.clearEditText((EditText)view);
        solo.sleep(1000);

        view = solo.getView(R.id.usernameSearchButton);
        solo.clickOnView(view);
        solo.sleep(1000);
        view = solo.getView(R.id.usernameSearchEditText);
        solo.clickOnView(view);
        assertTrue(solo.waitForText("Please input a username to search",
                1, 1000));
        solo.clearEditText((EditText)view);
        solo.sleep(1000);

        solo.enterText((EditText)view, "_");
        view = solo.getView(R.id.usernameSearchButton);
        solo.clickOnView(view);
        assertTrue(solo.waitForText("Username contains invalid characters '_' or ' ', please try again",
                1, 1000));
        view = solo.getView(R.id.usernameSearchEditText);
        solo.clearEditText((EditText)view);
        solo.sleep(1000);

        solo.enterText((EditText)view, "terry");
        view = solo.getView(R.id.usernameSearchButton);
        solo.clickOnView(view);
        solo.sleep(1000);
        solo.assertCurrentActivity("Wrong Activity", userProfileViewerActivity.class);
        assertTrue(solo.waitForText("terry", 1, 1000));

        view = solo.getView(R.id.qrViewerBackButton);
        solo.clickOnView(view);
        solo.sleep(1000);
        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);
    }

    @Test
    public void checkSearchByScanningQR(){
        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);
        view = solo.getView(R.id.MainMenuSearchButton);
        solo.clickOnView(view);
        solo.sleep(1000);
        solo.assertCurrentActivity("Wrong Activity", userSearchActivity.class);

        view = solo.getView(R.id.searchByQRButton);
        solo.clickOnView(view);
        solo.sleep(1000);
        solo.assertCurrentActivity("Wrong Activity", userSearchQRScanActivity.class);
        solo.goBack();
        solo.sleep(1000);
        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);
    }

    @After
    public void tearDown() throws Exception{
        solo.finishOpenedActivities();
    }
}
