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
 SCR:CommandId:get-user-roster
 SCR:Description:Gets user roster
 SCR:Help:user_jid
 Example: bin/tclmt.sh -u user@domain -p password -s node get-user-roster user2@domain2
 */

import tigase.tclmt.*
import tigase.jaxmpp.core.client.*
import tigase.jaxmpp.core.client.xml.*
import tigase.jaxmpp.core.client.xmpp.stanzas.*


def CMD_ID = "http://jabber.org/protocol/admin#get-user-roster"

def COMMANDS_XMLNS = "http://jabber.org/protocol/commands";

def conn = connection;

def sjid = (args != null && args.length > 0) ? args[0] : console.readLine("Users jid:");

def packet = new Command(null);
packet.setAttribute("to", "sess-man@" + serverName);
Command.setNode(packet, CMD_ID);

def data = Command.getData(packet);
data.addHiddenField("FORM_TYPE", "http://jabber.org/protocol/admin");
data.addJidSingleField("accountjid", JID.jidInstance(sjid));

def resultPacket = conn.sendSync(packet);

def roster = resultPacket.getChildrenNS("command", COMMANDS_XMLNS).getChildrenNS("x", "jabber:x:data").getChildrenNS("query", "jabber:iq:roster");
if (roster.getChildren() == null || roster.getChildren("item").isEmpty()) {
    console.writeLine "roster is emtpy"
}
else {
    roster.getChildren("item").each {
	console.writeLine it.getAsString()
    }
}

return roster;
