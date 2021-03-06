/*
 * Copyright (C) 2014 AChep@xda <artemchep@gmail.com>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
package com.achep.activedisplay;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.achep.activedisplay.activemode.ActiveModeService;
import com.achep.activedisplay.services.LockscreenService;
import com.achep.activedisplay.utils.AccessUtils;

import java.util.ArrayList;

/**
 * Created by Artem on 21.01.14.
 */
public class Config {

    private static final String TAG = "Config";

    private static final String PREFERENCES_FILE_NAME = "config";
    public static final String KEY_ENABLED = "a";
    public static final String KEY_ONLY_WHILE_CHARGING = "b";
    public static final String KEY_LOW_PRIORITY_NOTIFICATIONS = "c";

    // inactive time
    public static final String KEY_INACTIVE_TIME_FROM = "inactive_time_from";
    public static final String KEY_INACTIVE_TIME_TO = "inactive_time_to";
    public static final String KEY_INACTIVE_TIME_ENABLED = "inactive_time_enabled";

    // timeouts
    public static final String KEY_TIMEOUT_NORMAL = "timeout_normal";
    public static final String KEY_TIMEOUT_SHORT = "timeout_short";
    public static final String KEY_TIMEOUT_INSTANT = "timeout_instant";

    // lockscreen
    public static final String KEY_LOCK_SCREEN = "lock_screen";

    // active mode
    public static final String KEY_ACTIVE_MODE = "active_mode";

    // interface
    public static final String KEY_INTERFACE_WALLPAPER_SHOWN = "wallpaper_shown";
    public static final String KEY_INTERFACE_SHADOW_TOGGLE = "shadow_toggle";
    public static final String KEY_INTERFACE_DYNAMIC_BACKGROUND_MODE = "dynamic_background_mode";

    // swipe actions
    public static final String KEY_SWIPE_LEFT_ACTION = "swipe_left_action";
    public static final String KEY_SWIPE_RIGHT_ACTION = "swipe_right_action";

    public static final int DYNAMIC_BG_ARTWORK_MASK = 1;
    public static final int DYNAMIC_BG_NOTIFICATION_MASK = 2;

    private static Config sConfigSoft;

    private boolean mActiveDisplayEnabled;
    private boolean mEnabledOnlyWhileCharging;
    private boolean mLowPriorityNotificationsAllowed;
    private int mTimeoutNormal;
    private int mTimeoutShort;
    private int mInactiveTimeFrom;
    private int mInactiveTimeTo;
    private int mSwipeLeftAction;
    private int mSwipeRightAction;
    private int mDynamicBackgroundMode;
    private boolean mInactiveTimeEnabled;
    private boolean mLockscreenEnabled;
    private Boolean mActiveMode;
    private ArrayList<OnConfigChangedListener> mListeners;
    private boolean mWallpaperShown;
    private boolean mShadowShown;

    // //////////////////////////////////////////
    // /////////// -- LISTENERS -- //////////////
    // //////////////////////////////////////////

    public interface OnConfigChangedListener {
        public void onConfigChanged(Config config, String key, Object value);
    }

    public void addOnConfigChangedListener(OnConfigChangedListener listener) {
        if (Project.DEBUG) Log.d(TAG, "add_l=" + listener);
        mListeners.add(listener);
    }

    public void removeOnConfigChangedListener(OnConfigChangedListener listener) {
        if (Project.DEBUG) Log.d(TAG, "remove_l=" + listener);
        mListeners.remove(listener);
    }

    // //////////////////////////////////////////
    // ///////////// -- INIT -- /////////////////
    // //////////////////////////////////////////

    public static synchronized Config getInstance(Context context) {
        if (sConfigSoft == null)
            sConfigSoft = new Config(context);
        return sConfigSoft;
    }

