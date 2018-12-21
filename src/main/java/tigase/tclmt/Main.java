/*
 * Tigase XMPP Server Command Line Management Tool
 * Copyright (C) 2004-2018 "Tigase, Inc." <office@tigase.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. Look for COPYING file in the top folder.
 * If not, see http://www.gnu.org/licenses/.
 */

package tigase.tclmt;

import tigase.tclmt.ui.SystemConsole;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import javax.script.Bindings;
import javax.script.ScriptException;
import tigase.jaxmpp.core.client.BareJID;
import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.observer.Listener;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.modules.presence.PresenceModule;
import tigase.jaxmpp.core.client.xmpp.modules.presence.PresenceModule.PresenceEvent;
import tigase.jaxmpp.core.client.xmpp.stanzas.Presence.Show;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;
import tigase.jaxmpp.j2se.Jaxmpp;
import tigase.jaxmpp.j2se.connectors.socket.SocketConnector;

public class Main {

        private static final Logger log = Logger.getLogger(Main.class.getCanonicalName());
        private static final String APPNAME = "Tigase XMPP Server Command Line Management Tool";
        private static final String USER_KEY = "-u";
        private static final String SERVER_KEY = "-s";
        private static final String PASSWORD_KEY = "-p";
        private static final String INTERACTIVE_KEY = "-i";
        private static final String HELP_KEY1 = "-h";
        private static final String HELP_KEY2 = "-?";
        //private static User user = null;
        private static ConsoleIfc console = null;
        private static Jaxmpp jaxmpp = new Jaxmpp() {

                public Stanza sendSync(Stanza stanza) throws XMLException, JaxmppException {
                        return this.sendSync(stanza, null);
                }

                public Stanza sendSync(Stanza stanza, final Long timeout) throws XMLException, JaxmppException {
                        ScriptCallback callback = new ScriptCallback(jaxmpp);
                        console.writeLine("awaiting response...");
                        send(stanza, callback);

                        try {
                                synchronized (jaxmpp) {
                                        if (timeout != null) {
                                                jaxmpp.wait(timeout);
                                        }
                                        else {
                                                jaxmpp.wait();
                                        }
                                }
                        }
                        catch (InterruptedException ex) {
                                log.log(Level.SEVERE, null, ex);
                        }

                        return callback.getResult();
                }
        };
        private static String cmdId = "list";
        private static String serverName = null;
        private static boolean interactive = false;

        public static void main(String[] args) {
                initLogging();
                console = new SystemConsole();

                jaxmpp.getProperties().setUserProperty(Jaxmpp.CONNECTOR_TYPE, "socket");
                jaxmpp.getProperties().setUserProperty(SessionObject.RESOURCE, "tclmt");

                args = parseArgs(args);

                if (interactive) {
                        console.writeLine(APPNAME + " - ver. " + Main.class.getPackage().getImplementationVersion());
                        if (jaxmpp.getProperties().getUserProperty(SessionObject.USER_BARE_JID) == null) {
                                BareJID userJid = BareJID.bareJIDInstance(console.readLine("Username:"));
                                String password = new String(console.readPassword("Password:"));
                        
                                jaxmpp.getProperties().setUserProperty(SessionObject.USER_BARE_JID, userJid);
                                jaxmpp.getProperties().setUserProperty(SessionObject.PASSWORD, password);
                                jaxmpp.getProperties().setUserProperty(SocketConnector.SERVER_HOST, userJid.getDomain());
                                if (serverName == null)
                                        serverName = userJid.getDomain();
                        }
                }
                
                try {
                        initializeConnection();
                }
                catch (JaxmppException ex) {
                        log.log(Level.SEVERE, null, ex);
                        console.writeLine(ex.getMessage());
                        if (interactive)
                                return;
                }

                CommandManager cmdManager = new CommandManager();
                cmdManager.loadScripts(new String[] { "scripts" });

                boolean work = true;
                while (work) {
                        if (interactive) {
                                String line = console.readLine("$: ");
                                if ("exit".equals(line)) {
                                        break;
                                }

                                int idx = line.indexOf(" ");
                                if (idx > 0) {
                                        cmdId = line.substring(0, idx);
                                }
                                else {
                                        cmdId = line;
                                        line = "";
                                }
                                args = line.substring(idx + 1).split(" ");
                                if (args.length == 1 && args[0].isEmpty()) {
                                        args = new String[0];
                                }
                        }

                        try {
                                Bindings bindings = cmdManager.createBindings();
                                bindings.put("args", args);
                                bindings.put("console", console);
                                bindings.put("connection", jaxmpp);
                                bindings.put("serverName", serverName);
                                bindings.put("bindings", bindings);

                                Object result = cmdManager.executeScript(cmdId, bindings);

                        }
                        catch (ScriptException ex) {
                                log.log(Level.WARNING, "Execution exception", ex);
                                console.writeLine("Error executing script " + cmdId + "\n\t" + ex.getMessage());
                        }

                        if (!interactive) {
                                work = false;
                        }

                        console.writeLine("");
                }

                try {
                        if (jaxmpp.isConnected()) {
                                jaxmpp.disconnect();
                        }
                }
                catch (JaxmppException ex) {
                        log.log(Level.SEVERE, null, ex);
                }
        }

        private static String[] parseArgs(String[] args) {
                if (args == null || args.length == 0) {
                        return new String[0];
                }

                List<String> otherArgs = new ArrayList<String>();

                for (int i = 0; i < args.length; i++) {
                        if (USER_KEY.equals(args[i])) {
                                if (args.length > i + 1) {
                                        i++;
                                        BareJID jid = BareJID.bareJIDInstance(args[i]);
                                        jaxmpp.getProperties().setUserProperty(SessionObject.USER_BARE_JID, jid);
                                        jaxmpp.getProperties().setUserProperty(SocketConnector.SERVER_HOST, jid.getDomain());
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
                                        jaxmpp.getProperties().setUserProperty(SessionObject.PASSWORD, args[i]);
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

                if (otherArgs.size() > 0) {
                        cmdId = otherArgs.remove(0);
                }

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

        private static void initializeConnection() throws JaxmppException {
                jaxmpp.getModulesManager().getModule(PresenceModule.class).addListener(PresenceModule.BeforeInitialPresence,
                        new Listener<PresenceEvent>() {

                                public void handleEvent(PresenceEvent be) throws JaxmppException {
                                        be.setPriority(-10);
                                        be.setStatus("tclmt");
                                        be.setShow(Show.online);
                                }
                                
                        });
                
                jaxmpp.login(true);
        }
}
