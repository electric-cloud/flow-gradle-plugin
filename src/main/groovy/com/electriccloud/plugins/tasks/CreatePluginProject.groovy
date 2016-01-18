package com.electriccloud.plugins.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.Task;
import org.gradle.api.tasks.TaskAction

import com.electriccloud.export.ExportUtil;
import com.electriccloud.export.bindings.FormalParameter;
import com.electriccloud.export.bindings.Procedure;
import com.electriccloud.export.bindings.Property
import com.electriccloud.export.bindings.PropertySheet
import com.electriccloud.export.bindings.Step;
import com.electriccloud.form.FormUtil;
import com.electriccloud.manifest.ManifestUtil
import com.electriccloud.plugin.PluginUtil
import com.electriccloud.plugin.bindings.Component
import com.electriccloud.plugin.bindings.Components
import com.electriccloud.plugin.bindings.Plugin
import templates.ProjectTemplate


class CreatePluginProject extends DefaultTask {

	public CreatePluginProject() {
		super
		description = 'Create new plugin project'
		group = 'ElectricFlow templates'
	}

	def createForm(String path) {
		def manifestDocument = ManifestUtil.loadDocument("src/main/resources/project/manifest.xml")
		def manifestBinder = ManifestUtil.binder
		def manifest = manifestBinder.unmarshal(manifestDocument)

		def exportDocument = ExportUtil.loadDocument('src/main/resources/project/project.xml')
		def exportBinder = ExportUtil.binder
		def export = exportBinder.unmarshal(exportDocument)

		def File file = new File(path)
		def formName = file.name.split("\\.", 2)[0]

		manifest.setMapping("ui_forms/${file.name}", "//property[propertyName='ui_forms']/propertySheet/property[propertyName='$formName']/value")

		export.project.removeForm(formName)
		export.project.addForm(formName)

		manifestBinder.updateXML(manifest, manifestDocument.documentElement)
		ManifestUtil.saveDocument(manifestDocument, 'src/main/resources/project/manifest.xml')

		exportBinder.updateXML(export, exportDocument.documentElement)
		ExportUtil.saveDocument(exportDocument, 'src/main/resources/project/project.xml')
	}

