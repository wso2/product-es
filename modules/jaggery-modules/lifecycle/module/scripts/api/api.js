var api = {};
(function(api, core) {
    var log = new Log('lifecycle');

    function Lifecycle(definiton) {
        this.definition = definiton;
    }
    Lifecycle.prototype.getName = function() {
        if (!this.definition.name) {
            throw 'Unable to locate name attribute in the lifecycle definition ';
        }
        return this.definition.name;
    };
    /**
     * The function will obtain the next state of states from the current state
     * @return An array containing the name of the next set of states
     */
    Lifecycle.prototype.nextStates = function(currentStateName) {
        var currentStateName=currentStateName?currentStateName.toLowerCase():currentStateName;
        var states = this.definition.configuration.lifecycle.scxml.state;
        var nextStates = [];
        if (!states) {
            throw 'The lifecycle : ' + this.getName() + ' does not have any state information.Make sure that the states are defined in the scxml definition.';
        }
        if (!states[currentStateName]) {
            throw 'The state: ' + currentStateName + ' is not present in the lifecycle: ' + this.getName();        
        }
        if (!states[currentStateName].transition) {
           throw 'The state: ' + currentStateName + ' has not defined any transitions in the lifecycle: ' + this.getName(); 
        }
        var transitions = states[currentStateName].transition;
        for (var index = 0; index < transitions.length; index++) {
            var transition = {};
            transition.state = transitions[index].target;
            transition.action = transitions[index].event;
            nextStates.push(transition);
        }
        return nextStates;
        
    };

    /**
     * The function returns checklistItems binded with current state
     * @param  {[type]} name The name of the state
     * @return A json object representing the checklist items
     */
    Lifecycle.prototype.checklistItems = function(currentStateName) {
        var currentStateName=currentStateName?currentStateName.toLowerCase():currentStateName;
        var states = this.definition.configuration.lifecycle.scxml.state;
        var checklistItems = [];
        try{
            var datamodel = states[currentStateName].datamodel.data;
            log.info(datamodel);
            for (var index = 0; index < datamodel.length; index++) {
                if(datamodel[index].name == 'checkItems'){
                    //var items = {};
                    checklistItems = datamodel[index].item;
                }
            }
        }catch(e){
            log.warn(e);
        }
        return checklistItems;
    };

    /**
     * The function returns details about the current state
     * @param  {[type]} name The name of the state
     * @return A json object representing the state
     */
    Lifecycle.prototype.state = function(name) {
        var state = {};        
        try{
            state.nextstates = this.nextStates(name);
            state.checkList  = this.checklistItems(name);
        }catch(e){
            return e;
        }
        
        return state;

    };
    /**
     * The function returns the action that can cause transitions from the fromState to the toState
     * @param  {[type]} fromState The from state
     * @param  {[type]} toState   [description]
     * @return {[type]}           [description]
     */
    Lifecycle.prototype.transitionAction = function(fromState, toState) {
        var fromState=fromState?fromState.toLowerCase():fromState;
        var toState=toState?toState.toLowerCase():toState;
        //Get the list of states that can be reached from the fromState
        var states = this.nextStates(fromState);
        if (states.length == 0) {
            log.warn('There is no way to move from ' + fromState + ' to ' + toState + ' in lifecycle: ' + this.getName());
            return null;
        }
        for (var index = 0; index < states.length; index++) {
            if (states[index].state.toLowerCase() == toState.toLowerCase()) {
                return states[index].action;
            }
        }
        log.warn('There is no transition action to move from ' + fromState + ' to ' + toState + ' in lifecycle: ' + this.getName());
        return null;
    };
    /**
     * The function will return an array of execution event parameters.These parameters
     * are consumed by the executor for the event
     * @param  {[type]} state The state for which the execution event must be returned
     * @param  action The transition action for this state
     * @return {[type]}       An array of transition execution event parameters
     */
    Lifecycle.prototype.transitionExecution = function(state, action) {
        var state=state?state.toLowerCase():state;
        var action=action?action.toLowerCase():action;
        var states = this.definition.configuration.lifecycle.scxml.state;
        var parameters = [];
        if (!states) {
            throw 'The lifecycle : ' + this.getName() + ' does not have any state information.Make sure that the states are defined in the scxml definition.';
        }
        if (!states[state]) {
            log.warn('The state: ' + state + ' is not present in the lifecycle: ' + this.getName());
            return parameters;
        }
        var data = states[state].datamodel.data;
        if (!data) {
            log.warn('The lifecycle: ' + this.getName() + ' does not have a data property declared.Unable to obtain execution events.');
            return parameters;
        }
        var item;
        for (var index in data) {
            item = data[index];
            if (item.name == 'transitionExecution') {
                var executions = item.execution;
                var execution;
                if (executions) {
                    //Look for the event triggered by the provided action
                    for (var exIndex in executions) {
                        execution = executions[exIndex];
                        //Check if the event matches the action
                        if (execution.forEvent.toLowerCase() == action) {
                            parameters = execution.parameter || [];
                            return parameters;
                        }
                    }
                }
            }
        }
        return parameters;
    };
    // var getTenantId = function(session) {
    //     var server = require('store').server;
    //     var user = server.current(session);
    //     return user.tenantId;
    // };
    api.getLifecycle = function(lifecycleName, tenantId) {
        if (!tenantId) {
            throw 'Unable to locate lifecycle ' + lifecycleName + ' without a tenantId';
        }
        var lcJSON = core.getJSONDef(lifecycleName, tenantId);
        if (!lcJSON) {
            log.warn('Unable to locate lifecycle ' + lifecycleName + ' for the tenant: ' + tenantId);
            throw 'Unable to locate lifecycle ' + lifecycleName + ' for the tenant: ' + tenantId;
            //return null;
        }
        return new Lifecycle(lcJSON);
    };

    /**
     * The function will return a list of available lifecycles for the tenant
     * @param  tenantId:
     * @return {[type]}       An array of lifecycles
     */
    api.getLifecycleList = function(tenantId){
        if (!tenantId) {
            throw 'Unable to locate lifecycle without a tenantId';
        }
        var lcList = core.getLifecycleList(tenantId);
        if (!lcList) {
            throw 'Unable to locate lifecycles for the tenant: ' + tenantId;
            //return null;
        }
        return lcList;
    };
}(api, core));