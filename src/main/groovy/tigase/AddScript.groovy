/*
 SCR:CommandId:add-script
 SCR:Description:Adds script
 SCR:Help:comp_name command_id description language source_file
 */

import tigase.tclmt.*
import tigase.jaxmpp.core.client.*
import tigase.jaxmpp.core.client.xml.*
import tigase.jaxmpp.core.client.xmpp.stanzas.*

def DISCO_ITEMS_XMLNS = "http://jabber.org/protocol/disco#items";
def COMMANDS_XMLNS = "http://jabber.org/protocol/commands";

def conn = connection;

def componentName = (args != null && args.length > 0) ? args[0] : console.readLine("Component:");
def cmdId = (args != null && args.length > 1) ? args[1] : console.readLine("Command Id:");
def description = (args != null && args.length > 2) ? args[2] : console.readLine("Description:");
def language = (args != null && args.length > 3) ? args[3] : console.readLine("Language:");
def file = (args != null && args.length > 4) ? args[4] : console.readLine("Script file:");

def scriptLines = new File(file).text.split("\n");

def packet = new Command(null);
packet.setAttribute("to", componentName+"@"+serverName);
Command.setNode(packet, "add-script");

def data = Command.getData(packet);

data.addTextSingleField("Command Id", cmdId);
data.addTextSingleField("Description", description);
data.addListSingleField("Language", language);
data.addTextMultiField("Script text", scriptLines);
data.addBooleanField("Save to disk", true);

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