    private Config(Context context) {
        mListeners = new ArrayList<>(6);

        SharedPreferences prefs = getSharedPreferences(context);
        mActiveDisplayEnabled = prefs.getBoolean(KEY_ENABLED, false);
        mEnabledOnlyWhileCharging = prefs.getBoolean(KEY_ONLY_WHILE_CHARGING, false);
        mLowPriorityNotificationsAllowed = prefs.getBoolean(KEY_LOW_PRIORITY_NOTIFICATIONS, false);
        mLockscreenEnabled = prefs.getBoolean(KEY_LOCK_SCREEN, false);
        mActiveMode = prefs.getBoolean(KEY_ACTIVE_MODE, false);
        mWallpaperShown = prefs.getBoolean(KEY_INTERFACE_WALLPAPER_SHOWN, false);
        mShadowShown = prefs.getBoolean(KEY_INTERFACE_SHADOW_TOGGLE, false);
        mTimeoutNormal = prefs.getInt(KEY_TIMEOUT_NORMAL, 12000);
        mTimeoutShort = prefs.getInt(KEY_TIMEOUT_SHORT, 6000);
        mInactiveTimeFrom = prefs.getInt(KEY_INACTIVE_TIME_FROM, 0);
        mInactiveTimeTo = prefs.getInt(KEY_INACTIVE_TIME_TO, 0);
        mInactiveTimeEnabled = prefs.getBoolean(KEY_INACTIVE_TIME_ENABLED, false);
        mSwipeLeftAction = prefs.getInt(KEY_SWIPE_LEFT_ACTION, 2);
        mSwipeRightAction = prefs.getInt(KEY_SWIPE_RIGHT_ACTION, 2);
        mDynamicBackgroundMode = prefs.getInt(KEY_INTERFACE_DYNAMIC_BACKGROUND_MODE,
                DYNAMIC_BG_ARTWORK_MASK | DYNAMIC_BG_NOTIFICATION_MASK);
    }

