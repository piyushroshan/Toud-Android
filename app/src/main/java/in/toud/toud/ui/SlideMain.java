package in.toud.toud.ui;

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

import butterknife.ButterKnife;
import butterknife.OnClick;
import in.toud.toud.R;
import in.toud.toud.ui.PageIndicator;

public class SlideMain extends Activity {
    ViewFlipper flipper;
    private GestureDetector mGestureDetector;
    private PageIndicator pageIndicator;

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
        mGestureDetector = new GestureDetector(this, customGestureDetector);
        pageIndicator = (PageIndicator) findViewById(R.id.pageIndicator);
        pageIndicator.setTotalNoOfDots(flipper.getChildCount());
        pageIndicator.resizeCircle(0.5f);
        pageIndicator.setActiveDot(0);
        pageIndicator.setDotSpacing(5);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.registration_button)
    void registerButton(Button button) {
        button.setText("Lol");
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_MIN_DISTANCE = 30;
        private static final int SWIPE_THRESHOLD_VELOCITY = 5;
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            try {
                if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {

                    flipper.setOutAnimation(AnimationUtils.loadAnimation(
                            getBaseContext(), R.anim.push_out_left));
                    flipper.setInAnimation(AnimationUtils.loadAnimation(
                            getBaseContext(), R.anim.pull_in_right));
                    if (pageIndicator.getActiveDot() < pageIndicator.getTotalNoOfDots() - 1) {
                        pageIndicator.setActiveDot(pageIndicator.getActiveDot() + 1);
                        flipper.showNext();
                    }
                    return true; // Right to left
                } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {

                    flipper.setOutAnimation(AnimationUtils.loadAnimation(
                            getBaseContext(), R.anim.push_out_right));
                    flipper.setInAnimation(AnimationUtils.loadAnimation(
                            getBaseContext(), R.anim.pull_in_left));
                    if (pageIndicator.getActiveDot() > 0) {
                        pageIndicator.setActiveDot(pageIndicator.getActiveDot() - 1);
                        flipper.showPrevious();
                    }
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