package httpserv;

import java.io.InputStream;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/")
public class webGUI {

    @GET
    @Path("{subResources:.*}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getfile(@PathParam("subResources") String subResources) {
        if (subResources.length() == 0) {
            subResources = "/index.html";
        } else {
            subResources = "/" + subResources;
        }
        InputStream in;
        try {
            in = getClass().getResourceAsStream(subResources);
            if (in == null) {
                in = getClass().getResourceAsStream("/404.html");
                return Response.status(Response.Status.NOT_FOUND).entity(in).build();
            }
        } catch (Exception ex) {
            System.out.println(ex.toString());
            in = getClass().getResourceAsStream("/500.html");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(in).build();
        }

        if (subResources.endsWith(".html")) {
            return Response.ok(in, MediaType.TEXT_HTML_TYPE).build();
        } else if (subResources.endsWith(".js")) {
            return Response.ok(in, "text/javascript").build();
        } else if (subResources.endsWith(".css") || subResources.endsWith(".less")) {
            return Response.ok(in, "text/css").build();
        } else if (subResources.endsWith(".svg")) {
            return Response.ok(in, "image/svg+xml").build();
        } else if (subResources.endsWith(".woff")) {
            return Response.ok(in, "application/font-woff").build();
        } else {
            return Response.ok(in, MediaType.APPLICATION_OCTET_STREAM)
                .header("Content-Disposition", "attachment; filename=\"" + "somefile" + "\"") //optional
                .build();
        }
    }

}

