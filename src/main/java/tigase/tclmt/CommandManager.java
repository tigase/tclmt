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
package tigase.tclmt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.Bindings;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class CommandManager {

        private static final Logger log = Logger.getLogger(CommandManager.class.getCanonicalName());
        private static final Charset CHARSET = Charset.forName("UTF-8");
        private final ScriptEngineManager scriptEngineManager;
        private final Map<String, Script> scripts = new ConcurrentHashMap<String, Script>();

        public CommandManager() {
                scriptEngineManager = new ScriptEngineManager(CommandManager.class.getClassLoader());
        }

        public void loadScripts(String[] dirs) {

                for (String dir : dirs) {
                        try {
                                log.log(Level.CONFIG, "Loading scripts from path {0}", dir);
                                File scriptsDir = new File(dir);

                                if (scriptsDir.exists()) {

                                        for (File f : scriptsDir.listFiles()) {
                                                if (f.isFile() && !f.toString().endsWith("~")) {
                                                        loadScript(f);
                                                }
                                        }

                                }
                                else {
                                        log.log(Level.CONFIG, "Scripts directory {0} is missing", dir);
                                }
                        }
                        catch (Exception ex) {
                                log.log(Level.WARNING, "Error processing scripts directory", ex);
                        }
                }

        }

        public Object executeScript(String id, Bindings bindings) throws ScriptException {
                Script script = scripts.get(id);

                if (script == null) {
                        log.log(Level.WARNING, "No script with id {0}", id);
                        ConsoleIfc console = (ConsoleIfc) bindings.get("console");
                        if (console != null)
                                console.writeLine("No script with id "+id);
                        
                        return null;
                }

                bindings = initBindings(bindings);

                return script.execute(bindings);
        }

        public List<Script> getAll() {
                return new ArrayList<Script>(scripts.values());
        }

        private void loadScript(File f) throws FileNotFoundException, IOException {
                int extIdx = f.getName().lastIndexOf(".");
                if (extIdx < 0) {
                        return;
                }
                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f), CHARSET));

                StringBuilder builder = new StringBuilder(4096);
                String line = null;

                Script script = new Script(f.getName().substring(extIdx + 1));

                while ((line = reader.readLine()) != null) {
                        builder.append(line).append("\n");

                        int idx = line.indexOf(Script.SCRIPT_PREFIX);
                        if (idx != -1) {
                                idx += Script.SCRIPT_PREFIX.length();
                                int idx1 = line.indexOf(":", idx);

                                if (idx1 != -1) {
                                        script.setMetaProperty(line.substring(idx, idx1), line.substring(idx1 + 1).trim());
                                }
                        }
                }

                if (script.isValid()) {
                        script.setCode(builder.toString());
                        scripts.put(script.getId(), script);
                }
        }

        private Bindings initBindings(Bindings bindings) {
                if (bindings == null) {
                        bindings = scriptEngineManager.getBindings();
                }

                bindings.put("commandManager", this);
                bindings.put(Script.SCRIPT_MANAGER, scriptEngineManager);

                return bindings;
        }

        public Bindings createBindings() {
                return scriptEngineManager.getBindings();
        }
}
