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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tigase.tclmt.util;

import java.util.Calendar;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 *
 * @author andrzej
 */
public class LogFormatter extends Formatter {

        private Calendar cal = Calendar.getInstance();
        
        public LogFormatter() {
                
        }
        
        @Override
        public synchronized String format(LogRecord record) {
                StringBuilder sb = new StringBuilder(200);
                
                cal.setTimeInMillis(record.getMillis());
		sb.append(String.format("%1$tF %1$tT", cal));
        
                sb.append(" ");
                
                if (record.getLevel() != null) {
                        sb.append(record.getLevel().toString());
                        
                        while (sb.length() < 27)
                                sb.append(" ");
                        
                        sb.append("  ");
                }
                
                if (record.getSourceClassName() != null) {
                        String clsName = record.getSourceClassName();
                        
                        int idx = clsName.lastIndexOf(".");
                        if (idx > 0) {
                                clsName = clsName.substring(idx + 1);
                        }
                        
                        sb.append(clsName);
                }
                
                if (record.getSourceMethodName() != null) {
                        sb.append(".").append(record.getSourceMethodName()).append("()");
                }
                
                sb.append(" - ");                
                sb.append(formatMessage(record));
                
                if (record.getThrown() != null) {
                        StringBuilder tsb = new StringBuilder(2048);
                        formatThrowable(tsb, record.getThrown());
                }
                
                sb.append("\n");
                
                return sb.toString();
        }
        
        private void formatThrowable(StringBuilder sb, Throwable ex) {
                if (sb.length() > 0) {
                        sb.append("\nCaused by:");
                }
                
                StackTraceElement[] stackTrace = ex.getStackTrace();
                
                if (stackTrace != null) {
                        for (StackTraceElement element : stackTrace) {
                                sb.append("\tat ").append(element.toString());
                        }
                }

                Throwable cause = ex.getCause();
                if (cause != null) {
                        formatThrowable(sb, cause);
                }
        }
}
