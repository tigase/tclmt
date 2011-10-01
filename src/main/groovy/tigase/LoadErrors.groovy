/*
 SCR:CommandId:load-errors
 SCR:Description:Gets load errors catched by the server during execution
 SCR:Help: 
 */

import tigase.tclmt.*;
import tigase.jaxmpp.core.client.*;

def conn = connection;

def packet = new Command(null);
packet.setAttribute("to", "monitor@"+serverName);
Command.setNode(packet, "load-errors");

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