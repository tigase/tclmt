/**
 * Tigase XMPP Server Command Line Management Tool - bootstrap configuration for all Tigase projects
 * Copyright (C) 2004 Tigase, Inc. (office@tigase.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. Look for COPYING file in the top folder.
 * If not, see http://www.gnu.org/licenses/.
 */
/*
 SCR:CommandId:remote
 SCR:Description:Executes basic remote commands
 SCR:Help: componentName node args...
 Example: bin/tclmt.sh -u user@domain -p password -s node remote componentName cmd_id [...(other parameters required by remote command)]
 */

import tigase.tclmt.*
import tigase.jaxmpp.core.client.*
import tigase.jaxmpp.core.client.xml.*
import tigase.jaxmpp.core.client.xmpp.stanzas.*
import tigase.jaxmpp.core.client.xmpp.forms.*

def printFirstChildValue = { resultData, label, name ->
    def children = resultData.getChildren(name)
    if (children == null || children.isEmpty())
	return;
	
    console.writeLine(label.length() < 8 ? "%s:\t\t%s" : "%s:\t%s", label, children.get(0)?.getValue())
}

def valueAsString = { field ->
    def value = field.getFieldValue()
    if (field.getType().contains("list") || field.getType().contains("multi")) {
		value = java.util.Arrays.asList(value).toString()
    }
    return value;
}

def printValue = { field ->
    console.writeLine("%s: %s", field.getLabel() ?: field.getVar(), valueAsString(field));
}

def conn = connection;

def componentName = (args != null && args.length > 0) ? args[0] : console.readLine("Component:");
def node = (args != null && args.length > 1) ? args[1] : console.readLine("Node:");

def i = 2;

// creating initial request

def packet = new Command(null);
packet.setAttribute("to", (componentName.contains(".") || componentName.contains("@")) ? componentName : (componentName+"@"+serverName));
Command.setNode(packet, node);

try {
	while(packet != null) {

		// processing response
		def resultPacket = conn.sendSync(packet);
		if (resultPacket.getType() == StanzaType.error) {
			def errorNames = [];
			resultPacket.getChildren("error").get(0).getChildren().each { errorNames.add(it.getName()) };
			console.writeLine("Errors: %s", errorNames)
			break;
		}
		// fix for missing type of field
		resultPacket.getChildren("command").get(0).getChildren("x").get(0).getChildren("field").each { it ->
			if (!it.getAttribute("type"))
			it.setAttribute("type", "fixed");
		}
		def resultData = Command.getData(resultPacket)

		// preparing next request (will be used if needed)
		packet = new Command(null);
		packet.setAttribute("to", (componentName.contains(".") || componentName.contains("@")) ? componentName : (componentName+"@"+serverName));
		Command.setNode(packet, node);
		def data = Command.getData(packet);

		printFirstChildValue(resultData, "Title", "title");
		printFirstChildValue(resultData, "Instructions", "instructions");

		resultData.getFields().each {
			def value = null;
    
			if (it.getType() == "hidden") {
				data.addHiddenField(it.getVar(), it.getFieldValue());
				return;
			}
			else if (it.getType() == "fixed") {
				console.writeLine("%s: %s", it.getLabel() ?: it.getVar(), it.getFieldValue());
				return;
			}
    
			if (resultData.getType() != XDataType.form) {
				printValue(it);
				return;
			}
    
			if (i>(args.length-1)) {
				if (interactive) {
					if (it.getFieldValue()) {
						console.writeLine("%s [%s] [default=%s]", it.getLabel() ?: it.getVar(), it.getType(), valueAsString(it))
					}
					else {
						console.writeLine("%s [%s]", it.getLabel() ?: it.getVar(), it.getType())
					}
					value = console.readLine(": ");
				}
				else {
					value = it.getFieldValue();
				}
			}
			else {
				value = args[i];
			}
			i++;
    
			if (value != null) {
				switch(it.getType()) {
				case "text-single":
					data.addTextSingleField(it.getVar(), value);
					break;

				case "text-multi":
					try {
						value = value.split(",")
					} catch (Exception ex) {}
					data.addTextMultiField(it.getVar(), value);
					break;
		
				case "text-private":
					data.addTextPrivateField(it.getVar(), value);
					break;
		
				case "jid-single":
					value = JID.jidInstance(value);
					data.addJidSingleField(it.getVar(), value);
					break;
		
				case "jid-multi":
					tmp = []
					try {
						value = value.split(",")
					} catch (Exception ex) {}
					value.each { sjid -> tmp.add(JID.jidInstance(sjid)); }
					data.addJidMultiField(it.getVar(), tmp.toArray(new JID[tmp.size()]));
					break;

				case "list-single":
					data.addListSingleField(it.getVar(), value);
					break;

				case "list-multi":
					try {
						value = value.split(",");
					} catch (Exception ex) {}
					data.addListMultiField(it.getVar(), value);
					break;

				case "boolean":
					data.addBooleanField(it.getVar(), value == "true");
					break;
				}
			}
		}

		if (resultData.getType() != XDataType.form)
		break;

	}

}
catch (Exception ex) {
    ex.printStackTrace();
}
