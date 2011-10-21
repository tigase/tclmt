/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tigase.tclmt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import tigase.jaxmpp.core.client.Base64;
import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.j2se.connectors.socket.SocketConnector;
import tigase.xml.db.NodeNotFoundException;
import tigase.xml.db.XMLDB;
import tigase.xml.db.XMLDBException;

/**
 *
 * @author andrzej
 */
public class Config {

        public static final String JID_KEY = "jid";
        public static final String PASSWORD_KEY = "password";
        public static final String SERVER_NAME_KEY = "server-name";
        public static final String SERVER_IP_KEY = "server-ip";
        
        public static final String INTERACTIVE_KEY = "interactive";
        
        public static final List<String> NOT_PERSISTABLE = Arrays.asList(INTERACTIVE_KEY);
        
        private XMLDB xmldb = null;
        private ConcurrentMap<String,Object> params = new ConcurrentHashMap<String,Object>();

        private String configName = "default";
        private boolean save = false;
        
        public Config() {
                try { 
                        load();                
                } catch (Exception ex) {
                        
                }
                params.put(INTERACTIVE_KEY, false);
        }
        
        public Object get(String key) {
                Object param = params.get(key);
                if (param != null)
                        return param;
                
                if (xmldb != null)
                        param = getData(configName, key);
                
                return param;
        }
        
        public void put(String key, Object value) {
                params.put(key, value);
        }
        
        public void sync() throws IOException {
                if (!save)
                        return;
                
                for (String key : params.keySet()) {
                        if (NOT_PERSISTABLE.contains(key))
                                continue;
        
                        Object value = params.get(key);
                        if (PASSWORD_KEY.equals(key))
                                value = Base64.encodeString((String) value);
                        
                        setData(configName, key, value);
                }

                xmldb.sync();
        }
        
        public Object getData(String config, String key) {
                try {
                        Object value = xmldb.getData(config, key);
                        
                        if (value != null && PASSWORD_KEY.equals(key))
                                value = Base64.decodeString((String) value);
                                
                        return value;
                }
                catch (NodeNotFoundException ex) {
                        return null;
                }                
        }

        public void setData(String config, String key, Object value) throws IOException {
                try {
                        if (value == null) {
                                xmldb.removeData(config, key);
                        }
                        else {
                                xmldb.setData(config, key, value);
                        }
                }
                catch (NodeNotFoundException ex) {
                        try {
                                xmldb.addNode1(config);
                                xmldb.setData(config, key, value);
                        }
                        catch (Exception ex1) {
                                
                        }                        
                }
                xmldb.sync();
        }
        
        public void load() throws IOException, XMLDBException {
                String filePath = System.getProperty("user.home")+"/.tigase/tclmt.xml";
                File f = new File(filePath);
                if (f.exists()) {
                        xmldb = new XMLDB(filePath);
                }
                else {
                        xmldb = XMLDB.createDB(filePath, "tclmt", "config");
                }                
        }
        
        public String getConfigFilePath() {
                return xmldb.getDBFileName();
        }
        
                
        public String[] parseArgs(String[] args) {
                if (args == null || args.length == 0) {
                        return new String[0];
                }

                List<String> otherArgs = new ArrayList<String>();

                for (int i = 0; i < args.length; i++) {
                        if (Params.USER_KEY.equals(args[i])) {
                                if (args.length > i + 1) {
                                        i++;
                                        JID jid = JID.jidInstance(args[i]);
                                        params.put(JID_KEY, jid.toString());
                                        params.putIfAbsent(SERVER_IP_KEY, jid.getDomain());
                                        params.putIfAbsent(SERVER_NAME_KEY, jid.getDomain());
//                                        conn.getProperties().setUserProperty(SessionObject.USER_JID, jid);
//                                        if (serverIP == null)
//                                                conn.getProperties().setUserProperty(SocketConnector.SERVER_HOST, jid.getDomain());
//                                        if (serverName == null)
//                                                serverName = jid.getDomain();
                                }
                        }
                        else if (Params.SERVER_KEY.equals(args[i])) {
                                if (args.length > i + 1) {
                                        i++;
                                        if (!params.containsKey(SERVER_NAME_KEY) || params.get(SERVER_NAME_KEY).equals(params.get(SERVER_IP_KEY))) {
                                                params.put(SERVER_IP_KEY, args[i]);                                        
                                        }
                                        params.put(SERVER_NAME_KEY, args[i]);
                                        //jaxmpp.getProperties().setUserProperty(SocketConnector.SERVER_HOST, args[i]);
//                                        serverName = args[i];
                                }
                        }
                        else if (Params.SERVER_IP_KEY.equals(args[i])) {
                                if (args.length > i + 1) {
                                        i++;
                                        params.put(SERVER_IP_KEY, args[i]);
                                        //jaxmpp.getProperties().setUserProperty(SocketConnector.SERVER_HOST, args[i]);
//                                        serverIP = args[i];
//                                        conn.getProperties().setUserProperty(SocketConnector.SERVER_HOST, serverIP);
                                }                                
                        }
                        else if (Params.PASSWORD_KEY.equals(args[i])) {
                                if (args.length > i + 1) {
                                        i++;
                                        params.put(PASSWORD_KEY, args[i]);
//                                        conn.getProperties().setUserProperty(SessionObject.PASSWORD, args[i]);
                                }
                        }
                        else if (Params.HELP_KEY1.equals(args[i]) || Params.HELP_KEY2.equals(args[i])) {
                                otherArgs.add("help");
                        }                                
                        else if (Params.INTERACTIVE_KEY.equals(args[i])) {
                                params.put(INTERACTIVE_KEY, true);
                        }
                        else if (Params.CONFIG_KEY.equals(args[i])) {
                                if (args.length > i + 1) {
                                        i++;
                                        configName = args[i];                                        
                                }
                        }
                        else if (Params.SAVE_KEY.equals(args[i])) {
                                save = true;
                        }
                        else {
                                otherArgs.add(args[i]);
                        }
                }

                return otherArgs.toArray(new String[otherArgs.size()]);
        }
}
