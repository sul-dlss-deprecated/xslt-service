package edu.stanford.sulair.dlss.lyberstructure.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.stanford.sulair.dlss.lyberstructure.*;

@Path("xslt")

public class XsltResource {
	@Context
	UriInfo uriInfo;
	
	private static final Log LOG = LogFactory.getLog( XsltResource.class );

	
	@Path("marc2mods")
	@POST
	@Consumes("application/xml")
	public Response doMarc2ModsTransform(String marc) {
		String mods = marc;
		try {
			//Call xslt API with stylesheet and marc that was posted
			//mods = xslt.(stylesheet, xml);
			
		} catch (Exception e) {
			//Handle any errors
			LOG.error(e);
			return ResourceUtilities.createErrorResponse(e);
		}
		
		LOG.info("helpful log message");
		
		//Send xml response to client
		return Response.status(200).entity(mods).build();
	}


	
	

}