    private SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(PREFERENCES_FILE_NAME, Context.MODE_PRIVATE);
    }

    private void notifyConfigChanged(String key, Object value, OnConfigChangedListener listener) {
        if (Project.DEBUG) Log.d(TAG, "Notifying listeners: \"" + key + "\" = \"" + value + "\"");
        for (OnConfigChangedListener l : mListeners) {
            if (l == listener) continue;
            l.onConfigChanged(this, key, value);
        }
    }

    private void saveOption(Context context, String key, Object value,
                            OnConfigChangedListener listener, boolean changed) {
        if (!changed) return;

        if (Project.DEBUG) Log.d(TAG, "Saving \"" + key + "\" to config as \"" + value + "\"");

        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        if (value instanceof Boolean) {
            editor.putBoolean(key, (Boolean) value);
        } else if (value instanceof Integer) {
            editor.putInt(key, (Integer) value);
        } else throw new IllegalArgumentException("Unknown option type.");
        editor.apply();

        notifyConfigChanged(key, value, listener);
    }
    // //////////////////////////////////////////
    // ///////////// -- OPTIONS -- //////////////
    // //////////////////////////////////////////

    public boolean setActiveDisplayEnabled(Context context, boolean enabled,
                                           OnConfigChangedListener listener) {
        if (enabled && !(AccessUtils.isNotificationAccessEnabled(context)
                && AccessUtils.isDeviceAdminEnabled(context))) {
            return false;
        }

        boolean changed = mActiveDisplayEnabled != (mActiveDisplayEnabled = enabled);
        saveOption(context, KEY_ENABLED, enabled, listener, changed);

        if (changed) {
            ActiveModeService.handleState(context);
            LockscreenService.handleState(context);
        }
        return true;
    }

    public void setActiveDisplayEnabledOnlyWhileCharging(Context context, boolean enabled,
                                                         OnConfigChangedListener listener) {
        saveOption(context, KEY_ONLY_WHILE_CHARGING, enabled, listener,
                mEnabledOnlyWhileCharging != (mEnabledOnlyWhileCharging = enabled));
    }

    public void setLowPriorityNotificationsAllowed(Context context, boolean enabled,
                                                   OnConfigChangedListener listener) {
        saveOption(context, KEY_LOW_PRIORITY_NOTIFICATIONS, enabled, listener,
                mLowPriorityNotificationsAllowed != (mLowPriorityNotificationsAllowed = enabled));
    }

    // used via reflections!
    public void setTimeoutNormal(Context context, int delayMillis, OnConfigChangedListener listener) {
        saveOption(context, KEY_TIMEOUT_NORMAL, delayMillis, listener,
                mTimeoutNormal != (mTimeoutNormal = delayMillis));
    }

    // used via reflections!
    public void setTimeoutShort(Context context, int delayMillis, OnConfigChangedListener listener) {
        saveOption(context, KEY_TIMEOUT_SHORT, delayMillis, listener,
                mTimeoutShort != (mTimeoutShort = delayMillis));
    }

    public void setInactiveTimeEnabled(Context context, boolean enabled, OnConfigChangedListener listener) {
        saveOption(context, KEY_INACTIVE_TIME_ENABLED, enabled, listener,
                mInactiveTimeEnabled != (mInactiveTimeEnabled = enabled));
    }

    public void setInactiveTimeFrom(Context context, int minutes, OnConfigChangedListener listener) {
        saveOption(context, KEY_INACTIVE_TIME_FROM, minutes, listener,
                mInactiveTimeFrom != (mInactiveTimeFrom = minutes));
    }

    public void setInactiveTimeTo(Context context, int minutes, OnConfigChangedListener listener) {
        saveOption(context, KEY_INACTIVE_TIME_TO, minutes, listener,
                mInactiveTimeTo != (mInactiveTimeTo = minutes));
    }

    public void setSwipeLeftAction(Context context, int action, OnConfigChangedListener listener) {
        saveOption(context, KEY_SWIPE_LEFT_ACTION, action, listener,
                mSwipeLeftAction != (mSwipeLeftAction = action));
    }

    public void setSwipeRightAction(Context context, int action, OnConfigChangedListener listener) {
        saveOption(context, KEY_SWIPE_RIGHT_ACTION, action, listener,
                mSwipeRightAction != (mSwipeRightAction = action));
    }

    public void setLockscreenEnabled(Context context, boolean enabled, OnConfigChangedListener listener) {
        boolean changed = mLockscreenEnabled != (mLockscreenEnabled = enabled);

        saveOption(context, KEY_LOCK_SCREEN, enabled, listener, changed);

        // Launch / stop lockscreen service
        if (changed) LockscreenService.handleState(context);
    }

    public void setActiveModeEnabled(Context context, boolean enabled, OnConfigChangedListener listener) {
        boolean changed = mActiveMode != (mActiveMode = enabled);
        saveOption(context, KEY_ACTIVE_MODE, enabled, listener, changed);

        // Launch / stop sensor monitor service
        if (changed) ActiveModeService.handleState(context);
    }

    public void setWallpaperShown(Context context, boolean shown, OnConfigChangedListener listener) {
        saveOption(context, KEY_INTERFACE_WALLPAPER_SHOWN, shown, listener,
                mWallpaperShown != (mWallpaperShown = shown));
    }

    public void setShadowEnabled(Context context, boolean shown, OnConfigChangedListener listener) {
        saveOption(context, KEY_INTERFACE_SHADOW_TOGGLE, shown, listener,
                mShadowShown != (mShadowShown = shown));
    }

    public void setDynamicBackgroundMode(Context context, int mode, OnConfigChangedListener listener) {
        saveOption(context, KEY_INTERFACE_DYNAMIC_BACKGROUND_MODE, mode, listener,
                mDynamicBackgroundMode != (mDynamicBackgroundMode = mode));
    }

    public int getTimeoutNormal() {
        return mTimeoutNormal;
    }

    public int getTimeoutShort() {
        return mTimeoutShort;
    }

    public int getInactiveTimeFrom() {
        return mInactiveTimeFrom;
    }

    public int getInactiveTimeTo() {
        return mInactiveTimeTo;
    }

    public int getSwipeLeftAction() {
        return mSwipeLeftAction;
    }

    public int getSwipeRightAction() {
        return mSwipeRightAction;
    }

    public int getDynamicBackgroundMode() {
        return mDynamicBackgroundMode;
    }

    public boolean isActiveDisplayEnabled() {
        return mActiveDisplayEnabled;
    }

    public boolean isEnabledOnlyWhileCharging() {
        return mEnabledOnlyWhileCharging;
    }

    public boolean isLowPriorityNotificationsAllowed() {
        return mLowPriorityNotificationsAllowed;
    }

    public boolean isLockscreenEnabled() {
        return mLockscreenEnabled;
    }

    public boolean isActiveModeEnabled() {
        return mActiveMode;
    }

    public boolean isWallpaperShown() {
        return mWallpaperShown;
    }

    public boolean isShadowEnabled() {
        return mShadowShown;
    }

    public boolean isInactiveTimeEnabled() {
        return mInactiveTimeEnabled;
    }

}
