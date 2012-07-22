/*
 SCR:CommandId:delete-user
 SCR:Description:Deletes user
 SCR:Help:user_jid <user_jid2>
 Example: bin/tclmt.sh -u user@domain -p password -s node delete-user user2@domain2
 */

import tigase.tclmt.*;
import tigase.jaxmpp.core.client.*;

def conn = connection;
def accountjids = [];
if (args == null || args.length == 0) {
    while (true) {
        def sjid = console.readLine("User JID:");
	if (sjid == null || sjid.length() == 0)
	    break;
        accountjids.add(JID.jidInstance(sjid));
    }
}
else {
    for (sjid in args) {
	accountjids.add(JID.jidInstance(sjid));
    }
}

if (accountjids.isEmpty())
    return;

def packet = new Command(null);
packet.setAttribute("to", "sess-man@"+serverName);
Command.setNode(packet, "http://jabber.org/protocol/admin#delete-user");

def data = Command.getData(packet);
data.addJidMultiField('accountjids', accountjids.toArray(new JID[accountjids.size()]));

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
