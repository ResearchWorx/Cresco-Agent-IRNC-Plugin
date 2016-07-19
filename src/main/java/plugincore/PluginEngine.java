package plugincore;

import channels.plugins.RPCCall;
import httpserv.httpServerEngine;
import httpserv.webREST;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shared.Clogger;
import shared.MsgEvent;
import shared.MsgEventType;
import shared.PluginImplementation;

import java.io.File;
import java.io.FileInputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.jar.Attributes;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

public class PluginEngine {
    private static final Logger logger = LoggerFactory.getLogger(PluginEngine.class);

    private static CommandExec commandExec;
    private static WatchDog wd;

    static Clogger clog;
    static ConcurrentLinkedQueue<MsgEvent> logOutQueue;
    static PluginConfig config;
    static String pluginName;
    static String pluginVersion;

    public static String plugin;
    public static String agent;
    public static String region;
    public static boolean RESTfulActive;
    public static RPCCall rpcc;
    public static Map<String, MsgEvent> rpcMap;
    //public static ConcurrentLinkedQueue<MsgEvent> msgOutQueue;
    public static ConcurrentLinkedQueue<MsgEvent> msgInQueue;

    public PluginEngine() {
        pluginName = "cresco-agent-irnc-restful-plugin";
    }

    public static void shutdown() {
        logger.info("Plugin Shutdown : Agent=" + agent + "pluginname=" + plugin);
        RESTfulActive = false;
        for (webREST.QueueListener listener : webREST.listeners.values()) {
            listener.close();
        }
        wd.timer.cancel(); //prevent rediscovery
        try {
            Thread.sleep(2000);
            MsgEvent me = new MsgEvent(MsgEventType.CONFIG, region, null, null, "disabled");
            me.setParam("src_region", region);
            me.setParam("src_agent", agent);
            me.setParam("src_plugin", plugin);
            me.setParam("dst_region", region);

            //msgOutQueue.offer(me);
            msgInQueue.offer(me);
            //PluginEngine.rpcc.call(me);
            logger.debug("Sent disable message");
        } catch (Exception ex) {
            String msg2 = "Plugin Shutdown Failed: Agent=" + agent + "pluginname=" + plugin;
            //clog.error(msg2);
        }
    }

    public static String getName() {
        return pluginName;
    }

    public static String getVersion() //This should pull the version information from jar Meta data
    {
        String version;
        try {
            String jarFile = PluginImplementation.class.getProtectionDomain().getCodeSource().getLocation().getPath();
            File file = new File(jarFile.substring(5, (jarFile.length() - 2)));
            FileInputStream fis = new FileInputStream(file);
            @SuppressWarnings("resource")
            JarInputStream jarStream = new JarInputStream(fis);
            Manifest mf = jarStream.getManifest();

            Attributes mainAttribs = mf.getMainAttributes();
            version = mainAttribs.getValue("Implementation-Version");
        } catch (Exception ex) {
            String msg = "Unable to determine Plugin Version " + ex.toString();
            //clog.error(msg);
            version = "Unable to determine Version";
        }

        return pluginName + "." + version;

    }

    //steps to init the plugin
    public boolean initialize(ConcurrentLinkedQueue<MsgEvent> outQueue, ConcurrentLinkedQueue<MsgEvent> inQueue,
                              SubnodeConfiguration configObj, String newRegion, String newAgent, String newPlugin) {
        logger.trace("Call to initialize");
        logger.trace("Building rpcMap");
        rpcMap = new ConcurrentHashMap<>();
        logger.trace("Building rpcc");
        rpcc = new RPCCall();

        logger.trace("Building commandExec");
        commandExec = new CommandExec();


        logger.trace("Building msgOutQueue");
        ConcurrentLinkedQueue<MsgEvent> msgOutQueue = outQueue;
        logger.trace("Setting msgInQueue");
        msgInQueue = inQueue; //messages to agent should go here

        logger.trace("Setting Region");
        region = newRegion;
        logger.trace("Setting Agent");
        agent = newAgent;
        logger.trace("Setting Plugin");
        plugin = newPlugin;

        try {
            logger.trace("Building logOutQueue");
            logOutQueue = new ConcurrentLinkedQueue<>(); //create our own queue

            logger.trace("Checking msgInQueue");
            if (msgInQueue == null) {
                System.out.println("MsgInQueue==null");
                return false;
            }

            logger.trace("Building new PluginConfig");
            config = new PluginConfig(configObj);

            //create logger
            //clog = new Clogger(msgInQueue, region, agent, plugin); //send logs directly to outqueue

            String startmsg = "Initializing Plugin: Region=" + region + " Agent=" + agent + " plugin=" + plugin + " version" + getVersion();
            //clog.log(startmsg);

            System.out.println("Starting RESTChannel Plugin");
            RESTfulActive = true;
            try {
                System.out.println("Starting HTTP Service");
                new Thread(new httpServerEngine()).start();
                logger.trace("HTTP Service started");
            } catch (Exception ex) {
                System.out.println("Unable to Start HTTP Service : " + ex.toString());
                return false;
            }

            logger.trace("Starting WatchDog");
            wd = new WatchDog();
            logger.trace("Successfully started plugin");
            return true;
        } catch (Exception ex) {
            String msg = "ERROR IN PLUGIN: : Region=" + region + " Agent=" + agent + " plugin=" + plugin + " " + ex.getMessage();
            logger.error("initialize {}", msg);
            //clog.error(msg);
            return false;
        }
    }

    public static void msgIn(MsgEvent me) {
        final MsgEvent ce = me;
        try {
            Thread thread = new Thread() {
                public void run() {
                    try {
                        MsgEvent re = commandExec.cmdExec(ce);
                        if (re != null) {
                            re.setReturn(); //reverse to-from for return
                            msgInQueue.offer(re); //send message back to queue
                        }

                    } catch (Exception ex) {
                        System.out.println("IRNC Plugin : PluginEngine : msgIn Run Thread: " + ex.toString());
                        ex.printStackTrace();
                    }
                }
            };
            thread.start();
        } catch (Exception ex) {
            System.out.println("IRNC Plugin : PluginEngine : msgIn : " + ex.toString());
        }
    }
}
