package tigase.tclmt;

public interface ConsoleIfc {

        public void writeLine(Object obj);

        public void writeLine(String format, Object... args);

        public String readLine();

        public String readLine(String label);

        public char[] readPassword(String label);
}
