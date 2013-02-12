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
public class GenericCommandsTest
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
        public GenericCommandsTest(String testName) {
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
                return new TestSuite(GenericCommandsTest.class);
        }

        public void testGenericCommand1() throws JaxmppException {                
                String incoming = "<iq to='admin@test/tclmt' type='result' id='1lj5d' from='sess-man@test'><command status='executing' node='http://jabber.org/protocol/admin#get-active-users' xmlns='http://jabber.org/protocol/commands'>"
                        + "<x type='form' xmlns='jabber:x:data'><title>Requesting List of Active Users</title><instructions>Fill out this form to request the active users of this service.</instructions>"
                        + "<field var='FORM_TYPE' type='hidden'><value>http://jabber.org/protocol/admin</value></field><field label='The domain for the list of active users' var='domainjid' type='list-single'><value/><option label='test'><value>test</value></option><option label='test2'><value>test2</value></option></field>"
                        + "<field label='Maximum number of items to show' var='max_items' type='list-single'><value/><option label='25'><value>25</value></option><option label='50'><value>50</value></option><option label='75'><value>75</value></option><option label='100'><value>100</value></option><option label='150'><value>150</value></option><option label='200'><value>200</value></option><option label='None'><value>None</value></option></field>"
                        + "</x><actions execute='complete'><complete/></actions></command></iq>"
                        + ""
                        + "<iq to='admin@test/tclmt' type='result' id='Zktjj' from='sess-man@test'><command status='completed' node='http://jabber.org/protocol/admin#get-active-users' xmlns='http://jabber.org/protocol/commands'>"
                        + "<x type='result' xmlns='jabber:x:data'><field var='Users: 1' type='text-multi'><value>admin@test</value></field></x></command></iq>";
                
                initializeIncoming(incoming);

                tclmt.execute(new String[] { "remote", "sess-man", "http://jabber.org/protocol/admin#get-active-users", "test", "25" });                

                List<Stanza> results = conn.getOutgoing();
                
                String consoleOut = console.toString();
                
                Stanza e = null;
                assertTrue(!results.isEmpty());
                e = results.remove(0);
                Element command = e.getChildrenNS("command", COMMANDS_XMLNS);
                assertEquals("http://jabber.org/protocol/admin#get-active-users", command.getAttribute("node"));
                List<Element> fields = command.getChildrenNS("x", "jabber:x:data").getChildren("field");
                assertTrue(fields.isEmpty());

                assertTrue(!results.isEmpty());
                e = results.remove(0);
                command = e.getChildrenNS("command", COMMANDS_XMLNS);
                assertEquals("http://jabber.org/protocol/admin#get-active-users", command.getAttribute("node"));                
                fields = command.getChildrenNS("x", "jabber:x:data").getChildren("field");
                assertTrue(!fields.isEmpty());
                for (Element field : fields) {
                        if ("Users: 1".equals(field.getAttribute("var"))) {
                                assertEquals("admin@test", field.getFirstChild().getValue());
                        }
                }
                assertTrue(consoleOut.contains("Users: 1: [admin@test]"));
                        
        }
 
        public void testGenericCommand2() throws JaxmppException {                
                String incoming = "<iq to='admin@test/tclmt' type='result' id='1lj5d' from='sess-man@test'><command status='executing' node='http://jabber.org/protocol/admin#get-active-users' xmlns='http://jabber.org/protocol/commands'>"
                        + "<x type='form' xmlns='jabber:x:data'><title>Requesting List of Active Users</title><instructions>Fill out this form to request the active users of this service.</instructions>"
                        + "<field var='FORM_TYPE' type='hidden'><value>http://jabber.org/protocol/admin</value></field><field label='The domain for the list of active users' var='domainjid' type='list-single'><value/><option label='test'><value>test</value></option><option label='test2'><value>test2</value></option></field>"
                        + "<field label='Maximum number of items to show' var='max_items' type='list-single'><value/><option label='25'><value>25</value></option><option label='50'><value>50</value></option><option label='75'><value>75</value></option><option label='100'><value>100</value></option><option label='150'><value>150</value></option><option label='200'><value>200</value></option><option label='None'><value>None</value></option></field>"
                        + "</x><actions execute='complete'><complete/></actions></command></iq>"
                        + ""
                        + "<iq to='admin@test/tclmt' type='error' id='Zktjj' from='sess-man@test'><command status='completed' node='http://jabber.org/protocol/admin#get-active-users' xmlns='http://jabber.org/protocol/commands'/>"
                        + "<error type='modify' code='400'><bad-request xmlns='urn:ietf:params:xml:ns:xmpp-stanzas'/><bad-locale xmlns='http://jabber.org/protocol/commands'/></error></iq>";
                
                initializeIncoming(incoming);

                tclmt.execute(new String[] { "remote", "sess-man", "http://jabber.org/protocol/admin#get-active-users", "test", "25" });                

                List<Stanza> results = conn.getOutgoing();
                
                String consoleOut = console.toString();
                
                Stanza e = null;
                assertTrue(!results.isEmpty());
                e = results.remove(0);
                Element command = e.getChildrenNS("command", COMMANDS_XMLNS);
                assertEquals("http://jabber.org/protocol/admin#get-active-users", command.getAttribute("node"));
                List<Element> fields = command.getChildrenNS("x", "jabber:x:data").getChildren("field");
                assertTrue(fields.isEmpty());

                assertTrue(!results.isEmpty());
                e = results.remove(0);
                command = e.getChildrenNS("command", COMMANDS_XMLNS);
                assertEquals("http://jabber.org/protocol/admin#get-active-users", command.getAttribute("node"));                
                fields = command.getChildrenNS("x", "jabber:x:data").getChildren("field");
                assertTrue(!fields.isEmpty());
                for (Element field : fields) {
                        if ("Users: 1".equals(field.getAttribute("var"))) {
                                assertEquals("admin@test", field.getFirstChild().getValue());
                        }
                }                
                assertTrue(consoleOut.contains("bad-request"));
                assertTrue(consoleOut.contains("bad-locale"));
                        
        }

        public void testGenericCommand3() throws JaxmppException {                
                String incoming = "<iq from='basic-conf@test' type='result' id='aad3a' to='admin@test/tclmt'><command xmlns='http://jabber.org/protocol/commands' status='executing' node='config-list'><x xmlns='jabber:x:data' type='form'>"
                        + "<field type='list-single' label='Components' var='comp-name'><value>sess-man</value><option label='sess-man'><value>sess-man</value></option><option label='vhost-man'><value>vhost-man</value></option>"
                        + "<option label='basic-conf'><value>basic-conf</value></option><option label='stats'><value>stats</value></option><option label='message-router'><value>message-router</value></option><option label='amp'><value>amp</value></option>"
                        + "<option label='bosh'><value>bosh</value></option><option label='c2s'><value>c2s</value></option><option label='ext'><value>ext</value></option><option label='monitor'><value>monitor</value></option><option label='muc'><value>muc</value></option>"
                        + "<option label='pubsub'><value>pubsub</value></option><option label='s2s'><value>s2s</value></option><option label='srecv'><value>srecv</value></option></field></x><actions execute='complete'><complete/></actions></command></iq>"
                        + ""
                        + "<iq from='basic-conf@test' type='result' id='aad4a' to='admin@test/tclmt'><command xmlns='http://jabber.org/protocol/commands' status='completed' node='config-list'><x xmlns='jabber:x:data' type='result'><field type='fixed' var='comp-name'><value>sess-man</value></field>"
                        + "<field type='hidden' var='params-set'><value>params-set</value></field><field var='sess-man/command/http://jabber.org/protocol/admin#get-online-users-list'><value>LOCAL</value></field><field var='sess-man/command/http://jabber.org/protocol/admin#get-registered-user-list'><value>LOCAL</value></field>"
                        + "<field var='sess-man/command/http://jabber.org/protocol/admin#delete-user'><value>LOCAL</value></field><field var='sess-man/plugins-conf/amp/amp-jid'><value>amp@test</value></field><field var='sess-man/command/user-roster-management-ext'><value>LOCAL</value></field>"
                        + "<field var='sess-man/command/http://jabber.org/protocol/admin#change-user-password'><value>LOCAL</value></field><field var='sess-man/command/http://jabber.org/protocol/admin#add-user'><value>LOCAL</value></field><field var='sess-man/component-id'><value>sess-man@test</value></field>"
                        + "<field var='sess-man/def-hostname'><value>test</value></field><field var='sess-man/admins'><value>admin@test</value></field><field var='sess-man/scripts-dir'><value>scripts/admin</value></field><field var='sess-man/command/ALL'><value>ADMIN</value></field>"
                        + "<field var='sess-man/max-queue-size'><value>1000000</value></field><field var='sess-man/incoming-filters'><value>tigase.server.filters.PacketCounter</value></field><field var='sess-man/outgoing-filters'><value>tigase.server.filters.PacketCounter</value></field>"
                        + "<field var='sess-man/admin-scripts-dir'><value>scripts/admin/</value></field><field var='sess-man/plugins'><value>session-close, session-open, default-handler, jabber:iq:register, jabber:iq:auth, urn:ietf:params:xml:ns:xmpp-sasl, urn:ietf:params:xml:ns:xmpp-bind,"
                        + " urn:ietf:params:xml:ns:xmpp-session, jabber:iq:roster, jabber:iq:privacy, jabber:iq:version, http://jabber.org/protocol/stats, starttls, vcard-temp, http://jabber.org/protocol/commands, jabber:iq:private, urn:xmpp:ping, presence, domain-filter, disco, zlib, pep, amp</value></field>"
                        + "<field var='sess-man/plugins-concurrency'><value>amp=4,</value></field><field var='sess-man/skip-privacy'><value>false</value></field><field var='sess-man/trusted'><value>admin@test</value></field><field var='sess-man/offline-user-autocreate'><value>false</value></field>"
                        + "<field var='sess-man/sm-threads-pool'><value>default</value></field></x></command></iq>";
                
                initializeIncoming(incoming);

                tclmt.execute(new String[] { "remote", "basic-conf", "config-list", "sess-man" });                

                List<Stanza> results = conn.getOutgoing();
                
                String consoleOut = console.toString();
                
                Stanza e = null;
                assertTrue(!results.isEmpty());
                e = results.remove(0);
                Element command = e.getChildrenNS("command", COMMANDS_XMLNS);
                assertEquals("config-list", command.getAttribute("node"));
                List<Element> fields = command.getChildrenNS("x", "jabber:x:data").getChildren("field");
                assertTrue(fields.isEmpty());

                assertTrue(!results.isEmpty());
                e = results.remove(0);
                command = e.getChildrenNS("command", COMMANDS_XMLNS);
                assertEquals("config-list", command.getAttribute("node"));                
                fields = command.getChildrenNS("x", "jabber:x:data").getChildren("field");
                assertTrue(!fields.isEmpty());
                assertTrue(consoleOut.contains("session-close, session-open, default-handler, jabber:iq:register, jabber:iq:auth, urn:ietf:params:xml:ns:xmpp-sasl, urn:ietf:params:xml:ns:xmpp-bind, urn:ietf:params:xml:ns:xmpp-session, jabber:iq:roster, jabber:iq:privacy, jabber:iq:version, http://jabber.org/protocol/stats, starttls, vcard-temp, http://jabber.org/protocol/commands, jabber:iq:private, urn:xmpp:ping, presence, domain-filter, disco, zlib, pep, amp"));
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
