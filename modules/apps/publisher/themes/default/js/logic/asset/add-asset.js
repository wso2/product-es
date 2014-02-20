$(function () {
    var formManager = new FormManager('form-asset-create');


    console.log(JSON.stringify(formManager.formMap));
    console.log(JSON.stringify(formManager.fieldMap));

    formManager.init();

    $('#btn-create-asset').on('click', function () {
        var formData = formManager.getFormData();//formManager.validate();
        postData(formData);
    });

    /*
     The function is used to build a report message indicating the errors in the form
     @report: The report to be processed
     @return: An html string containing the validation issues
     */
    var processErrorReport=function (report) {
        var msg = '';
        for (var index in report) {

            for (var item in report[index]) {
                msg += report[index][item] + "<br>";
            }
        }

        return msg;
    }

    var postData = function (formData) {
        var type = $('#meta-asset-type').val();

        $.ajax({
            url: '/publisher/asset/' + type,
            type: 'POST',
            processData:false,
            contentType:false,
            dataType:'json',
            data:formData,
            success: function (response) {
                var result =response;

                //Check if the asset was added
                if (result.ok) {
                    showAlert('Asset added successfully.', 'success');
                    //window.location = '/publisher/assets/' + type + '/';
                } else {
                    var msg = processErrorReport(result.report);
                    showAlert(msg, 'error');
                }

            },
            error: function (response) {
                showAlert('Failed to add asset.', 'error');
            }
        });
    }
});