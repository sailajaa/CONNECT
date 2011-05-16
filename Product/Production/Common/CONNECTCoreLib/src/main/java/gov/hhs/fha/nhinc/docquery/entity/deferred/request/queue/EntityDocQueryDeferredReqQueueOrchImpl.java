/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011(Year date of delivery) United States Government, as represented by the Secretary of Health and Human Services.  All rights reserved.
 *
 */
package gov.hhs.fha.nhinc.docquery.entity.deferred.request.queue;

import gov.hhs.fha.nhinc.async.AsyncMessageProcessHelper;
import gov.hhs.fha.nhinc.asyncmsgs.dao.AsyncMsgRecordDao;
import gov.hhs.fha.nhinc.common.nhinccommon.AcknowledgementType;
import gov.hhs.fha.nhinc.common.nhinccommon.AssertionType;
import gov.hhs.fha.nhinc.common.nhinccommon.HomeCommunityType;
import gov.hhs.fha.nhinc.common.nhinccommon.NhinTargetCommunitiesType;
import gov.hhs.fha.nhinc.common.nhinccommon.NhinTargetSystemType;
import gov.hhs.fha.nhinc.common.nhinccommonentity.RespondingGatewayCrossGatewayQueryRequestType;
import gov.hhs.fha.nhinc.connectmgr.ConnectionManagerCache;
import gov.hhs.fha.nhinc.connectmgr.ConnectionManagerException;
import gov.hhs.fha.nhinc.connectmgr.data.CMUrlInfos;
import gov.hhs.fha.nhinc.docquery.DocQueryAuditLog;
import gov.hhs.fha.nhinc.docquery.DocQueryPolicyChecker;
import gov.hhs.fha.nhinc.docquery.entity.EntityDocQueryOrchImpl;
import gov.hhs.fha.nhinc.docquery.passthru.deferred.response.proxy.PassthruDocQueryDeferredResponseProxy;
import gov.hhs.fha.nhinc.docquery.passthru.deferred.response.proxy.PassthruDocQueryDeferredResponseProxyObjectFactory;
import gov.hhs.fha.nhinc.nhinclib.NhincConstants;
import gov.hhs.fha.nhinc.nhinclib.NullChecker;
import gov.hhs.fha.nhinc.transform.document.DocQueryAckTranforms;
import gov.hhs.healthit.nhin.DocQueryAcknowledgementType;
import oasis.names.tc.ebxml_regrep.xsd.query._3.AdhocQueryRequest;
import oasis.names.tc.ebxml_regrep.xsd.query._3.AdhocQueryResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author narendra.reddy
 */
public class EntityDocQueryDeferredReqQueueOrchImpl {

    private static final Log log = LogFactory.getLog(EntityDocQueryDeferredReqQueueOrchImpl.class);

    protected AsyncMessageProcessHelper createAsyncProcesser() {
        return new AsyncMessageProcessHelper();
    }

