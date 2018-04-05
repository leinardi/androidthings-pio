/*
 * Copyright 2018 Roberto Leinardi.
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

package com.leinardi.android.things.sample.softpwm;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.things.pio.PeripheralManager;
import com.google.android.things.pio.Pwm;
import com.leinardi.android.things.pio.SoftPwm;

import java.io.IOException;

/**
 * SoftPwmActivity is an example that use both software and hardware PWM.
 */
public class SoftPwmActivity extends Activity {
    private static final String TAG = SoftPwmActivity.class.getSimpleName();
    public static final float PWM_FREQUENCY_HZ = 100f; // Max 300 Hz for SoftPwm!

    private Pwm mSoftPwm;
    private Pwm mHardPwm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "Starting SoftPwmActivity");
        PeripheralManager pioService = PeripheralManager.getInstance();
        try {
            mSoftPwm = SoftPwm.openSoftPwm("BCM21");
            mHardPwm = pioService.openPwm("PWM0");

            mSoftPwm.setPwmFrequencyHz(PWM_FREQUENCY_HZ);
            mHardPwm.setPwmFrequencyHz(PWM_FREQUENCY_HZ);

            mSoftPwm.setPwmDutyCycle(31);
            mHardPwm.setPwmDutyCycle(31);

            mSoftPwm.setEnabled(true);
            mHardPwm.setEnabled(true);
        } catch (IOException e) {
            Log.e(TAG, "Error", e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Closing sensor");

        try {
            mSoftPwm.close();
        } catch (IOException e) {
            Log.w(TAG, "Unable to close PWM device", e);
        } finally {
            mSoftPwm = null;
        }
        try {
            mHardPwm.close();
        } catch (IOException e) {
            Log.w(TAG, "Unable to close PWM device", e);
        } finally {
            mHardPwm = null;
        }
    }

}
