/*
 SCR:CommandId:connection-time
 SCR:Description:Gets maximum and avarage connection time for all connected users
 SCR:Help: 
 Example: bin/tclmt.sh -u user@domain -p password -s node connection-time
 */

import tigase.tclmt.*;
import tigase.jaxmpp.core.client.*;

def conn = connection;

def packet = new Command(null);
packet.setAttribute("to", "sess-man@"+serverName);
Command.setNode(packet, "connection-time");

def data = Command.getData(packet);

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