    /**
     * 
     * @param msg
     * @param assertion
     * @param targets
     * @return DocQueryAcknowledgementType
     */
    public DocQueryAcknowledgementType respondingGatewayCrossGatewayQuery(AdhocQueryRequest msg, AssertionType assertion, NhinTargetCommunitiesType targets) {
        DocQueryAcknowledgementType respAck = new DocQueryAcknowledgementType();
        String ackMsg = "";

        RespondingGatewayCrossGatewayQueryRequestType respondingGatewayCrossGatewayQueryRequestType = new RespondingGatewayCrossGatewayQueryRequestType();
        respondingGatewayCrossGatewayQueryRequestType.setAdhocQueryRequest(msg);
        respondingGatewayCrossGatewayQueryRequestType.setAssertion(assertion);
        respondingGatewayCrossGatewayQueryRequestType.setNhinTargetCommunities(targets);

        String responseCommunityID = null;
        if (targets != null &&
                targets.getNhinTargetCommunity() != null &&
                targets.getNhinTargetCommunity().size() > 0 &&
                targets.getNhinTargetCommunity().get(0) != null &&
                targets.getNhinTargetCommunity().get(0).getHomeCommunity() != null) {
            responseCommunityID = targets.getNhinTargetCommunity().get(0).getHomeCommunity().getHomeCommunityId();
        }

        // Audit the incoming doc query request Message
        DocQueryAuditLog auditLogger = new DocQueryAuditLog();
        AcknowledgementType ack = auditLogger.auditDQRequest(msg, assertion, NhincConstants.AUDIT_LOG_INBOUND_DIRECTION, NhincConstants.AUDIT_LOG_ENTITY_INTERFACE, responseCommunityID);

        // ASYNCMSG PROCESSING - RSPPROCESS
        AsyncMessageProcessHelper asyncProcess = createAsyncProcesser();
        String messageId = "";
        if (assertion.getRelatesToList() != null &&
                assertion.getRelatesToList().size() > 0 &&
                assertion.getRelatesToList().get(0) != null) {
            messageId = assertion.getRelatesToList().get(0);
        }
        boolean bIsQueueOk = asyncProcess.processMessageStatus(messageId, AsyncMsgRecordDao.QUEUE_STATUS_RSPPROCESS);

        if (bIsQueueOk) {
            try {
                CMUrlInfos urlInfoList = getEndpoints(targets);

                if (urlInfoList != null &&
                        NullChecker.isNotNullish(urlInfoList.getUrlInfo()) &&
                        urlInfoList.getUrlInfo().get(0) != null &&
                        NullChecker.isNotNullish(urlInfoList.getUrlInfo().get(0).getHcid()) &&
                        NullChecker.isNotNullish(urlInfoList.getUrlInfo().get(0).getUrl())) {

                    HomeCommunityType targetHcid = new HomeCommunityType();
                    targetHcid.setHomeCommunityId(urlInfoList.getUrlInfo().get(0).getHcid());

                    if (isPolicyValid(msg, assertion, targetHcid)) {
                        NhinTargetSystemType target = new NhinTargetSystemType();
                        target.setUrl(urlInfoList.getUrlInfo().get(0).getUrl());


                        // Get the AdhocQueryResponse by passing the request to the QD Sync Flow.
                        AdhocQueryResponse response = null;
                        EntityDocQueryOrchImpl orchImpl = new EntityDocQueryOrchImpl();
                        response = orchImpl.respondingGatewayCrossGatewayQuery(msg, assertion, targets);

                        PassthruDocQueryDeferredResponseProxyObjectFactory factory = new PassthruDocQueryDeferredResponseProxyObjectFactory();
                        PassthruDocQueryDeferredResponseProxy proxy = factory.getPassthruDocQueryDeferredResponseProxy();


                        respAck = proxy.respondingGatewayCrossGatewayQuery(response, assertion, target);
                    } else {
                        ackMsg = "Outgoing Policy Check Failed";
                        log.error(ackMsg);
                        respAck = DocQueryAckTranforms.createAckMessage(NhincConstants.DOC_QUERY_DEFERRED_RESP_ACK_FAILURE_STATUS_MSG, NhincConstants.DOC_QUERY_DEFERRED_ACK_ERROR_AUTHORIZATION, ackMsg);
                        asyncProcess.processAck(messageId, AsyncMsgRecordDao.QUEUE_STATUS_REQSENTERR, AsyncMsgRecordDao.QUEUE_STATUS_REQSENTERR, respAck);
                    }
                } else {
                    ackMsg = "Failed to obtain target URL from connection manager";
                    log.error(ackMsg);
                    respAck = DocQueryAckTranforms.createAckMessage(NhincConstants.DOC_QUERY_DEFERRED_RESP_ACK_FAILURE_STATUS_MSG, NhincConstants.DOC_QUERY_DEFERRED_ACK_ERROR_INVALID, ackMsg);
                    asyncProcess.processAck(messageId, AsyncMsgRecordDao.QUEUE_STATUS_REQSENTERR, AsyncMsgRecordDao.QUEUE_STATUS_REQSENTERR, respAck);
                }
            } catch (Exception e) {
                ackMsg = "Exception processing Deferred Query For Documents: " + e.getMessage();
                log.error(ackMsg, e);
                respAck = DocQueryAckTranforms.createAckMessage(NhincConstants.DOC_QUERY_DEFERRED_RESP_ACK_FAILURE_STATUS_MSG, NhincConstants.DOC_QUERY_DEFERRED_ACK_ERROR_INVALID, ackMsg);
                asyncProcess.processAck(messageId, AsyncMsgRecordDao.QUEUE_STATUS_REQSENTERR, AsyncMsgRecordDao.QUEUE_STATUS_REQSENTERR, respAck);
            }
        } else {
            ackMsg = "Deferred Patient Discovery response processing halted; deferred queue repository error encountered";

            // Set the error acknowledgement status
            // fatal error with deferred queue repository
            respAck = DocQueryAckTranforms.createAckMessage(NhincConstants.DOC_QUERY_DEFERRED_ACK_ERROR_AUTHORIZATION, NhincConstants.DOC_QUERY_DEFERRED_ACK_ERROR_INVALID, ackMsg);
        }

        // Audit the responding Acknowledgement Message
        ack = auditLogger.logDocQueryAck(respAck, assertion, NhincConstants.AUDIT_LOG_OUTBOUND_DIRECTION, NhincConstants.AUDIT_LOG_ENTITY_INTERFACE, responseCommunityID);

        return respAck;
    }

    /**
     *
     * @param message
     * @param assertion
     * @param target
     * @return boolean
     */
    private boolean isPolicyValid(AdhocQueryRequest message, AssertionType assertion, HomeCommunityType target) {
        boolean policyIsValid = new DocQueryPolicyChecker().checkOutgoingRequestPolicy(message, assertion, target);

        return policyIsValid;
    }

    /**
     *
     * @param targetCommunities
     * @return CMUrlInfos
     */
    private CMUrlInfos getEndpoints(NhinTargetCommunitiesType targetCommunities) {
        CMUrlInfos urlInfoList = null;

        try {
            urlInfoList = ConnectionManagerCache.getEndpontURLFromNhinTargetCommunities(targetCommunities, NhincConstants.NHIN_DOCUMENT_QUERY_DEFERRED_RESP_SERVICE_NAME);
        } catch (ConnectionManagerException ex) {
            log.error("Failed to obtain target URLs", ex);
        }

        return urlInfoList;
    }
}