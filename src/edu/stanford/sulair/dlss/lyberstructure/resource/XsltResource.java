package edu.stanford.sulair.dlss.lyberstructure.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
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

	//private static final String xsltUrlPrefix = "http://cosimo.stanford.edu/services/dor-transforms/";
	
	@Path("marc2mods")
	@POST
	@Consumes("application/xml")
	@Produces("application/xml")
	public Response doMarc2ModsTransform(String marc) {
		String mods;
		try {
			String xsltURL= "file:DLF_STANFORD_MARC2MODS3-3.xsl";
			mods=runTransform(xsltURL,marc);
			
		} catch (Exception e) {
			//Handle any errors
			LOG.error(e);
			return ResourceUtilities.createErrorResponse(e);
		}
		
		LOG.info("helpful log message");
		
		//Send xml response to client
		return Response.status(200).entity(mods).build();
	}
	
	@Path("dor_marc2mods")
	@POST
	@Consumes("application/xml")
	@Produces("application/xml")
	public Response doDorMarc2ModsTransform(@Context UriInfo uriInfo, String marc) {
		String mods;
		try {
			// the XSLT requires Params
			MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
			String xsltURL="file:DOR_MARC2MODS3-3.xsl";
			mods=runTransform(xsltURL,marc, queryParams);
			
		} catch (Exception e) {
			//Handle any errors
			LOG.error(e);
			return ResourceUtilities.createErrorResponse(e);
		}
		
		LOG.info("helpful log message");
		
		//Send xml response to client
		return Response.status(200).entity(mods).build();
	}


	@Path("mods2dc")
	@POST
	@Consumes("application/xml")
	@Produces("application/xml")
	public Response doMods2DcTransform(String mods) {
		String dc;
		try {
			String xsltURL= "file:MODS3-22simpleDC.xsl";
			dc=runTransform(xsltURL,mods);
			
		} catch (Exception e) {
			//Handle any errors
			LOG.error(e);
			return ResourceUtilities.createErrorResponse(e);
		}
		
		LOG.info("helpful log message");
		
		//Send xml response to client
		return Response.status(200).entity(dc).build();
	}
	
	@Path("dor_mods2dc")
	@POST
	@Consumes("application/xml")
	@Produces("application/xml")
	public Response doDorMods2DcTransform(@Context UriInfo uriInfo, String mods) {
		String dc;
		try {
			MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
			String xsltURL="file:DOR_MODS3_DC.xsl";
			dc=runTransform(xsltURL,mods, queryParams);
			
		} catch (Exception e) {
			//Handle any errors
			LOG.error(e);
			return ResourceUtilities.createErrorResponse(e);
		}
		
		LOG.info("helpful log message");
		
		//Send xml response to client
		return Response.status(200).entity(dc).build();
	}

	@Path("dor2contentmap")
	@POST
	@Consumes("application/xml")
	@Produces("application/xml")
	public Response doDor2ContentMapTransform(String objMd) {
		String dc;
		try {
			String xsltURL= "file:ContentMapfromDorMetadata.xsl";
			dc=runTransform(xsltURL,objMd);
			
		} catch (Exception e) {
			//Handle any errors
			LOG.error(e);
			return ResourceUtilities.createErrorResponse(e);
		}
		
		LOG.info("helpful log message");
		
		//Send xml response to client
		return Response.status(200).entity(dc).build();
	}
	
	@Path("dor2tm")
	@POST
	@Consumes("application/xml")
	@Produces("application/xml")
	public Response doDor2TmTransform(String objMd) {
		String dc;
		try {
			String xsltURL= "TMfromDorMetadata.xsl";
			dc=runTransform(xsltURL,objMd);
			
		} catch (Exception e) {
			//Handle any errors
			LOG.error(e);
			return ResourceUtilities.createErrorResponse(e);
		}
		
		LOG.info("helpful log message");
		
		//Send xml response to client
		return Response.status(200).entity(dc).build();
	}	

	private String runTransform (String xsltURL, String inputXml) throws SaxonApiException {
		return runTransform(xsltURL, inputXml, null);
	}
	
	private String runTransform (String xsltURL, String inputXml, MultivaluedMap<String,String> params ) throws SaxonApiException {
		// http://www.saxonica.com/documentation/javadoc/net/sf/saxon/s9api/package-summary.html
		// http://www.saxonica.com/download/S9APIExamples.java
		Processor proc = new Processor(false);
		proc.setConfigurationProperty(FeatureKeys.VERSION_WARNING, false);
        XsltCompiler comp = proc.newXsltCompiler();
        XsltExecutable exp = comp.compile(new StreamSource(xsltURL));
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
        if (params != null) {
        	for (Entry<String,List<String>> param : params.entrySet()) {
        		QName qname = new QName(param.getKey());
        		XdmValue value = new XdmAtomicValue ( param.getValue().get(0));
        		trans.setParameter(qname, value);
        	}
        }
        trans.transform();
        String outputXml = sw.toString();
        return outputXml;
	}
	

	
	

}
