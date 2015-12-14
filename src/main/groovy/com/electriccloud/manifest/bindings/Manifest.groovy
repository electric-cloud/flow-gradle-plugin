package com.electriccloud.manifest.bindings

class Manifest extends Fileset {

	def private getMappingNode(String path) {
		files.find { it.path == path }
	}

	/**
	 * Get project's XPath mapping for file
	 * @param path path to file
	 * @return XPath mapping
	 */
	def String getMapping(String path) {
		getMappingNode(path)?.xpath
	}

	/**
	 * Remove project's XPath mapping for file
	 * @param path path to file
	 */
	def removeMapping(String path) {
		files.remove(getMappingNode(path))
	}

	/**
	 * Set project's XPath mapping for file
	 * @param path path to file
	 */
	def setMapping(String p, String xp) {
		def file = getMappingNode(p)

		if(!file) {
			file = new File()
			file.with {
				path = p
				xpath = xp
			}
			files.add(file)
		}

		file.xpath = xp
	}
}
