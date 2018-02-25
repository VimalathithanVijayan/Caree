package darrenretinambpcrystalwell.dots;


import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.os.Handler;

import com.parse.Parse;

import AndroidCallback.DotsAndroidCallback;
import Sockets.DotsServerClientParent;
import darrenretinambpcrystalwell.Fragments.GameFragment;
import darrenretinambpcrystalwell.Fragments.MainFragment;
import darrenretinambpcrystalwell.Fragments.RulesFragment;
import darrenretinambpcrystalwell.sound.SoundHelper;
import darrenretinambpcrystalwell.sound.SoundService;


public class MainActivity extends ActionBarActivity {
    private final String TAG = "Main Activity";

    private DotsScreen      dotsScreen;
    private DotView         dotView;
    private SurfaceViewDots surfaceViewDots;
//
    private DotsServerClientParent dotsServerClientParent;
    private DotsAndroidCallback androidCallback;

    private Fragment[] fragments;
    private static final Handler  GARBAGE_COLLECT_HANDLER = new Handler();
    private static final Runnable GARBAGE_COLLECT_RUNNABLE =
            new Runnable() { @Override public void run() { System.gc(); } };




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        postGarbageCollect(0);

        // Initialize parse
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "jtDIpaAPL4ZT2CkLcBnP4QznoFVrH4ZTk6tCtEhk", "dbQ63Aur5FqeMQ2offnDXUPFsz5gxpw9vUlIr2jE");


        // sets up views
        setContentView(R.layout.activity_main);
        setUpFragment(savedInstanceState);


    }

    @Override
    protected void onResume() {
        super.onResume();
        // play background music
        Intent objIntent = new Intent(this, SoundService.class);
        startService(objIntent);

    }

    @Override
    protected void onPause() {
        super.onPause();
        Intent objIntent = new Intent(this, SoundService.class);
        stopService(objIntent);
    }

    public static void postGarbageCollect(long delay) {
        GARBAGE_COLLECT_HANDLER.removeCallbacks(GARBAGE_COLLECT_RUNNABLE);
        if (delay == 0) {
            System.gc();
        } else {
            GARBAGE_COLLECT_HANDLER.postDelayed(GARBAGE_COLLECT_RUNNABLE,delay);
        }

    }


    private void setUpFragment(Bundle savedInstanceState) {
        // Check that the activity is using the layout version with
        // the fragment_container FrameLayout
        if (findViewById(R.id.root_layout) != null) {
            postGarbageCollect(2000);

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }

            // Create a new Fragment to be placed in the activity layout
            Fragment firstFragment = this.getFragment(0);

            // In case this activity was started with special instructions from an
            // Intent, pass the Intent's extras to the fragment as arguments
            firstFragment.setArguments(getIntent().getExtras());

            // Add the fragment to the 'fragment_container' FrameLayout
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.root_layout, firstFragment).commit();
        }
    }

    /**
     * Get fragment with no arguments
     * @param i
     * @return
     */
    public Fragment getFragment(int i) {


        return this.getFragment(i, new String[2]);
    }

    public Fragment getFragment(int i, String args[]) {

        /**
         * 0 Main fragment
         * 1 Connection fragment
         * 2 Game fragment
         *
         */

        if (this.fragments == null) {
            this.fragments = new Fragment[DotsAndroidConstants.NO_OF_FRAGMENTS];
        }

        // This if else is to check if the fragment has already been created and start it again
        // Somehow we cant reuse fragments that have already been created
//        if (this.fragments[i] == null) {

            Fragment fragmentToCreate;
            if (i == 0) {
                postGarbageCollect(0);
                fragmentToCreate = MainFragment.newInstance(args[0], args[1]);
            } else if (i == 1) {
                postGarbageCollect(0);
                fragmentToCreate = GameFragment.newInstance(args[0], args[1]);
//                fragmentToCreate = ConnectionFragment.newInstance(args[0], args[1]);
            } else if (i == 2) {
                postGarbageCollect(0);
                fragmentToCreate = RulesFragment.newInstance(args[0], args[1]);
            } else {
                postGarbageCollect(0);
                Log.e(TAG, "Unknown Fragment id");
                return null;
            }


            this.fragments[i] = fragmentToCreate;

        return this.fragments[i];

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void updateScreenButton(View view) throws InterruptedException {
//        Effects.castFadeOutEffect(dotsScreen.getDotList()[24], 500, false, false);
//        Effects.castFadeOutEffect(dotsScreen.getDotList()[25], 500, false, false);
    }
}
