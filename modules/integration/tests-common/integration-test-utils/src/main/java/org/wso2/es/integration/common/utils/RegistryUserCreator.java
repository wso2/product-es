package org.wso2.es.integration.common.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.beans.Tenant;
import org.wso2.carbon.automation.engine.context.beans.User;
import org.wso2.carbon.integration.common.admin.client.AuthenticatorClient;
import org.wso2.carbon.integration.common.admin.client.UserManagementClient;
import org.wso2.carbon.user.mgt.common.UserAdminException;
import org.wso2.carbon.user.mgt.stub.UserAdminUserAdminException;

import javax.xml.xpath.XPathExpressionException;
import java.rmi.RemoteException;


public class RegistryUserCreator {
    private static final Log log = LogFactory.getLog(RegistryUserCreator.class);
    private UserManagementClient userAdminStub;
    private String sessionCookie;


    protected static String login(String userName, String password, String hostName)
            throws RemoteException, LoginAuthenticationExceptionException {
        AuthenticatorClient loginClient = new AuthenticatorClient(hostName);
        return loginClient.login(userName, password, hostName);
    }

    public void deleteUsers(String adminUserKey, String userName) throws Exception {
        setInfoRolesAndUsers(adminUserKey);
        userAdminStub.deleteUser(userName);
    }

    public void addUser(String adminUserKey, String userName, String userPassword, String roleName)
            throws Exception {
        setInfoRolesAndUsers(adminUserKey);
        try {
            String roles[] = {roleName};
            userAdminStub.addUser(userName, userPassword, roles, null);
        } catch (UserAdminUserAdminException e) {
            log.error("Add user fail" + e);
            throw new UserAdminException("Add user fail" + e);
        }
    }

    public void setInfoRolesAndUsers(String  adminUserKey)
            throws LoginAuthenticationExceptionException, RemoteException,
                   XPathExpressionException {
        //todo -
//        FrameworkProperties isProperties = FrameworkFactory.getFrameworkProperties(ProductConstant.IS_SERVER_NAME);
        AutomationContext isContext = new AutomationContext("IS", "is", "carbon.supper", adminUserKey);
        Tenant userAdminDetails = isContext.getContextTenant();
        sessionCookie = login(userAdminDetails.getContextUser().getUserName(), userAdminDetails.getContextUser().getPassword(),
                              isContext.getContextUrls().getBackEndUrl());
        userAdminStub = new UserManagementClient(isContext.getContextUrls().getBackEndUrl(), sessionCookie);


    }

}
