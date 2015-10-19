package com.electriccloud.plugins

import org.gradle.api.GradleException
import org.gradle.api.Project;
import org.gradle.api.Plugin;
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.compile.JavaCompile

import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPathFactory
import javax.xml.xpath.XPathConstants
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.TransformerFactory

class BuildPlugin implements Plugin<Project> {

	@Override
	public void apply(Project project) {
		project.afterEvaluate {
			project.tasks.processProjectXml.dependsOn('processResources')
			project.tasks.deploy.dependsOn('jar')
			project.tasks.jarWithVersion.dependsOn('jar')

			project.tasks.deploy.setDescription('Deploys plugin on Commander server')
			project.tasks.printPluginVersion.setDescription('Prints current plugin version')

			project.tasks.systemtest.dependsOn('jar')
			project.tasks.systemtest.setDescription('Run system tests on Commander server')
			project.tasks.unittest.setDescription('Run perl unit tests')

			project.tasks.compileGwt.dependsOn('processProjectXml')
		}

		if(!project.getTasksByName('processProjectXml', true).size()) {
			project.task('processProjectXml') << {
				def resourcesPath = "${project.buildDir}/resources/main"
				def projectPath = "$resourcesPath/project"

				def xml = DocumentBuilderFactory.newInstance().newDocumentBuilder()
				def xmlp = XPathFactory.newInstance().newXPath()

				def projectXml = xml.parse("$projectPath/project.xml")
				def replacements = xml.parse("$projectPath/manifest.xml")

				/* For each file entry in manifest, replace content of node
				 * located by xpath expression with file content
				 */
				xmlp.evaluate("//file", replacements.documentElement, XPathConstants.NODESET).each {
					def file = "$projectPath/${xmlp.evaluate('path', it)}"
					def xpath = xmlp.evaluate('xpath', it)
					def nodes = xmlp.evaluate(xpath, projectXml.documentElement, XPathConstants.NODESET)

					nodes.each {
						it.setTextContent(new File(file).text)
					}
				}

				def source = new DOMSource(projectXml)
				def result = new StreamResult(new File("$resourcesPath/META-INF/project.xml"))
				def transformer = TransformerFactory.newInstance().newTransformer()

				transformer.transform(source, result)
			}
		}

		if(!project.getTasksByName('printPluginVersion', true).size()) {
			project.task('printPluginVersion') << { println "${project.version}" }
		}

		if(!project.getTasksByName('jarWithVersion', true).size()) {
			project.task('jarWithVersion') << {
				project.copy {
					from "${project.buildDir}/${project.name}"
					into "${project.buildDir}/${project.name}"
					rename("${project.name}.jar", "${project.name}-${project.version}.jar")
				}
			}
		}

		if(!project.getTasksByName('unittest', true).size()) {
			project.task('unittest') << {
				if(null == project.ntestHome) {
					throw new GradleException('Required TESTFRAMEWORK_HOME environment variable not set.')
				}

				def envmap = [
					'COMMANDER_SERVER': project.commanderServer,
					'COMMANDER_USER': project.commanderUser,
					'COMMANDER_PASSWORD': project.commanderPassword,
					'COMMANDER_HOME': project.commanderHome
				]

				project.exec {
					environment envmap
					commandLine project.ecperl, "-I${project.ntestHome}/perl", "-Isrc/main/resources/project", "-MTest::Harness", "-e", "\$\$Test::Harness::verbose=1; runtests glob('t/*.t');"
				}
			}
		}

		if(!project.getTasksByName('systemtest', true).size()) {
			project.task('systemtest') << {
				if(null == project.ntestHome) {
					throw new GradleException('Required TESTFRAMEWORK_HOME environment variable not set.')
				}

				def outtop = System.env.OUTTOP ? System.env.OUTTOP : "${project.buildDir}/tests"

				def envmap = [
					'COMMANDER_SERVER': project.commanderServer,
					'COMMANDER_USER': project.commanderUser,
					'COMMANDER_PASSWORD': project.commanderPassword,
					'COMMANDER_HOME': project.commanderHome,

					'PLUGINS_ARTIFACTS': project.buildDir,
					'PLUGIN_NAME': project.pluginName,
					'PLUGIN_VERSION': project.version,

					'OUTTOP': outtop
				]

				project.exec {
					environment envmap
					commandLine project.ecperl, 'systemtest/setup.pl', project.commanderServer, outtop
				}

				project.exec {
					environment envmap
					commandLine project.ecperl, "-I${project.ntestHome}/ntest", "-I${project.ntestHome}/perl", "${project.ntestHome}/ntest/ntest", "--target", project.commanderServer, "systemtest"
				}
			}
		}

		if(!project.getTasksByName('deploy', true).size()) {
			project.task('deploy') << {
				def serverOpt = "--server ${project.commanderServer}"

				def envmap = [
					'COMMANDER_SERVER': project.commanderServer,
					'COMMANDER_USER': project.commanderUser,
					'COMMANDER_PASSWORD': project.commanderPassword,
					'COMMANDER_HOME': project.commanderHome
				]

				project.exec {
					environment envmap
					commandLine project.ectool, serverOpt, 'login', project.commanderUser, project.commanderPassword
				}

				project.exec {
					environment envmap
					commandLine project.ectool, serverOpt, 'installPlugin', project.jar.archivePath
				}

				project.exec {
					environment envmap
					commandLine project.ectool, serverOpt, 'promotePlugin', project.pluginName
				}
			}
		}

		project.configure(project) {
			apply plugin: 'java'
			apply plugin: 'eclipse'
			apply plugin: 'gwt-compiler'

			sourceCompatibility = 1.6
			targetCompatibility = 1.6

			defaultTasks 'jarWithVersion'

			ext {
				buildNumber = System.env.BUILD_NUMBER ? System.env.BUILD_NUMBER : '0'
				commanderHome = System.env.COMMANDER_HOME ? System.env.COMMANDER_HOME : '/opt/electriccloud/electriccommander'
				commanderServer = System.env.COMMANDER_SERVER ? System.env.COMMANDER_SERVER : 'localhost'
				commanderUser = System.env.COMMANDER_USER ? System.env.COMMANDER_USER : 'admin'
				commanderPassword = System.env.COMMANDER_PASSWORD ? System.env.COMMANDER_PASSWORD : 'changeme'
				ecperl = commanderHome ? "$commanderHome/bin/ec-perl" : "ec-perl"
				ectool = commanderHome ? "$commanderHome/bin/ectool" : "ectool"
				ntestHome = System.env.TESTFRAMEWORK_HOME
			}

			version =  "${project.version}.${project.buildNumber}"
			ext.pluginName = "${project.name}-${project.version}"


			repositories {
				maven { url 'http://dl.bintray.com/ecpluginsdev/maven' }

				mavenCentral()
				jcenter()
				flatDir { dirs 'libs' }
			}

			dependencies {
				compile group: project.group, name: "commander-sdk", version: "5.+"
				compile group: project.group, name: "commander-client", version: "5.+"
				compile group: project.group, name: "ec_internal", version: "5.+"
				compile group: project.group, name: "ec-test", version: "5.+"

				compile "com.intellij:annotations:132.839-PATCH1"
				compile "com.google.guava:guava-gwt:16.+"
				compile "com.google.gwt.inject:gin:1.5.0"
				compile "com.gwtplatform:gwtp-all:0.8-PATCH5"
				compile group: "com.google.gwt", name: "gwt-dev", version: "2.5.0"
				compile group: "com.google.gwt", name: "gwt-servlet", version: "2.5.0"
				compile group: "com.google.gwt", name: "gwt-user", version: "2.5.0"
			}

			processResources {
				from(projectDir, {
					include 'agent/**'
					include 'htdocs/**'
					include 'cgi-bin/**'
					include 'pages/**'
				})

				/* Apply macros substitution only on subset of files.
				 * This is to ensure, that no binary resources is corrupted by filter
				 */
				[
					"**/*.xml",
					"**/*.css",
					"**/*.pm",
					"**/*.pl",
					"**/*.cgi"
				].each {
					filesMatching(it, {
						filter(org.apache.tools.ant.filters.ReplaceTokens, tokens: [
							PLUGIN_NAME: project.pluginName.toString(),
							PLUGIN_KEY: project.name.toString(),
							PLUGIN_VERSION: project.version.toString()])
					})
				}
			}

			jar {
				dependsOn = [
					processProjectXml,
					compileGwt
				]

				manifest {
					attributes (
							'Implementation-Vendor': 'Electric Cloud, Inc.',
							'Implementation-Title': project.name,
							'Implementation-Version': project.version,
							'Implementation-Vendor-Id': project.group
							)
				}

				outputs.upToDateWhen { false }
				archiveName = "${project.name}.jar"
				destinationDir = new File("${project.buildDir}/${project.name}")
				includeEmptyDirs = false
				excludes = [
					'WEB-INF/**',
					'ecplugins/**',
					'project/**',
					'**/*.pl',
					'**/*.pm'
				]

				from sourceSets.main.output
				from (tasks.compileGwt.outputs, { into('htdocs/war') })
			}

			tasks.withType(JavaCompile) {
				doFirst {
					if (sourceCompatibility == '1.6' && System.env.RTJAR6_PATH != null) {
						options.fork = true
						options.bootClasspath = System.env.RTJAR6_PATH
					}
				}
			}

			gwt {
				// Setup reasonable memory defaults for GWT compiler
				minHeapSize '512M'
				maxHeapSize '1024M'
				gwtVersion '2.5.0'
			}
		}
	}
}
