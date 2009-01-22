package edu.stanford.sulair.dlss.lyberstructure.resource;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.ws.rs.core.Response;

public class ResourceUtilities {
	public static Response createErrorResponse(Exception e) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		return Response.serverError().entity(sw.toString()).build();
	}
}
