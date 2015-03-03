# flow-gradle-plugin
A gradle plugin that houses the logic to build our ElectricFlow plugins.

To build plugin use ./gradlew uploadArchives

Sample build.gradle:

```groovy
buildscript {
    repositories {
        maven {
            url uri('../flow-gradle-plugin/repo')
        }
        jcenter()
    }
    dependencies {
        classpath group: 'com.electriccloud.plugins', name: 'flow-gradle-plugin',
                                  version: '1.0-SNAPSHOT'
    }
}

apply plugin: 'commander-gradle-plugin'
group = 'com.electriccloud'
description = "Plugins : <plugin name>"
version = "1.0.0.$buildNumber"

gwt {
	modules 'GWT module name, e.g. ecplugins.<plugin>.ConfigurationManagement'
}

```
