package com.electriccloud.plugins

import static org.junit.Assert.*

import javax.xml.bind.Binder
import javax.xml.bind.Unmarshaller

import org.junit.After
import org.junit.Before
import org.junit.Test

import com.electriccloud.manifest.ManifestUtil

import org.w3c.dom.Node


class ManifestTestCase {
	Binder<Node> binder

	@Before
	public void setUp() throws Exception {
		binder = ManifestUtil.binder
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testExistingManifest() {
		withTestFile '<fileset/>', { manifest, file ->
		}
	}


	@Test
	public void testIncorrectXmlManifest() {
		try {
			withTestFile 'Incorrect xml', {
			}
		} catch(all) {}
	}

	@Test
	public void testGetMapping() {
		withTestFile '<fileset><file><path>manifest.xml</path><xpath>//project</xpath></file></fileset>', { manifest, file ->
			assertEquals('//project', manifest.getMapping('manifest.xml'))
		}
	}

	@Test
	public void testGetNonExistentMapping() {
		withTestFile '<fileset><file><path>manifest.xml</path><xpath>//project</xpath></file></fileset>', { manifest, file ->
			assertNull(manifest.getMapping('manifest1.xml'))
		}
	}

	@Test
	public void testRemoveMapping() {
		withTestFile '<fileset><file><path>manifest.xml</path><xpath>//project</xpath></file></fileset>', { manifest, file ->
			manifest.removeMapping("manifest.xml")
			assertNull(manifest.getMapping('manifest.xml'))
		}
	}

	@Test
	public void testSetMapping() {
		withTestFile '<fileset></fileset>', { manifest, file ->
			manifest.setMapping('manifest.xml', '//project')
			assertEquals('//project', manifest.getMapping('manifest.xml'))
		}
	}

	@Test
	public void testSetExistingMapping() {
		withTestFile '<fileset><file><path>manifest.xml</path><xpath>//project</xpath></file></fileset>', { manifest, file ->
			manifest.setMapping("manifest.xml", "//procedure")
			assertEquals('//procedure', manifest.getMapping('manifest.xml'))
		}
	}

	def withTestFile(content = null, cl) {
		def file = new File("${System.getProperty('java.io.tmpdir')}/manifest.xml")
		file.delete()
		file.createNewFile()

		if(content) {
			file << content
		}

		try {
			cl(binder.unmarshal(ManifestUtil.loadDocument(file.absolutePath)), file)
		} catch (all) {
			file.delete()
			throw all
		}

		file.delete()
	}
}
