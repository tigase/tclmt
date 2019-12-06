/**
 * Tigase XMPP Server Command Line Management Tool - bootstrap configuration for all Tigase projects
 * Copyright (C) 2004 Tigase, Inc. (office@tigase.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, version 3 of the License.
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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
import tigase.jaxmpp.j2se.connectors.socket.StreamListener;
import tigase.jaxmpp.j2se.connectors.socket.XMPPDomBuilderHandler;
import tigase.jaxmpp.j2se.xml.J2seElement;
import tigase.xml.SimpleParser;
import tigase.xml.SingletonFactory;

/**
 *
 * @author andrzej
 */
public class UtilityCommandsTest
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
        public UtilityCommandsTest(String testName) {
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
                return new TestSuite(UserCommandsTest.class);
        }

        /**
         * Test for connection-time command
         */
        public void testConnectionTime() throws JaxmppException {                
                String incoming = "<iq type='result' to='test@test' id='10'><command xmlns='http://jabber.org/protocol/commands' node='connection-time'><x xmlns='jabber:x:data'>"
                                        + "<field var='Connections time: ' type='text-multi'>"
                                                + "<value>Longest connection: 3600</value>"
                                                + "<value>Average connection time: 2000</value>"
                                        + "</field></x></command></iq>";
                
                initializeIncoming(incoming);

                tclmt.execute(new String[] { "connection-time" });                

                List<Stanza> results = conn.getOutgoing();
                
                String consoleOut = console.toString();
                
                Stanza e = null;
                assertTrue(!results.isEmpty());
                e = results.remove(0);
                Element command = e.getChildrenNS("command", COMMANDS_XMLNS);
                assertEquals("connection-time", command.getAttribute("node"));
                        
                assertTrue(consoleOut.contains("Longest connection: 3600"));
        }

        /**
         * Test for load-errors command
         */
        public void testLoadErrors() throws JaxmppException {                
                String incoming = "<iq type='result' to='test@test' id='10'><command xmlns='http://jabber.org/protocol/commands' node='load-errors'><x xmlns='jabber:x:data'>"
                                        + "<field var='Errors: ' type='text-multi'>"
                                                + "<value>Processing error 1</value>"
                                                + "<value>Processing error 2</value>"
                                        + "</field></x></command></iq>";
                
                initializeIncoming(incoming);

                tclmt.execute(new String[] { "load-errors" });                

                List<Stanza> results = conn.getOutgoing();
                
                String consoleOut = console.toString();
                
                Stanza e = null;
                assertTrue(!results.isEmpty());
                e = results.remove(0);
                Element command = e.getChildrenNS("command", COMMANDS_XMLNS);
                assertEquals("load-errors", command.getAttribute("node"));
                        
                assertTrue(consoleOut.contains("Processing error 2"));
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
