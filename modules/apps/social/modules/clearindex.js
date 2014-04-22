(function(){
    var sso_sessions = application.get('sso_sessions');
        if(!sso_sessions) {
        return;
    }
        l = new Log();
    l.debug("session deleting :: " + session.getId() + " :: " + sso_sessions[session.getId()]);
    delete sso_sessions[session.getId()];
}());