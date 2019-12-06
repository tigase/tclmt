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
 SCR:CommandId:announce
 SCR:Description:Sends announcement to online users
 SCR:Help:from subject message
 Example: bin/tclmt.sh -u user@domain -p password -s node announce sender@domain "Subject" "Message to send"
 */

import tigase.tclmt.*
import tigase.jaxmpp.core.client.*
import tigase.jaxmpp.core.client.xml.*
import tigase.jaxmpp.core.client.xmpp.stanzas.*

def conn = connection;

def sjid = (args != null && args.length > 0) ? args[0] : console.readLine("From:");
def subject = (args != null && args.length > 1) ? args[1] : console.readLine("Subject:");
def msg = (args != null && args.length > 2) ? args[2].split("\n") : null;

if (msg == null) {
    msg = [];
    def tmp = console.readLine("Message:");    
    while (tmp != null && !tmp.isEmpty()) {
	msg += tmp;
	tmp = console.readLine(":");
    }
    msg = msg as String[];
}

def packet = new Command(null);
packet.setAttribute("to", "sess-man@"+serverName);
Command.setNode(packet, "http://jabber.org/protocol/admin#announce");

def data = Command.getData(packet);
data.addHiddenField("FORM_TYPE", "http://jabber.org/protocol/admin");
data.addJidSingleField("from-jid", JID.jidInstance(sjid));
data.addTextSingleField("subject", subject);
data.addListSingleField("msg-type", "normal");
data.addTextMultiField("announcement", msg);

def resultPacket = conn.sendSync(packet);

data = Command.getData(resultPacket);
data.getFields().each {
    console.writeLine it.getLabel() ?: it.getVar();
    if ("text-multi" == it.getType()) {
	def lines = it.getFieldValue();
	if (lines != null) {
	    for (def line : lines) {
		console.writeLine "\t" + line;
	    }
	}
    }
    else if ("text-single" == it.getType() || "fixed" == it.getType()) {
	console.writeLine "\t" + it.getFieldValue();
    }
    else {
	console.writeLine "\t" + it.getFieldValue();
    }
}


