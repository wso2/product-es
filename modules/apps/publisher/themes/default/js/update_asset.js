$(function() {
    var obtainFormMeta = function(formId) {
        return $(formId).data();
    };
    $(document).ready(function() {
        $('#form-asset-update').ajaxForm({
            success: function() {
                alert('Updated the asset successfully.');
                //var options=obtainFormMeta('#form-asset-update');
                //alert('Aww snap! '+JSON.stringify(options));
                //window.location=options.redirectUrl;
            },
            error: function() {
                alert('Unable to update the asset');
            }
        });
    });
});