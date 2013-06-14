package edu.stanford.sulair.dlss.lyberstructure.resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

import javax.ws.rs.core.MediaType;

import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.Before;
import org.xml.sax.SAXException;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import edu.stanford.sulair.dlss.lyberstructure.AbstractHttpServerTester;

public class XsltResourceTests extends AbstractHttpServerTester {

    @Before
    public void setup(){
        XsltResource.setTestXsltPrefixes(getUri().build().toString());
    }

    @Test
    public void transform() throws IOException, SAXException {
        File marcxmlFile = new File("test/xmlTestData/marcxml-6272783.xml");
        String marcxmlData = readToString(marcxmlFile, "UTF-8");
        File modsFile = new File("test/xmlTestData/mods-6272783a.xml");
        String modsExpected = readToString(modsFile, "UTF-8");
        startServer(XsltResource.class);
        WebResource objResource = Client.create().resource(getUri().path("transform/MARC21slim2MODS3-3").build());
        ClientResponse r = objResource.entity(marcxmlData, "application/xml").post(
                ClientResponse.class);
        Assert.assertEquals(200, r.getStatus());
        Assert.assertEquals(MediaType.APPLICATION_XML_TYPE, r.getType());
        String modsReturned = r.getEntity(String.class);
        //System.out.println(modsReturned);
        Diff xmlDiff = new Diff(modsExpected, modsReturned);
        //DetailedDiff detailDiff = new DetailedDiff(xmlDiff);
        //System.out.println(detailDiff.toString());
        Assert.assertTrue(xmlDiff.identical());
    }

	@Test
	public
    void dorMarc2Mods() throws IOException, SAXException {
		File marcxmlFile = new File("test/xmlTestData/marcxml-887408.xml");
		String marcxmlData = readToString(marcxmlFile, "UTF-8");
		File modsFile = new File("test/xmlTestData/mods-887408.xml");
		String modsExpected = readToString(modsFile, "UTF-8");
		startServer(XsltResource.class);
		WebResource objResource = Client.create().resource(getUri().path("dor_marc2mods")
				.queryParam("catkey", "887408")
				.queryParam("barcode", "36105036457831")
				.queryParam("druid", "druid:xxxxxxxxxxx")
				.queryParam("test", "true")
				.build());
		ClientResponse r = objResource.entity(marcxmlData, "application/xml").post(
				ClientResponse.class);
		Assert.assertEquals(200, r.getStatus());
		Assert.assertEquals(MediaType.APPLICATION_XML_TYPE, r.getType());
		String modsReturned = r.getEntity(String.class);
		Diff xmlDiff = new Diff(modsExpected, modsReturned);
		DetailedDiff detailedDiff = new DetailedDiff(xmlDiff);
		System.out.println(detailedDiff.toString());
		Assert.assertTrue(xmlDiff.identical());
	}

    @Test
    public void marc2ModsXsltName() {
        String mods = null;
        String xslt = null;
        Assert.assertEquals("MARC21slim2MODS3-4",XsltResource.getMarc2ModsXsltName(mods,xslt));
        mods = "3.4";
        Assert.assertEquals("MARC21slim2MODS3-4",XsltResource.getMarc2ModsXsltName(mods,xslt));
        xslt = "2.0";
        Assert.assertEquals("MARC21slim_MODS3-4_XSLT2-0",XsltResource.getMarc2ModsXsltName(mods,xslt));
        mods = "3.3";
        Assert.assertEquals("MARC21slim2MODS3-3",XsltResource.getMarc2ModsXsltName(mods,xslt));
    }

	@Test
	public void marc2Mods33() throws IOException, SAXException {
		File marcxmlFile = new File("test/xmlTestData/marcxml-6272783.xml");
		String marcxmlData = readToString(marcxmlFile, "UTF-8");
		File modsFile = new File("test/xmlTestData/mods-6272783a.xml");
		String modsExpected = readToString(modsFile, "UTF-8");
		startServer(XsltResource.class);
		WebResource objResource = Client.create().resource(getUri().path("marc2mods").queryParam("mods", "3.3").build());
		ClientResponse r = objResource.entity(marcxmlData, "application/xml").post(
				ClientResponse.class);
		Assert.assertEquals(200, r.getStatus());
		Assert.assertEquals(MediaType.APPLICATION_XML_TYPE, r.getType());
		String modsReturned = r.getEntity(String.class);
        //System.out.println(modsReturned);
		Diff xmlDiff = new Diff(modsExpected, modsReturned);
        //DetailedDiff detailDiff = new DetailedDiff(xmlDiff);
		//System.out.println(detailDiff.toString());
		Assert.assertTrue(xmlDiff.identical());
	}

