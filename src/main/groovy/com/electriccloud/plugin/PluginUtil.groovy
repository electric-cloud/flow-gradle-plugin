package com.electriccloud.plugin

import javax.xml.bind.Binder
import javax.xml.bind.JAXBContext
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

import org.w3c.dom.Node

import com.electriccloud.plugin.bindings.Plugin

class PluginUtil {
	
	def static loadDocument(path) {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder()
		db.parse(new File(path))
	}

	def static saveDocument(document, path) {
		TransformerFactory tf = TransformerFactory.newInstance()
		Transformer t = tf.newTransformer()
		t.setOutputProperty(OutputKeys.INDENT, "yes")
		t.setOutputProperty(OutputKeys.STANDALONE, "yes")
		t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

		t.transform(new DOMSource(document), new StreamResult(new File(path)))
	}

	def static Binder<Node> getBinder() {
		def jc = JAXBContext.newInstance(Plugin.class)
		jc.createBinder()
	}
}