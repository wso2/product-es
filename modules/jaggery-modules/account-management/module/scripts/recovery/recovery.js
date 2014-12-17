/*
 *  Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

var recovery = {};
(function (recovery) {

       var userInformationRecoveryService = new Packages.org.wso2.carbon.identity.mgt.services.UserInformationRecoveryService();
       var userIdentityManagementAdminService = new Packages.org.wso2.carbon.identity.mgt.services.UserIdentityManagementAdminService();
       var EMAIL_NOTIFICATION = "email";
       var log = new Log("jaggery-modules.account-management.recovery");

       /**
        * Get new captcha info
        * @return captcha info
        */
       recovery.getCaptcha = function () {
              try {
                     return userInformationRecoveryService.getCaptcha();
              } catch (e) {
                     log.error("Retrieving captcha failed", e);
                     throw(e);
              }
       };

       /**
        * Verify user
        * @param username user name to be verified
        * @param captcha captcha info
        * @return verification info
        */
       recovery.verifyUser = function (username, captcha) {
              try {
                     return userInformationRecoveryService.verifyUser(username, captcha);
              } catch (e) {
                     log.error("User verification failed for user:" + username, e);
                     throw(e);
              }
       };

       /**
        * Send recovery notification to user
        * @param username user name
        * @param key verification key
        * @param notificationType notification type (email)
        * @return verification info
        */
       recovery.sendRecoveryNotification = function (username, key, notificationType) {
              try {
                     return userInformationRecoveryService.sendRecoveryNotification(username, key, notificationType);
              } catch (e) {
                     log.error("Recovery notification sending failed for user:" + username, e);
                     throw(e);
              }
       };

       /**
        * Verify confirmation code
        * @param username user name
        * @param code confirmation code
        * @param captcha captcha info
        * @return verification info
        */
       recovery.verifyConfirmationCode = function (username, code, captcha) {
              try {
                     return userInformationRecoveryService.verifyConfirmationCode(username, code, captcha);
              } catch (e) {
                     log.error("Code verification failed for user:" + username + " code:" + code, e);
                     throw(e);
              }
       };

       /**
        * Update password of user
        * @param username user name
        * @param confirmationCode confirmation code
        * @param newPassword new password
        * @return update status
        */
       recovery.updatePassword = function (username, confirmationCode, newPassword) {
              try {
                     return userInformationRecoveryService.updatePassword(username, confirmationCode, newPassword);
              } catch (e) {
                     log.error("Password updating failed for user:" + username + " password:" + newPassword, e);
                     throw(e);
              }
       };

       /**
        * Create captcha information bean
        * @param path captcha image path
        * @param key secret key
        * @param answer captcha answer
        * @return {Packages.org.wso2.carbon.identity.mgt.dto.CaptchaInfoBean}
        */
       recovery.createCaptchaInfoBean = function (path, key, answer) {
              var captchaInfoBean = new Packages.org.wso2.carbon.captcha.mgt.beans.CaptchaInfoBean();
              captchaInfoBean.setImagePath(path);
              captchaInfoBean.setSecretKey(key);
              captchaInfoBean.setUserAnswer(answer);
              return captchaInfoBean;
       }

       /**
        * Create challenge question dto
        * @param question challenge question
        * @param promoteQuestion if the question should be promoted
        * @param order order of question
        * @param questionSetId question set id
        * @return {Packages.org.wso2.carbon.identity.mgt.dto.ChallengeQuestionDTO}
        */
       recovery.createChallengeQuestionDTO = function (question, promoteQuestion, order, questionSetId) {
              var challengeQuestion = new Packages.org.wso2.carbon.identity.mgt.dto.ChallengeQuestionDTO();
              challengeQuestion.setQuestion(question);
              challengeQuestion.setPromoteQuestion(promoteQuestion);
              challengeQuestion.setOrder(order);
              challengeQuestion.setQuestionSetId(questionSetId);
              return challengeQuestion;
       };

       /**
        * Create user challenge dto
        * @param id question id
        * @param question challenge question
        * @param answer answer
        * @param order order of question
        * @return {Packages.org.wso2.carbon.identity.mgt.dto.UserChallengesDTO}
        */
       recovery.createUserChallengesDTO = function (id, question, answer, order) {
              var userChallenge = new Packages.org.wso2.carbon.identity.mgt.dto.UserChallengesDTO();
              userChallenge.setId(id);
              userChallenge.setQuestion(question);
              userChallenge.setAnswer(answer);
              userChallenge.setOrder(order);
              return userChallenge;
       };

       /**
        * Set challenge question of user
        * @param username user name
        * @param questionArray array of questions
        * @return success status
        */
       recovery.setChallengeQuestionForUser = function (username, questionArray) {
              var userChallengesArray = java.lang.reflect.Array.newInstance(Packages.org.wso2.carbon.identity.mgt.dto.UserChallengesDTO, questionArray.length);
              for (var index = 0; index < questionArray.length; index++) {
                     var userChallenge = new Packages.org.wso2.carbon.identity.mgt.dto.UserChallengesDTO();
                     var questionData = questionArray[index];
                     userChallenge.setQuestion(questionData.question);
                     userChallenge.setAnswer(questionData.answer);
                     userChallenge.setId(questionData.id);
                     userChallengesArray[index] = userChallenge;
              }
              try {
                     userIdentityManagementAdminService.setChallengeQuestionsOfUser(username, userChallengesArray);
                     return true;
              } catch (e) {
                     log.error("Failed to set challenge question set for ES", e);
                     throw(e);
              }
       };

       /**
        * Get challenge question set
        * @return array of questions
        */
       recovery.getChallengeQuestionSet = function () {
              try {
                     return userIdentityManagementAdminService.getAllChallengeQuestions();
              } catch (e) {
                     log.error('Failed to retreive challenge questions', e);
                     throw(e);
              }
       };

       /**
        * Get challenge question ids
        * @param username user name
        * @param confirmation confirmation code
        * @return array of question Ids
        */
       recovery.getChallengeQuestionIds = function (username, confirmation) {
              try {
                     return userInformationRecoveryService.getUserChallengeQuestionIds(username, confirmation);
              } catch (e) {
                     log.error('Failed to retreive user challenge question id for user: ' + username, e);
                     throw(e);
              }
       };

       /**
        * Get challenge question of user
        * @param username user name
        * @param confirmation confirmation code
        * @param questionId question id
        * @return challenge question
        */
       recovery.getChallengeQuestionOfUser = function (username, confirmation, questionId) {
              try {
                     return userInformationRecoveryService.getUserChallengeQuestion(username, confirmation, questionId);
              } catch (e) {
                     log.error('Failed to retrive user challenge question: ' + questionId + ' for user: ' + username + ', questionId:' + questionId + ', confirmation key:' + confirmation, e);
                     throw(e);
              }
       }

       /**
        * Verify challenge
        * @param username user name
        * @param confirmation confirmation code
        * @param questionId question id
        * @param answer challenge answer
        * @return verification info
        */
       recovery.verifyChallenge = function (username, confirmation, questionId, answer) {
              try {
                     return userInformationRecoveryService.verifyUserChallengeAnswer(username, confirmation, questionId, answer);
              } catch (e) {
                     log.error('Challenge verification failed for user: ' + username + 'for question:' + questionId + ', answer:' + answer + ', confirmation key:' + confirmation, e);
                     throw(e);
              }
       };
})(recovery);


