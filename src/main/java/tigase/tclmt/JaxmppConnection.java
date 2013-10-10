/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tigase.tclmt;

import java.util.logging.Level;
import tigase.jaxmpp.core.client.Connector;
import tigase.jaxmpp.core.client.Connector.ConnectorEvent;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.modules.presence.PresenceModule;
import tigase.jaxmpp.core.client.xmpp.modules.roster.RosterModule;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;
import tigase.jaxmpp.j2se.Jaxmpp;
import tigase.jaxmpp.j2se.connectors.socket.SocketConnector;

/**
 *
 * @author andrzej
 */
public class JaxmppConnection extends Jaxmpp implements SynchronizedConnection {

        private final ConsoleIfc console;

        JaxmppConnection(ConsoleIfc console) {
                this.console = console;
                
                PresenceModule presenceModule = getModulesManager().getModule(PresenceModule.class);
                getModulesManager().unregister(presenceModule);
                
                RosterModule rosterModule = getModulesManager().getModule(RosterModule.class);
                getModulesManager().unregister(rosterModule);
				
				this.getSessionObject().setUserProperty(SocketConnector.COMPRESSION_DISABLED_KEY, true);
        }

        public Stanza sendSync(Stanza stanza) throws XMLException, JaxmppException {
                return this.sendSync(stanza, null);
        }

        public Stanza sendSync(Stanza stanza, final Long timeout) throws XMLException, JaxmppException {
                ScriptCallback callback = new ScriptCallback(this);
                console.writeLine("awaiting response...");
                send(stanza, callback);

                try {
                        synchronized (this) {
                                if (timeout != null) {
                                        this.wait(timeout);
                                }
                                else {
                                        this.wait();
                                }
                        }
                }
                catch (InterruptedException ex) {
                        log.log(Level.SEVERE, null, ex);
                }

                return callback.getResult();
        }

};
