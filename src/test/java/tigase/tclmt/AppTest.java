package tigase.tclmt;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;

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
        public void setUp() {
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
}
