package com.example.qrhunter;

import android.app.Activity;
import android.view.View;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;
import com.robotium.solo.Solo;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class LoginQRCodeGeneratorActivityTest {

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
    public void checkLoginQRCodeGeneratorActivityOpen(){
        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);
        view = solo.getView(R.id.MainMenuLoginQRButton);
        solo.clickOnView(view);
        solo.sleep(1000);
        solo.assertCurrentActivity("Wrong Activity", loginQRCodeGeneratorActivity.class);
    }

    @Test
    public void checkLoginQRCodeGeneratorActivityFunction(){
        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);
        view = solo.getView(R.id.MainMenuLoginQRButton);
        solo.clickOnView(view);
        solo.sleep(1000);

        solo.assertCurrentActivity("Wrong Activity", loginQRCodeGeneratorActivity.class);
        view = solo.getView(R.id.loginQRReturnToMainButton);
        solo.clickOnView(view);
        solo.sleep(1000);
        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);
    }

    @After
    public void tearDown() throws Exception{
        solo.finishOpenedActivities();
    }

}
