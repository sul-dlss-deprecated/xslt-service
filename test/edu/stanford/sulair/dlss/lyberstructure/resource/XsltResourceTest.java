package edu.stanford.sulair.dlss.lyberstructure.resource;


import org.junit.Test;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import edu.stanford.sulair.dlss.lyberstructure.AbstractHttpServerTester;

public class XsltResourceTest extends AbstractHttpServerTester {
	

    
	public XsltResourceTest(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	@Test
    public void testMarc2Mods() {
		startServer(XsltResource.class);
        WebResource objResource = Client.create().resource(getUri().path("xslt/marc2mods").build());
        
        ClientResponse r = objResource.entity("<marc>stuff</marc>",
        		"application/xml").post(ClientResponse.class);
        assertEquals(200, r.getStatus());
        String mods = r.getEntity(String.class);
        assertEquals("<marc>stuff</marc>", mods);

    }
	
}
