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
 SCR:CommandId:add-user
 SCR:Description:Adds user
 SCR:Help:user_jid password email
 Example: bin/tclmt.sh -u user@domain -p password -s node add-user user2@domain2 "password2" user@email
 */

import tigase.tclmt.*;
import tigase.jaxmpp.core.client.*;

def conn = connection;
def accountjid = JID.jidInstance((args != null && args.length > 0) ? args[0] : console.readLine("User JID:"));
def password = (args != null && args.length > 1) ? args[1] : new String(console.readPassword("Password:"));
def email = (args != null && args.length > 2) ? args[2] : console.readLine("EMail:");

def packet = new Command(null);
packet.setAttribute("to", "sess-man@"+serverName);
Command.setNode(packet, "http://jabber.org/protocol/admin#add-user");

def data = Command.getData(packet);
data.addJidSingleField('accountjid', accountjid);
data.addTextPrivateField('password', password);
data.addTextPrivateField('password-verify', password);
data.addTextSingleField('email', email);


def resultPacket = conn.sendSync(packet);


data = Command.getData(resultPacket);
data.getFields().each { 
    console.writeLine it.getLabel() ?: it.getVar();
    if ("text-multi" == it.getType()) {
	def lines = it.getFieldValue();
	for (def line : lines) {
	    console.writeLine "\t" + line;
	}
    }
    else if ("text-single" == it.getType() || "fixed" == it.getType()) {
	console.writeLine "\t" + it.getFieldValue();
    }
}
