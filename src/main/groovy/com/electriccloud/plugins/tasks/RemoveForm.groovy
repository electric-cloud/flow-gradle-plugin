package com.electriccloud.plugins.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import com.electriccloud.export.ExportUtil;
import com.electriccloud.export.Project
import com.electriccloud.manifest.ManifestUtil
import com.electriccloud.manifest.bindings.Manifest;;;


class RemoveForm extends DefaultTask {

	public RemoveForm() {
		super
		description = 'Remove FormXML definition'
		group = 'ElectricFlow templates'
	}

	@TaskAction
	def void removeFormXml() {
		try {
			def manifestFile = new File("src/main/resources/project/manifest.xml")
			def exportFile = new File("src/main/resources/project/project.xml")

			def manifest = ManifestUtil.unmarshaller.unmarshal(manifestFile)
			def export = ExportUtil.unmarshaller.unmarshal(exportFile)

			def formName = project.property("formName")
			File uiForm = new File("src/main/resources/project/ui_forms/${formName}.xml")
			uiForm.delete()

			manifest.removeMapping("ui_forms/${formName}.xml")

			export.project.removeForm(formName)

			ManifestUtil.marshaller.marshal(manifest, manifestFile)
			ExportUtil.marshaller.marshal(export,exportFile)
		} catch(MissingPropertyException e) {
			println '''One of required properties missed: formName'''
		}
	}
}
