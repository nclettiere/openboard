/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.bondiola.nicoliniboard.inputmethod.keyboard;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;

import com.bondiola.nicoliniboard.inputmethod.keyboard.emoji.EmojiPalettesView;
import com.bondiola.nicoliniboard.inputmethod.keyboard.media.CompactMediaView;
import com.bondiola.nicoliniboard.inputmethod.keyboard.media.MediaPalettesView;
import com.bondiola.nicoliniboard.inputmethod.keyboard.internal.KeyboardState;
import com.bondiola.nicoliniboard.inputmethod.keyboard.internal.KeyboardTextsSet;

import com.bondiola.nicoliniboard.inputmethod.event.Event;

import com.bondiola.nicoliniboard.inputmethod.latin.InputView;
import com.bondiola.nicoliniboard.inputmethod.latin.LatinIME;
import com.bondiola.nicoliniboard.inputmethod.latin.R;
import com.bondiola.nicoliniboard.inputmethod.latin.RichInputMethodManager;
import com.bondiola.nicoliniboard.inputmethod.latin.WordComposer;
import com.bondiola.nicoliniboard.inputmethod.latin.define.ProductionFlags;
import com.bondiola.nicoliniboard.inputmethod.latin.settings.Settings;
import com.bondiola.nicoliniboard.inputmethod.latin.settings.SettingsValues;
import com.bondiola.nicoliniboard.inputmethod.latin.utils.CapsModeUtils;
import com.bondiola.nicoliniboard.inputmethod.latin.utils.LanguageOnSpacebarUtils;
import com.bondiola.nicoliniboard.inputmethod.latin.utils.RecapitalizeStatus;
import com.bondiola.nicoliniboard.inputmethod.latin.utils.ResourceUtils;
import com.bondiola.nicoliniboard.inputmethod.latin.utils.ScriptUtils;

import javax.annotation.Nonnull;

public final class KeyboardSwitcher implements KeyboardState.SwitchActions {
    private static final String TAG = KeyboardSwitcher.class.getSimpleName();

    public Context context;
    private InputView mCurrentInputView;
    private View mMainKeyboardFrame;
    private CompactMediaView mCompactMediaView;
    private View mSuggestionsStripView;
    private MainKeyboardView mKeyboardView;
    private EmojiPalettesView mEmojiPalettesView;
    private MediaPalettesView mMediaPalettesView;
    private LatinIME mLatinIME;
    private RichInputMethodManager mRichImm;
    private boolean mIsHardwareAcceleratedDrawingEnabled;

    private KeyboardState mState;

    private KeyboardLayoutSet mKeyboardLayoutSet;
    // TODO: The following {@link KeyboardTextsSet} should be in {@link KeyboardLayoutSet}.
    private final KeyboardTextsSet mKeyboardTextsSet = new KeyboardTextsSet();

    private KeyboardTheme mKeyboardTheme;
    private Context mThemeContext;
    private int mCurrentUiMode;

    private static final KeyboardSwitcher sInstance = new KeyboardSwitcher();

    public static KeyboardSwitcher getInstance() {
        return sInstance;
    }

    private KeyboardSwitcher() {
        // Intentional empty constructor for singleton.
    }

    public static void init(final LatinIME latinIme) {
        sInstance.initInternal(latinIme);
    }

    private void initInternal(final LatinIME latinIme) {
        mLatinIME = latinIme;
        mRichImm = RichInputMethodManager.getInstance();
        mState = new KeyboardState(this);
        mIsHardwareAcceleratedDrawingEnabled = mLatinIME.enableHardwareAcceleration();
    }

    public void updateKeyboardTheme() {
        final boolean themeUpdated = updateKeyboardThemeAndContextThemeWrapper(
                mLatinIME, KeyboardTheme.getKeyboardTheme(mLatinIME /* context */));
        if (themeUpdated && mKeyboardView != null) {
            mLatinIME.setInputView(onCreateInputView(mIsHardwareAcceleratedDrawingEnabled));
        }
    }

