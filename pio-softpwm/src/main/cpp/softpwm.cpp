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

#include <android_native_app_glue.h>
#include <cmath>
#include <string>
#include <unistd.h>
#include "jni_helpers.h"
#include "native_debug.h"
#include "softpwm.h"

#define HIGH 1
#define LOW 0

SoftPwm::SoftPwm(JNIEnv *env, const char *gpioName) {
    mEnv = env;
    mGpioName = gpioName;
    mClient = APeripheralManagerClient_new();
    if (!mClient) {
        throwIOException(env, "failed to open peripheral manager mClient");
        return;
    }
    int openResult = APeripheralManagerClient_openGpio(mClient, mGpioName, &mGpio);
    if (openResult != 0) {
        throwIOException(env, "failed to open GPIO: %s", mGpioName);
        return;
    }
    int setDirectionResult = AGpio_setDirection(mGpio, AGPIO_DIRECTION_OUT_INITIALLY_LOW);
    if (setDirectionResult != 0) {
        throwIOException(env, "failed to set direction for GPIO: %s", mGpioName);
        return;
    }
}

SoftPwm::~SoftPwm() {
    stopThread();
    if (mGpio) {
        AGpio_delete(mGpio);
    }
    if (mClient) {
        APeripheralManagerClient_delete(mClient);
    }
}

void SoftPwm::setEnabled(bool enabled) {
    if (enabled) {
        startThread();
    } else {
        stopThread();
    }
}

void SoftPwm::startThread() {
    stopThread();
    mLock.test_and_set(std::memory_order_acquire);
    mPwmThread = std::thread(&SoftPwm::run, this);
}

void SoftPwm::stopThread() {
    if (mPwmThread.joinable()) {
        mLock.clear(std::memory_order_release);
        mPwmThread.join();
    }
}

void SoftPwm::run() {
    while (mLock.test_and_set(std::memory_order_acquire)) {
        if (mPeriodHigh != 0) {
            if (AGpio_setValue(mGpio, HIGH) != 0) {
                LOGE("failed to set value for GPIO: %s", mGpioName);
                continue;
            }
            usleep(mPeriodHigh);
        }
        if (mPeriodLow != 0) {
            if (AGpio_setValue(mGpio, LOW) != 0) {
                LOGE("failed to set value for GPIO: %s", mGpioName);
                continue;
            }
            usleep(mPeriodLow);
        }
    }
}

void SoftPwm::setPwmDutyCycle(double dutyCycle) {
    mDutyCycle = dutyCycle;
    mPeriodHigh = (useconds_t) lround((float) mPeriodTotal / 100.0 * (float) dutyCycle);
    mPeriodLow = mPeriodTotal - mPeriodHigh;
}

void SoftPwm::setPwmFrequencyHz(double freqHz) {
    mFreqHz = freqHz;
    mPeriodTotal = (useconds_t) lround((float) HZ_IN_MICROSECONDS / freqHz);
    setPwmDutyCycle(mDutyCycle);
}

extern "C"
JNIEXPORT jlong JNICALL
Java_com_leinardi_android_things_pio_SoftPwm_jniSoftPwmNew(JNIEnv *env, jobject instance, jstring gpioName_) {
    const char *gpioName = env->GetStringUTFChars(gpioName_, 0);
    return (long) (new SoftPwm(env, gpioName));
}

extern "C"
JNIEXPORT void JNICALL
Java_com_leinardi_android_things_pio_SoftPwm_jniSoftPwmDelete(JNIEnv *env, jobject instance, jlong ptr) {
    delete (SoftPwm *) (ptr);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_leinardi_android_things_pio_SoftPwm_jniSoftPwmSetEnabled(JNIEnv *env, jobject instance, jlong ptr,
                                                                  jboolean enabled) {
    SoftPwm *softPwm = (SoftPwm *) ptr;
    softPwm->setEnabled(enabled);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_leinardi_android_things_pio_SoftPwm_jniSoftPwmSetPwmDutyCycle(JNIEnv *env, jobject instance, jlong ptr,
                                                                       jdouble dutyCycle) {
    SoftPwm *softPwm = (SoftPwm *) ptr;
    softPwm->setPwmDutyCycle(dutyCycle);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_leinardi_android_things_pio_SoftPwm_jniSoftPwmSetPwmFrequencyHz(JNIEnv *env, jobject instance, jlong ptr,
                                                                         jdouble freqHz) {
    SoftPwm *softPwm = (SoftPwm *) ptr;
    softPwm->setPwmFrequencyHz(freqHz);
}
