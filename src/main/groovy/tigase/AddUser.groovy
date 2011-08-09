/*
 SCR:CommandId:add-user
 SCR:Description:Adds user
 SCR:Help:user_jid password
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