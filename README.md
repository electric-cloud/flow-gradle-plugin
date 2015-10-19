# flow-gradle-plugin
A gradle plugin that houses the logic to build ElectricFlow plugins.

To build plugin use ./gradlew jar.
To upload on Bintray use ./gradlew bintrayUpload.
If you make multiple uploads of same version, make sure you delete version through bintray web interface. 

Sample build.gradle:

```groovy
buildscript {
    repositories {
        maven {
            url 'http://dl.bintray.com/ecpluginsdev/maven'
        }

        jcenter()
    }
    dependencies {
        classpath group: 'com.electriccloud.plugins', name: 'flow-gradle-plugin', version: '+'
    }
}

group = 'com.electriccloud'
description = "Plugins : <plugin name>"
version = "1.0.0"

apply plugin: 'flow-gradle-plugin'

gwt {
	modules 'GWT module name, e.g. ecplugins.<plugin>.ConfigurationManagement'
}

```

# Environment variables

When running plugin tasks, following variables can be set to customize them:

* COMMANDER_SERVER hostname or IP address of ElectricFlow server (default is *localhost*)
* COMMANDER_HOME path to installed ElectricFlow (default is */opt/electriccloud/electriccommander*)
* COMMANDER_USER login name for ElectricFlow user (default is *admin*)
* COMMANDER_USER password for ElectricFlow user (default is *changeme*)
* BUILD_NUMBER build number for plugin, included in plugin version (default is *0*)
* TESTFRAMEWORK_HOME path to internal plugin testing framework, for perl and system tests
* RTJAR6_PATH path to jre's rt.jar (for example, <nimbus>/build/lib/java6/rt.jar)