	@TaskAction
	def void createPluginProject() {
		try {
			def shortName = project.name.split("-")[1]
			def name = shortName.toLowerCase()

			def template = { templateName ->
				[ template: "/templates/flow/pluginProject/$templateName",
					name: name,
					shortPluginName: shortName,
					pluginName: project.name,
					version: project.version,
					year: Calendar.getInstance().get(Calendar.YEAR) ]
			}

			def filePath = { fileName ->
				[ file: "/templates/flow/pluginProject/$fileName" ]
			}

			ProjectTemplate.fromUserDir {
				'.' {
					'.gitattributes' template('gitattributes')
					'.gitignore' template('gitignore')
					'README.md' template('README.md')
				}

				'pages' { "${project.name}_help.xml" template('pages/plugin_help.xml') }
				'htdocs' { 'pluginhelp.css' template('htdocs/pluginhelp.css') }
				'libs' {
					'annotations-132.839-PATCH1.jar' filePath('libs/annotations-132.839-PATCH1.jar')
					'commander-client-5.0.0-SNAPSHOT.jar' filePath('libs/commander-client-5.0.0-SNAPSHOT.jar')
					'ec-test-5.0.0-SNAPSHOT.jar' filePath('libs/ec-test-5.0.0-SNAPSHOT.jar')
					'gwtp-all-0.8-PATCH5.jar' filePath('libs/gwtp-all-0.8-PATCH5.jar')
				}

				'src/main/resources/META-INF' { 'plugin.xml' template('src/main/resources/META-INF/plugin.xml') }

				'src/main/resources/project' {
					'ec_setup.pl' template('src/main/resources/project/ec_setup.pl')
					'manifest.xml' template('src/main/resources/project/manifest.xml')
					'project.xml' template('src/main/resources/project/project.xml')
				}

				'src/main/resources/project/ui_forms' {
					'createConfigForm.xml' template('src/main/resources/project/ui_forms/createConfigForm.xml')
					'editConfigForm.xml' template('src/main/resources/project/ui_forms/editConfigForm.xml')
					'sayHello.xml' template('src/main/resources/project/ui_forms/sayHello.xml')
				}

				'src/main/resources/project/procedures' { 'sayHello.pl' template('src/main/resources/project/procedures/sayHello.pl') }
			}

			updatePluginDescriptor(name)

			def createFormPath = 'src/main/resources/project/ui_forms/createConfigForm.xml'
			createForm(createFormPath)
			createForm('src/main/resources/project/ui_forms/editConfigForm.xml')

			def form = FormUtil.parseForm(createFormPath)

			Procedure createProcedure = new Procedure()
			createProcedure.with {
				procedureName = 'CreateConfiguration'
				description = "Create a $shortName configuration"
				jobNameTemplate = "${name}-cfg-\$[jobId]"
				projectName = '@PLUGIN_KEY@-@PLUGIN_VERSION@'
				propertySheet = new PropertySheet()
			}

			Step step = new Step()
			step.with {
				stepName = 'CreateConfiguration'
				alwaysRun = 0
				broadcast = 0
				command = ''
				description = "Create a $shortName configuration"
				errorHandling = 'failProcedure'
				exclusive = 0
				postProcessor = 'postp'
				parallel = 0
				releaseExclusive = 0
				resourceName = 'local'
				retries = 0
				shell = 'ec-perl'
				timeLimit = '5'
				timeLimitUnits = 'minutes'
				procedureName = 'CreateConfiguration'
				projectName = '@PLUGIN_KEY@-@PLUGIN_VERSION@'
				propertySheet = new PropertySheet()
			}

			Property stepEditorData = new Property()
			stepEditorData.with {
				propertyName = 'ec_customEditorData'
				propertySheet = new PropertySheet()
			}
			Property cmd = new Property()
			cmd.with {
				propertyName = 'formType'
				expandable = 1
				value = 'command'
			}
			stepEditorData.propertySheet.properties.add(cmd)
			step.propertySheet.properties.add(stepEditorData)

			createProcedure.step.add(step)

			Property visibility = new Property()
			visibility.with {
				propertyName = 'ec_visibility'
				expandable = 1
				value = 'hidden'
			}
			createProcedure.propertySheet.properties.add(visibility)

			Property editorData = new Property()
			editorData.with {
				propertyName = 'ec_customEditorData'
				propertySheet = new PropertySheet()
			}

			Property parameters = new Property()
			parameters.with {
				propertyName = 'parameters'
				propertySheet = new PropertySheet()
			}
			editorData.propertySheet.properties.add(parameters)
			createProcedure.propertySheet.properties.add(editorData)

			form.formElements.each { element ->
				FormalParameter parameter = new FormalParameter()
				parameter.with {
					formalParameterName = element.property
					defaultValue = element.value
					description = element.documentation
					type = element.type
					required = element.required
				}

				createProcedure.formalParameter.add(parameter)

				Property formProperty = new Property()
				formProperty.with {
					propertyName = element.property
					propertySheet = new PropertySheet()
				}

				Property formTypeProperty = new Property()
				formTypeProperty.with {
					propertyName = 'formType'
					expandable = 1
					value = 'standard'
				}

				formProperty.propertySheet.properties.add(formTypeProperty)
				parameters.propertySheet.properties.add(formProperty)
			}

			Procedure deleteProcedure = new Procedure()
			deleteProcedure.with {
				procedureName = 'DeleteConfiguration'
				description = "Delete a $shortName configuration"
				jobNameTemplate = "${name}-cfg-\$[jobId]"
				projectName = '@PLUGIN_KEY@-@PLUGIN_VERSION@'
				propertySheet = new PropertySheet()
			}

			step = new Step()
			step.with {
				stepName = 'DeleteConfiguration'
				alwaysRun = 0
				broadcast = 0
				command = ''
				description = "Delete a $shortName configuration"
				errorHandling = 'failProcedure'
				exclusive = 0
				postProcessor = 'postp'
				parallel = 0
				releaseExclusive = 0
				resourceName = 'local'
				retries = 0
				shell = 'ec-perl'
				timeLimit = '5'
				timeLimitUnits = 'minutes'
				procedureName = 'DeleteConfiguration'
				projectName = '@PLUGIN_KEY@-@PLUGIN_VERSION@'
				propertySheet = new PropertySheet()
			}

			stepEditorData = new Property()
			stepEditorData.with {
				propertyName = 'ec_customEditorData'
				propertySheet = new PropertySheet()
			}
			cmd = new Property()
			cmd.with {
				propertyName = 'formType'
				expandable = 1
				value = 'command'
			}
			stepEditorData.propertySheet.properties.add(cmd)
			step.propertySheet.properties.add(stepEditorData)

			deleteProcedure.step.add(step)

			visibility = new Property()
			visibility.with {
				propertyName = 'ec_visibility'
				expandable = 1
				value = 'hidden'
			}
			deleteProcedure.propertySheet.properties.add(visibility)

			editorData = new Property()
			editorData.with {
				propertyName = 'ec_customEditorData'
				propertySheet = new PropertySheet()
			}

			parameters = new Property()
			parameters.with {
				propertyName = 'parameters'
				propertySheet = new PropertySheet()
			}
			editorData.propertySheet.properties.add(parameters)
			deleteProcedure.propertySheet.properties.add(editorData)

			Property formProperty = new Property()
			formProperty.with {
				propertyName = 'config'
				propertySheet = new PropertySheet()
			}

			Property formTypeProperty = new Property()
			formTypeProperty.with {
				propertyName = 'formType'
				expandable = 1
				value = 'standard'
			}

			formProperty.propertySheet.properties.add(formTypeProperty)
			parameters.propertySheet.properties.add(formProperty)

			FormalParameter parameter = new FormalParameter()
			parameter.with {
				formalParameterName = 'config'
				description = 'The configuration name'
				type = 'entry'
				required = 1
			}

			deleteProcedure.formalParameter.add(parameter)

			def exportDocument = ExportUtil.loadDocument("src/main/resources/project/project.xml")
			def exportBinder = ExportUtil.binder
			def export = exportBinder.unmarshal(exportDocument)
			export.project.procedures.add(createProcedure)
			export.project.procedures.add(deleteProcedure)
			exportBinder.updateXML(export, exportDocument.documentElement)
			ExportUtil.saveDocument(exportDocument, 'src/main/resources/project/project.xml')

			def manifestDocument = ManifestUtil.loadDocument("src/main/resources/project/manifest.xml")
			def manifestBinder = ManifestUtil.binder
			def manifest = manifestBinder.unmarshal(manifestDocument)

			manifest.setMapping("conf/createcfg.pl", "//procedure[procedureName='CreateConfiguration']/step[stepName='CreateConfiguration']/command")
			manifest.setMapping("conf/deletecfg.pl", "//procedure[procedureName='DeleteConfiguration']/step[stepName='DeleteConfiguration']/command")

			manifestBinder.updateXML(manifest, manifestDocument.documentElement)
			ManifestUtil.saveDocument(manifestDocument, 'src/main/resources/project/manifest.xml')

			def resourcesPath = "src/main/resources"
			def projectPath = "$resourcesPath/project"

			template = { templateName ->
				[ template: "/templates/flow/configuration/$templateName",
					name: name,
					shortPluginName: shortName,
					pluginName: project.name,
					version: project.version,
					year: Calendar.getInstance().get(Calendar.YEAR) ]
			}

			ProjectTemplate.fromUserDir {
				'cgi-bin' {
					"${name}.cgi" template('cgi-bin/cgi.tmpl')
					"${name}Monitor.cgi" template('cgi-bin/cgiMonitor.tmpl')
				}

				'pages' {
					'configurations.xml' template('pages/configurations.tmpl.xml')
					'editConfiguration.xml' template('pages/editConfiguration.tmpl.xml')
					'newConfiguration.xml' template('pages/newConfiguration.tmpl.xml')
				}

				"$resourcesPath/ecplugins/${name}" { 'ConfigurationManagement.gwt.xml' template('resources/ConfigurationManagement.gwt.tmpl') }

				"$projectPath/conf" {
					'createcfg.pl' template('resources/createcfg.tmpl')
					'deletecfg.pl' template('resources/deletecfg.tmpl')
				}

				"src/main/java/ecplugins/${name}/client" {
					'ConfigurationList.java' template('java/ConfigurationList.tmpl')
					'ConfigurationManagementFactory.java' template('java/ConfigurationManagementFactory.tmpl')
					'CreateConfiguration.java' template('java/CreateConfiguration.tmpl')
					"${shortName}ConfigList.java" template('java/pluginConfigList.tmpl')
					"${shortName}ConfigListLoader.java" template('java/pluginConfigListLoader.tmpl')
				}
			}
		} catch(MissingPropertyException e) {
			e.printStackTrace()
			println '''An error occured'''
		}
	}

	private updatePluginDescriptor(String name) {
		def pluginBinder = PluginUtil.binder
		def pluginDocument = PluginUtil.loadDocument('src/main/resources/META-INF/plugin.xml')

		Plugin plugin = pluginBinder.unmarshal(pluginDocument)
		plugin.configure = 'configurations.xml'

		if(!plugin.components)
			plugin.components = new Components()

		def component = plugin.components.componentOrCustomType.find { component ->
			(component instanceof Component) && component.name == 'ConfigurationManagement'
		}

		if(!component) {
			component = new Component()
			component.name = 'ConfigurationManagement'
			plugin.components.componentOrCustomType.add(component)
			component.javascript = "war/ecplugins.${name}.ConfigurationManagement/ecplugins.${name}.ConfigurationManagement.nocache.js"
		}

		pluginBinder.updateXML(plugin, pluginDocument.documentElement)
		PluginUtil.saveDocument(pluginDocument, 'src/main/resources/META-INF/plugin.xml')
	}
}