    private boolean updateKeyboardThemeAndContextThemeWrapper(final Context context,
            final KeyboardTheme keyboardTheme) {
        final boolean nightModeChanged = (mCurrentUiMode & Configuration.UI_MODE_NIGHT_MASK)
                != (context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK);
        if (mThemeContext == null || !keyboardTheme.equals(mKeyboardTheme) || nightModeChanged) {
            mKeyboardTheme = keyboardTheme;
            mThemeContext = new ContextThemeWrapper(context, keyboardTheme.mStyleId);
            mCurrentUiMode = context.getResources().getConfiguration().uiMode;
            KeyboardLayoutSet.onKeyboardThemeChanged();
            return true;
        }
        return false;
    }

    public void loadKeyboard(final EditorInfo editorInfo, final SettingsValues settingsValues,
            final int currentAutoCapsState, final int currentRecapitalizeState) {
        final KeyboardLayoutSet.Builder builder = new KeyboardLayoutSet.Builder(
                mThemeContext, editorInfo);
        final Resources res = mThemeContext.getResources();
        final int keyboardWidth = ResourceUtils.getDefaultKeyboardWidth(res);
        final int keyboardHeight = ResourceUtils.getKeyboardHeight(res, settingsValues);
        builder.setKeyboardGeometry(keyboardWidth, keyboardHeight);
        builder.setSubtype(mRichImm.getCurrentSubtype());
        builder.setVoiceInputKeyEnabled(settingsValues.mShowsVoiceInputKey);
        builder.setNumberRowEnabled(settingsValues.mShowsNumberRow);
        builder.setLanguageSwitchKeyEnabled(mLatinIME.shouldShowLanguageSwitchKey());
        builder.setEmojiKeyEnabled(settingsValues.mShowsEmojiKey);
        builder.setSplitLayoutEnabledByUser(ProductionFlags.IS_SPLIT_KEYBOARD_SUPPORTED
                && settingsValues.mIsSplitKeyboardEnabled);
        mKeyboardLayoutSet = builder.build();
        try {
            mState.onLoadKeyboard(currentAutoCapsState, currentRecapitalizeState);
            mKeyboardTextsSet.setLocale(mRichImm.getCurrentSubtypeLocale(), mThemeContext);
        } catch (KeyboardLayoutSet.KeyboardLayoutSetException e) {
            Log.w(TAG, "loading keyboard failed: " + e.mKeyboardId, e.getCause());
        }
    }

    public void saveKeyboardState() {
        if (getKeyboard() != null || isShowingEmojiPalettes()) {
            mState.onSaveKeyboardState();
        }
    }

    public void onHideWindow() {
        if (mKeyboardView != null) {
            mKeyboardView.onHideWindow();
        }
    }

