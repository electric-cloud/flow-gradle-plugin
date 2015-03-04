# flow-gradle-plugin
A gradle plugin that houses the logic to build our ElectricFlow plugins.

To build plugin use ./gradlew jar.
To upload on Bintray use ./gradlew bintrayUpload.

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
        classpath group: 'com.electriccloud.plugins', name: 'flow-gradle-plugin'
    }
}

apply plugin: 'flow-gradle-plugin'
group = 'com.electriccloud'
description = "Plugins : <plugin name>"
version = "1.0.0.$buildNumber"

gwt {
	modules 'GWT module name, e.g. ecplugins.<plugin>.ConfigurationManagement'
}

```
