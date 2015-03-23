/*
 * Copyright (c) 2009-2014, United States Government, as represented by the Secretary of Health and Human Services.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above
 *       copyright notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the documentation
 *       and/or other materials provided with the distribution.
 *     * Neither the name of the United States Government nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE UNITED STATES GOVERNMENT BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package gov.hhs.fha.nhinc.admingui.universalclient.managed;

import gov.hhs.fha.nhinc.admingui.constant.NavigationConstant;
import gov.hhs.fha.nhinc.admingui.managed.TabBean;
import gov.hhs.fha.nhinc.admingui.universalclient.metadata.Document;
import gov.hhs.fha.nhinc.admingui.universalclient.metadata.Patient;
import gov.hhs.fha.nhinc.admingui.universalclient.metadata.SearchCriteria;
import java.io.IOException;
import java.util.Date;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.log4j.Logger;

/**
 *
 * @author sadusumilli
 */
@ManagedBean(name = "universalClientBean")
@SessionScoped
public class UniversalClientBean {

    private static Logger log = Logger.getLogger(UniversalClientBean.class);

    private String firstName = null;
    private String lastName = null;
    private Date dateOfBirth;
    private String gender = null;
    private String organization = null;
    public Patient foundPatients;
    private boolean patientFound = false;
    private boolean documentFound = false;
    private String documenttitle = null;
    private String documenttype = null;
    private Date docCreationTimeFrom;
    private Date docCreationTimeTo;
    private List<Document> foundDocument;

    /**
     * default constructor
     */
    public UniversalClientBean() {
    }

