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
 SCR:CommandId:comp-repo-reload
 SCR:Description:Reloads component repository items
 SCR:Help:comp_name
 Example VHost: bin/tclmt.sh -u admin@domain -p "password" -s node comp-repo-reload vhost-man
 */

import tigase.tclmt.*
import tigase.jaxmpp.core.client.*
import tigase.jaxmpp.core.client.xml.*
import tigase.jaxmpp.core.client.xmpp.stanzas.*

def conn = connection;

def componentName = (args != null && args.length > 0) ? args[0] : console.readLine("Component:");

def packet = new Command(null);
packet.setAttribute("to", componentName+"@"+serverName);
Command.setNode(packet, "comp-repo-reload");

def data = Command.getData(packet);

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


