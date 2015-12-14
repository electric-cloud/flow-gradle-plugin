package com.electriccloud.export

import com.electriccloud.export.bindings.AbstractProject
import com.electriccloud.export.bindings.Procedure
import com.electriccloud.export.bindings.Property
import com.electriccloud.export.bindings.PropertySheet

class Project extends AbstractProject {
	def Procedure getProcedure(String name) {
		getProcedures().find { procedure ->
			procedure.procedureName == name
		}
	}

	def Property getForms() {
		Property uiForms = propertySheet.properties.find { property ->
			property.propertyName == 'ui_forms'
		}

		if(!uiForms) {
			uiForms = new Property()
			uiForms.with {
				propertyName = 'ui_forms'
				description = 'Property sheet to hold XML specification for forms'
				propertySheet = new PropertySheet()
			}

			propertySheet.properties.add(uiForms)
		}

		uiForms
	}

	def Property form(String name) {
		forms.propertySheet.properties.find { property ->
			property.propertyName == name
		}
	}

	def removeForm(String name) {
		forms.propertySheet.properties.remove(form(name))
	}

	def addForm(String name, String desc = null) {
		def form = form(name)

		if(!form) {
			form = new Property()
			form.with {
				propertyName = name
				description = desc
				expandable = 1
				value = ''
			}

			forms.propertySheet.properties.add(form)
		}

		form
	}
}