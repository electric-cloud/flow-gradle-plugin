package com.electriccloud.plugins.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.Task;
import org.gradle.api.tasks.TaskAction

import com.electriccloud.export.ExportUtil
import com.electriccloud.manifest.ManifestUtil

import groovy.lang.Closure;;


class CreateForm extends DefaultTask {

	def formFileName
	def formDescription
	
	public File getFormFile() {
		new File(project.hasProperty('formFile') ? project.property('formFile') : formFileName)
	}
	
	public String getFormDescription() {
		project.hasProperty('formDescription') ? project.property('formDescription') : formDescription
	}
	
	public CreateForm() {
		super
		description = 'Create new FormXML definition'
		group = 'ElectricFlow templates'
	}

	@Override
	def Task configure(Closure closure) {
		formFileName = null
		formDescription = null
		super.configure(closure)
	}
	
	@TaskAction
	def void createFormXml() {

		try {
			def manifestDocument = ManifestUtil.loadDocument("src/main/resources/project/manifest.xml")
			def manifestBinder = ManifestUtil.binder
			def manifest = manifestBinder.unmarshal(manifestDocument)
			
			def exportDocument = ExportUtil.loadDocument('src/main/resources/project/project.xml')
			def exportBinder = ExportUtil.binder
			def export = exportBinder.unmarshal(exportDocument)

			def File file = getFormFile()
			def formName = file.name.split("\\.", 2)[0]

			project.copy {
				from 	file.canonicalPath
				into 'src/main/resources/project/ui_forms'
				include file.name
			}
			
			manifest.setMapping("ui_forms/${file.name}", "//property[propertyName='ui_forms']/propertySheet/property[propertyName='$formName']/value")

			export.project.removeForm(formName)
			export.project.addForm(formName, getFormDescription())

			manifestBinder.updateXML(manifest, manifestDocument.documentElement)
			ManifestUtil.saveDocument(manifestDocument, 'src/main/resources/project/manifest.xml')

			exportBinder.updateXML(export, exportDocument.documentElement)
			ExportUtil.saveDocument(exportDocument, 'src/main/resources/project/project.xml')
		} catch(MissingPropertyException e) {
			println '''One of required properties missed: formFile'''
		}
	}
}
