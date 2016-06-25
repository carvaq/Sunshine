package cvv.udacity.sunshine;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;

/**
 * Created by Caro Vaquero
 * Date: 24.06.2016
 * Project: Sunshine
 */
public class CustomView extends View {

    private static final String TAG = CustomView.class.getSimpleName();
    private Paint mTextPaint;
    private Paint mTransparentPaint;
    private Paint mCirclePaint;

    private float mWindDirection = 0f;
    private String mWindSpeed = "0";

    public CustomView(Context context) {
        super(context);
        init();
    }

    public CustomView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        AccessibilityManager accessibilityManager =
                (AccessibilityManager) getContext().getSystemService(
                        Context.ACCESSIBILITY_SERVICE);
        if (accessibilityManager.isEnabled()) {
            sendAccessibilityEvent(
                    AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED);
        }

        mTextPaint = new Paint();
        mTransparentPaint = new Paint();
        mCirclePaint = new Paint();

        mTextPaint.setColor(Color.BLACK);
        mTextPaint.setTextSize(26);
        mTransparentPaint.setColor(Color.WHITE);
        mCirclePaint.setColor(getResources().getColor(R.color.sunshine_blue));
    }



    public void setWindDirection(float windDirection) {
        mWindDirection = windDirection;
        Log.d(TAG, "setWindDirection: " + windDirection);
    }

    public void setWindSpeed(float windSpeed) {
        mWindSpeed = String.valueOf(windSpeed);
        Log.d(TAG, "setWindSpeed: mWindSpeed" + mWindSpeed);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int desiredWidth = getResources().getDimensionPixelSize(R.dimen.desired_view_size);
        int desiredHeight = getResources().getDimensionPixelSize(R.dimen.desired_view_size);

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;

        //Measure Width
        if (widthMode == MeasureSpec.EXACTLY) {
            //Must be this size
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            //Can't be bigger than...
            width = Math.min(desiredWidth, widthSize);
        } else {
            //Be whatever you want
            width = desiredWidth;
        }

        //Measure Height
        if (heightMode == MeasureSpec.EXACTLY) {
            //Must be this size
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            //Can't be bigger than...
            height = Math.min(desiredHeight, heightSize);
        } else {
            //Be whatever you want
            height = desiredHeight;
        }

        if (height != width) {
            int size = Math.min(height, width);
            width = size;
            height = size;
        }

        //MUST CALL THIS
        setMeasuredDimension(width, height);
        Log.d(TAG, "onMeasure: height" + height);
        Log.d(TAG, "onMeasure: width" + width);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int size = 60;
        float x = getX() + size;
        float y = getY() + size;
        int bigRadius = 40;
        int tinyRadius = 20;

        float tinyX = (float) (Math.sin(mWindDirection) * size) + x;
        float tinyY = (float) (Math.cos(mWindDirection) * size) + y;

        Log.d(TAG, "onDraw: x " + x + "\n" +
                "y " + y + "\n" +
                "size " + size + "\n" +
                "bigRadius " + bigRadius + "\n" +
                "tinyRadius " + tinyRadius + "\n" +
                "tinyX " + tinyX + "\n" +
                "tinyY " + tinyY);

        canvas.drawCircle(x + size, y + size, bigRadius, mCirclePaint);
        canvas.drawCircle(tinyX, tinyY, size, mTransparentPaint);
        canvas.drawCircle(tinyX, tinyY, tinyRadius, mCirclePaint);
        canvas.drawText(mWindSpeed, tinyX - 5, tinyY, mTextPaint);
    }

    @Override
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        super.dispatchPopulateAccessibilityEvent(event);
        event.getText().add(mWindSpeed);
        return true;
    }
}