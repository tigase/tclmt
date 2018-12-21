/*
 * Tigase XMPP Server Command Line Management Tool
 * Copyright (C) 2004-2018 "Tigase, Inc." <office@tigase.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
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
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tigase.tclmt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import tigase.jaxmpp.core.client.UserProperties;
import tigase.jaxmpp.core.client.XmppModulesManager;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.modules.presence.PresenceModule;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;

/**
 *
 * @author andrzej
 */
public class TestSynchronizedConnection implements SynchronizedConnection {

        private final List<Stanza> outgoing = new ArrayList<Stanza>();
        private final List<Stanza> incoming = new ArrayList<Stanza>();
        private final UserProperties userProps = new TestUserProperties();
        private final XmppModulesManager modulesManager;

        public TestSynchronizedConnection() {
                modulesManager = new XmppModulesManager(null, null);
        }
        
        public List<Stanza> getOutgoing() {
                return outgoing;
        }
        
        public void setIncoming(Stanza... stanzas) {
                for (Stanza stanza : stanzas) {
                        incoming.add(stanza);
                }
        }
        
        public Stanza sendSync(Stanza stanza) throws XMLException, JaxmppException {
                outgoing.add(stanza);
                return incoming.remove(0);
        }

        public Stanza sendSync(Stanza stanza, Long timeout) throws XMLException, JaxmppException {
                outgoing.add(stanza);
                return incoming.remove(0);
        }

        public UserProperties getProperties() {
                return userProps;
        }

        public boolean isConnected() {
                return true;
        }

        public void login(boolean sync) throws JaxmppException {
        }

        public void disconnect() throws JaxmppException {
        }

        public XmppModulesManager getModulesManager() {
                return modulesManager;
        }
        
        private class TestUserProperties implements UserProperties {

                private final Map<String,Object> props = new HashMap<String,Object>();
                
                public <T> T getUserProperty(String key) {
                        return (T) props.get(key);
                }

                public UserProperties setUserProperty(String key, Object value) {
                        props.put(key, value);
                        return this;
                }
                
        }
}
