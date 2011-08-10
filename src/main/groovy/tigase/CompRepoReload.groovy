/*
 SCR:CommandId:comp-repo-reload
 SCR:Description:Reloads component repository
 SCR:Help:comp-id
 */

import tigase.tclmt.*;
import tigase.jaxmpp.core.client.*;

def conn = connection;
def compId = (args != null && args.length > 0) ? args[0] : console.readLine("Component id:");

def packet = new Command(null);
packet.setAttribute("to", compId+"@"+serverName);
Command.setNode(packet, "comp-repo-reload");

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