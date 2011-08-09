package tigase.tclmt.ui;

import java.io.Console;
import tigase.tclmt.ConsoleIfc;

public class SystemConsole implements ConsoleIfc {

        public SystemConsole() {
        }

        public void writeLine(Object obj) {
                System.console().writer().println(obj);
        }

        public void writeLine(String format, Object... args) {
                System.console().writer().printf(format + "\n", args);
        }

        public String readLine() {
                return System.console().readLine();
        }

        public String readLine(String label) {
                Console console = System.console();
                return console.readLine("%s", label);
        }

        public char[] readPassword(String label) {
                return System.console().readPassword("%s", label);
        }
}
