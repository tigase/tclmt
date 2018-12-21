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
 SCR:CommandId:list-remote
 SCR:Description:Lists remote commands
 SCR:Help:comp_name
 Example: bin/tclmt.sh -u user@domain -p password -s node list-remote vhost-man
 */

import tigase.tclmt.*
import tigase.jaxmpp.core.client.*
import tigase.jaxmpp.core.client.xml.*
import tigase.jaxmpp.core.client.xmpp.stanzas.*

def DISCO_ITEMS_XMLNS = "http://jabber.org/protocol/disco#items";
def COMMANDS_XMLNS = "http://jabber.org/protocol/commands";

def conn = connection;

def componentName = (args != null && args.length > 0) ? args[0] : console.readLine("Component:");

def query = new DefaultElement("query");
query.setXMLNS(DISCO_ITEMS_XMLNS);
query.setAttribute("node", COMMANDS_XMLNS);

def iq = IQ.create();
iq.setAttribute("type", "get");
iq.setAttribute("to", componentName+"@"+serverName);

iq.addChild(query);

def resultPacket = conn.sendSync(iq);

query = resultPacket.getChildrenNS("query", DISCO_ITEMS_XMLNS);
def commands = query.getChildren("item");

commands.sort { it.getAttribute("node"); }

commands.each {
    console.writeLine "\t%1\$-35s %2\$-35s", it.getAttribute("node"), it.getAttribute("name")
}

//def scripts = commandManager.getAll().sort { it.getId() };
//println "Available commands:"
//scripts.each {
//    //println "\t"+ it.getId() + "\t\t" + it.getDescription();
//    console.writeLine "\t%1\$-25s %2\$-35s", it.getId(), it.getDescription()
//}
