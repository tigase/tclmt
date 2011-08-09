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
