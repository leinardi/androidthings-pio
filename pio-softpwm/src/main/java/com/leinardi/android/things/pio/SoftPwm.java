/*
 * Copyright 2017 Roberto Leinardi.
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

import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManagerService;
import com.google.android.things.pio.Pwm;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Controls a GPIO pin providing PWM capabilities. Opening a GPIO pin takes ownership of it for the whole system,
 * preventing anyone else from opening/accessing the GPIO until you call close(). Forgetting to call close() will
 * prevent anyone (including the same process/app) from using the GPIO.
 */
public class SoftPwm extends Pwm {
    public static final int MAX_FREQ = 5000; // 5 kHz
    private static final int NANOS_PER_MILLI = (int) TimeUnit.MILLISECONDS.toNanos(1);
    private static final long HZ_IN_NANOSECONDS = (int) TimeUnit.SECONDS.toNanos(1);
    private static final String TAG = SoftPwm.class.getSimpleName();
    private Gpio mGpio;
    private double mFreq;
    private double mDutyCycle;
    private Thread mThread;
    private long mPeriodTotal;
    private long mPeriodHighMs;
    private int mPeriodHighNs;
    private long mPeriodLowMs;
    private int mPeriodLowNs;
    private boolean mEnabled;

    private SoftPwm() {
    }

    /**
     * Open a GPIO pin providing software PWM. A GPIO pin can only be opened once at any given time on the system.
     * To close and release the GPIO pin, you need to call close() explicitly.
     *
     * @param gpioName Name of the GPIO pin as returned by  {@link PeripheralManagerService#getGpioList()}.
     * @return The {@link SoftPwm} object
     * @throws IOException
     */
    public static Pwm openSoftPwm(String gpioName) throws IOException {
        PeripheralManagerService manager = new PeripheralManagerService();
        SoftPwm softPwm = new SoftPwm();
        Gpio gpio = manager.openGpio(gpioName);
        gpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
        softPwm.setGpio(gpio);
        return softPwm;
    }

    private void setGpio(Gpio gpio) {
        mGpio = gpio;
    }

    @Override
    public void close() {
        try {
            stopThread();
        } catch (IOException e) {
            Log.w(TAG, "An error occurred while stopping the thread", e);
        }
        if (mGpio != null) {
            try {
                mGpio.close();
            } catch (IOException e) {
                Log.w(TAG, "Unable to close GPIO device", e);
            } finally {
                mGpio = null;
            }
        }
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
        if (mGpio == null) {
            throw new IllegalStateException("GPIO Device not open");
        }
        if (mFreq == 0) {
            throw new IllegalStateException("Frequency must be set via setPwmFrequencyHz() before enabling the pin");
        }
        if (mEnabled != enabled) {
            mEnabled = enabled;
            if (enabled) {
                startNewThread();
            } else {
                stopThread();
            }
        }
    }

    private void startNewThread() throws IOException {
        stopThread();
        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        if (mPeriodHighMs != 0 || mPeriodHighNs != 0) {
                            mGpio.setValue(true);
                            Thread.sleep(mPeriodHighMs, mPeriodHighNs);
                        }
                        if (mPeriodLowMs != 0 || mPeriodLowNs != 0) {
                            mGpio.setValue(false);
                            Thread.sleep(mPeriodLowMs, mPeriodLowNs);
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    } catch (IOException e) {
                        Log.e(TAG, "GPIO error", e);
                        try {
                            stopThread();
                        } catch (IOException ignored) {
                        }
                    }
                }
            }
        });
        mThread.setPriority(Thread.MAX_PRIORITY);
        mThread.setName("softpwm-thread");
        mThread.start();
    }

    private void stopThread() throws IOException {
        if (mThread != null && mThread.isAlive()) {
            mThread.interrupt();
        }
        mGpio.setValue(false);
    }

    /**
     * Get the duty cycle.
     *
     * @return A double between 0 and 100 (inclusive).
     */
    public double getPwmDutyCycle() {
        return mDutyCycle;
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
        mDutyCycle = dutyCycle;
        mPeriodHighNs = (int) Math.round(mPeriodTotal / 100f * dutyCycle);
        mPeriodLowNs = (int) (mPeriodTotal - mPeriodHighNs);
        mPeriodHighMs = mPeriodHighNs / NANOS_PER_MILLI;
        mPeriodHighNs %= NANOS_PER_MILLI;
        mPeriodLowMs = mPeriodLowNs / NANOS_PER_MILLI;
        mPeriodLowNs %= NANOS_PER_MILLI;
    }

    /**
     * Get the frequency of the signal.
     *
     * @return Frequency in Hertz to use for the signal. Must be positive.
     */
    public double getPwmFrequencyHz() {
        return mFreq;
    }

    /**
     * Set the frequency of the signal.
     *
     * @param freqHz Frequency in Hertz to use for the signal. Must be positive (max 5 kHz).
     */
    @Override
    public void setPwmFrequencyHz(double freqHz) {
        if (freqHz <= 0 || freqHz > MAX_FREQ) {
            throw new IllegalArgumentException("Invalid frequency value (must be bigger than 0 and lower than "
                    + MAX_FREQ + "Hz). Freq:" + freqHz);
        }
        mFreq = freqHz;
        mPeriodTotal = Math.round(HZ_IN_NANOSECONDS / freqHz);
        setPwmDutyCycle(mDutyCycle);
    }
}
