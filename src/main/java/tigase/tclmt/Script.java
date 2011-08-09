package tigase.tclmt;

import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class Script {

        private static final Logger log = Logger.getLogger(Script.class.getCanonicalName());
        public static final String SCRIPT_PREFIX = "SCR:";
        public static final String SCRIPT_ID = "CommandId";
        public static final String SCRIPT_DESCRIPTION = "Description";
        public static final String SCRIPT_HELP = "Help";
        public static final String SCRIPT_MANAGER = "scriptEngineManager";
        private String id;
        private String description;
        private String help;
        private String ext;
        private String script;
        private CompiledScript compiledScript;

        public Script(String ext) {
                this.ext = ext;
        }

        public String getId() {
                return id;
        }

        public String getDescription() {
                return description;
        }

        public String getHelp() {
                return help;
        }

        public void setCode(String script) {
                this.script = script;
        }

        public void setMetaProperty(String key, String value) {
                if (SCRIPT_ID.equals(key)) {
                        id = value;
                }
                else if (SCRIPT_DESCRIPTION.equals(key)) {
                        description = value;
                }
                else if (SCRIPT_HELP.equals(key)) {
                        help = value;
                }
        }

        public boolean isValid() {
                return id != null;
        }

        public Object execute(Bindings binds) throws ScriptException {
                ScriptEngineManager engineManager = (ScriptEngineManager) binds.get(SCRIPT_MANAGER);
                ScriptEngine engine = engineManager.getEngineByExtension(ext);

                if (engine == null) {
                        engine = engineManager.getEngineByName(ext);
                }

                if (engine instanceof Compilable) {
                        compiledScript = ((Compilable) engine).compile(script);
                }

                ScriptContext context = null;
                StringWriter writer = null;
                Object res = "";

                try {
                        binds.put("result", res);
                        context = engine.getContext();
                        context.setBindings(binds, ScriptContext.ENGINE_SCOPE);
                        writer = new StringWriter();
                        context.setErrorWriter(writer);

                        if (compiledScript != null) {
                                res = compiledScript.eval(context);
                        }
                        else {
                                res = engine.eval(script, context);
                        }

                        if (res == null) {
                                res = binds.get("result");
                        }

                }
                catch (Exception ex) {
                        log.log(Level.WARNING, "Exception executing script " + id, ex);
                        ConsoleIfc console = (ConsoleIfc) binds.get("console");
                        if (console != null) {
                                console.writeLine("Error executing script " + id + "\n\t" + ex.getMessage());
                        }
                }

                return res;
        }
}