    private void setKeyboard(
            @Nonnull final int keyboardId,
            @Nonnull final KeyboardSwitchState toggleState) {
        // Make {@link MainKeyboardView} visible and hide {@link EmojiPalettesView}.
        final SettingsValues currentSettingsValues = Settings.getInstance().getCurrent();
        setMainKeyboardFrame(currentSettingsValues, toggleState);
        // TODO: pass this object to setKeyboard instead of getting the current values.
        final MainKeyboardView keyboardView = mKeyboardView;
        final Keyboard oldKeyboard = keyboardView.getKeyboard();
        final Keyboard newKeyboard = mKeyboardLayoutSet.getKeyboard(keyboardId);
        keyboardView.setKeyboard(newKeyboard);
        mCurrentInputView.setKeyboardTopPadding(newKeyboard.mTopPadding);
        keyboardView.setKeyPreviewPopupEnabled(currentSettingsValues.mKeyPreviewPopupOn);
        keyboardView.setKeyPreviewAnimationParams(
                currentSettingsValues.mHasCustomKeyPreviewAnimationParams,
                currentSettingsValues.mKeyPreviewShowUpStartXScale,
                currentSettingsValues.mKeyPreviewShowUpStartYScale,
                currentSettingsValues.mKeyPreviewShowUpDuration,
                currentSettingsValues.mKeyPreviewDismissEndXScale,
                currentSettingsValues.mKeyPreviewDismissEndYScale,
                currentSettingsValues.mKeyPreviewDismissDuration);
        keyboardView.updateShortcutKey(mRichImm.isShortcutImeReady());
        final boolean subtypeChanged = (oldKeyboard == null)
                || !newKeyboard.mId.mSubtype.equals(oldKeyboard.mId.mSubtype);
        final int languageOnSpacebarFormatType = LanguageOnSpacebarUtils
                .getLanguageOnSpacebarFormatType(newKeyboard.mId.mSubtype);
        final boolean hasMultipleEnabledIMEsOrSubtypes = mRichImm
                .hasMultipleEnabledIMEsOrSubtypes(true /* shouldIncludeAuxiliarySubtypes */);
        keyboardView.startDisplayLanguageOnSpacebar(subtypeChanged, languageOnSpacebarFormatType,
                hasMultipleEnabledIMEsOrSubtypes);
    }

    public Keyboard getKeyboard() {
        if (mKeyboardView != null) {
            return mKeyboardView.getKeyboard();
        }
        return null;
    }

    // TODO: Remove this method. Come up with a more comprehensive way to reset the keyboard layout
    // when a keyboard layout set doesn't get reloaded in LatinIME.onStartInputViewInternal().
    public void resetKeyboardStateToAlphabet(final int currentAutoCapsState,
            final int currentRecapitalizeState) {
        mState.onResetKeyboardStateToAlphabet(currentAutoCapsState, currentRecapitalizeState);
    }

    public void onPressKey(final int code, final boolean isSinglePointer,
            final int currentAutoCapsState, final int currentRecapitalizeState) {
        mState.onPressKey(code, isSinglePointer, currentAutoCapsState, currentRecapitalizeState);
    }

    public void onReleaseKey(final int code, final boolean withSliding,
            final int currentAutoCapsState, final int currentRecapitalizeState) {
        mState.onReleaseKey(code, withSliding, currentAutoCapsState, currentRecapitalizeState);
    }

    public void onFinishSlidingInput(final int currentAutoCapsState,
            final int currentRecapitalizeState) {
        mState.onFinishSlidingInput(currentAutoCapsState, currentRecapitalizeState);
    }

    // Implements {@link KeyboardState.SwitchActions}.
    @Override
    public void setAlphabetKeyboard() {
        if (DEBUG_ACTION) {
            Log.d(TAG, "setAlphabetKeyboard");
        }
        setKeyboard(KeyboardId.ELEMENT_ALPHABET, KeyboardSwitchState.OTHER);
    }

    // Implements {@link KeyboardState.SwitchActions}.
    @Override
    public void setAlphabetManualShiftedKeyboard() {
        if (DEBUG_ACTION) {
            Log.d(TAG, "setAlphabetManualShiftedKeyboard");
        }
        setKeyboard(KeyboardId.ELEMENT_ALPHABET_MANUAL_SHIFTED, KeyboardSwitchState.OTHER);
    }

    // Implements {@link KeyboardState.SwitchActions}.
    @Override
    public void setAlphabetAutomaticShiftedKeyboard() {
        if (DEBUG_ACTION) {
            Log.d(TAG, "setAlphabetAutomaticShiftedKeyboard");
        }
        setKeyboard(KeyboardId.ELEMENT_ALPHABET_AUTOMATIC_SHIFTED, KeyboardSwitchState.OTHER);
    }

