package com.electriccloud.plugins

import static org.junit.Assert.*

import javax.xml.bind.Binder;
import javax.xml.bind.Unmarshaller

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.w3c.dom.Node;

import com.electriccloud.export.ExportUtil
import com.electriccloud.export.bindings.ExportedData;


class ProjectTestCase {

	@Test
	public void testGetProcedure() {
		withTestFile '<exportedData><project><procedure><procedureName>test</procedureName></procedure></project></exportedData>', { project, file ->
			assertEquals('test', project.getProcedure('test').procedureName)
		}
	}

	@Test
	public void testAddForm() {
		withTestFile '<exportedData><project><propertySheet></propertySheet></project></exportedData>', { project, file ->
			project.removeForm("test1")
			project.addForm("test1")
			assertEquals("test1", project.form('test1').propertyName)
			project.removeForm("test1")
			assertNull(project.form('test1'))
		}
	}

	def withTestFile(content = null, cl) {
		def file = new File("${System.getProperty('java.io.tmpdir')}/project.xml")
		file.delete()

		if(content) {
			file << content
		}

		try {
			XmlUtil.withXml(file.absolutePath, ExportedData.class, cl)
			cl(binder.unmarshal(ExportUtil.loadDocument(file.absolutePath)).project, file)
		} catch (all) {
			file.delete()
			throw all
		}

		file.delete()
	}
}
