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
 SCR:CommandId:ssl-certificate-add
 SCR:Description:Adds SSL certificate for domain
 SCR:Help:domain cert_file
 Example: bin/tclmt.sh -u user@domain -p password -s node ssl-certificate-add domain.com /xyz/domain.pem
 */

import tigase.tclmt.*
import tigase.jaxmpp.core.client.*
import tigase.jaxmpp.core.client.xml.*
import tigase.jaxmpp.core.client.xmpp.stanzas.*

def CMD_ID = "ssl-certificate-add"

def MARKER = "command-marker"
def VHOST = "VHost"
def CERTIFICATE = "Certificate in PEM format"
def SAVE_TO_DISK = "Save to disk"

def conn = connection;

def domain = (args != null && args.length > 0) ? args[0] : console.readLine("Domain:");
def file = (args != null && args.length > 1) ? args[1] : console.readLine("Certificate file:");

def certLines = new File(file).text.split("\n");

def packet = new Command(null);
packet.setAttribute("to", "vhost-man@"+serverName);
Command.setNode(packet, CMD_ID);

def data = Command.getData(packet);

data.addHiddenField(MARKER, MARKER);
data.addTextSingleField(VHOST, domain);
data.addTextMultiField(CERTIFICATE, certLines);
data.addBooleanField(SAVE_TO_DISK, true);

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