    // Implements {@link KeyboardState.SwitchActions}.
    @Override
    public void setAlphabetShiftLockedKeyboard() {
        if (DEBUG_ACTION) {
            Log.d(TAG, "setAlphabetShiftLockedKeyboard");
        }
        setKeyboard(KeyboardId.ELEMENT_ALPHABET_SHIFT_LOCKED, KeyboardSwitchState.OTHER);
    }

    // Implements {@link KeyboardState.SwitchActions}.
    @Override
    public void setAlphabetShiftLockShiftedKeyboard() {
        if (DEBUG_ACTION) {
            Log.d(TAG, "setAlphabetShiftLockShiftedKeyboard");
        }
        setKeyboard(KeyboardId.ELEMENT_ALPHABET_SHIFT_LOCK_SHIFTED, KeyboardSwitchState.OTHER);
    }

    // Implements {@link KeyboardState.SwitchActions}.
    @Override
    public void setSymbolsKeyboard() {
        if (DEBUG_ACTION) {
            Log.d(TAG, "setSymbolsKeyboard");
        }
        setKeyboard(KeyboardId.ELEMENT_SYMBOLS, KeyboardSwitchState.OTHER);
    }

    // Implements {@link KeyboardState.SwitchActions}.
    @Override
    public void setSymbolsShiftedKeyboard() {
        if (DEBUG_ACTION) {
            Log.d(TAG, "setSymbolsShiftedKeyboard");
        }
        setKeyboard(KeyboardId.ELEMENT_SYMBOLS_SHIFTED, KeyboardSwitchState.SYMBOLS_SHIFTED);
    }

    public boolean isImeSuppressedByHardwareKeyboard(
            @Nonnull final SettingsValues settingsValues,
            @Nonnull final KeyboardSwitchState toggleState) {
        return settingsValues.mHasHardwareKeyboard && toggleState == KeyboardSwitchState.HIDDEN;
    }

    private void setMainKeyboardFrame(
            @Nonnull final SettingsValues settingsValues,
            @Nonnull final KeyboardSwitchState toggleState) {

        mCompactMediaView.setVisibility(View.GONE);
        final int visibility =  isImeSuppressedByHardwareKeyboard(settingsValues, toggleState)
                ? View.GONE : View.VISIBLE;
        mKeyboardView.setVisibility(visibility);
        // The visibility of {@link #mKeyboardView} must be aligned with {@link #MainKeyboardFrame}.
        // @see #getVisibleKeyboardView() and
        // @see LatinIME#onComputeInset(android.inputmethodservice.InputMethodService.Insets)
        mMainKeyboardFrame.setVisibility(visibility);
        mEmojiPalettesView.setVisibility(View.GONE);
        mMediaPalettesView.setVisibility(View.GONE);
        mSuggestionsStripView.setVisibility(View.VISIBLE);
        mEmojiPalettesView.stopEmojiPalettes();
    }

    // Implements {@link KeyboardState.SwitchActions}.
    @Override
    public void setEmojiKeyboard() {
        if (DEBUG_ACTION) {
            Log.d(TAG, "setEmojiKeyboard");
        }
        final Keyboard keyboard = mKeyboardLayoutSet.getKeyboard(KeyboardId.ELEMENT_ALPHABET);
        mMainKeyboardFrame.setVisibility(View.GONE);
        // The visibility of {@link #mKeyboardView} must be aligned with {@link #MainKeyboardFrame}.
        // @see #getVisibleKeyboardView() and
        // @see LatinIME#onComputeInset(android.inputmethodservice.InputMethodService.Insets)
        mKeyboardView.setVisibility(View.GONE);
        mCompactMediaView.setVisibility(View.GONE);
        mEmojiPalettesView.startEmojiPalettes(
                mKeyboardTextsSet.getText(KeyboardTextsSet.SWITCH_TO_ALPHA_KEY_LABEL),
                mKeyboardView.getKeyVisualAttribute(), keyboard.mIconsSet);
        mEmojiPalettesView.setVisibility(View.VISIBLE);
    }

