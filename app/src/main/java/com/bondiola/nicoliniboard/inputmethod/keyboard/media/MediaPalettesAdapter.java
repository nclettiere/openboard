/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bondiola.nicoliniboard.inputmethod.keyboard.media;

import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bondiola.nicoliniboard.inputmethod.keyboard.Key;
import com.bondiola.nicoliniboard.inputmethod.keyboard.Keyboard;
import com.bondiola.nicoliniboard.inputmethod.keyboard.KeyboardView;
import com.bondiola.nicoliniboard.inputmethod.keyboard.emoji.OnKeyEventListener;
import com.bondiola.nicoliniboard.inputmethod.latin.R;

final class MediaPalettesAdapter extends RecyclerView.Adapter<MediaPalettesAdapter.ViewHolder>{
    private static final String TAG = MediaPalettesAdapter.class.getSimpleName();
    private static final boolean DEBUG_PAGER = false;

    private final OnKeyEventListener mListener;
    private int mActivePosition = 0;

    public MediaPalettesAdapter(final OnKeyEventListener listener) {
        mListener = listener;
    }

    public void flushPendingRecentKeys() {
    }

    public void addRecentKey(final Key key) {

    }

    public void onPageScrolled() {
        releaseCurrentKey(false /* withKeyRegistering */);
    }

    public void releaseCurrentKey(final boolean withKeyRegistering) {

    }

/*
    @Override
    public Object instantiateItem(final ViewGroup container, final int position) {
        if (DEBUG_PAGER) {
            Log.d(TAG, "instantiate item: " + position);
        }
        final EmojiPageKeyboardView oldKeyboardView = mActiveKeyboardViews.get(position);
        if (oldKeyboardView != null) {
            oldKeyboardView.deallocateMemory();
            // This may be redundant but wanted to be safer..
            mActiveKeyboardViews.remove(position);
        }
        final Keyboard keyboard =
                mEmojiCategory.getKeyboardFromPagePosition(position);
        final LayoutInflater inflater = LayoutInflater.from(container.getContext());
        final EmojiPageKeyboardView keyboardView = (EmojiPageKeyboardView) inflater.inflate(
                R.layout.emoji_keyboard_page, container, false);
        keyboardView.setKeyboard(keyboard);
        keyboardView.setOnKeyEventListener(mListener);
        container.addView(keyboardView);
        mActiveKeyboardViews.put(position, keyboardView);
        return keyboardView;
    }

    @Override
    public void setPrimaryItem(final ViewGroup container, final int position,
            final Object object) {
        if (mActivePosition == position) {
            return;
        }
        final EmojiPageKeyboardView oldKeyboardView = mActiveKeyboardViews.get(mActivePosition);
        if (oldKeyboardView != null) {
            oldKeyboardView.releaseCurrentKey(false);
            oldKeyboardView.deallocateMemory();
        }
        mActivePosition = position;
    }

    @Override
    public boolean isViewFromObject(final View view, final Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(final ViewGroup container, final int position,
            final Object object) {
        if (DEBUG_PAGER) {
            Log.d(TAG, "destroy item: " + position + ", " + object.getClass().getSimpleName());
        }
        final EmojiPageKeyboardView keyboardView = mActiveKeyboardViews.get(position);
        if (keyboardView != null) {
            keyboardView.deallocateMemory();
            mActiveKeyboardViews.remove(position);
        }
        if (object instanceof View) {
            container.removeView((View)object);
        } else {
            Log.w(TAG, "Warning!!! Emoji palette may be leaking. " + object);
        }
    }
*/

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        /*if (DEBUG_PAGER) {
            Log.d(TAG, "instantiate item: " + viewType);
        }
        final EmojiPageKeyboardView oldKeyboardView = mActiveKeyboardViews.get(viewType);
        if (oldKeyboardView != null) {
            oldKeyboardView.deallocateMemory();
            // This may be redundant but wanted to be safer..
            mActiveKeyboardViews.remove(viewType);
        }
        final Keyboard keyboard =
                mEmojiCategory.getKeyboardFromPagePosition(parent.getVerticalScrollbarPosition());*/
        //final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        //final EmojiPageKeyboardView keyboardView = (EmojiPageKeyboardView)inflater.inflate(
        //        R.layout.emoji_keyboard_page, parent, false);
        /*keyboardView.setKeyboard(keyboard);
        keyboardView.setOnKeyEventListener(mListener);
        parent.addView(keyboardView);
        mActiveKeyboardViews.put(parent.getVerticalScrollbarPosition(), keyboardView);*/
        //return new ViewHolder(keyboardView);

        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull MediaPalettesAdapter.ViewHolder holder, int position) {
        if (DEBUG_PAGER) {
            Log.d(TAG, "instantiate item: " + position);
        }
        //final EmojiPageKeyboardView oldKeyboardView = mActiveKeyboardViews.get(position);
        //if (oldKeyboardView != null) {
        //    oldKeyboardView.deallocateMemory();
        //    // This may be redundant but wanted to be safer..
        //    mActiveKeyboardViews.remove(position);
        //}
        //final Keyboard keyboard =
        //        mEmojiCategory.getKeyboardFromAdapterPosition(position);
        //holder.getKeyboardView().setKeyboard(keyboard);
        //holder.getKeyboardView().setOnKeyEventListener(mListener);
        //parent.addView(keyboardView);
        //mActiveKeyboardViews.put(position, holder.getKeyboardView());

        /*if (mActivePosition == position) {
            return;
        }
        final EmojiPageKeyboardView oldKeyboardView = mActiveKeyboardViews.get(mActivePosition);
        if (oldKeyboardView != null) {
            oldKeyboardView.releaseCurrentKey(false);
            oldKeyboardView.deallocateMemory();
        }
        mActivePosition = position;*/
    }

    @Override
    public int getItemCount() {
        return 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        //private EmojiPageKeyboardView customView;

        public ViewHolder(View v) {
            super(v);
            //customView = (EmojiPageKeyboardView) v;
        }

       // public EmojiPageKeyboardView getKeyboardView() {
        //    return customView;
        //}

    }
}

