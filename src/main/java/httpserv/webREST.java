package httpserv;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import plugincore.PluginEngine;
import shared.MsgEvent;
import shared.MsgEventType;

import com.google.gson.Gson;

@Path("/API")
public class webREST {
    private static final Logger logger = LoggerFactory.getLogger(webREST.class);

    @GET
    public Response index(
            @QueryParam("type") String type,
            @QueryParam("region") String region,
            @QueryParam("agent") String agent,
            @QueryParam("plugin") String plugin,
            @QueryParam("paramkey") final List<String> paramskey,
            @QueryParam("paramvalue") final List<String> paramsvalue) {
        logger.debug("Call to index");
        try {
            MsgEvent me;
            try {
                if ((paramskey.size() != paramsvalue.size())) {
                    logger.debug("index : Params key:value size does not match");
                    return Response.status(Response.Status.BAD_REQUEST).entity("Params key:value size does not match").build();
                }
                if (paramskey.isEmpty()) {
                    logger.debug("index : No params values provided in the request");
                    return Response.status(Response.Status.BAD_REQUEST).entity("No params values provided in the request").build();
                }
                Map<String, String> params = new HashMap<>();
                for (String param : paramskey) {
                    params.put(param, paramsvalue.get(paramskey.indexOf(param)));
                }
                me = new MsgEvent(MsgEventType.valueOf(type.toUpperCase()), region, agent, plugin, params);
                if ((region != null) && (agent != null) && (plugin != null)) {
                    me.setSrc(PluginEngine.region, PluginEngine.agent, PluginEngine.plugin);
                    me.setDst(region, agent, plugin);
                } else if ((region != null) && (agent != null)) {
                    me.setSrc(PluginEngine.region, PluginEngine.agent, PluginEngine.plugin);
                    me.setParam("dst_region", region);
                    me.setParam("dst_agent", agent);
                }
            } catch (Exception ex) {
                logger.error("index : Building Message {}", ex.getMessage());
                return Response.status(Response.Status.BAD_REQUEST).entity("bad request").build();
            }

            try {
                MsgEvent ce = PluginEngine.rpcc.call(me);
                String returnString;

                if (ce != null) {
                    Gson gson = new Gson();
                    returnString = gson.toJson(ce);
                } else {
                    returnString = "ok";
                }
                return Response.ok(returnString, MediaType.TEXT_PLAIN_TYPE).build();
            } catch (Exception ex) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Internal Server Error").build();
            }
        } catch (Exception ex) {
            logger.error("index {}", ex.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Internal Server Error").build();
        }
    }
}

