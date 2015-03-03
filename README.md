# flow-gradle-plugin
A gradle plugin that houses the logic to build our ElectricFlow plugins.

To build plugin use ./gradlew uploadArchives

To use plugin:

```groovy
buildscript {
    repositories {
        maven {
            url uri('../flow-gradle-plugin/repo')
        }
        jcenter()
    }
    dependencies {
        classpath group: 'com.electriccloud.plugins', name: 'commander-gradle-plugin',
                                  version: '1.0-SNAPSHOT'
    }
}

apply plugin: 'commander-gradle-plugin'
```
