package plugincore;

import httpserv.webREST;
import shared.MsgEvent;
import shared.MsgEventType;

import java.util.HashMap;
import java.util.Map;


public class CommandExec {

    public CommandExec() {
        //toats
    }

    public MsgEvent cmdExec(MsgEvent ce) {
        try {

            String callId = ce.getParam("callId-" + PluginEngine.region + "-" + PluginEngine.agent + "-" + PluginEngine.plugin); //unique callId
            if (callId != null) //this is a callback put in RPC hashmap
            {
                PluginEngine.rpcMap.put(callId, ce);
                return null;
            } else if ((ce.getParam("dst_region") != null) && (ce.getParam("dst_agent") != null) && (ce.getParam("dst_plugin") != null)) //its a message for this plugin
            {
                if ((ce.getParam("dst_region").equals(PluginEngine.region)) && (ce.getParam("dst_agent").equals(PluginEngine.agent)) && (ce.getParam("dst_plugin").equals(PluginEngine.plugin))) {
                    //message is for this plugin!
                    if (ce.getMsgType() == MsgEventType.DISCOVER) {
                        //ce.setParams(PluginEngine.config.getPluginConfig());
                        /*
						StringBuilder sb = new StringBuilder();
						sb.append("help\n");
						sb.append("show\n");
						sb.append("show_name\n");
						sb.append("show_version\n");
						sb.append("From AMPQ\n");
						
						ce.setMsgBody(sb.toString());
						*/
                        ce.setMsgBody(PluginEngine.config.getPluginConfigString());

                    } else if (ce.getMsgType() == MsgEventType.EXEC) {
                        //System.out.println("Execute Parameters: " + ce.getParams());
                        if (ce.getParam("cmd").equals("show") || ce.getParam("cmd").equals("?") || ce.getParam("cmd").equals("help")) {
                            StringBuilder sb = new StringBuilder();
                            sb.append("\nPlugin " + PluginEngine.pluginName + " Help\n");
                            sb.append("-\n");
                            sb.append("show\t\t\t\t\t Shows Commands\n");
                            sb.append("show name\t\t\t\t Shows Plugin Name\n");
                            sb.append("show version\t\t\t\t Shows Plugin Version");
                            ce.setMsgBody(sb.toString());
                        } else if (ce.getParam("cmd").equals("show_version")) {
                            ce.setMsgBody(PluginEngine.pluginVersion);
                        } else if (ce.getParam("cmd").equals("show_name")) {
                            ce.setMsgBody(PluginEngine.pluginName);
                        } else if (ce.getParam("cmd").equals("delete_exchange")) {
                            webREST.QueueListener listener = webREST.listeners.get(ce.getParam("exchange"));
                            if (listener != null) {
                                listener.done();
                            }
                            Map<String, String> params = new HashMap<>();
                            params.put("src_region", PluginEngine.region);
                            params.put("src_agent", PluginEngine.agent);
                            params.put("src_plugin", PluginEngine.plugin);
                            params.put("dst_region", PluginEngine.region);
                            params.put("dst_agent", PluginEngine.agent);
                            params.put("controllercmd", "regioncmd");
                            params.put("configtype", "pluginremove");
                            params.put("plugin", ce.getParam("src_plugin"));
                            PluginEngine.msgInQueue.offer(new MsgEvent(MsgEventType.CONFIG, PluginEngine.region, PluginEngine.agent, PluginEngine.plugin, params));
                        } else if (ce.getParam("cmd").equals("execution_log")) {
                            webREST.QueueListener listener = webREST.listeners.get(ce.getParam("exchange"));
                            if (listener != null) {
                                listener.log(ce.getParam("log"));
                            }
                        }
						/*
						else if(ce.getParam("cmd").equals("enablelogconsumer"))
						{
							//ce.setMsgBody(PluginEngine.pluginName);
							PluginEngine.enableLogConsumer(true);
							return null;
						}
						*/
                    } else {
                        ce.setMsgBody("AMPQ Plugin Command [" + ce.getMsgType().toString() + "] unknown");
                    }
                } else {
                    ce.setMsgPlugin(ce.getParam("dst_plugin"));
                    ce.setMsgAgent(ce.getParam("dst_agent"));
                    //MsgEvent cr = PluginEngine.acc.call(ce);
                    //return cr;
                    //call remote
                    return null;
                }

            } else if ((ce.getParam("dst_region") != null) && (ce.getParam("dst_agent") != null) && (ce.getParam("dst_plugin") == null)) {
                //System.out.println("Agent Message for someone else...");
                ce.setMsgPlugin(null);
                ce.setMsgAgent(ce.getParam("dst_agent"));
                ce.setMsgRegion(ce.getParam("dst_region"));

                //MsgEvent cr = PluginEngine.acc.call(ce);
                //return cr;
                //call remote
                return null;

            } else if ((ce.getParam("dst_region") != null) && (ce.getParam("dst_agent") == null) && (ce.getParam("dst_plugin") == null)) //log message
            {
                //System.out.println("This is a log message");
                PluginEngine.logOutQueue.offer(ce);
                return null;
            }
            return ce;

        } catch (Exception ex) {
            ex.printStackTrace();
            MsgEvent ee = PluginEngine.clog.getError("Agent : CommandExec : Error" + ex.toString());
            System.out.println("MsgType=" + ce.getMsgType().toString());
            System.out.println("Region=" + ce.getMsgRegion() + " Agent=" + ce.getMsgAgent() + " plugin=" + ce.getMsgPlugin());
            System.out.println("params=" + ce.getParamsString());
            return ee;
        }
    }

}