    /**
     *
     * @return
     */
    public String patientSearch() {
        log.debug("Searching for patient: " + getLastName() + ", " + getFirstName());
        SearchCriteria searchCriteria = createSearchCriteria();
        List<Patient> patientsList = buildPatientListFromDatabase();
        patientFound = findPatient(patientsList, searchCriteria);
        if (patientFound) {
            return NavigationConstant.UNIVERSAL_CLIENT;

        } else {

            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "No Patient Found: ", ""));
            patientFound = false;
            log.error("Error in Search Patient: ");
            return "No Patient found";
        }

    }

    /**
     *
     * @return
     */
    public String documentSearch() {

        SearchCriteria docSearchCriteria = createSearchCriteria();
        List<Document> documentList = foundPatients.getPatientDocuments();
        documentFound = findDocument(documentList, docSearchCriteria);
        if (documentFound) {
            return NavigationConstant.UNIVERSAL_CLIENT;
        } else {

            FacesContext.getCurrentInstance().addMessage("noDocumentFoundMsg",
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "No Document Found: ", ""));
            documentFound = false;
            return "No documents found";

        }
    }

    /**
     *
     * @param documentList
     * @param searchCriteria
     * @return
     */
    public boolean findDocument(List<Document> documentList, SearchCriteria searchCriteria) {
        Iterator<Document> documentIterator = documentList.iterator();
        foundDocument = new ArrayList<Document>();
        while (documentIterator.hasNext()) {
            Document doc = documentIterator.next();
            if (doc != null) {
                if ((foundPatients.getPatientDocuments() != null) || (doc.getDocumentTitle().equalsIgnoreCase(searchCriteria.getDocumentTitle()))) {
                    foundDocument.add(doc);
                    documentFound = true;
                    break;
                }
            }
        }
        if (documentFound) {
            return documentFound;
        } else {
            documentFound = false;
        }

        return documentFound;
    }

    /**
     *
     * @param patientsList
     * @param searchCriteria
     * @return
     */
    public boolean findPatient(List<Patient> patientsList, SearchCriteria searchCriteria) {
        Iterator<Patient> patientsIterator = patientsList.iterator();
        foundPatients = new Patient();
        while (patientsIterator.hasNext()) {
            Patient patient = patientsIterator.next();
            if (patient != null) {
                if ((patient.getFirstName().equalsIgnoreCase(searchCriteria.getFirstName()))) {
                    foundPatients = patient;
                    patientFound = true;
                    break;
                }
            }
        }
        if (patientFound) {
            return patientFound;
        } else {
            patientFound = false;
        }

        return patientFound;
    }

    /**
     *
     * @return
     */
    public String resetPatient() {
        this.patientFound = false;
        this.foundPatients = null;
        clearPatientSearch();
        return NavigationConstant.UNIVERSAL_CLIENT;
    }

    /**
     *
     */
    private void clearPatientSearch() {
        this.organization = null;
        this.firstName = null;
        this.lastName = null;
        this.gender = null;
        this.dateOfBirth = null;
    }

    /**
     *
     * @return
     */
    public String goBackToPatientSearch() {

        FacesContext.getCurrentInstance().getApplication().createValueBinding("#{universalClientBean}").setValue(FacesContext.getCurrentInstance(), null);
        return NavigationConstant.UNIVERSAL_CLIENT;
    }

    public SearchCriteria createSearchCriteria() {
        SearchCriteria searchCriteria = new SearchCriteria();
        if (this.firstName != null) {
            searchCriteria.setFirstName(this.firstName);
        }
        if (this.lastName != null) {
            searchCriteria.setLastName(this.lastName);
        }
        if (this.dateOfBirth != null) {
            searchCriteria.setDateOfBirth(this.dateOfBirth);
        }
        if (this.organization != null) {
            searchCriteria.setOrganization(this.organization);
        }
        if (this.gender != null) {
            searchCriteria.setGender(this.gender);
        }
        return searchCriteria;
    }

    public List<Patient> buildPatientListFromDatabase() {
        List<Patient> patientsList = new ArrayList<Patient>();

        Patient p1 = new Patient();
        p1.setPatientid("1");
        p1.setFirstName("connect");
        p1.setLastName("connect");
        p1.setDateOfBirth(new Date(03 - 12 - 2015));
        p1.setOrganization("1234325");
        p1.setGender("Male");

        List<Document> p1Documents = new ArrayList<Document>();
        Document p1d1 = new Document();
        p1d1.setDocumentId("1232346");
        p1d1.setDocumentTitle("Clinical Patient Document 1");
        p1d1.setDocumentType("Clinical Document-1");
        p1d1.setCreatedTimeFrom(new Date(12 / 12 / 2013));
        p1d1.setCreatedTimeTo(new Date(12 / 12 / 2014));

        Document p1d2 = new Document();
        p1d2.setDocumentId("2345C234");
        p1d2.setDocumentTitle("Clinical Patient Document 2");
        p1d1.setDocumentType("Clinical Document-2");
        p1d1.setCreatedTimeFrom(new Date(12 / 12 / 2013));
        p1d1.setCreatedTimeTo(new Date(12 / 12 / 2014));

        p1Documents.add(p1d1);
        p1Documents.add(p1d2);
        p1.setPatientDocuments(p1Documents);

        Patient p2 = new Patient();
        p2.setPatientid("2");
        p2.setFirstName("bob");
        p2.setLastName("bob");
        p1.setDateOfBirth(new Date(2013 / 12 / 12));
        p2.setOrganization("1234325");
        p2.setGender("Female");

        patientsList.add(p1);
        patientsList.add(p2);

        return patientsList;
    }

    /**
     *
     * @return
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     *
     * @param firstName
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     *
     * @return
     */
    public String getLastName() {
        return lastName;
    }

    /**
     *
     * @param lastName
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     *
     * @return
     */
    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    /**
     *
     * @param dateOfBirth
     */
    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    /**
     *
     * @return
     */
    public String getGender() {
        return gender;
    }

    /**
     *
     * @param gender
     */
    public void setGender(String gender) {
        this.gender = gender;
    }

    /**
     *
     * @return
     */
    public Patient getFoundPatients() {
        return foundPatients;
    }

    /**
     *
     * @param foundPatients
     */
    public void setFoundPatients(Patient foundPatients) {
        this.foundPatients = foundPatients;
    }

    /**
     *
     * @return
     */
    public boolean isPatientFound() {
        return patientFound;
    }

    /**
     *
     * @param patientFound
     */
    public void setPatientFound(boolean patientFound) {
        this.patientFound = patientFound;
    }

    /**
     *
     * @return
     */
    public String getOrganization() {
        return organization;
    }

    /**
     *
     * @param organization
     */
    public void setOrganization(String organization) {
        this.organization = organization;
    }

    /**
     *
     * @return
     */
    public boolean isDocumentFound() {
        return documentFound;
    }

    /**
     *
     * @param documentFound
     */
    public void setDocumentFound(boolean documentFound) {
        this.documentFound = documentFound;
    }

    /**
     *
     * @return
     */
    public String getDocumenttitle() {
        return documenttitle;
    }

    /**
     *
     * @param documenttitle
     */
    public void setDocumenttitle(String documenttitle) {
        this.documenttitle = documenttitle;
    }

    /**
     *
     * @return
     */
    public String getDocumenttype() {
        return documenttype;
    }

    /**
     *
     * @param documenttype
     */
    public void setDocumenttype(String documenttype) {
        this.documenttype = documenttype;
    }

    /**
     *
     * @return
     */
    public Date getDocCreationTimeFrom() {
        return docCreationTimeFrom;
    }

    /**
     *
     * @param docCreationTimeFrom
     */
    public void setDocCreationTimeFrom(Date docCreationTimeFrom) {
        this.docCreationTimeFrom = docCreationTimeFrom;
    }

    /**
     *
     * @return
     */
    public Date getDocCreationTimeTo() {
        return docCreationTimeTo;
    }

    /**
     *
     * @param docCreationTimeTo
     */
    public void setDocCreationTimeTo(Date docCreationTimeTo) {
        this.docCreationTimeTo = docCreationTimeTo;
    }

    /**
     *
     * @return
     */
    public List<Document> getFoundDocument() {
        return foundDocument;
    }

    /**
     *
     * @param foundDocument
     */
    public void setFoundDocument(List<Document> foundDocument) {
        this.foundDocument = foundDocument;
    }

}
