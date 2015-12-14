package com.electriccloud.form

import javax.xml.bind.Binder
import javax.xml.bind.JAXBContext
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

import org.w3c.dom.Node

import com.electriccloud.form.bindings.Editor

class FormUtil {
	
	def static loadDocument(path) {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder()
		db.parse(new File(path))
	}

	def static Editor parseForm(path) {
		def document = loadDocument(path)
		binder.unmarshal(document)
	}
	
	def static Binder<Node> getBinder() {
		def jc = JAXBContext.newInstance(Editor.class)
		jc.createBinder()
	}
}