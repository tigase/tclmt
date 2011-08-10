/*
 SCR:CommandId:config-list
 SCR:Description:Lists configuration of component
 SCR:Help:comp-name
 */

import tigase.tclmt.*;

def conn = connection;
def domain = (args != null && args.length > 0) ? args[0] : console.readLine("Component name:");

def packet = new Command(null);
packet.setAttribute("to", "basic-conf@"+serverName);
Command.setNode(packet, "config-list");

def data = Command.getData(packet);
data.addTextSingleField('comp-name', domain);


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