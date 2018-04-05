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

package com.leinardi.android.things.pio;

import com.google.android.things.pio.PeripheralManager;
import com.google.android.things.pio.Pwm;

import java.io.IOException;

/**
 * Controls a GPIO pin providing PWM capabilities. Opening a GPIO pin takes ownership of it for the whole system,
 * preventing anyone else from opening/accessing the GPIO until you call close(). Forgetting to call close() will
 * prevent anyone (including the same process/app) from using the GPIO.
 */
public class SoftPwm implements Pwm {
    public static final int MAX_FREQ = 300;
    private static final String TAG = SoftPwm.class.getSimpleName();

    static {
        System.loadLibrary("softpwm");
    }

    private final long mJniSoftPwmPtr;

    private SoftPwm(String gpioName) throws IOException {
        mJniSoftPwmPtr = jniSoftPwmNew(gpioName);
    }

    /**
     * Open a GPIO pin providing software PWM. A GPIO pin can only be opened once at any given time on the system.
     * To close and release the GPIO pin, you need to call close() explicitly.
     *
     * @param gpioName Name of the GPIO pin as returned by  {@link PeripheralManager#getGpioList()}.
     * @return The {@link SoftPwm} object
     * @throws IOException
     */
    public static SoftPwm openSoftPwm(String gpioName) throws IOException {
        return new SoftPwm(gpioName);
    }

    @Override
    public void close() {
        jniSoftPwmDelete(mJniSoftPwmPtr);
    }

    /**
     * Enable the GPIO/PWM pin. Frequency must be set via {@link #setPwmFrequencyHz(double)} before enabling the pin,
     * but frequency and duty cycle settings can be set in both enabled and disabled state and will be remembered if the
     * PWM is disabled and then re-enabled.
     *
     * @param enabled True to enable the PWM, false to disable.
     */
    @Override
    public void setEnabled(boolean enabled) throws IOException {
        jniSoftPwmSetEnabled(mJniSoftPwmPtr, enabled);
    }

    /**
     * Set the duty cycle.
     *
     * @param dutyCycle A double between 0 and 100 (inclusive).
     */
    @Override
    public void setPwmDutyCycle(double dutyCycle) {
        if (dutyCycle < 0 || dutyCycle > 100) {
            throw new IllegalArgumentException("Invalid duty cycle value (must be between 0 and 100 included). "
                    + "Duty cycle:" + dutyCycle);
        }
        jniSoftPwmSetPwmDutyCycle(mJniSoftPwmPtr, dutyCycle);
    }

    /**
     * Set the frequency of the signal.
     *
     * @param freqHz Frequency in Hertz to use for the signal. Must be positive (max 300 Hz).
     */
    @Override
    public void setPwmFrequencyHz(double freqHz) {
        if (freqHz <= 0 || freqHz > MAX_FREQ) {
            throw new IllegalArgumentException("Invalid frequency value (must be bigger than 0 and lower than "
                    + MAX_FREQ + "Hz). Freq:" + freqHz);
        }
        if (freqHz > 40) {
            double adjustedFreq = regressionLine(freqHz);
            jniSoftPwmSetPwmFrequencyHz(mJniSoftPwmPtr, adjustedFreq);
        } else {
            jniSoftPwmSetPwmFrequencyHz(mJniSoftPwmPtr, freqHz);
        }

    }

    /**
     * For higher frequencies we need to compensate a little to stay close to the desired value.
     * This equation represent the regression line: https://en.wikipedia.org/wiki/Least_squares
     */
    private double regressionLine(double freqHz) {
        return -6.261 + (1.151 * freqHz);
    }

    private native long jniSoftPwmNew(String gpioName) throws IOException;

    private native void jniSoftPwmDelete(long ptr);

    private native void jniSoftPwmSetEnabled(long ptr, boolean enabled);

    private native void jniSoftPwmSetPwmDutyCycle(long ptr, double dutyCycle);

    private native void jniSoftPwmSetPwmFrequencyHz(long ptr, double freqHz);
}
