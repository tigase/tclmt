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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import tigase.jaxmpp.core.client.BareJID;
import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.j2se.connectors.socket.SocketConnector;

/**
 * Unit test for simple App.
 */
public class AppTest
        extends TestCase {

        private TestConsole console = null;
        private TestSynchronizedConnection conn = null;
        private Tclmt tclmt = null;
        /**
         * Create the test case
         *
         * @param testName name of the test case
         */
        public AppTest(String testName) {
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
                return new TestSuite(AppTest.class);
        }

        /**
         * Rigourous Test :-)
         */
        public void testList() throws JaxmppException {                
                tclmt.execute(new String[] { "list" });                
                String consoleOut = console.toString();
                assertFalse(consoleOut.contains("No script"));
        }

        /**
         * Rigourous Test :-)
         */
        public void testParseArgs() throws JaxmppException {                
                String[] rest = tclmt.parseArgs(new String[] {
                        "-u", "admin@test.com",
                        "-p", "password",
                        "list"
                });
                tclmt.initialize();
                assertEquals(conn.getProperties().getUserProperty(SessionObject.USER_BARE_JID), BareJID.bareJIDInstance("admin@test.com"));
//                assertEquals(conn.getProperties().getUserProperty(SocketConnector.SERVER_HOST), null);//"test.com");
                assertEquals(conn.getProperties().getUserProperty(SessionObject.PASSWORD), "password");
                tclmt.execute(new String[] { "list" });                
                String consoleOut = console.toString();
                assertFalse(consoleOut.contains("No script"));
        }
}
