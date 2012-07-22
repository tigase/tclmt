/*
 SCR:CommandId:comp-repo-reload
 SCR:Description:Reloads component repository items
 SCR:Help:comp_name
 Example VHost: bin/tclmt.sh -u admin@domain -p "password" -s node comp-repo-reload vhost-man
 */

import tigase.tclmt.*
import tigase.jaxmpp.core.client.*
import tigase.jaxmpp.core.client.xml.*
import tigase.jaxmpp.core.client.xmpp.stanzas.*

def conn = connection;

def componentName = (args != null && args.length > 0) ? args[0] : console.readLine("Component:");

def packet = new Command(null);
packet.setAttribute("to", componentName+"@"+serverName);
Command.setNode(packet, "comp-repo-reload");

def data = Command.getData(packet);

def resultPacket = conn.sendSync(packet);

data = Command.getData(resultPacket);
data.getFields().each {
    console.writeLine it.getLabel() ?: it.getVar();
    if ("text-multi" == it.getType()) {
	def lines = it.getFieldValue();
	if (lines != null) {
	    for (def line : lines) {
		console.writeLine "\t" + line;
	    }
	}
    }
    else if ("text-single" == it.getType() || "fixed" == it.getType()) {
	console.writeLine "\t" + it.getFieldValue();
    }
    else {
	console.writeLine "\t" + it.getFieldValue();
    }
}


