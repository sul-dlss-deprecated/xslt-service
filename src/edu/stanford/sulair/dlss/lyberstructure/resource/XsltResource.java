package edu.stanford.sulair.dlss.lyberstructure.resource;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import net.sf.saxon.FeatureKeys;
import net.sf.saxon.s9api.*;
import org.w3c.dom.Document;
import org.xml.sax.*;
import org.xml.sax.helpers.XMLFilterImpl;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.io.*;


import edu.stanford.sulair.dlss.lyberstructure.*;

@Path("/")
public class XsltResource {
	@Context
	UriInfo uriInfo;
	
	//Commons Logging has a default LogFactory which follows these steps to get a Log implementation:
	//	* looks for a property called org.apache.commons.logging.Log. If it exists, tries to use that Log implementation
	//	* If that doesn't exist, tries to find log4j on the classpath. If it is there, uses that implementation.
	//	* If log4j is not present, and the JDK is 1.4+, uses Java's own logging implementation
	private static final Log LOG = LogFactory.getLog( XsltResource.class );

    private static String xsltFilePrefix = System.getProperty("xslt.home.dir") + "/xslt_files/";
	
    public static void setTestMode(String mode){
        xsltFilePrefix = System.getProperty("user.dir") + "/xslt_files/";
    }

    private static String xsltURL(String xsltname) {
        if (! xsltname.endsWith(".xsl")) {
            xsltname = xsltname + ".xsl";
        }
        return xsltFilePrefix + xsltname;
    }

    private static String readFileAsString(String filePath) throws java.io.IOException{
        byte[] buffer = new byte[(int) new File(filePath).length()];
        FileInputStream f = new FileInputStream(filePath);
        f.read(buffer);
        return new String(buffer);
    }

    @Path("xsltfile/{xsltname}")
    @GET
    @Produces("application/xml")
    public Response getXsltFile(@PathParam("xsltname") String xsltname) {
        String xsl = null;
		try {
			xsl = readFileAsString(xsltURL(xsltname));
		} catch (Exception e) {
			LOG.error(e);
			return ResourceUtilities.createErrorResponse(e);
		}
		LOG.info("serving: " + xsltname);
		return Response.status(200).entity(xsl).build();
    }

    @Path("transform/{xsltname}")
    @POST
    @Consumes("application/xml")
    @Produces("application/xml")
    public Response doTransform(@PathParam("xsltname") String xsltname, String input) {
        return transformResponse(xsltname, input, null);
    }

	@Path("marc2mods")
	@POST
	@Consumes("application/xml")
	@Produces("application/xml")
	public Response doMarc2ModsTransform(@Context UriInfo uriInfo, String marcxml) {
        String mods = uriInfo.getQueryParameters().getFirst("mods");
        String xslt = uriInfo.getQueryParameters().getFirst("xslt");
        String xsltname = getMarc2ModsXsltName(mods, xslt);
        return transformResponse(xsltname, marcxml, null);
	}

    public static String getMarc2ModsXsltName(String mods, String xslt) {
        if (mods == null) mods = "3.4";
        if (xslt == null) xslt = "1.0";
        String xsltname = null;
        if (mods.equals("3.2")) {
           xsltname = "MARC21slim2MODS3-2";
        } else if (mods.equals("3.3")) {
           xsltname = "MARC21slim2MODS3-3";
        } else if (mods.equals("3.4") && xslt.equals("1.0")) {
           xsltname = "MARC21slim2MODS3-4";
        } else {
           xsltname = "MARC21slim_MODS" + mods.replace('.','-') + "_XSLT" +  xslt.replace('.','-');
        }
        return xsltname;
    }
	
	@Path("dor_marc2mods")
	@POST
	@Consumes("application/xml")
	@Produces("application/xml")
    @Deprecated
	public Response doDorMarc2ModsTransform(@Context UriInfo uriInfo, String marcxml) {
        String xsltname = "DOR_MARC2MODS3-3";
        return transformResponse(xsltname, marcxml, uriInfo);
	}

	@Path("mods2dc")
	@POST
	@Consumes("application/xml")
	@Produces("application/xml")
	public Response doMods2DcTransform(String modsxml) {
        String mods = uriInfo.getQueryParameters().getFirst("mods");
        String xslt = uriInfo.getQueryParameters().getFirst("xslt");
        String xsltname = getMods2DcXsltName(mods, xslt);
        return transformResponse(xsltname, modsxml, null);
	}

    public static String getMods2DcXsltName(String mods, String xslt) {
        if (mods == null) mods = "3.4";
        if (xslt == null) xslt = "1.0";
        String xsltname = null;
        if (mods.equals("3.2") || mods.equals("3.3")) {
           xsltname = "MODS3-22simpleDC";
        } else {
           xsltname = "MODS" + mods.replace('.','-') + "_DC_XSLT" +  xslt.replace('.','-');
        }
        return xsltname;
    }

	@Path("dor_mods2dc")
	@POST
	@Consumes("application/xml")
	@Produces("application/xml")
    @Deprecated
	public Response doDorMods2DcTransform(@Context UriInfo uriInfo, String mods) {
        String xsltname = "DOR_MODS3_DC";
        return transformResponse(xsltname, mods, uriInfo);
	}

    private Response transformResponse(String xsltfile, String inputXml, UriInfo uriInfo)  {
        String response = null;
        try {
            response = runTransform(xsltfile, inputXml, uriInfo);
        } catch (Exception e) {
            LOG.error(e);
            return ResourceUtilities.createErrorResponse(e);
        }
        LOG.info(xsltfile + " transform completed");
        return Response.status(200).entity(response).build();
    }

	private String runTransform (String xsltname, String inputXml, UriInfo uriInfo ) throws SaxonApiException {
		// http://www.saxonica.com/documentation/javadoc/net/sf/saxon/s9api/package-summary.html
		// http://www.saxonica.com/download/S9APIExamples.java
		Processor proc = new Processor(false);
		proc.setConfigurationProperty(FeatureKeys.VERSION_WARNING, false);
        XsltCompiler comp = proc.newXsltCompiler();
        // System.out.println("before xslt fetch");
        XsltExecutable exp = comp.compile(new StreamSource(xsltURL(xsltname)));
        XdmNode source = proc.newDocumentBuilder().build(new StreamSource(new StringReader(inputXml)));
        Serializer out = new Serializer();
        out.setOutputProperty(Serializer.Property.METHOD, "xml");
        // net.sourceforge.lists.saxon-help
        // If you serialize to a string, the requested encoding will be named 
        // in the XML declaration of the output, but it's documentary only. 
        // The serializer doesn't actually encode the characters, 
        // because you have asked for them as characters, not as bytes.
        out.setOutputProperty(Serializer.Property.ENCODING, "UTF8");
        out.setOutputProperty(Serializer.Property.INDENT, "yes");
        // need to rely on StringWriter to do proper encoding
        StringWriter sw = new StringWriter();
        out.setOutputWriter(sw);
        XsltTransformer trans = exp.load();
        trans.setInitialContextNode(source);
        trans.setDestination(out);
        if (uriInfo != null)   {
            MultivaluedMap<String, String> params = uriInfo.getQueryParameters();
            if (params != null) {
                for (Entry<String,List<String>> param : params.entrySet()) {
                    QName qname = new QName(param.getKey());
                    XdmValue value = new XdmAtomicValue ( param.getValue().get(0));
                    trans.setParameter(qname, value);
                }
            }
        }
        trans.transform();
        String outputXml = sw.toString();
        return outputXml;
	}


}
