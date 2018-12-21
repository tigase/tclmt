#
# Tigase XMPP Server Command Line Management Tool
# Copyright (C) 2004-2018 "Tigase, Inc." <office@tigase.com>
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU Affero General Public License as published by
# the Free Software Foundation, either version 3 of the License,
# or (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Affero General Public License for more details.
#   *
# You should have received a copy of the GNU Affero General Public License
# along with this program. Look for COPYING file in the top folder.
# If not, see http://www.gnu.org/licenses/.
#
#
# SCR:CommandId:example-python
# SCR:Description:Lists remote commands (Python)
# SCR:Help:comp_name

from java.lang import *
from tigase.tclmt import *
from tigase.jaxmpp.core.client import *
from tigase.jaxmpp.core.client.xml import *
from tigase.jaxmpp.core.client.xmpp.stanzas import *

DISCO_ITEMS_XMLNS = "http://jabber.org/protocol/disco#items";
COMMANDS_XMLNS = "http://jabber.org/protocol/commands";

conn = connection;

componentName = None
if args is None or len(args) == 0:
    componentName = console.readLine("Component:");
else:
    componentName = args[0];
#componentName = (args != null && args.length > 0) ? args[0] : console.readLine("Component:");

query = DefaultElement("query");
query.setXMLNS(DISCO_ITEMS_XMLNS);
query.setAttribute("node", COMMANDS_XMLNS);

iq = IQ.create();
iq.setAttribute("type", "get");
iq.setAttribute("to", componentName+"@"+serverName);

iq.addChild(query);

resultPacket = conn.sendSync(iq);

query = resultPacket.getChildrenNS("query", DISCO_ITEMS_XMLNS);
commands = query.getChildren("item");


for it in commands:
    console.writeLine('\t%s \t%s' % (it.getAttribute("node"), it.getAttribute("name")))


