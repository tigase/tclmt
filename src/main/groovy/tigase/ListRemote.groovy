/*
 SCR:CommandId:list-remote
 SCR:Description:Lists remote commands
 SCR:Help:comp_name
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