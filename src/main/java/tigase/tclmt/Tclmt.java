/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tigase.tclmt;

import java.io.ByteArrayInputStream;
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

/**
 *
 * @author andrzej
 */
public class Tclmt {

        private static final Logger log = Logger.getLogger(Main.class.getCanonicalName());
        private static final String APPNAME = "Tigase XMPP Server Command Line Management Tool";
        private static final String USER_KEY = "-u";
        private static final String SERVER_KEY = "-s";
        private static final String PASSWORD_KEY = "-p";
        private static final String INTERACTIVE_KEY = "-i";
        private static final String HELP_KEY1 = "-h";
        private static final String HELP_KEY2 = "-?";

        private ConsoleIfc console = null;
        private SynchronizedConnection conn = null;

        private String cmdId = "list";
        private String serverName = null;
        private boolean interactive = false;
        private CommandManager cmdManager;
        
        public Tclmt(SynchronizedConnection conn, ConsoleIfc console) {
                this.conn = conn;
                this.console = console; 
        
                conn.getProperties().setUserProperty(Jaxmpp.CONNECTOR_TYPE, "socket");
                conn.getProperties().setUserProperty(SessionObject.RESOURCE, "tclmt");
        }
        
        public void initialize() {
                if (interactive) {
                        console.writeLine(APPNAME + " - ver. " + Main.class.getPackage().getImplementationVersion());
                        if (conn.getProperties().getUserProperty(SessionObject.USER_JID) == null) {
                                JID userJid = JID.jidInstance(console.readLine("Username:"));
                                String password = new String(console.readPassword("Password:"));
                        
                                conn.getProperties().setUserProperty(SessionObject.USER_JID, userJid);
                                conn.getProperties().setUserProperty(SessionObject.PASSWORD, password);
                                conn.getProperties().setUserProperty(SocketConnector.SERVER_HOST, userJid.getDomain());
                                if (serverName == null)
                                        serverName = userJid.getDomain();
                        }
                }     
         
                try {
                        PresenceModule presenceModule = conn.getModulesManager().getModule(PresenceModule.class);
                        if (presenceModule != null)
                                presenceModule.addListener(PresenceModule.BeforeInitialPresence,
                                        new Listener<PresenceEvent>() {

                                                public void handleEvent(PresenceEvent be) throws JaxmppException {
                                                        be.setPriority(-10);
                                                        be.setStatus("tclmt");
                                                        be.setShow(Show.online);
                                                }
                                        });

                        conn.login(true);                        
                }
                catch (JaxmppException ex) {
                        log.log(Level.SEVERE, null, ex);
                        console.writeLine(ex.getMessage());
                        if (interactive)
                                return;                        
                }
                
                cmdManager = new CommandManager();
                cmdManager.loadScripts(new String[] { "scripts", "src/main/groovy/tigase" });
                
        }
        
        public void execute(String[] args) throws JaxmppException {
                boolean work = true;
                while (work) {
                        if (interactive) {
                                String line = console.readLine("$: ");
                                if ("exit".equals(line)) {
                                        break;
                                }

//                                int idx = line.indexOf(" ");
//                                if (idx > 0) {
//                                        cmdId = line.substring(0, idx);
//                                }
//                                else {
//                                        cmdId = line;
//                                        line = "";
//                                }
//                                args = line.substring(idx + 1).split(" ");
//                                if (args.length == 1 && args[0].isEmpty()) {
//                                        args = new String[0];
//                                }
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
                                bindings.put("serverName", serverName);
                                bindings.put("bindings", bindings);

                                Object result = cmdManager.executeScript(cmdId, bindings);
                        }
                        catch (ScriptException ex) {
                                if (!interactive)
                                        throw new JaxmppException("Error executing script " + cmdId, ex);
                                
                                log.log(Level.WARNING, "Execution exception", ex);
                                console.writeLine("Error executing script " + cmdId + "\n\t" + ex.getMessage());
                        }

                        if (!interactive) {
                                work = false;
                        }

                        console.writeLine("");
                }

                try {
                        if (conn.isConnected()) {
                                conn.disconnect();
                        }
                }
                catch (JaxmppException ex) {
                        log.log(Level.SEVERE, "received Jaxmpp exception", ex);
                }                
        }
        
        public static void main(String[] args) {
                initLogging();
                
                ConsoleIfc console = new SystemConsole();
                Tclmt tclmt = new Tclmt(new JaxmppConnection(console), console);
                
                args = tclmt.parseArgs(args);
                
                tclmt.initialize();
                
                try {
                        tclmt.execute(args);
                }
                catch (JaxmppException ex) {
                        console.writeLine(ex.getMessage());
                }
        }        
        
        protected String[] parseArgs(String[] args) {
                if (args == null || args.length == 0) {
                        return new String[0];
                }

                List<String> otherArgs = new ArrayList<String>();

                for (int i = 0; i < args.length; i++) {
                        if (USER_KEY.equals(args[i])) {
                                if (args.length > i + 1) {
                                        i++;
                                        JID jid = JID.jidInstance(args[i]);
                                        conn.getProperties().setUserProperty(SessionObject.USER_JID, jid);
                                        conn.getProperties().setUserProperty(SocketConnector.SERVER_HOST, jid.getDomain());
                                        if (serverName == null)
                                                serverName = jid.getDomain();
                                }
                        }
                        else if (SERVER_KEY.equals(args[i])) {
                                if (args.length > i + 1) {
                                        i++;
                                        //jaxmpp.getProperties().setUserProperty(SocketConnector.SERVER_HOST, args[i]);
                                        serverName = args[i];
                                }
                        }
                        else if (PASSWORD_KEY.equals(args[i])) {
                                if (args.length > i + 1) {
                                        i++;
                                        conn.getProperties().setUserProperty(SessionObject.PASSWORD, args[i]);
                                }
                        }
                        else if (HELP_KEY1.equals(args[i]) || HELP_KEY2.equals(args[i])) {
                                otherArgs.add("help");
                        }                                
                        else if (INTERACTIVE_KEY.equals(args[i])) {
                                interactive = true;
                        }
                        else {
                                otherArgs.add(args[i]);
                        }
                }

//                if (otherArgs.size() > 0) {
//                        cmdId = otherArgs.remove(0);
//                }

                return otherArgs.toArray(new String[otherArgs.size()]);
        }

        private static void initLogging() {
                try {
                        String config = "handlers=java.util.logging.FileHandler\n"
                                + "java.util.logging.FileHandler.level = ALL";
                        final ByteArrayInputStream bis = new ByteArrayInputStream(config.getBytes());

                        LogManager.getLogManager().readConfiguration(bis);
                        bis.close();
                }
                catch (IOException e) {
                        log.log(Level.SEVERE, "Can not configure logManager", e);
                } // end of try-catch
        }
        
}
