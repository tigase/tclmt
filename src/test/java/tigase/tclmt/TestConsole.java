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
