/*
 SCR:CommandId:user-stats
 SCR:Description:Gets user statistics
 SCR:Help:user_jid
 Example: bin/tclmt.sh -u user@domain -p password -s node user-stats user2@domain2
 */

import tigase.tclmt.*;
import tigase.jaxmpp.core.client.*;

def conn = connection;
def userJid = (args != null && args.length > 0) ? args[0] : console.readLine("User JID:");

def packet = new Command(null);
packet.setAttribute("to", "sess-man@"+serverName);
Command.setNode(packet, "http://jabber.org/protocol/admin#user-stats");

def data = Command.getData(packet);
data.addJidSingleField('accountjid', JID.jidInstance(userJid));


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
