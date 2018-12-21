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

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;
import tigase.jaxmpp.core.client.xmpp.stanzas.StanzaType;
import tigase.jaxmpp.j2se.connectors.socket.StreamListener;
import tigase.jaxmpp.j2se.connectors.socket.XMPPDomBuilderHandler;
import tigase.jaxmpp.j2se.xml.J2seElement;
import tigase.xml.SimpleParser;
import tigase.xml.SingletonFactory;

/**
 * Unit test for simple App.
 */
public class CompRepoCommandsTest
        extends TestCase {

        private static final String COMMANDS_XMLNS = "http://jabber.org/protocol/commands";
        
        private SimpleParser parser = SingletonFactory.getParserInstance();
        
        private TestConsole console = null;
        private TestSynchronizedConnection conn = null;
        private Tclmt tclmt = null;
        /**
         * Create the test case
         *
         * @param testName name of the test case
         */
        public CompRepoCommandsTest(String testName) {
                super(testName);
        }

        @Override
        public void setUp() throws JaxmppException {
                console = new TestConsole();
                conn = new TestSynchronizedConnection();
                tclmt = new Tclmt(conn, console);
                tclmt.initialize();
                
        }
        
        @Override
        public void tearDown() {
                tclmt = null;
                try {
                        conn.disconnect();
                } catch (Exception ex) { }
                conn = null;
                console = null;                
        }
        
        /**
         * @return the suite of tests being tested
         */
        public static Test suite() {
                return new TestSuite(CompRepoCommandsTest.class);
        }

        /**
         * Test for comp-repo-item-add command
         */
        public void testItemAdd() throws JaxmppException {                  
                String incoming = "<iq from=\"vhost-man@zeus\" type=\"result\" to=\"admin@zeus/Psi\" id=\"ab2fa\">"
                        + "<command xmlns=\"http://jabber.org/protocol/commands\" status=\"executing\" node=\"comp-repo-item-add\">"
                        + "<x xmlns=\"jabber:x:data\" type=\"form\">"
                        + "<field var=\"Domain name\">"
                        + "<value/>"
                        + "</field>"
                        + "<field type=\"boolean\" var=\"Enabled\">"
                        + "<value>true</value>"
                        + "</field>"
                        + "<field type=\"boolean\" var=\"Anonymous enabled\">"
                        + "<value>true</value>"
                        + "</field>"
                        + "<field type=\"boolean\" var=\"In-band registration\">"
                        + "<value>true</value>"
                        + "</field>"
                        + "<field var=\"Max users\">"
                        + "<value>0</value>"
                        + "</field>"
                        + "<field var=\"Other parameters\">"
                        + "<value/>"
                        + "</field>"
                        + "<field var=\"Owner\">"
                        + "<value>admin@zeus</value>"
                        + "</field>"
                        + "<field var=\"Administrators\">"
                        + "<value/>"
                        + "</field>"
                        + "<field type=\"hidden\" var=\"command-marker\">"
                        + "<value>command-marker</value>"
                        + "</field>"
                        + "</x>"
                        + "<actions execute=\"complete\">"
                        + "<complete/>"
                        + "</actions>"
                        + "</command>"
                        + "</iq>"
                        + "<iq type='result' to='test@test' id='10'><command xmlns='http://jabber.org/protocol/commands' node='comp-repo-add-item'><x xmlns='jabber:x:data'>"
                                        + "<field var='note' type='text-single'><value>Operation successful</value></field></x></command></iq>";
                
                initializeIncoming(incoming);

                tclmt.execute(new String[] { "comp-repo-item-add", "vhost-man", "test.com", "true", "true", "true", "10", "", "owner@test.com", "admin@test.com" });                
                
                List<Stanza> results = conn.getOutgoing();
                
                String consoleOut = console.toString();
                
                Stanza e = null;
                assertTrue(!results.isEmpty());
                e = results.remove(0);
                Element command = e.getChildrenNS("command", COMMANDS_XMLNS);
                assertEquals("comp-repo-item-add", command.getAttribute("node"));
                e = results.remove(0);
                command = e.getChildrenNS("command", COMMANDS_XMLNS);
                assertEquals("comp-repo-item-add", command.getAttribute("node"));
                List<Element> fields = command.getChildrenNS("x", "jabber:x:data").getChildren("field");
                assertTrue(!fields.isEmpty());
                for (Element field : fields) {
                        if ("Domain name".equals(field.getAttribute("var"))) {
                                assertEquals("test.com", field.getFirstChild().getValue());
                        }
                        else if ("Enabled".equals(field.getAttribute("var"))) {
                                assertEquals("1", field.getFirstChild().getValue());
                        }
                        else if ("Max users".equals(field.getAttribute("var"))) {
                                assertEquals("10", field.getFirstChild().getValue());
                        }
                        else if ("Owner".equals(field.getAttribute("var"))) {
                                assertEquals("owner@test.com", field.getFirstChild().getValue());
                        }
                        else if ("Administrators".equals(field.getAttribute("var"))) {
                                assertEquals("admin@test.com", field.getFirstChild().getValue());
                        }
                }
                        
                assertTrue(consoleOut.contains("successful"));
        }
        
        /**
         * Test for comp-repo-item-update command
         */
        public void testItemUpdate() throws JaxmppException {                  
                String incoming = "<iq from=\"vhost-man@zeus\" type=\"result\" to=\"admin@zeus/Psi\" id=\"ab2fa\">"
                        + "<command xmlns=\"http://jabber.org/protocol/commands\" status=\"executing\" node=\"comp-repo-item-update\">"
                        + "<x xmlns=\"jabber:x:data\" type=\"form\">"
                        + "<field var=\"Domain name\">"
                        + "<value/>"
                        + "</field>"
                        + "<field type=\"boolean\" var=\"Enabled\">"
                        + "<value>true</value>"
                        + "</field>"
                        + "<field type=\"boolean\" var=\"Anonymous enabled\">"
                        + "<value>true</value>"
                        + "</field>"
                        + "<field type=\"boolean\" var=\"In-band registration\">"
                        + "<value>true</value>"
                        + "</field>"
                        + "<field var=\"Max users\">"
                        + "<value>0</value>"
                        + "</field>"
                        + "<field var=\"Other parameters\">"
                        + "<value/>"
                        + "</field>"
                        + "<field var=\"Owner\">"
                        + "<value>admin@zeus</value>"
                        + "</field>"
                        + "<field var=\"Administrators\">"
                        + "<value/>"
                        + "</field>"
                        + "<field type=\"hidden\" var=\"command-marker\">"
                        + "<value>command-marker</value>"
                        + "</field>"
                        + "</x>"
                        + "<actions execute=\"complete\">"
                        + "<complete/>"
                        + "</actions>"
                        + "</command>"
                        + "</iq>"
                        + "<iq type='result' to='test@test' id='10'><command xmlns='http://jabber.org/protocol/commands' node='comp-repo-item-update'><x xmlns='jabber:x:data'>"
                                        + "<field var='note' type='text-single'><value>Operation successful</value></field></x></command></iq>";
                
                initializeIncoming(incoming);

                tclmt.execute(new String[] { "comp-repo-item-update", "vhost-man", "test.com", "test.com", "true", "true", "true", "10", "", "owner@test.com", "admin@test.com" });                
                
                List<Stanza> results = conn.getOutgoing();
                
                String consoleOut = console.toString();
                
                Stanza e = null;
                assertTrue(!results.isEmpty());
                e = results.remove(0);
                Element command = e.getChildrenNS("command", COMMANDS_XMLNS);
                assertEquals("comp-repo-item-update", command.getAttribute("node"));
                e = results.remove(0);
                command = e.getChildrenNS("command", COMMANDS_XMLNS);
                assertEquals("comp-repo-item-update", command.getAttribute("node"));
                List<Element> fields = command.getChildrenNS("x", "jabber:x:data").getChildren("field");
                assertTrue(!fields.isEmpty());
                for (Element field : fields) {
                        if ("Domain name".equals(field.getAttribute("var"))) {
                                assertEquals("test.com", field.getFirstChild().getValue());
                        }
                        else if ("Enabled".equals(field.getAttribute("var"))) {
                                assertEquals("1", field.getFirstChild().getValue());
                        }
                        else if ("Max users".equals(field.getAttribute("var"))) {
                                assertEquals("10", field.getFirstChild().getValue());
                        }
                        else if ("Owner".equals(field.getAttribute("var"))) {
                                assertEquals("owner@test.com", field.getFirstChild().getValue());
                        }
                        else if ("Administrators".equals(field.getAttribute("var"))) {
                                assertEquals("admin@test.com", field.getFirstChild().getValue());
                        }
                }
                        
                assertTrue(consoleOut.contains("successful"));
        }
        
        /**
         * Test for comp-repo-item-remove command
         */
        public void testItemRemove() throws JaxmppException {                  
                String incoming = "<iq type='result' to='test@test' id='10'><command xmlns='http://jabber.org/protocol/commands' node='comp-repo-add-item'><x xmlns='jabber:x:data'>"
                                        + "<field var='note' type='text-single'><value>Operation successful</value></field></x></command></iq>";
                
                initializeIncoming(incoming);

                tclmt.execute(new String[] { "comp-repo-item-remove", "vhost-man", "test.com" });                
                
                List<Stanza> results = conn.getOutgoing();
                
                String consoleOut = console.toString();
                
                Stanza e = null;
                assertTrue(!results.isEmpty());
                e = results.remove(0);
                Element command = e.getChildrenNS("command", COMMANDS_XMLNS);
                assertEquals("comp-repo-item-remove", command.getAttribute("node"));
                List<Element> fields = command.getChildrenNS("x", "jabber:x:data").getChildren("field");
                assertTrue(!fields.isEmpty());
                for (Element field : fields) {
                        if ("item-list".equals(field.getAttribute("var"))) {
                                assertEquals("test.com", field.getFirstChild().getValue());
                        }
                }
                        
                assertTrue(consoleOut.contains("successful"));
        }

        /**
         * Test for comp-repo-item-remove command
         */
        public void testReload() throws JaxmppException {                  
                String incoming = "<iq type='result' to='test@test' id='10'><command xmlns='http://jabber.org/protocol/commands' node='comp-repo-reload'><x xmlns='jabber:x:data'>"
                                        + "<field var='note' type='text-single'><value>Operation successful</value></field></x></command></iq>";
                
                initializeIncoming(incoming);

                tclmt.execute(new String[] { "comp-repo-reload", "vhost-man" });                
                
                List<Stanza> results = conn.getOutgoing();
                
                String consoleOut = console.toString();
                
                Stanza e = null;
                assertTrue(!results.isEmpty());
                e = results.remove(0);
                Element command = e.getChildrenNS("command", COMMANDS_XMLNS);
                assertEquals("comp-repo-reload", command.getAttribute("node"));
                        
                assertTrue(consoleOut.contains("successful"));
        }

        public void initializeIncoming(String incoming) throws XMLException {                
                XMPPDomBuilderHandler handler = new XMPPDomBuilderHandler(new StreamListener() {

                        public void nextElement(tigase.xml.Element e) {
                                try {
                                        conn.setIncoming(Stanza.create(new J2seElement(e)));
                                } catch (JaxmppException ex) {
                                        Logger.getLogger(CompRepoCommandsTest.class.getName()).log(Level.SEVERE, null, ex);
                                }
                        }

                        public void xmppStreamClosed() {
                        }

                        public void xmppStreamOpened(Map<String, String> map) {
                        }
                        
                });

                char[] data = incoming.toCharArray();
                parser.parse(handler, data, 0, data.length);
        }
}