    @Override
    public void setMediaKeyboard() {
        if (DEBUG_ACTION) {
            Log.d(TAG, "setEmojiKeyboard");
        }
        final Keyboard keyboard = mKeyboardLayoutSet.getKeyboard(KeyboardId.ELEMENT_ALPHABET);

        mMainKeyboardFrame.setVisibility(View.GONE);

        mMediaPalettesView.startMediaPalettes(
                this,
                mKeyboardTextsSet.getText(KeyboardTextsSet.SWITCH_TO_ALPHA_KEY_LABEL),
                mKeyboardView.getKeyVisualAttribute(), keyboard.mIconsSet);
        mMediaPalettesView.setVisibility(View.VISIBLE);
    }

    @Override
    public void setMediaKeyboardWithKeyboard() {
        if (DEBUG_ACTION) {
            Log.d(TAG, "setEmojiKeyboard");
        }
        final Keyboard keyboard = mKeyboardLayoutSet.getKeyboard(KeyboardId.ELEMENT_ALPHABET);

        // The visibility of {@link #mKeyboardView} must be aligned with {@link #MainKeyboardFrame}.
        // @see #getVisibleKeyboardView() and
        // @see LatinIME#onComputeInset(android.inputmethodservice.InputMethodService.Insets)

        hideSoftKeyboard();
        mCompactMediaView.mKeyboardView = mKeyboardView;
        mMainKeyboardFrame.setVisibility(View.VISIBLE);
        mCompactMediaView.setVisibility(View.VISIBLE);
        mEmojiPalettesView.setVisibility(View.GONE);
        mMediaPalettesView.setVisibility(View.GONE);
        mSuggestionsStripView.setVisibility(View.GONE);
        mEmojiPalettesView.stopEmojiPalettes();
    }

    public void hideSoftKeyboard() {
        //mLatinIME.hideWindow();
        //mLatinIME.onFinishInput();
        //mLatinIME.onCancelInput();
        //View view = mLatinIME.getApplication()..getCurrentFocus();
        InputMethodManager imm = (InputMethodManager) mLatinIME.getSystemService(Context.INPUT_METHOD_SERVICE);
        //imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        imm.restartInput(mCompactMediaView.mSearchBox);
        //if (view != null) {
        //    view.clearFocus();
//
        //}else {
        //    Log.d(TAG, "hideSoftKeyboard: View is NULL");
        //}
        //mLatinIME.startShowingInputView(true);
    }

    public enum KeyboardSwitchState {
        HIDDEN(-1),
        SYMBOLS_SHIFTED(KeyboardId.ELEMENT_SYMBOLS_SHIFTED),
        EMOJI(KeyboardId.ELEMENT_EMOJI_RECENTS),
        MEDIA(KeyboardId.ELEMENT_MEDIA),
        OTHER(-1);

        final int mKeyboardId;

        KeyboardSwitchState(int keyboardId) {
            mKeyboardId = keyboardId;
        }
    }

    public KeyboardSwitchState getKeyboardSwitchState() {
        boolean hidden = !isShowingEmojiPalettes()
                && (mKeyboardLayoutSet == null
                || mKeyboardView == null
                || !mKeyboardView.isShown());
        KeyboardSwitchState state;
        if (hidden) {
            return KeyboardSwitchState.HIDDEN;
        } else if (isShowingEmojiPalettes()) {
            return KeyboardSwitchState.EMOJI;
        } else if (isShowingKeyboardId(KeyboardId.ELEMENT_SYMBOLS_SHIFTED)) {
            return KeyboardSwitchState.SYMBOLS_SHIFTED;
        }
        return KeyboardSwitchState.OTHER;
    }

