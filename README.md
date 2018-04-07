# Android Things user-space PIO 

[![GitHub tag](https://img.shields.io/github/tag/leinardi/androidthings-pio.svg?style=plastic)](https://github.com/leinardi/androidthings-pio/releases)
[![Build Status](https://img.shields.io/travis/leinardi/androidthings-pio/master.svg?style=plastic)](https://travis-ci.org/leinardi/androidthings-pio)
[![GitHub license](https://img.shields.io/github/license/leinardi/androidthings-pio.svg?style=plastic)](https://github.com/leinardi/androidthings-pio/blob/master/LICENSE)


Sample PIO for Android Things.

NOTE: these PIO are not production-ready. They are offered as sample
implementations of Android Things user space PIO 
as part of the Developer Preview release. There is no guarantee
of correctness, completeness or robustness.


# How to use a PIO

For your convenience, PIO in this repository are also published to JCenter
as Maven artifacts. Look at their artifact and group ID in their build.gradle
and add them as dependencies to your own project.

For example, to use the `pio-softpwm` driver, version `0.1`, simply add the line
below to your project's `build.gradle`:


```
dependencies {
    compile 'com.leinardi.android.things:pio-softpwm:0.2'
}
```


## Current contrib drivers

<!-- PIO_LIST_START -->
Driver | Type | Usage (add to your gradle dependencies) | Note
:---:|:---:| --- | ---
[pio-softpwm](pio-softpwm) | Software PWM | `implementation 'com.leinardi.android.things:pio-softpwm:0.1'` | [![Maven metadata URI](https://img.shields.io/maven-metadata/v/http/jcenter.bintray.com/com/leinardi/android/things/pio-softpwm/maven-metadata.xml.svg)](https://jcenter.bintray.com/com/leinardi/android/things/pio-softpwm/maven-metadata.xml) [changelog](pio-softpwm/CHANGELOG.md)
<!-- PIO_LIST_END -->


## License

Copyright 2017 Roberto Leinardi.

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
