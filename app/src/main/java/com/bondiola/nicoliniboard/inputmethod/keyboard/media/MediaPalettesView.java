package com.bondiola.nicoliniboard.inputmethod.keyboard.media;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TextView;

import com.bondiola.nicoliniboard.inputmethod.keyboard.KeyboardSwitcher;
import com.bondiola.nicoliniboard.inputmethod.keyboard.internal.KeyDrawParams;
import com.bondiola.nicoliniboard.inputmethod.keyboard.internal.KeyVisualAttributes;
import com.bondiola.nicoliniboard.inputmethod.keyboard.internal.KeyboardIconsSet;
import com.bondiola.nicoliniboard.inputmethod.latin.R;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.bondiola.nicoliniboard.inputmethod.keyboard.Key;
import com.bondiola.nicoliniboard.inputmethod.keyboard.emoji.OnKeyEventListener;
import com.bondiola.nicoliniboard.inputmethod.latin.common.Constants;

@SuppressWarnings("deprecation")
public final class MediaPalettesView extends LinearLayout
        implements OnTabChangeListener, View.OnClickListener, View.OnTouchListener,OnKeyEventListener
{

    private final int mFunctionalKeyBackgroundId;
    private final MediaLayoutParams mMediaLayoutParams;
    private final Context context;
    private KeyboardSwitcher mSwitcher;
    private ImageButton mSearchButton;

    public MediaPalettesView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.emojiPalettesViewStyle);
    }

    public MediaPalettesView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        final Resources res = context.getResources();
        mMediaLayoutParams = new MediaLayoutParams(res);
        @SuppressLint("CustomViewStyleable")
        final TypedArray keyboardViewAttr = context.obtainStyledAttributes(attrs,
                R.styleable.KeyboardView, defStyleAttr, R.style.KeyboardView);
        final int keyBackgroundId = keyboardViewAttr.getResourceId(
                R.styleable.KeyboardView_keyBackground, 0);
        mFunctionalKeyBackgroundId = keyboardViewAttr.getResourceId(
                R.styleable.KeyboardView_functionalKeyBackground, keyBackgroundId);
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        return false;
    }

    @Override
    public void onTabChanged(String s) {

    }

    @Override
    public void onPressKey(Key key) {

    }

    @Override
    public void onReleaseKey(Key key) {

    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mSearchButton = findViewById(R.id.media_keyboard_alphabet_left);
        mSearchButton.setBackgroundResource(mFunctionalKeyBackgroundId);
        mSearchButton.setTag(Constants.CODE_MEDIA);

        //mSearchButton.setOnTouchListener((view, motionEvent) -> {
        //    mSwitcher.setMediaKeyboardWithKeyboard();
        //    return true;
        //});

        mSearchButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mSwitcher.setMediaKeyboardWithKeyboard();
            }
        });
    }

    public void setHardwareAcceleratedDrawingEnabled(final boolean enabled) {
        if (!enabled) return;
        // TODO: Should use LAYER_TYPE_SOFTWARE when hardware acceleration is off?
        setLayerType(LAYER_TYPE_HARDWARE, null);
    }

    private static void setupAlphabetKey(final TextView alphabetKey, final String label,
                                         final KeyDrawParams params) {
        alphabetKey.setText(label);
        alphabetKey.setTextColor(params.mFunctionalTextColor);
        alphabetKey.setTextSize(TypedValue.COMPLEX_UNIT_PX, params.mLabelSize);
        alphabetKey.setTypeface(params.mTypeface);
    }

    public void startMediaPalettes(
            final KeyboardSwitcher switcher,
            final String switchToAlphaLabel,
            final KeyVisualAttributes keyVisualAttr,
            final KeyboardIconsSet iconSet) {
        mSwitcher = switcher;
        final KeyDrawParams params = new KeyDrawParams();
        params.updateParams(mMediaLayoutParams.getActionBarHeight(), keyVisualAttr);
        //setupAlphabetKey(mAlphabetKeyLeft, switchToAlphaLabel, params);
    }
}