    public void onToggleKeyboard(@Nonnull final KeyboardSwitchState toggleState) {
        KeyboardSwitchState currentState = getKeyboardSwitchState();
        Log.w(TAG, "onToggleKeyboard() : Current = " + currentState + " : Toggle = " + toggleState);
        if (currentState == toggleState) {
            mLatinIME.stopShowingInputView();
            mLatinIME.hideWindow();
            setAlphabetKeyboard();
        } else {
            mLatinIME.startShowingInputView(true);
            if (toggleState == KeyboardSwitchState.EMOJI) {
                setEmojiKeyboard();
            }
            else if(toggleState == KeyboardSwitchState.MEDIA) {
                Log.d(TAG, "onToggleKeyboard: MEDIA KEY SELECTED");
                //setEmojiKeyboard();
            }
            else {
                mEmojiPalettesView.stopEmojiPalettes();
                mEmojiPalettesView.setVisibility(View.GONE);

                mMediaPalettesView.setVisibility(View.GONE);

                mMainKeyboardFrame.setVisibility(View.VISIBLE);
                mKeyboardView.setVisibility(View.VISIBLE);
                setKeyboard(toggleState.mKeyboardId, toggleState);
            }
        }
    }

    // Future method for requesting an updating to the shift state.
    @Override
    public void requestUpdatingShiftState(final int autoCapsFlags, final int recapitalizeMode) {
        if (DEBUG_ACTION) {
            Log.d(TAG, "requestUpdatingShiftState: "
                    + " autoCapsFlags=" + CapsModeUtils.flagsToString(autoCapsFlags)
                    + " recapitalizeMode=" + RecapitalizeStatus.modeToString(recapitalizeMode));
        }
        mState.onUpdateShiftState(autoCapsFlags, recapitalizeMode);
    }

    // Implements {@link KeyboardState.SwitchActions}.
    @Override
    public void startDoubleTapShiftKeyTimer() {
        if (DEBUG_TIMER_ACTION) {
            Log.d(TAG, "startDoubleTapShiftKeyTimer");
        }
        final MainKeyboardView keyboardView = getMainKeyboardView();
        if (keyboardView != null) {
            keyboardView.startDoubleTapShiftKeyTimer();
        }
    }

    // Implements {@link KeyboardState.SwitchActions}.
    @Override
    public void cancelDoubleTapShiftKeyTimer() {
        if (DEBUG_TIMER_ACTION) {
            Log.d(TAG, "setAlphabetKeyboard");
        }
        final MainKeyboardView keyboardView = getMainKeyboardView();
        if (keyboardView != null) {
            keyboardView.cancelDoubleTapShiftKeyTimer();
        }
    }

    // Implements {@link KeyboardState.SwitchActions}.
    @Override
    public boolean isInDoubleTapShiftKeyTimeout() {
        if (DEBUG_TIMER_ACTION) {
            Log.d(TAG, "isInDoubleTapShiftKeyTimeout");
        }
        final MainKeyboardView keyboardView = getMainKeyboardView();
        return keyboardView != null && keyboardView.isInDoubleTapShiftKeyTimeout();
    }

    /**
     * Updates state machine to figure out when to automatically switch back to the previous mode.
     */
    public void onEvent(final Event event, final int currentAutoCapsState,
            final int currentRecapitalizeState) {
        mState.onEvent(event, currentAutoCapsState, currentRecapitalizeState);
    }

    public boolean isShowingKeyboardId(@Nonnull int... keyboardIds) {
        if (mKeyboardView == null || !mKeyboardView.isShown()) {
            return false;
        }
        int activeKeyboardId = mKeyboardView.getKeyboard().mId.mElementId;
        for (int keyboardId : keyboardIds) {
            if (activeKeyboardId == keyboardId) {
                return true;
            }
        }
        return false;
    }

    public boolean isShowingEmojiPalettes() {
        return mEmojiPalettesView != null && mEmojiPalettesView.isShown();
    }

    public boolean isShowingMediaPalettes() {
        return mMediaPalettesView != null && mMediaPalettesView.isShown();
    }

