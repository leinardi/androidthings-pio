# Software PWM library for Android Things

[![Maven metadata URI](https://img.shields.io/maven-metadata/v/http/jcenter.bintray.com/com/leinardi/android/things/pio-softpwm/maven-metadata.xml.svg?style=plastic)](https://jcenter.bintray.com/com/leinardi/android/things/pio-softpwm/maven-metadata.xml)
[![Build Status](https://img.shields.io/travis/leinardi/androidthings-pio/master.svg?style=plastic)](https://travis-ci.org/leinardi/androidthings-pio)
[![GitHub license](https://img.shields.io/github/license/leinardi/androidthings-pio.svg?style=plastic)](https://github.com/leinardi/androidthings-pio/blob/master/LICENSE)

This library provides a software PWM that can be used with any GPIO pin.

**NOTE**: PWM signals that are not driven from a hardware channel 
are destined to be **too inaccurate for most use cases**.
The multiprocess and multithreaded nature of a system like
Android makes it too likely for the scheduler to cause minor
disruptions that affect the output signal.

One common use case for software PWM is power the speed of DC
motors (e.g. via the L298) if the accuracy of the speed is not
that important. If possible always use [hardware PWM](https://developer.android.com/things/sdk/pio/pwm.html).

## How to use SoftPWM

### Gradle dependency

To use the `pio-softpwm` simply add the line below to your project's `build.gradle`,
where `<version>` matches the last version of the library available on [jcenter][jcenter].

```
dependencies {
    implementation 'com.leinardi.android.things:pio-softpwm:<version>'
}
```

### Sample usage

```java
import com.leinardi.android.things.pio.SoftPwm;

public class HomeActivity extends Activity {
    // GPIO Name
    private static final String GPIO_NAME = ...;  // e.g. BCM20

    private Pwm mPwm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Attempt to access the PWM port
        try {
            mPwm = SoftPwm.openSoftPwm(GPIO_NAME);
            initializePwm(mPwm);
        } catch (IOException e) {
            Log.w(TAG, "Unable to access PWM", e);
        }
    }
    
    public void initializePwm(Pwm pwm) throws IOException {
        pwm.setPwmFrequencyHz(120);
        pwm.setPwmDutyCycle(25);
    
        // Enable the PWM signal
        pwm.setEnabled(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mPwm != null) {
            try {
                mPwm.close();
                mPwm = null;
            } catch (IOException e) {
                Log.w(TAG, "Unable to close PWM", e);
            }
        }
    }
}

```

## License

Copyright 2017 Roberto Leinardi

Licensed to the Apache Software Foundation (ASF) under one or more contributor
license agreements.  See the NOTICE file distributed with this work for
additional information regarding copyright ownership.  The ASF licenses this
file to you under the Apache License, Version 2.0 (the "License"); you may not
use this file except in compliance with the License.  You may obtain a copy of
the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
License for the specific language governing permissions and limitations under
the License.

[jcenter]: https://bintray.com/leinardi/androidthings/pio-softpwm/_latestVersion
