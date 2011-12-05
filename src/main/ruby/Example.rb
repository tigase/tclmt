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