    public boolean isShowingMoreKeysPanel() {
        if (isShowingEmojiPalettes()) {
            return false;
        }
        return mKeyboardView.isShowingMoreKeysPanel();
    }

    public View getVisibleKeyboardView() {
        if (isShowingEmojiPalettes()) {
            return mEmojiPalettesView;
        }
        if (isShowingMediaPalettes()) {
            return mMediaPalettesView;
        }
        return mKeyboardView;
    }

    public MainKeyboardView getMainKeyboardView() {
        return mKeyboardView;
    }

    // TODO : Mosnye - Deallocate media memory
    public void deallocateMemory() {
        if (mKeyboardView != null) {
            mKeyboardView.cancelAllOngoingEvents();
            mKeyboardView.deallocateMemory();
        }
        if (mEmojiPalettesView != null) {
            mEmojiPalettesView.stopEmojiPalettes();
        }
    }

    public View onCreateInputView(final boolean isHardwareAcceleratedDrawingEnabled) {
        if (mKeyboardView != null) {
            mKeyboardView.closing();
        }

        updateKeyboardThemeAndContextThemeWrapper(
                mLatinIME, KeyboardTheme.getKeyboardTheme(mLatinIME /* context */));
        mCurrentInputView = (InputView)LayoutInflater.from(mThemeContext).inflate(
                R.layout.input_view, null);
        mMainKeyboardFrame = mCurrentInputView.findViewById(R.id.main_keyboard_frame);
        mCompactMediaView = mMainKeyboardFrame.findViewById(R.id.compact_media_view);
        mSuggestionsStripView = mMainKeyboardFrame.findViewById(R.id.suggestion_strip_view);
        mEmojiPalettesView = mCurrentInputView.findViewById(
                R.id.emoji_palettes_view);

        mMediaPalettesView = mCurrentInputView.findViewById(
                R.id.media_palettes_view);

        mKeyboardView = mCurrentInputView.findViewById(R.id.keyboard_view);
        mKeyboardView.setHardwareAcceleratedDrawingEnabled(isHardwareAcceleratedDrawingEnabled);
        mKeyboardView.setKeyboardActionListener(mLatinIME);
        mEmojiPalettesView.setHardwareAcceleratedDrawingEnabled(
                isHardwareAcceleratedDrawingEnabled);
        mEmojiPalettesView.setKeyboardActionListener(mLatinIME);

        //mMediaPalettesView.setHardwareAcceleratedDrawingEnabled(
        //        isHardwareAcceleratedDrawingEnabled);
        //mMediaPalettesView.setKeyboardActionListener(mLatinIME);

        return mCurrentInputView;
    }

    public int getKeyboardShiftMode() {
        final Keyboard keyboard = getKeyboard();
        if (keyboard == null) {
            return WordComposer.CAPS_MODE_OFF;
        }
        switch (keyboard.mId.mElementId) {
        case KeyboardId.ELEMENT_ALPHABET_SHIFT_LOCKED:
        case KeyboardId.ELEMENT_ALPHABET_SHIFT_LOCK_SHIFTED:
            return WordComposer.CAPS_MODE_MANUAL_SHIFT_LOCKED;
        case KeyboardId.ELEMENT_ALPHABET_MANUAL_SHIFTED:
            return WordComposer.CAPS_MODE_MANUAL_SHIFTED;
        case KeyboardId.ELEMENT_ALPHABET_AUTOMATIC_SHIFTED:
            return WordComposer.CAPS_MODE_AUTO_SHIFTED;
        default:
            return WordComposer.CAPS_MODE_OFF;
        }
    }

    public int getCurrentKeyboardScriptId() {
        if (null == mKeyboardLayoutSet) {
            return ScriptUtils.SCRIPT_UNKNOWN;
        }
        return mKeyboardLayoutSet.getScriptId();
    }
}
