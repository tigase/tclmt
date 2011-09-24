/*
 SCR:CommandId:get-active-users-list
 SCR:Description:Gets list of active users for domain
 SCR:Help:domain.com <max_items>
 */

import tigase.tclmt.*;

def conn = connection;
def domain = (args != null && args.length > 0) ? args[0] : console.readLine("Domain:");
def max_items = "25";
if (args != null && args.length > 1) 
    max_items = args[1];

def packet = new Command(null);
packet.setAttribute("to", "sess-man@"+serverName);
Command.setNode(packet, "http://jabber.org/protocol/admin#get-active-users");

def data = Command.getData(packet);
data.addTextSingleField('domainjid', domain);
data.addTextSingleField('max_items', max_items);


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
}