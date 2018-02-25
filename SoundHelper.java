package darrenretinambpcrystalwell.sound;

import android.content.Context;
import android.media.MediaPlayer;

import darrenretinambpcrystalwell.dots.R;

/**
 * Created by JiaHao on 8/4/15.
 */
public class SoundHelper {

    private final Context context;

    public SoundHelper(Context context) {
        this.context = context;
    }

    public void playSoundEffectForPowerUp() {
        playSound(R.raw.power_up_received);
    }

    public void playSoundForInteraction(int interaction) {

        int soundId;

        switch (interaction) {
            case 0:
                soundId = R.raw.touch_down;
                break;
            case 1:
                soundId = R.raw.touch_moved;
                break;
            case 2:
                soundId = R.raw.touch_up;
                break;
            default:
                soundId = -1;
                break;

        }

        if (soundId != -1) {

            playSound(soundId);

        }
    }

    private void playSound(int soundId) {
        MediaPlayer player = MediaPlayer.create(this.context, soundId);
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.release();
            }
        });

        player.setVolume(1.0f, 1.0f);
        player.start();
    }

}
