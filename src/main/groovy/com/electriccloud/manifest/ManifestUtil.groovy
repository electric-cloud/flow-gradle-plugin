package com.electriccloud.manifest

import javax.xml.bind.Binder;
import javax.xml.bind.JAXBContext
import javax.xml.bind.Marshaller
import javax.xml.bind.Unmarshaller
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

import org.eclipse.persistence.jaxb.JAXBContextProperties
import org.w3c.dom.Node;

import com.electriccloud.manifest.bindings.Fileset

class ManifestUtil {
	
	def static loadDocument(path) {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder()
		db.parse(new File(path))
	}

	def static saveDocument(document, path) {
		TransformerFactory tf = TransformerFactory.newInstance()
		Transformer t = tf.newTransformer()
		t.setOutputProperty(OutputKeys.INDENT, "yes");
		t.setOutputProperty(OutputKeys.STANDALONE, "yes");
		t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

		t.transform(new DOMSource(document), new StreamResult(new File(path)))
	}

	def static Binder<Node> getBinder() {
		def classLoader = Thread.currentThread().contextClassLoader
		def iStream = classLoader.getResourceAsStream("com/electriccloud/manifest/bindings/oxm.xml")
		def properties = new HashMap<String, Object>()
		properties.put(JAXBContextProperties.OXM_METADATA_SOURCE, iStream)

		def jc = JAXBContext.newInstance([Fileset.class] as Class[], properties)
		jc.createBinder()
	}
}