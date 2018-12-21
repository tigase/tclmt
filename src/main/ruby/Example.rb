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
# SCR:CommandId:example-ruby
# SCR:Description:Lists remote commands (Ruby)
# SCR:Help:comp_name

require 'java'

Dir["libs/\*.jar"].each { |jar| require jar } 

include_class Java::tigase.jaxmpp.core.client.xml.DefaultElement;
include_class Java::tigase.jaxmpp.core.client.xmpp.stanzas.IQ;

DISCO_ITEMS_XMLNS = "http://jabber.org/protocol/disco#items";
COMMANDS_XMLNS = "http://jabber.org/protocol/commands";

args = $args
console = $console;
conn = $connection;
serverName = $serverName;

componentName = nil;
if args.nil? or args.length == 0
    componentName = console.readLine("Component:");
else
    componentName = args[0];
end

query = DefaultElement.new("query");
query.setXMLNS(DISCO_ITEMS_XMLNS);
query.setAttribute("node", COMMANDS_XMLNS);

iq = IQ.create();
iq.setAttribute("type", "get");
iq.setAttribute("to", componentName+"@"+serverName);

iq.addChild(query);

resultPacket = conn.sendSync(iq);

query = resultPacket.getChildrenNS("query", DISCO_ITEMS_XMLNS);
commands = query.getChildren("item");


commands.each do |it| console.writeLine("\t%s \t%s", it.getAttribute("node"), it.getAttribute("name")) end