package darrenretinambpcrystalwell.Fragments;

import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.view.SurfaceView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import AndroidCallback.DotsAndroidCallback;
import Constants.DotsConstants;
import Dots.DotsPoint;
import Dots.DotsPowerUp;
import Dots.DotsPowerUpState;
import Dots.DotsPowerUpType;
import Model.Interaction.DotsInteraction;
import Model.Interaction.DotsInteractionStates;
import darrenretinambpcrystalwell.Game.DotsGameTask;
import darrenretinambpcrystalwell.sound.SoundHelper;
import darrenretinambpcrystalwell.dots.DotsAndroidConstants;
import darrenretinambpcrystalwell.dots.DotsScreen;
import darrenretinambpcrystalwell.dots.MainActivity;
import darrenretinambpcrystalwell.dots.R;
import darrenretinambpcrystalwell.dots.SurfaceViewDots;
import darrenretinambpcrystalwell.dots.GifRun;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link GameFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GameFragment extends Fragment {
    private final String TAG = "GameFragment";

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    // variable to keep track of whether elements have already been initialised, so onResume does
    // not double create the views
    private boolean initialized = false;

    private int playerId;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment GameFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static GameFragment newInstance(String param1, String param2) {
        GameFragment fragment = new GameFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public GameFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_game, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        SurfaceView v = (SurfaceView) this.getActivity().findViewById(R.id.surfaceView);
        GifRun gifRun = new GifRun(this.getActivity());
        gifRun.LoadGiff(v, this.getActivity(), R.drawable.star_field_background);


    }


    @Override
    public void onResume() {
        super.onResume();

        if (!this.initialized) {

            this.initialized = true;

            TextView view = (TextView)this.getActivity().findViewById(R.id.latency);


            int playerId = Integer.parseInt(this.mParam1);
            try {
                startServerOrClient(playerId);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();


                // Connection failed, go back to connection fragment
                FragmentTransactionHelper.pushFragment(1, this, new String[2], (MainActivity)getActivity(), false);

                FragmentTransactionHelper.showToast("Connection Failed!", this.getActivity(), DotsAndroidConstants.SCORE_TOAST_LENGTH);

            } catch (java.lang.InstantiationException e) {
                e.printStackTrace();
            }

            view.bringToFront();
        }

    }



    /**
     * Starts a server or client
     * @param inputPlayerId 0 for server, 1 for client, -1 for single player
     */
    private void startServerOrClient(final int inputPlayerId) throws InterruptedException, IOException, java.lang.InstantiationException {


        String serverIp = this.mParam2;

        RelativeLayout rootLayout = (RelativeLayout) this.getActivity().findViewById(R.id.gameFragment);
        final DotsScreen dotsScreen = new DotsScreen(rootLayout, this.getActivity());

        final DotsGameTask dotsGameTask;


        if (inputPlayerId == -1) {
            playerId = 0;
            dotsGameTask = new DotsGameTask(inputPlayerId, DotsConstants.SINGLE_PLAYER_PORT, serverIp);

        } else {
            playerId = inputPlayerId;
            dotsGameTask = new DotsGameTask(inputPlayerId, DotsConstants.CLIENT_PORT, serverIp);
        }

//        Log.d(TAG, "Starting game, playerId: " + playerId + " ip: " + this.mParam2);

        final SurfaceViewDots surfaceViewDots = new SurfaceViewDots(this.getActivity(), rootLayout, dotsGameTask.getDotsServerClientParent(), dotsScreen.getCorrespondingDotCoordinates());

        final SoundHelper soundHelper = new SoundHelper(this.getActivity());

        final Fragment thisFragment = this;

        DotsAndroidCallback androidCallback = new DotsAndroidCallback() {
            @Override
            public void onSocketConnected() {
                removeDeviceIpFromParse();
            }

            @Override
            public void onValidPlayerInteraction(final DotsInteraction dotsInteraction) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        surfaceViewDots.setTouchedPath(dotsInteraction, dotsScreen);
                    }
                });

                playSoundForInteraction(dotsInteraction, soundHelper);

            }

            @Override
            public void onBoardChanged(final ArrayList<DotsPoint> dotsPoints) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dotsScreen.updateScreen(dotsPoints);
                    }
                });
            }



            @Override
            public void onGameOver(int i, int[] ints) {
                final String message = "GAME OVER, WINNER: " + i + " FINAL SCORE: " + Arrays.toString(ints);
                Log.d(TAG, message);


                // kill threads and stop the game
                dotsGameTask.getDotsServerClientParent().stopGame();

//                getActivity().runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        FragmentTransactionHelper.showToast(message, getActivity(), DotsAndroidConstants.SCORE_TOAST_LENGTH);
//
//                    }
//                });

                // initialise the game over fragments with arguments
                GameOverFragment gameOverFragment = new GameOverFragment();
                gameOverFragment.setArguments(playerId, ints);

                // Push the game over fragment out
                FragmentTransactionHelper.pushFragment(gameOverFragment, thisFragment, (MainActivity)getActivity(), true);

            }

            @Override
            public void onScoreUpdated(final int[] ints) {
                Log.d(TAG, "Score: " + Arrays.toString(ints));

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        System.out.println("SHOWING TOAST");
//                        FragmentTransactionHelper.showToast(Arrays.toString(ints), getActivity(), DotsAndroidConstants.SCORE_TOAST_LENGTH);
                        //fixed playerID to update score
                        if (playerId == 0) {
                            dotsScreen.scoreBoard0.setScore(ints[0]);
                            dotsScreen.scoreBoard1.setScore(ints[1]);
                        } else {
                            dotsScreen.scoreBoard0.setScore(ints[1]);
                            dotsScreen.scoreBoard1.setScore(ints[0]);
                        }

                    }
                });

            }

            @Override
            public void latencyChanged(long l) {
                Log.d(TAG, "Latency: " + l);
            }

            @Override
            public void onPowerUpReceived(final DotsPowerUp dotsPowerUp) {
                Log.d("POWERUP", dotsPowerUp.toString());

                soundHelper.playSoundEffectForPowerUp();

//                getActivity().runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        FragmentTransactionHelper.showToast(dotsPowerUp.toString(), getActivity(), DotsAndroidConstants.SCORE_TOAST_LENGTH);
//                    }
//                });

                if (dotsPowerUp.getPowerUpType() == DotsPowerUpType.BOMB) {

                    if (dotsPowerUp.getPowerUpState() == DotsPowerUpState.STARTED) {
                        surfaceViewDots.setConfused(true);

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dotsScreen.getConfused().setVisibility(View.VISIBLE);

                            }
                        });

                    } else {
                        surfaceViewDots.setConfused(false);
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                dotsScreen.getConfused().setVisibility(View.GONE);
                            }
                        });
                    }


                } else if (dotsPowerUp.getPowerUpType()== DotsPowerUpType.FREEZE) {

                    if (dotsPowerUp.getPowerUpState() == DotsPowerUpState.STARTED) {
                        surfaceViewDots.setTouchEnabled(false);
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                dotsScreen.getFreeze().setVisibility(View.VISIBLE);
                            }
                        });

                    } else {
                        surfaceViewDots.setTouchEnabled(true);
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                dotsScreen.getFreeze().setVisibility(View.GONE);
                            }
                        });
                    }

                }



            }
        };

        dotsGameTask.setDotsAndroidCallback(androidCallback);

