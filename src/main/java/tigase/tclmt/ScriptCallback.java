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
package tigase.tclmt;

import tigase.jaxmpp.core.client.AsyncCallback;
import tigase.jaxmpp.core.client.XMPPException.ErrorCondition;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;
import tigase.jaxmpp.j2se.Jaxmpp;

public class ScriptCallback implements AsyncCallback {
    
    private final Jaxmpp jaxmpp;
    private Stanza result = null;

    ScriptCallback(Jaxmpp jaxmpp) {
        this.jaxmpp = jaxmpp;
    }

    public void onError(Stanza responseStanza, ErrorCondition error) throws JaxmppException {
        result = responseStanza;
        finished();
    }

    public void onSuccess(Stanza responseStanza) throws JaxmppException {
        result = responseStanza;
        finished();
    }

    public void onTimeout() throws JaxmppException {        
        finished();
    }
    
    public void finished() {
        synchronized(jaxmpp) {
            jaxmpp.notifyAll(); 
        }        
    }
    
    public Stanza getResult() {
        return result;
    }
}
