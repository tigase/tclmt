package tigase.tclmt.ui;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import tigase.tclmt.ConsoleIfc;

public class SystemConsole implements ConsoleIfc {

        private final BufferedReader in;
        private final PrintWriter out;
        
        public SystemConsole() {
                if (System.console() != null) {
                        in = null;
                        out = System.console().writer();
                }
                else {
                        in = new BufferedReader(new InputStreamReader(System.in));
                        out = new PrintWriter(System.out, true);
                }
        }        

        public void writeLine(Object obj) {
                out.println(obj);
        }

        public void writeLine(String format, Object... args) {
                out.printf(format + "\n", args);
        }

        public String readLine() {
                if (in == null) {
                        return System.console().readLine();
                }
                try {
                        return in.readLine();
        //                return System.console().readLine();
                } catch (IOException ex) {
                        return null;
                }
        }

        public String readLine(String label) {
                if (in == null) {
                        return System.console().readLine("%s", label);
                }
                try {
                        out.printf("%s", label);
                        return in.readLine();
        //                return System.console().readLine();
                } catch (IOException ex) {
                        return null;
                }
        }

        public char[] readPassword(String label) {
                if (in == null) {
                        return System.console().readPassword("%s", label);
                }
                String line = readLine(label);
                return line.toCharArray();
        }
}
