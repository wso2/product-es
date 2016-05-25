/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/

package org.wso2.es.integration.common.clients;

import org.apache.axis2.AxisFault;
import org.apache.axis2.databinding.types.URI;
import org.wso2.carbon.governance.notifications.worklist.stub.WorkListServiceStub;
import org.wso2.carbon.humantask.stub.ui.task.client.api.HumanTaskClientAPIAdminStub;
import org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault;
import org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault;
import org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalOperationFault;
import org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault;
import org.wso2.carbon.humantask.stub.ui.task.client.api.types.TSimpleQueryCategory;
import org.wso2.carbon.humantask.stub.ui.task.client.api.types.TSimpleQueryInput;
import org.wso2.carbon.humantask.stub.ui.task.client.api.types.TTaskSimpleQueryResultRow;
import org.wso2.carbon.humantask.stub.ui.task.client.api.types.TTaskSimpleQueryResultSet;
import org.wso2.carbon.user.mgt.stub.UserAdminStub;

import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.List;


public class HumanTaskAdminClient {

	private HumanTaskClientAPIAdminStub htStub;
	private UserAdminStub umStub;
	private WorkListServiceStub wlStub;

	public HumanTaskAdminClient(String backEndUrl, String sessionCookie) throws AxisFault {
		htStub = new HumanTaskClientAPIAdminStub(backEndUrl + "HumanTaskClientAPIAdmin");
		AuthenticateStub.authenticateStub(sessionCookie, htStub);

		umStub = new UserAdminStub(backEndUrl + "UserAdmin");
		AuthenticateStub.authenticateStub(sessionCookie, umStub);

		wlStub = new WorkListServiceStub(backEndUrl + "WorkListService");
		AuthenticateStub.authenticateStub(sessionCookie, wlStub);
	}

	public HumanTaskAdminClient(String backEndUrl, String userName, String password) throws AxisFault {

		htStub = new HumanTaskClientAPIAdminStub(backEndUrl + "HumanTaskClientAPIAdmin");
		AuthenticateStub.authenticateStub(userName, password, htStub);

		umStub = new UserAdminStub(backEndUrl + "UserAdmin");
		AuthenticateStub.authenticateStub(userName, password, umStub);

		wlStub = new WorkListServiceStub(backEndUrl + "WorkListService");
		AuthenticateStub.authenticateStub(userName, password, wlStub);
	}


	public WorkItem[] getWorkItems()
			throws IllegalStateFault, IllegalAccessFault, RemoteException, IllegalArgumentFault {

		TSimpleQueryInput queryInput = new TSimpleQueryInput();
		queryInput.setPageNumber(0);
		queryInput.setSimpleQueryCategory(TSimpleQueryCategory.ALL_TASKS);

		TTaskSimpleQueryResultSet resultSet = htStub.simpleQuery(queryInput);
		if (resultSet == null || resultSet.getRow() == null || resultSet.getRow().length == 0) {
			return new WorkItem[0];
		}
		List<WorkItem> workItems = new LinkedList<>();
		for (TTaskSimpleQueryResultRow row : resultSet.getRow()) {
			URI id = row.getId();
			String taskUser = "";
			//Ready state notification doesn't have taskUser
			if (htStub.loadTask(id) != null && htStub.loadTask(id).getActualOwner() != null) {
				taskUser = htStub.loadTask(id).getActualOwner().getTUser();
			}

			workItems.add(new WorkItem(id, row.getPresentationSubject(),
					row.getPresentationName(), row.getPriority(), row.getStatus(),
					row.getCreatedTime(), taskUser));
		}
		return workItems.toArray(new WorkItem[workItems.size()]);
	}

	/**
	 * change the workItem status to Complete. it will hide from the management console
	 *
	 * @param id
	 * @throws RemoteException
	 * @throws IllegalStateFault
	 * @throws IllegalOperationFault
	 * @throws IllegalArgumentFault
	 * @throws IllegalAccessFault
	 */
	public void completeTask(URI id) throws RemoteException, IllegalStateFault,
			IllegalOperationFault, IllegalArgumentFault, IllegalAccessFault {
		htStub.start(id);
		htStub.complete(id, "<WorkResponse>true</WorkResponse>");
	}
}
