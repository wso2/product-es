var time = {};
(function(time) {
    time.getCurrentTime = function(dateLength) {
    	var dateLength=dateLength||20;
        var now = new String(new Date().valueOf());
        var length = now.length;
        var prefix = dateLength;
        var onsetVal = '';
        if (length != prefix) {
            var onset = prefix - length;
            for (var i = 0; i < onset; i++) {
                onsetVal += '0';
            }
        }
        return onsetVal + now;
    };
}(time));