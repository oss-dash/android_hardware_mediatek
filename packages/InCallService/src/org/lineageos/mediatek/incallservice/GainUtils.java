/*
 * Copyright (C) 2023 The LineageOS Project
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.mediatek.incallservice;

import android.media.AudioSystem;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.os.SystemProperties;
import android.util.Log;

public class GainUtils {
    public static final String LOG_TAG = "MtkInCallService";
    public static final int volSteps = SystemProperties.getInt("ro.config.vc_call_vol_steps", 7);

    /**
     * Maps a STREAM_VOICE_CALL index to the vendor range 1..volSteps.
     * The framework can use more call volume steps than the vendor (for example it raises
     * the call range to the Bluetooth SCO one), so clamping would turn the whole upper part
     * of the slider into the maximum gain.
     * @param audioManager Used to read the call stream range.
     * @param index The STREAM_VOICE_CALL index.
     * @return The index scaled to the vendor range.
     */
    public static int toVendorIndex(AudioManager audioManager, int index) {
        int maxIndex = audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);
        if (maxIndex <= volSteps || index <= 1) {
            return index;
        }
        return 1 + Math.round((index - 1) * (volSteps - 1) / (float) (maxIndex - 1));
    }

    /**
     * Sets the gain level for a given audio device.
     * @param audioDevice The audio device to set the gain level for.
     * @param gainIndex The gain level to set.
     * @param streamType The stream type to set the gain level for.
     */
    public static void setGainLevel(int audioDevice, int gainIndex, int streamType) {
        String parameters = String.format("volumeDevice=%d;volumeIndex=%d;volumeStreamType=%d",
                                          audioDevice, Math.min(volSteps, gainIndex), streamType);
        Log.d(LOG_TAG, "Setting audio parameters to: " + parameters);
        AudioSystem.setParameters(parameters);
    }

    /**
     * Sets the gain level for built-in earpiece and bluetooth SCO devices.
     * @param gainIndex The gain level to set.
     */
    public static void setGainLevel(int gainIndex) {
        GainUtils.setGainLevel(AudioDeviceInfo.TYPE_BUILTIN_EARPIECE, gainIndex, AudioSystem.STREAM_VOICE_CALL);
        GainUtils.setGainLevel(AudioDeviceInfo.TYPE_BLUETOOTH_SCO, gainIndex, AudioSystem.STREAM_VOICE_CALL);
    }
}
