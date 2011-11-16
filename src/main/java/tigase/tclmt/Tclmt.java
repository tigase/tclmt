/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tigase.tclmt;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import javax.script.Bindings;
import javax.script.ScriptException;
import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.observer.Listener;
import tigase.jaxmpp.core.client.xmpp.modules.presence.PresenceModule;
import tigase.jaxmpp.core.client.xmpp.modules.presence.PresenceModule.PresenceEvent;
import tigase.jaxmpp.core.client.xmpp.stanzas.Presence.Show;
import tigase.jaxmpp.j2se.Jaxmpp;
import tigase.jaxmpp.j2se.connectors.socket.SocketConnector;
import tigase.tclmt.ui.SystemConsole;
import tigase.xml.db.XMLDBException;

/**
 *
 * @author andrzej
 */
public class Tclmt {

        private static final Logger log = Logger.getLogger(Main.class.getCanonicalName());
        private static final String APPNAME = "Tigase XMPP Server Command Line Management Tool";

        private static final List<String> offlineCmds = Arrays.asList( "list", "help" );
        
        private ConsoleIfc console = null;
        private SynchronizedConnection conn = null;
        
        private String cmdId = "list";
        private boolean interactive = false;
        
        private Config config = new Config();        
        private CommandManager cmdManager;
        
        public Tclmt(SynchronizedConnection conn, ConsoleIfc console) {
                this.conn = conn;
                this.console = console; 
        
                conn.getProperties().setUserProperty(Jaxmpp.CONNECTOR_TYPE, "socket");
                conn.getProperties().setUserProperty(SessionObject.RESOURCE, "tclmt");
        }
        
        public void initialize() throws JaxmppException {
                cmdManager = new CommandManager();
                cmdManager.loadScripts(new String[] { "scripts", "src/main/groovy/tigase" });                
                
                interactive = (Boolean) config.get(Config.INTERACTIVE_KEY);
                if (interactive) {
                        console.writeLine(APPNAME + " - ver. " + Main.class.getPackage().getImplementationVersion());

                        if (config.get(Config.JID_KEY) == null) {
                                JID userJid = JID.jidInstance(console.readLine("Username:"));
                                String password = new String(console.readPassword("Password:"));
                        
                                config.put(Config.JID_KEY, userJid.toString());
                                config.put(Config.PASSWORD_KEY, password);
                                config.put(Config.SERVER_NAME_KEY, userJid.getDomain());
                                config.put(Config.SERVER_IP_KEY, userJid.getDomain());
                        }
                }     
         
                PresenceModule presenceModule = conn.getModulesManager().getModule(PresenceModule.class);
                if (presenceModule != null) {
                        presenceModule.addListener(PresenceModule.BeforeInitialPresence,
                                new Listener<PresenceEvent>() {

                                        public void handleEvent(PresenceEvent be) throws JaxmppException {
                                                be.setPriority(-10);
                                                be.setStatus("tclmt");
                                                be.setShow(Show.online);
                                        }
                                });
                }

                if (config.get(Config.JID_KEY) != null) {
                        conn.getProperties().setUserProperty(SessionObject.USER_JID, 
                                JID.jidInstance((String) config.get(Config.JID_KEY)));
                        conn.getProperties().setUserProperty(SessionObject.PASSWORD, 
                                config.get(Config.PASSWORD_KEY));
                        conn.getProperties().setUserProperty(SocketConnector.SERVER_HOST, 
                                config.get(Config.SERVER_IP_KEY));
                }
                
                conn.login(true);

        }
        
        public void execute(String[] args) throws JaxmppException {
                boolean work = true;
                while (work) {
                        if (interactive) {
                                String line = console.readLine("$: ");
                                if ("exit".equals(line)) {
                                        break;
                                }

                                args = line.split(" ");
                        }

                        if (args.length > 0) {
                                cmdId = args[0];
                                args = Arrays.copyOfRange(args, 1, args.length);
                        }
                        try {
                                Bindings bindings = cmdManager.createBindings();
                                bindings.put("args", args);
                                bindings.put("console", console);
                                bindings.put("connection", conn);
                                bindings.put("serverName", config.get(Config.SERVER_NAME_KEY));
                                bindings.put("bindings", bindings);
                                bindings.put("interactive", interactive);

                                Object result = cmdManager.executeScript(cmdId, bindings);
                        }
                        catch (ScriptException ex) {
                                if (!interactive)
                                        throw new JaxmppException("Error executing script " + cmdId + "\n\t" + ex.getMessage(), ex);
                                
                                log.log(Level.WARNING, "Execution exception", ex);
                                console.writeLine("Error executing script " + cmdId + "\n\t" + ex.getMessage());
                        }

                        if (!interactive) {
                                work = false;
                        }

                        console.writeLine("");
                }

                if (conn.isConnected()) {
                        conn.disconnect();
                }
        }
        
        public static void main(String[] args) {
                initLogging();
                
                ConsoleIfc console = new SystemConsole();
                
                try {
                        Tclmt tclmt = new Tclmt(new JaxmppConnection(console), console);
                
                        args = tclmt.config.parseArgs(args);
                        try {
                                tclmt.initialize();
                        }
                        catch (JaxmppException ex) {
//                                log.log(Level.SEVERE, null, ex);
//                                console.writeLine(ex.getMessage());
                                if (args.length != 0 && !offlineCmds.contains(args[0]))
                                        throw ex;
                        }
                
                        try {
                                tclmt.config.sync();
                        }
                        catch (Exception ex) {
                                log.log(Level.WARNING, "configuration not saved due to exception", ex);
                                console.writeLine("configuration not saved due to exception = "+ex.getMessage());
                                ex.printStackTrace();
                        }
                        tclmt.execute(args);
                }
                catch (JaxmppException ex) {
                        log.log(Level.SEVERE, null, ex);
                        console.writeLine(ex.getMessage());
                }
        }        
        
        public String[] parseArgs(String[] args) {
                return config.parseArgs(args);
        }

        private static void initLogging() {
                try {
//                        String config = ".level=ALL\ntigase.xml.level=INFO\ntigase.xmpp.level=INFO\nhandlers=java.util.logging.FileHandler\n"
//                                + "java.util.logging.FileHandler.pattern=tclmt.log\n"
//                                + "java.util.logging.FileHandler.level=ALL";
                        String config = "handlers=java.util.logging.FileHandler\n"
                                + "java.util.logging.FileHandler.level=ALL";
                        final ByteArrayInputStream bis = new ByteArrayInputStream(config.getBytes());

                        LogManager.getLogManager().readConfiguration(bis);
                        bis.close();
                }
                catch (IOException e) {
                        log.log(Level.SEVERE, "Can not configure logManager", e);
                } // end of try-catch
        }
        
}
