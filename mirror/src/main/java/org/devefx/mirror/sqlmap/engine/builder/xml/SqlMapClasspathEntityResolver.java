package org.devefx.mirror.sqlmap.engine.builder.xml;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class SqlMapClasspathEntityResolver implements EntityResolver {
	@Override
	public InputSource resolveEntity(String publicId, String systemId)
			throws SAXException, IOException {
		byte[] bytes = "<?xml version='1.0' encoding='UTF-8'?>".getBytes();
		return new InputSource(new ByteArrayInputStream(bytes));
	}
}
