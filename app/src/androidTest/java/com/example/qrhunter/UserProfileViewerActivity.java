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

public class UserProfileViewerActivity {

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
        view = solo.getView(R.id.MainMenuProfileButton);
        solo.clickOnView(view);
        solo.sleep(1000);
        solo.assertCurrentActivity("Wrong Activity", userProfileViewerActivity.class);
    }

    @Test
    public void checkUserProfileViewerFunction(){
        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);
        view = solo.getView(R.id.MainMenuProfileButton);
        solo.clickOnView(view);
        solo.sleep(1000);
        solo.assertCurrentActivity("Wrong Activity", userProfileViewerActivity.class);

        view = solo.getView(R.id.profileUserScoreLeaderboardButton);
        solo.clickOnView(view);
        solo.sleep(1000);
        Assert.assertTrue(solo.waitForActivity(leaderboardActivity.class));
        view = solo.getView(R.id.leaderboardBackButton);
        solo.clickOnView(view);
        solo.sleep(1000);
        Assert.assertTrue(solo.waitForActivity(userProfileViewerActivity.class));

        view = solo.getView(R.id.profileUserScanLeaderboardButton);
        solo.clickOnView(view);
        solo.sleep(1000);
        Assert.assertTrue(solo.waitForActivity(leaderboardActivity.class));
        view = solo.getView(R.id.leaderboardBackButton);
        solo.clickOnView(view);
        solo.sleep(1000);
        Assert.assertTrue(solo.waitForActivity(userProfileViewerActivity.class));

        view = solo.getView(R.id.profileUserQRScoreLeaderboardButton);
        solo.clickOnView(view);
        solo.sleep(1000);
        Assert.assertTrue(solo.waitForActivity(leaderboardActivity.class));
        view = solo.getView(R.id.leaderboardBackButton);
        solo.clickOnView(view);
        solo.sleep(1000);
        Assert.assertTrue(solo.waitForActivity(userProfileViewerActivity.class));

        //view = solo.getView(R.id.userProfileQRHistoryButton);
        //solo.clickOnView(view);
        //solo.sleep(1000);
        //Assert.assertTrue(solo.waitForActivity(QRHistory.class));

        view = solo.getView(R.id.qrViewerBackButton);
        solo.clickOnView(view);
        solo.sleep(1000);
        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);
    }

    @After
    public void tearDown() throws Exception{
        solo.finishOpenedActivities();
    }
}
