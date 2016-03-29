/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package httpserv;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import plugincore.PluginEngine;

import javax.ws.rs.core.UriBuilder;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Enumeration;

public class httpServerEngine implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(httpServerEngine.class);
    private static String ipAddress;
    private static final URI BASE_URI = getBaseURI();

    public httpServerEngine() {

    }

    public void run() {
        logger.info("Starting up HTTP Server Thread");
        try {
            thed();
        } catch (IOException e) {
            logger.error("run : thed {}", e.getMessage());
        }

        HttpServer httpServer = null;

        try {
            httpServer = startServer();
        } catch (IOException e) {
            logger.error("run : startServer {}", e.getMessage());
        }

        while (PluginEngine.RESTfulActive) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                logger.error("run : while {}", e.getMessage());
            }
        }

        if (httpServer != null) {
            logger.info("Shutting down HTTP Server");
            httpServer.shutdown();
        }
    }

    private static int getPort(int defaultPort) {
        logger.debug("Call to getPort [defaultPort = {}]", defaultPort);
        String port = System.getProperty("jersey.test.port");
        if (null != port) {
            try {
                return Integer.parseInt(port);
            } catch (NumberFormatException e) {
                logger.error("getPort {}", e.getMessage());
            }
        }
        return defaultPort;
    }

    private static URI getBaseURI() {
        logger.debug("Call to getBaseURI");
        InetAddress address;
        try {
            address = InetAddress.getLocalHost();
            ipAddress = address.getHostAddress();
        } catch (UnknownHostException e) {
            logger.error("getBaseURI {}", e.getMessage());
        }
        int httpPort = 32001;
        while (isPortInUse(ipAddress, httpPort)) {
            httpPort++;
        }
        return UriBuilder.fromUri("http://0.0.0.0/").port(getPort(httpPort)).build();
    }

    private static boolean isPortInUse(String hostName, int portNumber) {
        logger.debug("Call to isPortInUse [hostName = {}, portNumber = {}]", hostName, portNumber);
        boolean result;
        try {
            Socket s = new Socket(hostName, portNumber);
            s.close();
            result = true;
        } catch (Exception e) {
            result = false;
        }
        return (result);
    }

    private void thed() throws IOException {
        logger.debug("Call to thed");
        ClassLoader classloader = this.getClass().getClassLoader();
        Enumeration<URL> urls = classloader.getResources("httpserv");
        while (urls.hasMoreElements()) {
            URL param = urls.nextElement();
            System.out.println(param);
        }
    }


    private static HttpServer startServer() throws IOException {
        logger.debug("Call to startServer");

        ResourceConfig config = new ResourceConfig(webREST.class);
        return GrizzlyHttpServerFactory.createHttpServer(BASE_URI, config);
    }
}
