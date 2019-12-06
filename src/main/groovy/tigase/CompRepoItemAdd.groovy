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
 SCR:CommandId:comp-repo-item-add
 SCR:Description:Adds component repository item
 SCR:Help:comp_name ...                                                                                                          	
 Example VHost: bin/tclmt.sh -u admin@domain -p "password" -s node comp-repo-item-add vhost-man new_domain enabled anonymous register max_users presence_forward message_forward other_options owner_of@domain admin_of@domain
 */

import tigase.tclmt.*
import tigase.jaxmpp.core.client.*
import tigase.jaxmpp.core.client.xml.*
import tigase.jaxmpp.core.client.xmpp.stanzas.*

def conn = connection;

def componentName = (args != null && args.length > 0) ? args[0] : console.readLine("Component:");

def packet = new Command(null);
packet.setAttribute("to", componentName+"@"+serverName);
Command.setNode(packet, "comp-repo-item-add");

def data = Command.getData(packet);

def resultPacket = conn.sendSync(packet);

resultPacket.getChildren("command").get(0).getChildren("x").get(0).getChildren("field").each {
    if (!it.getAttribute("type")) {
	it.setAttribute("type", "text-single");
    }
}
	    

data = Command.getData(resultPacket);

packet = new Command(null);
packet.setAttribute("to", componentName+"@"+serverName);
Command.setNode(packet, "comp-repo-item-add");
req = Command.getData(packet);

def i = 1;

data.getFields().each {
    def label = it.getLabel() ?: it.getVar();
    def value = null;
    def options = null;
    
    if (it.getFieldValue() != null)
	value = it.getFieldValue();
	
    if (it.getType().contains("list")) {
	options = []
	it.getChildren("option").each {
	    options += it.getFirstChild().getValue();
	}
    }

    if (it.getType() != "hidden") {
        if (args.length > i) {
    	    value = args[i];
    	    i++;
        }
        else {
            console.writeLine "\t%1\$-35s %2\$-25s %3\$-25s", label, (value ? "\t[default="+value+"]" : ""), options ? "[" + options + "]": ""
    	    value = console.readLine(": ");
    	}
        if (value == null || value == "") {
    	    value = it.getFieldValue();
        }
        
        switch (it.getType()) {
    	    case "text-single":
    		req.addTextSingleField(it.getVar(), value);
    		break;
    	    case "text-multi":
    		req.addTextMultiField(it.getVar(), (value && !value.isEmpty()) ? value.split(",") : new String[0]);
    		break;
    	    case "text-private":
    		req.addTextPrivateField(it.getVar(), value);
    		break;
    	    case "jid-single":
    		req.addJidSingleField(it.getVar(), (value && !value.isEmpty()) ? JID.jidInstance(value) : null);
    		break;
    	    case "jid-multi":
    		if (value != null) {
    		    def v = value;
    		    value = [];
    		    v.split(",").each { sjid -> value += JID.jidInstance(sjid); }
    		    value = value as JID[];
    		}
    		req.addJidMultiField(it.getVar(), value);
    		break;
    	    case "list-single":
    		if (value != null && value != "") {
    		    def opts = it.getChildren("option").findAll { option -> option.getFirstChild().getValue() == value }
    		    if (opts == null || opts.isEmpty()) {
    			console.writeLine "option '" + value + "' is not allowed for field = '" + label + "'"
    			return null;
    		    }
    		}
    		req.addListSingleField(it.getVar(), value);
    		break;
    	    case "list-multi":
    		if (value != null && value != "") {
    		    def v = value.split(",");
    		    value = [];
    		    v.each { opt -> 
    			if (it.getChildren("option").findAll { option -> option.getFirstChild().getValue() == opt } ) value += v;
    		    }
    		    if (v.size() != value.size()) {
    			console.writeLine "some options are not allowed for field = '" + label + "'";
    			return null;
    		    }
    		    value = value as String[];
    		}
    		req.addListMultiField(it.getVar(), value);
    		break;
    	    case "boolean":
    		if (value != null) {
    		    value = Boolean.valueOf(value);
    		}
    		req.addBooleanField(it.getVar(), value);
    		break;
    	    default:
    		req.addFixedField(it.getVar(), value);
        }
        
    }
    else {
	req.addHiddenField(it.getVar(), it.getFieldValue());
    }
}

resultPacket = conn.sendSync(packet);

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


