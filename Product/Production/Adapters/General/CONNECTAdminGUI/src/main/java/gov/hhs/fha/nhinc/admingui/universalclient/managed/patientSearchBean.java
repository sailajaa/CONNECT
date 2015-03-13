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
import gov.hhs.fha.nhinc.admingui.universalclient.metadata.Document;
import gov.hhs.fha.nhinc.admingui.universalclient.metadata.Patient;
import gov.hhs.fha.nhinc.admingui.universalclient.metadata.SearchCriteria;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

/**
 *
 * @author sadusumilli
 */
@ManagedBean(name = "patientSearchBean")
@SessionScoped
public class patientSearchBean {

    private static Logger log = Logger.getLogger(patientSearchBean.class);

    private String firstName = null;
    private String lastName = null;
    private String dateOfBirth = null;
    private String gender = null;
    private String organization = null;
    public List<Patient> foundPatients;
    private boolean patientFound = false;
    private String errorMessage = "";
    private boolean documentFound = false;

    /**
     * default constructor
     */
    public patientSearchBean() {
    }

    public String patientSearch() {

        System.out.println("inside patient search");
        SearchCriteria searchCriteria = createSearchCriteria();
        List<Patient> patientsList = buildPatientListFromDatabase();
        patientFound = findPatient(patientsList, searchCriteria);
        if (patientFound) {
            System.out.println("*******************patient found******************" + patientFound);
            return NavigationConstant.UNIVERSAL_CLIENT;
        }

        FacesContext.getCurrentInstance().addMessage("noRecordFoundMsg",
            new FacesMessage(FacesMessage.SEVERITY_ERROR, "No Patient Found: ", ""));
        patientFound = false;
        log.error("Error in Search Patient: ");
        return "No Patient found****";
    }

    public String documentSearch() {

        //foundDocuments = buildDocumentListFromDatabase();
        //if (patientFound) {
        System.out.println("*******************document found******************");
        documentFound = true;
        if (documentFound) {
            return NavigationConstant.UNIVERSAL_CLIENT;
        } else {

            FacesContext.getCurrentInstance().addMessage("noDocumentFoundMsg",
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "No Document Found: ", ""));
            documentFound = false;
            return "No documents found";

        }
        //return documentList;
    }

    public boolean findPatient1(List<Patient> patientsList, SearchCriteria searchCriteria) {
        try {
            //boolean patientFound = false;
            Iterator<Patient> patientsIterator = patientsList.iterator();
            foundPatients = new ArrayList<Patient>();
            while (patientsIterator.hasNext()) {
                Patient patient = patientsIterator.next();
                if (patient != null) {
                    if ((patient.getFirstName().equalsIgnoreCase(searchCriteria.getFirstName()))) {
                        foundPatients.add(patient);
                        patientFound = true;
                        return patientFound;
                    }
                }
            }

        } catch (Exception e) {
            FacesContext.getCurrentInstance().validationFailed();
            FacesContext.getCurrentInstance().addMessage("patientSearchErrors",
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "No Patient Found: " + e.getLocalizedMessage(), ""));
            patientFound = false;
            log.error("Error in Search Patient: " + e.getMessage());
        }

        firstName = null;
        lastName = null;
        return patientFound;
    }

    public boolean findPatient(List<Patient> patientsList, SearchCriteria searchCriteria) {
        Iterator<Patient> patientsIterator = patientsList.iterator();
        foundPatients = new ArrayList<Patient>();
        while (patientsIterator.hasNext()) {
            Patient p = patientsIterator.next();
            if (p != null) {
                if ((p.getFirstName().equalsIgnoreCase(searchCriteria.getFirstName()))) {
                    foundPatients.add(p);
                    patientFound = true;
                }
            }
        }
        if (patientFound) {
            return patientFound;
        } else {
            patientFound = false;
        }

        organization = null;
        firstName = null;
        lastName = null;
        dateOfBirth = null;
        gender = null;

        return patientFound;
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
        p1.setDateOfBirth("03/09/2015");
        p1.setOrganization("1");
        p1.setGender("1");

        List<Document> p1Documents = new ArrayList<Document>();
        Document p1d1 = new Document();
        p1d1.setDocumentId("1");
        p1d1.setDocumentTitle("Clinical Patient Document 1");
        p1d1.setDocumentType("Anual checkup document");
        p1d1.setStartDate("12/12/2012");
        p1d1.setEndDate("12/12/2015");

        Document p1d2 = new Document();
        p1d2.setDocumentId("2");
        p1d2.setDocumentTitle("Clinical Patient Document 2");
        p1d1.setDocumentType("Physical checkup history document");
        p1d1.setStartDate("12/12/2012");
        p1d1.setEndDate("12/12/2015");

        p1Documents.add(p1d1);
        p1Documents.add(p1d2);
        p1.setPatientDocuments(p1Documents);

        Patient p2 = new Patient();
        p2.setPatientid("2");
        p2.setFirstName("bob");
        p2.setLastName("bob");
        p2.setDateOfBirth("11/11/2011");
        p2.setOrganization("2");
        p2.setGender("2");

        patientsList.add(p1);
        patientsList.add(p2);

        return patientsList;
    }

    public void updateTab() {

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
    public String getDateOfBirth() {
        return dateOfBirth;
    }

    /**
     *
     * @param dateOfBirth
     */
    public void setDateOfBirth(String dateOfBirth) {
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

    public List<Patient> getFoundPatients() {
        return foundPatients;
    }

    public void setFoundPatients(List<Patient> foundPatients) {
        this.foundPatients = foundPatients;
    }

    public boolean isPatientFound() {
        return patientFound;
    }

    public void setPatientFound(boolean patientFound) {
        this.patientFound = patientFound;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public boolean isDocumentFound() {
        return documentFound;
    }

    public void setDocumentFound(boolean documentFound) {
        this.documentFound = documentFound;
    }

}
