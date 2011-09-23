/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tigase.tclmt;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author andrzej
 */
public class TestConsole implements ConsoleIfc {

        private final StringBuilder buf = new StringBuilder(2048);
        private List<String> input = new ArrayList<String>();
        
        public TestConsole() {                
        }
        
        public void addLines(String... lines) {
                for (String line : lines) {
                        input.add(line);
                }
        }
        
        public void writeLine(Object obj) {
                buf.append(obj.toString());
        }

        public void writeLine(String format, Object... args) {
                buf.append(String.format(format, args));
        }

        public String readLine() {
                return input.remove(0);
        }

        public String readLine(String label) {
                return input.remove(0);
        }

        public char[] readPassword(String label) {
                return input.remove(0).toCharArray();
        }
        
        @Override
        public String toString() {
                return buf.toString();
        }
}
