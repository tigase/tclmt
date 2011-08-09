/*
 SCR:CommandId:change-user-password
 SCR:Description:Changes user password
 SCR:Help:user_jid password
 */

import tigase.tclmt.*;
import tigase.jaxmpp.core.client.*;

def conn = connection;
def accountjid = JID.jidInstance((args != null && args.length > 0) ? args[0] : console.readLine("User JID:"));
def password = (args != null && args.length > 1) ? args[1] : new String(console.readPassword("Password:"));

def packet = new Command(null);
packet.setAttribute("to", "sess-man@"+serverName);
Command.setNode(packet, "http://jabber.org/protocol/admin#change-user-password");

def data = Command.getData(packet);
data.addJidSingleField('accountjid', accountjid);
data.addTextPrivateField('password', password);

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