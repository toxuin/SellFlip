package ru.toxuin.sellflip.library.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageButton;
import ru.toxuin.sellflip.R;

public class CameraImageButton extends ImageButton {
    private static final int[] STATE_MESSAGE_BEEP = {R.attr.state_recording_beep};
    private boolean beep;

    public CameraImageButton(Context context) {
        super(context);
    }

    public CameraImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CameraImageButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public int[] onCreateDrawableState(int extraSpace) {
        if (beep) {
            final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
            mergeDrawableStates(drawableState, STATE_MESSAGE_BEEP);
            return drawableState;
        } else {
            return super.onCreateDrawableState(extraSpace);
        }
    }

    public void beep() {
        this.beep = !beep;
        refreshDrawableState();
    }

    public void beep(boolean boop) {
        this.beep = boop;
    }
}