//        surfaceViewDots.setDotsServerClientParent(this.dotsServerClientParent);
//        surfaceViewDots.setCorrespondingDotCoordinates(dotsScreen.getCorrespondingDotCoordinates());

        Thread thread = new Thread(dotsGameTask);
        thread.start();

    }

    private void playSoundForInteraction(DotsInteraction interaction, SoundHelper soundHelper) {

        Log.d(TAG, interaction.toString());
        DotsInteractionStates state = interaction.getState();
        // only play on touch up, and when stuff is being cleared
        if (state == DotsInteractionStates.TOUCH_UP && interaction.isAnimate()) {

            int soundId = state.ordinal();

            soundHelper.playSoundForInteraction(soundId);

        }

    }

    /**
     * Query for a parse object of the current ip address and deletes it from the cloud
     */
    private void removeDeviceIpFromParse() {
        // Create a query object
        ParseQuery<ParseObject> query = ParseQuery.getQuery(DotsAndroidConstants.PARSE_OBJECT_NAME);

        // Where the ip address is the same
        query.whereEqualTo(DotsAndroidConstants.PARSE_IP_KEY, MainFragment.wifiIpAddress(getActivity()));

        // find the objects
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {

                for (ParseObject parseObject : parseObjects) {
                    parseObject.deleteInBackground();
                }

            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // call this here so we will also delete the ip from parse if the fragment is destroyed
        removeDeviceIpFromParse();

    }
}
