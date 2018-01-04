cmake_minimum_required(VERSION 3.4.1)

# Add FindAndroidThings.cmake dir to the module path.
set(CMAKE_MODULE_PATH ${CMAKE_MODULE_PATH} ${PROJECT_DIR}/libandroidthings)
# Resolve Android Things headers and libraries
find_package(AndroidThings REQUIRED)

add_library(softpwm SHARED
            softpwm.cpp
            jni_helpers.cpp)

target_include_directories(softpwm PRIVATE
                           ${ANDROID_NDK}/sources/android/native_app_glue
                           ${ANDROIDTHINGS_INCLUDE_DIRS})

add_library(native-app-glue STATIC
            ${ANDROID_NDK}/sources/android/native_app_glue/android_native_app_glue.c)

# now build app's shared lib
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11 -Werror")

add_library(androidthings SHARED
            IMPORTED)
set_target_properties(androidthings
                      PROPERTIES IMPORTED_LOCATION
                      ${ANDROIDTHINGS_LIBRARIES})

# Include libraries needed for softpwm lib
target_link_libraries(softpwm
                      android
                      log
                      native-app-glue
                      androidthings)
