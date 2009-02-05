package edu.stanford.sulair.dlss.lyberstructure.resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

import org.custommonkey.xmlunit.Diff;
import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import edu.stanford.sulair.dlss.lyberstructure.AbstractHttpServerTester;

public class XsltResourceTest extends AbstractHttpServerTester {



	@Test
	public void testMarc2Mods() throws IOException, SAXException {
		File marcxmlFile = new File("test/xmlTestData/marcxml-10094.xml");
		String marcxmlData = readToString(marcxmlFile, "UTF-8");
		File modsFile = new File("test/xmlTestData/mods-10094.xml");
		String modsExpected = readToString(modsFile, "UTF-8");
		startServer(XsltResource.class);
		WebResource objResource = Client.create().resource(getUri().path("xslt/marc2mods").build());
		ClientResponse r = objResource.entity(marcxmlData, "application/xml").post(
				ClientResponse.class);
		Assert.assertEquals(200, r.getStatus());
		String modsReturned = r.getEntity(String.class);
		Diff xmlDiff = new Diff(modsExpected, modsReturned);
		Assert.assertTrue(xmlDiff.identical());
	}

	@Test
	public void testMarc2ModsUnicode() throws IOException, SAXException {
		File marcxmlFile = new File("test/xmlTestData/marcxml-6272783.xml");
		String marcxmlData = readToString(marcxmlFile, "UTF-8");
		File modsFile = new File("test/xmlTestData/mods-6272783.xml");
		String modsExpected = readToString(modsFile, "UTF-8");
		startServer(XsltResource.class);
		WebResource objResource = Client.create().resource(getUri().path("xslt/marc2mods").build());
		ClientResponse r = objResource.entity(marcxmlData, "application/xml").post(
				ClientResponse.class);
		Assert.assertEquals(200, r.getStatus());
		String modsReturned = r.getEntity(String.class);
		Diff xmlDiff = new Diff(modsExpected, modsReturned);
		Assert.assertTrue(xmlDiff.identical());
	}

	@Test
	public void testMods2Dc() throws IOException, SAXException {
		File modsFile = new File("test/xmlTestData/mods-10094.xml");
		String modsData = readToString(modsFile, "UTF-8");
		File dcFile = new File("test/xmlTestData/dc-10094.xml");
		String dcExpected = readToString(dcFile, "UTF-8");
		startServer(XsltResource.class);
		WebResource objResource = Client.create().resource(getUri().path("xslt/mods2dc").build());
		ClientResponse r = objResource.entity(modsData, "application/xml").post(
				ClientResponse.class);
		Assert.assertEquals(200, r.getStatus());
		String dcReturned = r.getEntity(String.class);
		Diff xmlDiff = new Diff(dcExpected, dcReturned);
		Assert.assertTrue(xmlDiff.identical());
	}

	/**
	 * Copies characters from reader to writer
	 * http://stackoverflow.com/questions/9913/java-file-io-compendium
	 */
	public static void copy(Reader reader, Writer writer) throws IOException {
		char[] buffer = new char[1024];
		while (true) {
			int r = reader.read(buffer);
			if (r <= 0) {
				break;
			}
			writer.write(buffer, 0, r);
		}
	}

	/**
	 * Copies contents of byte stream to String. Decodes from given encoding.
	 * http://stackoverflow.com/questions/9913/java-file-io-compendium
	 */
	public static String readToString(InputStream in, String encoding) throws IOException {
		Reader reader = new InputStreamReader(in, encoding);
		try {
			StringWriter stringWriter = new StringWriter();
			copy(reader, stringWriter);
			return stringWriter.toString();
		} finally {
			reader.close();
		}
	}

	/**
	 * Copies contents of File to String. Decodes from given encoding.
	 * http://stackoverflow.com/questions/9913/java-file-io-compendium
	 * */
	public static String readToString(File file, String encoding) throws IOException {
		InputStream in = new FileInputStream(file);
		try {
			return readToString(in, encoding);
		} finally {
			in.close();
		}
	}

}
