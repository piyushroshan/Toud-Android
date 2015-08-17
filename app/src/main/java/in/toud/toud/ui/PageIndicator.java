package in.toud.toud.ui;

import java.text.AttributedCharacterIterator.Attribute;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import in.toud.toud.R;

public class PageIndicator extends View{
    int totalNoOfDots;
    int activeDot;
    int dotSpacing;
    int horizontalSpace = 5;
    Bitmap activeDotBitmap;
    Bitmap normalDotBitmap;
    int x=0;

    private Paint paint;
    public PageIndicator(Context context) {
        super(context);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        activeDotBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.active_dot);
        normalDotBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.inactive_dot);
    }

    public PageIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        activeDotBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.active_dot);
        normalDotBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.inactive_dot);

    }

    public PageIndicator(Context context, AttributeSet attrs , int defStyle) {
        super(context,attrs,defStyle);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        activeDotBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.active_dot);
        normalDotBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.inactive_dot);

    }

    private Bitmap getResizedBitmap(Bitmap bm, float scale) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float newWidth = bm.getWidth() * scale;
        float newHeight = bm.getHeight() * scale;
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        return resizedBitmap;
    }

    public void resizeCircle(float scale) {
        activeDotBitmap = getResizedBitmap(activeDotBitmap, scale);
        normalDotBitmap = getResizedBitmap(normalDotBitmap, scale);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawDot(canvas);
        super.onDraw(canvas);

    }


    private void drawDot(Canvas canvas){
        for(int i=0;i<totalNoOfDots;i++){
            if(i==activeDot){
                canvas.drawBitmap(activeDotBitmap, x, 0, paint);
            }else{
                canvas.drawBitmap(normalDotBitmap, x, 0, paint);
            }
            x=x+activeDotBitmap.getWidth()+horizontalSpace+dotSpacing;
        }
    }





    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = totalNoOfDots * (activeDotBitmap.getWidth() + horizontalSpace + getDotSpacing());
        width = resolveSize(width, widthMeasureSpec);
        int height = activeDotBitmap.getHeight();
        height = resolveSize(height, heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    public void refersh(){
        x = 0;
        invalidate();
    }


    public int getTotalNoOfDots() {
        return totalNoOfDots;
    }

    public void setTotalNoOfDots(int totalNoOfDots) {
        this.totalNoOfDots = totalNoOfDots;
        x=0;
        invalidate();
    }

    public int getActiveDot() {
        return activeDot;
    }

    public void setActiveDot(int activeDot) {
        this.activeDot = activeDot;
        x=0;
        invalidate();
    }

    public int getDotSpacing() {
        return dotSpacing;
    }

    public void setDotSpacing(int dotSpacing) {
        this.dotSpacing = dotSpacing;
        x=0;
        invalidate();
    }

}
