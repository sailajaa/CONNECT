package gov.hhs.fha.nhinc.docsubmission.nhin.deferred.response;

import gov.hhs.fha.nhinc.adapter.xdr.async.response.proxy.AdapterXDRResponseProxy;
import gov.hhs.fha.nhinc.adapter.xdr.async.response.proxy.AdapterXDRResponseProxyObjectFactory;
import gov.hhs.fha.nhinc.async.AsyncMessageIdExtractor;
import gov.hhs.fha.nhinc.common.nhinccommon.AcknowledgementType;
import gov.hhs.fha.nhinc.common.nhinccommon.AssertionType;
import gov.hhs.fha.nhinc.nhinclib.NhincConstants;
import gov.hhs.fha.nhinc.properties.PropertyAccessException;
import gov.hhs.fha.nhinc.properties.PropertyAccessor;
import gov.hhs.fha.nhinc.saml.extraction.SamlTokenExtractor;
import javax.xml.ws.WebServiceContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import gov.hhs.fha.nhinc.docsubmission.XDRAuditLogger;
import gov.hhs.fha.nhinc.docsubmission.XDRPolicyChecker;
import gov.hhs.healthit.nhin.XDRAcknowledgementType;
import oasis.names.tc.ebxml_regrep.xsd.rs._3.RegistryResponseType;

/**
 *
 * @author patlollav
 */
public class NhinXDRResponseImpl
{
    private static final Log logger = LogFactory.getLog(NhinXDRResponseImpl.class);

    /**
     *
     * @return
     */
    protected Log getLogger(){
        return logger;
    }

    /**
     *
     * @param body
     * @param context
     * @return
     */
    public XDRAcknowledgementType provideAndRegisterDocumentSetBResponse(RegistryResponseType body,WebServiceContext context ) {

        XDRAcknowledgementType result = new XDRAcknowledgementType();
        RegistryResponseType regResp = new RegistryResponseType();
        regResp.setStatus(NhincConstants.XDR_ACK_STATUS_MSG);
        result.setMessage(regResp);

        getLogger().debug("Entering provideAndRegisterDocumentSetBResponse");

        AssertionType assertion = createAssertion(context);

        // Extract the message id value from the WS-Addressing Header and place it in the Assertion Class
        if (assertion != null) {
            assertion.setAsyncMessageId(extractMessageId(context));
        }

        AcknowledgementType ack = getXDRAuditLogger().auditNhinXDRResponse(body, assertion, NhincConstants.AUDIT_LOG_INBOUND_DIRECTION);

        getLogger().debug("Audit Log Ack Message:" + ack.getMessage());

        String localHCID = retrieveHomeCommunityID();

        getLogger().debug("Local Home Community ID: " + localHCID);

        if (isPolicyOk(body, assertion, assertion.getHomeCommunity().getHomeCommunityId(), localHCID))
        {
            getLogger().debug("Policy Check Succeeded");
            result = forwardToAgency(body, assertion);
        }
        else
        {
            getLogger().error("Policy Check Failed");
        }

        ack = getXDRAuditLogger().auditAcknowledgement(result, assertion, NhincConstants.AUDIT_LOG_OUTBOUND_DIRECTION, NhincConstants.XDR_RESPONSE_ACTION);

        getLogger().debug("Audit Log Ack Message for Outbound Acknowledgement:" + ack.getMessage());

        getLogger().debug("Exiting provideAndRegisterDocumentSetBResponse");

         return result;
    }


        /**
     *
     * @param context
     * @return
     */
    protected AssertionType createAssertion(WebServiceContext context){
        AssertionType assertion = SamlTokenExtractor.GetAssertion(context);
        return assertion;
    }

    /**
     *
     * @return
     */
    protected String retrieveHomeCommunityID(){
        String localHCID = null;
        try {
            localHCID = PropertyAccessor.getProperty(NhincConstants.GATEWAY_PROPERTY_FILE, NhincConstants.HOME_COMMUNITY_ID_PROPERTY);
        } catch (PropertyAccessException ex) {
            logger.error("Exception while retrieving home community ID", ex);
        }

        return localHCID;
    }
    /**
     *
     * @return
     */
    protected XDRAuditLogger getXDRAuditLogger(){
        return new XDRAuditLogger();
    }

    /**
     *
     * @param body
     * @param context
     * @return
     */
    protected XDRAcknowledgementType forwardToAgency(RegistryResponseType body, AssertionType assertion)
    {
        getLogger().debug("Entering forwardToAgency");

        AdapterXDRResponseProxyObjectFactory factory = new AdapterXDRResponseProxyObjectFactory();

        AdapterXDRResponseProxy proxy = factory.getAdapterXDRResponseProxy();

        XDRAcknowledgementType response = proxy.provideAndRegisterDocumentSetBResponse(body, assertion);

        getLogger().debug("Exiting forwardToAgency");

        return response;
    }

    /**
     *
     * @param newRequest
     * @param assertion
     * @param senderHCID
     * @param receiverHCID
     * @return
     */
    protected boolean isPolicyOk(RegistryResponseType request, AssertionType assertion, String senderHCID, String receiverHCID) {

        boolean isPolicyOk = false;

        getLogger().debug("Check policy");

        XDRPolicyChecker policyChecker = new XDRPolicyChecker();
        isPolicyOk = policyChecker.checkXDRResponsePolicy(request, assertion, senderHCID ,receiverHCID, NhincConstants.POLICYENGINE_INBOUND_DIRECTION);

        getLogger().debug("Response from policy engine: " + isPolicyOk);

        return isPolicyOk;

    }

    protected String extractMessageId (WebServiceContext context) {
        AsyncMessageIdExtractor msgIdExtractor = new AsyncMessageIdExtractor();
        return msgIdExtractor.GetAsyncRelatesTo(context);
    }

}