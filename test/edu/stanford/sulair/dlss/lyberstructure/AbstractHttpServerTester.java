/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://jersey.dev.java.net/CDDL+GPL.html
 * or jersey/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at jersey/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package edu.stanford.sulair.dlss.lyberstructure;

import java.io.IOException;
import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import org.junit.After;

import com.sun.jersey.api.container.ContainerFactory;
import com.sun.jersey.api.container.httpserver.HttpServerFactory;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public abstract class AbstractHttpServerTester{

    public static final String CONTEXT = "/context";
    private HttpServer server;
    private int port = TestHelper.getEnvVariable("JERSEY_HTTP_PORT", 9998);


    public UriBuilder getUri() {
        return UriBuilder.fromUri("http://localhost").port(port).path(CONTEXT);
    }

    public void startServer(Class... resources) {
        start(ContainerFactory.createContainer(HttpHandler.class, resources));
    }

    public void startServer(ResourceConfig config) {
        start(ContainerFactory.createContainer(HttpHandler.class, config));
    }

    public void startServer(String packageName) {
        start(ContainerFactory.createContainer(HttpHandler.class, packageName));
    }

    public void start(HttpHandler handler) {
        if (server != null) {
            stopServer();
        }
        
        // want to make the information available in hudson cli output
        System.out.println("Starting HttpServer port number = " + port);

        URI u = UriBuilder.fromUri("http://localhost").port(port).path(CONTEXT).
                build();

        try {
            server = HttpServerFactory.create(u, handler);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        server.start();
        System.out.println(System.getProperty("user.dir"));
        System.out.println("Started HttpServer");

        int timeToSleep = TestHelper.getEnvVariable("JERSEY_HTTP_SLEEP",2000);
        if (timeToSleep > 0) {
            System.out.println("Sleeping for " + timeToSleep + " ms");
            try {
                // Wait for the server to start
                Thread.sleep(timeToSleep);
            } catch (InterruptedException ex) {
                System.out.println("Sleeping interrupted: " + ex.getLocalizedMessage());
            }
        }
    }

    public void stopServer() {
        if (server != null) {
            System.out.println("Stopping HttpServer port number = " + server.getAddress().getPort());
            server.stop(TestHelper.getEnvVariable("JERSEY_HTTP_STOPSEC", 1));
            System.out.println("Stopped HttpServer");
            int timeToSleep = TestHelper.getEnvVariable("JERSEY_HTTP_SLEEP", 3000);
            if (timeToSleep > 0) {
                System.out.println("Sleeping after stopping for " + timeToSleep + " ms");
                try {
                    // Wait for the server to start
                    Thread.sleep(timeToSleep);
                } catch (InterruptedException ex) {
                    System.out.println("Sleeping interrupted: " + ex.getLocalizedMessage());
                }
            }
        }
    }

    @After
    public void tearDown() {
        stopServer();
    }
    

}
