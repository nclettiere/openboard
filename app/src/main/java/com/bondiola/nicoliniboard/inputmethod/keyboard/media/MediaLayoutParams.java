package com.bondiola.nicoliniboard.inputmethod.keyboard.media;

import android.content.res.Resources;
import android.view.View;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.RecyclerView;
import com.bondiola.nicoliniboard.inputmethod.latin.R;

import com.bondiola.nicoliniboard.inputmethod.latin.utils.ResourceUtils;

final class MediaLayoutParams {
    private static final int DEFAULT_KEYBOARD_ROWS = 4;

    public final int mEmojiListHeight;
    private final int mEmojiListBottomMargin;
    public final int mEmojiKeyboardHeight;
    private final int mEmojiCategoryPageIdViewHeight;
    public final int mEmojiActionBarHeight;
    public final int mKeyVerticalGap;
    private final int mKeyHorizontalGap;
    private final int mBottomPadding;
    private final int mTopPadding;

    public MediaLayoutParams(final Resources res) {
        final int defaultKeyboardHeight = ResourceUtils.getDefaultKeyboardHeight(res);
        final int defaultKeyboardWidth = ResourceUtils.getDefaultKeyboardWidth(res);
        mKeyVerticalGap = (int) res.getFraction(R.fraction.config_key_vertical_gap_holo,
                defaultKeyboardHeight, defaultKeyboardHeight);
        mBottomPadding = (int) res.getFraction(R.fraction.config_keyboard_bottom_padding_holo,
                defaultKeyboardHeight, defaultKeyboardHeight);
        mTopPadding = (int) res.getFraction(R.fraction.config_keyboard_top_padding_holo,
                defaultKeyboardHeight, defaultKeyboardHeight);
        mKeyHorizontalGap = (int) (res.getFraction(R.fraction.config_key_horizontal_gap_holo,
                defaultKeyboardWidth, defaultKeyboardWidth));
        mEmojiCategoryPageIdViewHeight =
                (int) (res.getDimension(R.dimen.config_emoji_category_page_id_height));
        final int baseheight = defaultKeyboardHeight - mBottomPadding - mTopPadding
                + mKeyVerticalGap;
        mEmojiActionBarHeight = baseheight / DEFAULT_KEYBOARD_ROWS
                - (mKeyVerticalGap - mBottomPadding) / 2;
        mEmojiListHeight = defaultKeyboardHeight - mEmojiActionBarHeight
                - mEmojiCategoryPageIdViewHeight;
        mEmojiListBottomMargin = 0;
        mEmojiKeyboardHeight = mEmojiListHeight - mEmojiListBottomMargin - 1;
    }

    public void setEmojiListProperties(final RecyclerView vp) {
        final LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) vp.getLayoutParams();
        lp.height = mEmojiKeyboardHeight;
        lp.bottomMargin = mEmojiListBottomMargin;
        vp.setLayoutParams(lp);
    }

    public void setCategoryPageIdViewProperties(final View v) {
        final LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) v.getLayoutParams();
        lp.height = mEmojiCategoryPageIdViewHeight;
        v.setLayoutParams(lp);
    }

    public int getActionBarHeight() {
        return mEmojiActionBarHeight - mBottomPadding;
    }

    public void setActionBarProperties(final LinearLayout ll) {
        final LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) ll.getLayoutParams();
        lp.height = getActionBarHeight();
        ll.setLayoutParams(lp);
    }

    public void setKeyProperties(final View v) {
        final LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) v.getLayoutParams();
        lp.leftMargin = mKeyHorizontalGap / 2;
        lp.rightMargin = mKeyHorizontalGap / 2;
        v.setLayoutParams(lp);
    }
}