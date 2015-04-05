package ru.toxuin.sellflip.library.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

public class PrescalableImageView extends ImageView {
    private float ratio = 1;

    public PrescalableImageView(Context context) {
        super(context);
    }

    public PrescalableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PrescalableImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (ratio != 0 && ratio != 1) {
            setMeasuredDimension(getMeasuredWidth(), (int) (getMeasuredWidth() / ratio));
        }
    }

    public void setRatio(float ratio) {
        this.ratio = ratio;
    }
}
