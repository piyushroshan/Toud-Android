package in.toud.toud;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ViewFlipper;
import com.trncic.library.DottedProgressBar;

import in.toud.toud.ui.PageIndicator;

public class SlideMain extends Activity {
    ViewFlipper flipper;
    private GestureDetector mGestureDetector;
    private PageIndicator pageIndicator;
    private Animation inFromRightAnimation() {

        Animation inFromRight = new TranslateAnimation(
                Animation.RELATIVE_TO_PARENT,  +1.0f, Animation.RELATIVE_TO_PARENT,  0.0f,
                Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,   0.0f
        );
        inFromRight.setDuration(500);
        inFromRight.setInterpolator(new AccelerateInterpolator());
        return inFromRight;
    }

    private Animation outToLeftAnimation() {
        Animation outtoLeft = new TranslateAnimation(
                Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,  -1.0f,
                Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,   0.0f
        );
        outtoLeft.setDuration(500);
        outtoLeft.setInterpolator(new AccelerateInterpolator());
        return outtoLeft;
    }

    private Animation inFromLeftAnimation() {
        Animation inFromLeft = new TranslateAnimation(
                Animation.RELATIVE_TO_PARENT,  -1.0f, Animation.RELATIVE_TO_PARENT,  0.0f,
                Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,   0.0f
        );
        inFromLeft.setDuration(500);
        inFromLeft.setInterpolator(new AccelerateInterpolator());
        return inFromLeft;
    }
    private Animation outToRightAnimation() {
        Animation outtoRight = new TranslateAnimation(
                Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,  +1.0f,
                Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,   0.0f
        );
        outtoRight.setDuration(500);
        outtoRight.setInterpolator(new AccelerateInterpolator());
        return outtoR/ight;
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        return mGestureDetector.onTouchEvent(event);
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        GestureListener customGestureDetector = new GestureListener();
        flipper = (ViewFlipper) findViewById(R.id.flipper);
        /*pageIndicator = (PageIndicator)findViewById(R.id.pageIndicator);
        pageIndicator.setTotalNoOfDots(flipper.getChildCount());
        pageIndicator.setActiveDot(1);
        pageIndicator.setDotSpacing(10);*/

    }


    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_MIN_DISTANCE = 50;
        private static final int SWIPE_THRESHOLD_VELOCITY = 20;
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            try {
                if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {

                    flipper.setOutAnimation(AnimationUtils.loadAnimation(
                            getBaseContext(), R.anim.push_out_left));
                    flipper.setInAnimation(AnimationUtils.loadAnimation(
                            getBaseContext(), R.anim.pull_in_right));
                    flipper.showNext();
                    //pageIndicator.setActiveDot(pageIndicator.getActiveDot()+1);
                    return true; // Right to left
                } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {

                    flipper.setOutAnimation(AnimationUtils.loadAnimation(
                            getBaseContext(), R.anim.push_out_right));
                    flipper.setInAnimation(AnimationUtils.loadAnimation(
                            getBaseContext(), R.anim.pull_in_left));
                    flipper.showPrevious();
                    //pageIndicator.setActiveDot(pageIndicator.getActiveDot()-1);
                    return true; // Left to right
                }

                if (e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                    return true; // Bottom to top
                } else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                    return true; // Top to bottom
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }
    }


}