/*
 SCR:CommandId:get-user-roster
 SCR:Description:Gets user roster
 SCR:Help:user_jid
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