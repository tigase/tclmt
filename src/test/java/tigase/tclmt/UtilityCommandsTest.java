/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tigase.tclmt;

import java.util.List;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;
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
                XMPPDomBuilderHandler handler = new XMPPDomBuilderHandler(null);
                char[] data = incoming.toCharArray();
                parser.parse(handler, data, 0, data.length);
                for (tigase.xml.Element e : handler.getParsedElements()) {
                        conn.setIncoming(Stanza.create(new J2seElement(e)));
                }
        }        
}
