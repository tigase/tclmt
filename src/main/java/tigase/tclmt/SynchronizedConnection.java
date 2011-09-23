/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tigase.tclmt;

import tigase.jaxmpp.core.client.UserProperties;
import tigase.jaxmpp.core.client.XmppModulesManager;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;

/**
 *
 * @author andrzej
 */
public interface SynchronizedConnection {

        public Stanza sendSync(Stanza stanza) throws XMLException, JaxmppException;
        
        public Stanza sendSync(Stanza stanza, Long timeout) throws XMLException, JaxmppException;        
        
        public UserProperties getProperties();
        
        public boolean isConnected();
        
        public void login(boolean sync) throws JaxmppException;
        
        public void disconnect() throws JaxmppException;
        
        public XmppModulesManager getModulesManager();
}