    @Test
	public void marc2Mods34() throws IOException, SAXException {
		File marcxmlFile = new File("test/xmlTestData/4084372-marc.xml");
		String marcxmlData = readToString(marcxmlFile, "UTF-8");
		File modsFile = new File("test/xmlTestData/4084372-mods-3-4.xml");
		String modsExpected = readToString(modsFile, "UTF-8");
		startServer(XsltResource.class);
		WebResource objResource = Client.create().resource(getUri().path("marc2mods").queryParam("mods", "3.4").build());
		ClientResponse r = objResource.entity(marcxmlData, "application/xml").post(
				ClientResponse.class);
		Assert.assertEquals(200, r.getStatus());
		Assert.assertEquals(MediaType.APPLICATION_XML_TYPE, r.getType());
		String modsReturned = r.getEntity(String.class);
        //System.out.println(modsReturned);
		Diff xmlDiff = new Diff(modsExpected, modsReturned);
        //DetailedDiff detailDiff = new DetailedDiff(xmlDiff);
		//System.out.println(detailDiff.toString());
		Assert.assertTrue(xmlDiff.identical());
	}

    @Test
    public void Mods2DcXsltName() {
        String mods = null;
        String xslt = null;
        Assert.assertEquals("MODS3-4_DC_XSLT1-0",XsltResource.getMods2DcXsltName(mods,xslt));
        mods = "3.4";
        Assert.assertEquals("MODS3-4_DC_XSLT1-0",XsltResource.getMods2DcXsltName(mods,xslt));
        xslt = "2.0";
        Assert.assertEquals("MODS3-4_DC_XSLT2-0",XsltResource.getMods2DcXsltName(mods,xslt));
        mods = "3.3";
        Assert.assertEquals("MODS3-22simpleDC",XsltResource.getMods2DcXsltName(mods,xslt));
    }

    @Test
	public void mods2dc() throws IOException, SAXException {
		File modsxmlFile = new File("test/xmlTestData/4084372-mods-3-4.xml");
		String modsxmlData = readToString(modsxmlFile, "UTF-8");
		File dcFile = new File("test/xmlTestData/4084372-dc.xml");
		String dcExpected = readToString(dcFile, "UTF-8");
		startServer(XsltResource.class);
		WebResource objResource = Client.create().resource(getUri().path("mods2dc").queryParam("mods", "3.4").build());
		ClientResponse r = objResource.entity(modsxmlData, "application/xml").post(
				ClientResponse.class);
		Assert.assertEquals(200, r.getStatus());
		Assert.assertEquals(MediaType.APPLICATION_XML_TYPE, r.getType());
		String modsReturned = r.getEntity(String.class);
        //System.out.println(modsReturned);
		Diff xmlDiff = new Diff(dcExpected, modsReturned);
        //DetailedDiff detailDiff = new DetailedDiff(xmlDiff);
		//System.out.println(detailDiff.toString());
        XMLUnit.setIgnoreWhitespace(true);
		Assert.assertTrue(xmlDiff.identical());
        //Unless we reset this option, subsequent Diffs will fail with error:
        // java.lang.IllegalArgumentException: When a DOMSource is used, saxon9-dom.jar must be on the classpath
        XMLUnit.setIgnoreWhitespace(false);
	}

	@Test
	public void dorMods2Dc() throws IOException, SAXException {
		File modsFile = new File("test/xmlTestData/mods-10094.xml");
		String modsData = readToString(modsFile, "UTF-8");
		File dcFile = new File("test/xmlTestData/dc-10094.xml");
		String dcExpected = readToString(dcFile, "UTF-8");
		startServer(XsltResource.class);
		WebResource objResource = Client.create().resource(getUri().path("dor_mods2dc")
				.queryParam("identifiers", "id1:value1,id2:value2,id3:value3")
				.build());
		ClientResponse r = objResource.entity(modsData, "application/xml").post(
				ClientResponse.class);
		Assert.assertEquals(200, r.getStatus());
		Assert.assertEquals(MediaType.APPLICATION_XML_TYPE, r.getType());
		String dcReturned = r.getEntity(String.class);
		// System.out.println(dcReturned);
		Diff xmlDiff = new Diff(dcExpected, dcReturned);
		// DetailedDiff detailDiff = new DetailedDiff(xmlDiff);
		// System.out.println(detailDiff.toString());
		Assert.assertTrue(xmlDiff.identical());
	}

    @Test
    public void getXsltFile(){
        startServer(XsltResource.class);
		WebResource objResource = Client.create().resource(getUri().path("xsltfile/MARC21slim2MODS3-4.xsl")
				.build());
		ClientResponse r = objResource.get(ClientResponse.class);
        String xslReturned = r.getEntity(String.class);
		Assert.assertEquals(200, r.getStatus());
		Assert.assertEquals(MediaType.APPLICATION_XML_TYPE, r.getType());
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
