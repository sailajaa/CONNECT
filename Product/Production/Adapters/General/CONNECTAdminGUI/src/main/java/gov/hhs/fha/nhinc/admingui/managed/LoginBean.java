/*
 *  Copyright (c) 2009-2014, United States Government, as represented by the Secretary of Health and Human Services.
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *      * Redistributions of source code must retain the above
 *        copyright notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the documentation
 *        and/or other materials provided with the distribution.
 *      * Neither the name of the United States Government nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE UNITED STATES GOVERNMENT BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package gov.hhs.fha.nhinc.admingui.managed;

import gov.hhs.fha.nhinc.admingui.constant.NavigationConstant;
import gov.hhs.fha.nhinc.admingui.jee.jsf.UserAuthorizationListener;
import gov.hhs.fha.nhinc.admingui.model.Login;
import gov.hhs.fha.nhinc.admingui.services.LoginService;
import gov.hhs.fha.nhinc.admingui.services.exception.UserLoginException;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The Class LoginBean.
 * 
 * @author sadusumilli
 */
@ManagedBean
@SessionScoped
@Component
public class LoginBean {

	public static final Logger log = Logger.getLogger(LoginBean.class);

	/** The user name. */
	private String userName;

	/** The password. */
	private String password;

	/** The login service. */
	@Autowired
	private LoginService loginService;
	
	private FacesMessage msg;

	/**
	 * Gets the user name. 
	 * @return the user name
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * Sets the user name. 
	 * @param userName
	 * the new user name
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	/**
	 * Gets the password. 
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Sets the password. 
	 * @param password
	 * the new password
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * Instantiates a new login bean.
	 */
	public LoginBean() {
	}

	/**
	 * Invoke patient.
	 * 
	 * @return the string
	 */
	public String invokeLogin() {

		/**if (!checkUsername()) {
			FacesContext.getCurrentInstance().addMessage("username",
					new FacesMessage("Not valid username   "));
			return null;
		}*/

		if (!login()) {
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR,
							"username and password not valid", userName)); 
			return null;
		} 
		//return NavigationConstant.STATUS_PAGE;
		return NavigationConstant.STATUS_PAGE;

		// please build and redeploy ok
		// System.out.println("inside invokelogin");
		// if (!validateFields()) {
		// System.out.println("after the validate fileds check from invokelogin");
		// if (login()) {
		// System.out.println("inside login of invokelogin");
		// return NavigationConstant.STATUS_PAGE;
		// }
		// else{
		// System.out.println("login else condition of invokelogin method");
		// }
		// } else {
		// log.info("incorrect username and password");
		// // return NavigationConstant.LOGIN_PAGE;
		// return null;
		// }
		// return null;
	}

	/**
	 * Validates all of the required fields that are entered. If entered, it
	 * must be valid. this method is used by invokeLogin()
	 * 
	 * @return boolean
	 */
/**	public boolean validateFields() {
		System.out.println("this is from Validatefields method");
		FacesContext context = FacesContext.getCurrentInstance();
		//if (StringUtils.isBlank(getUserName()) || StringUtils.isBlank(getPassword())) {
			if(this.getUserName().contains("$$")){
			// FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,"User Name is Required","loginForm:username");
			//context.addMessage(null, new FacesMessage("Username or password is invalid from bean"));
			
			//addMessage(context,"liabilityLimitsDetailsForm:medicalPayments",ValidationResource.REQUIRED,FacesMessage.SEVERITY_ERROR);
			
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,"User details are not valid...!!!", userName);
			FacesContext.getCurrentInstance().addMessage(null, msg);		  
		}
		System.out.println("return value from bean"+context.getMessages().hasNext());
		return context.getMessages().hasNext();
	}*/
	public boolean checkUsername(){
		return userName.contains("9");
	
	}
	
	public boolean validateFields() {
		System.out.println("this is from Validatefields method");
		// if (StringUtils.isBlank(getUserName()) ||
		// StringUtils.isBlank(getPassword())) {
		if ("connectadmin".equals(userName) && "password".equals(password)) {
			return true;
		} else {
			return false;
		}
	}

	
	/**
	 * Logout. 
	 * @return the string
	 */
	public String logout() {
		userName = null;
		password = null;

		FacesContext facesContext = FacesContext.getCurrentInstance();
		HttpSession session = (HttpSession) facesContext.getExternalContext()
				.getSession(false);
		if (session != null) {
			session.invalidate();
		}
		return NavigationConstant.LOGIN_PAGE;
	}

	/**
	 * Login.
	 * 
	 * @return true, if successful
	 */
	private boolean login() {
		boolean loggedIn = false;
		Login login = new Login(userName, password);
		try {
			loggedIn = loginService.login(login);
			System.out.println("status after the loginservice call"+loggedIn);
			if (loggedIn) {
				System.out.println("inside the loggedin if"+loggedIn);
				FacesContext facesContext = FacesContext.getCurrentInstance();
				HttpSession session = (HttpSession) facesContext
						.getExternalContext().getSession(false);
				session.setAttribute(
						UserAuthorizationListener.USER_INFO_SESSION_ATTRIBUTE,
						login);
			}
		} catch (UserLoginException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("before final return loggedin from login"+loggedIn);
		return loggedIn;
	}

	public FacesMessage getMsg() {
		return msg;
	}

	public void setMsg(FacesMessage msg) {
		this.msg = msg;
	}
}
