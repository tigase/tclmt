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

import java.util.List;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.DefaultElement;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.forms.JabberDataElement;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;

/**
 *
 * @author andrzej
 */
public class Command extends Stanza {

        public Command(String type) throws XMLException {
                super(new DefaultElement("iq"));

                Element command = new DefaultElement("command");
                command.setXMLNS("http://jabber.org/protocol/commands");
                this.addChild(command);

                Element x = new DefaultElement("x");
                x.setXMLNS("jabber:x:data");
                if (type != null) {
                        x.setAttribute("type", type);
                }

                command.addChild(x);
        }

        public Command(String type, JabberDataElement data) throws XMLException {
                super(new DefaultElement("iq"));

                Element command = new DefaultElement("command");
                command.setAttribute("xmlns", "http://jabber.org/protocol/commands");
                this.addChild(command);

                command.addChild(data);
        }

        private static Element getChild(Stanza stanza, String path) throws XMLException {
                if (path == null || path.isEmpty()) {
                        return null;
                }

                String[] parts = path.split("/");
                Element el = stanza;
                for (int i = 2; i < parts.length; i++) {
                        List<Element> children = el.getChildren(parts[i]);

                        if (children == null || children.isEmpty()) {
                                return null;
                        }

                        el = children.get(0);
                }

                return el;
        }

        public static String getType(Stanza stanza) throws XMLException {
                Element child = getChild(stanza, "/iq/command/x");

                if (child == null) {
                        return null;
                }

                return child.getAttribute("type");
        }

        public static void setType(Stanza stanza, String type) throws XMLException {
                Element child = getChild(stanza, "/iq/command/x");

                if (child == null) {
                        return;
                }

                child.setAttribute("type", type);
        }

        public static String getAction(Stanza stanza) throws XMLException {
                Element child = getChild(stanza, "/iq/command");

                if (child == null) {
                        return null;
                }

                return child.getAttribute("action");
        }

        public static void setAction(Stanza stanza, String action) throws XMLException {
                Element child = getChild(stanza, "/iq/command");

                if (child == null) {
                        return;
                }

                child.setAttribute("action", action);
        }

        public static void setNode(Stanza stanza, String node) throws XMLException {
                Element cmdChild = getChild(stanza, "/iq/command");
                if (cmdChild == null) {
                        return;
                }

                cmdChild.setAttribute("node", node);
        }

        public static JabberDataElement getData(Stanza stanza) throws JaxmppException {
                Element child = getChild(stanza, "/iq/command/x");

                if (child instanceof JabberDataElement) {
                        return (JabberDataElement) child;
                }

                return new JabberDataElement(child);
        }
}
