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

#ifndef ANDROIDTHINGS_PIO_SOFTPWM_H
#define ANDROIDTHINGS_PIO_SOFTPWM_H

#include <atomic>
#include <jni.h>
#include <pio/gpio.h>
#include <pio/peripheral_manager_client.h>
#include <thread>

class SoftPwm {
public:
    SoftPwm(JNIEnv *env, const char *gpioName);

    ~SoftPwm();

    void setEnabled(bool enabled);

    void setPwmDutyCycle(double dutyCycle);

    void setPwmFrequencyHz(double freqHz);

private:
    JNIEnv *mEnv;
    const char *mGpioName;
    APeripheralManagerClient *mClient;
    AGpio *mGpio;

    double mDutyCycle = 0;
    double mFreqHz;
    const useconds_t HZ_IN_MICROSECONDS = 1000000;
    useconds_t mPeriodTotal;
    useconds_t mPeriodHigh;
    useconds_t mPeriodLow;

    std::atomic_flag mLock = ATOMIC_FLAG_INIT;
    std::thread mPwmThread;

    void startThread();

    void stopThread();

    void run();
};

#endif //ANDROIDTHINGS_PIO_SOFTPWM_H
