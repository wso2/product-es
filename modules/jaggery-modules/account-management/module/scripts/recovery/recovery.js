/*
 * Copyright (c) 2014, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

var recovery = {};
(function (recovery) {

       var userInformationRecoveryService = new Packages.org.wso2.carbon.identity.mgt.services.UserInformationRecoveryService();
       var userIdentityManagementAdminService = new Packages.org.wso2.carbon.identity.mgt.services.UserIdentityManagementAdminService();
       var EMAIL_NOTIFICATION = "email";
       var log = new Log("recovery module");
       //TODO add comments

       /**
        * Get new captcha info
        * @return captcha info
        */
       recovery.getCaptcha = function () {
              try {
                     return userInformationRecoveryService.getCaptcha();
              } catch (e) {
                     log.error("Retrieving captcha failed", e);
                     return null;
              }
       }

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
                     return null;
              }
       }

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
                     return null;
              }
       }

       /**
        * Request password recovery
        * @param username user name
        * @param captcha captcha info
        * @return recovery status
        */
       recovery.requestRecovery = function (username, captcha) {
              var isSuccessful = false;
              var verificationBean;
              try {
                     verificationBean = userInformationRecoveryService.verifyUser(username, captcha);
              } catch (e) {
                     log.error("Recovery notification sending failed for user:" + username, e);
                     return isSuccessful;
              }
              var key = verificationBean.getKey();
              if (verificationBean.isVerified()) {
                     try {
                            userInformationRecoveryService.sendRecoveryNotification(username, key, EMAIL_NOTIFICATION);
                            isSuccessful = true;
                     } catch (e) {
                            log.error("Recovery failed for user:" + username, e);
                     }
              }
              return isSuccessful;
       }

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
                     return null;
              }
       }

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
                     return null;
              }
       }

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
              var dto = new Packages.org.wso2.carbon.identity.mgt.dto.ChallengeQuestionDTO();
              dto.setQuestion(question);
              dto.setPromoteQuestion(promoteQuestion);
              dto.setOrder(order);
              dto.setQuestionSetId(questionSetId);
              return dto;
       }

       /**
        * Create user challenge dto
        * @param id question id
        * @param question challenge question
        * @param answer answer
        * @param order order of question
        * @return {Packages.org.wso2.carbon.identity.mgt.dto.UserChallengesDTO}
        */
       recovery.createUserChallengesDTO = function (id, question, answer, order) {
              var dto = new Packages.org.wso2.carbon.identity.mgt.dto.UserChallengesDTO();
              dto.setId(id);
              dto.setQuestion(question);
              dto.setAnswer(answer);
              dto.setOrder(order);
              return dto;
       }

       /**
        * Set challenge question of user
        * @param username user name
        * @param questionArray array of questions
        * @return success status
        */
       recovery.setChallengeQuestionForUser = function (username, questionArray) {
              //var questionArray = JSON.parse(question);
              var dtoArray = java.lang.reflect.Array.newInstance(Packages.org.wso2.carbon.identity.mgt.dto.UserChallengesDTO, questionArray.length);
              for (var index = 0; index < questionArray.length; index++) {
                     var dto = new Packages.org.wso2.carbon.identity.mgt.dto.UserChallengesDTO();
                     var data = questionArray[index];
                     for (attribute in data) {
                            if (data.hasOwnProperty(attribute)) {
                                   dto[attribute] = data[attribute];
                            }
                     }
                     dtoArray[index] = dto;
              }
              try {
                     userIdentityManagementAdminService.setChallengeQuestionsOfUser(username, dtoArray);
                     return true;
              } catch (e) {
                     log.error("Failed to set challenge question set for ES", e);
                     return false;
              }
       }

       /**
        * Get challenge question set
        * @return array of questions
        */
       recovery.getChallengeQuestionSet = function () {
              try {
                     return userIdentityManagementAdminService.getAllChallengeQuestions();
              } catch (e) {
                     log.error('Failed to retreive challenge questions', e);
                     return null;
              }
       }

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
                     return null;
              }
       }

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
                     log.error('Failed to retrive user challenge question: ' + questionId + ' for user: ' + username, e);
                     if (log.isDebugEnabled()) {
                            log.debug('Challenge question retrieving failure details: user:' + username + ', questionId:' + questionId + ', confirmation key:' + confirmation);
                     }
                     return null;
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
                     log.error('Challenge verification failed for user: ' + username, 'for question:' + questionId);
                     if (log.isDebugEnabled()) {
                            log.debug('Challenge verification failure details: user:' + username + ', questionId:' + questionId + ', answer:' + answer + ', confirmation key:' + confirmation);
                     }
                     return null;
              }
       }
})(recovery);


