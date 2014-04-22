/**
 * Description: The following script is used to create an asset.It uses the form manager
 *              to manage the input fields
 *
 */
$(function() {

    var formManager = new FormManager('form-asset-create');

    //Initialize the plugins
    formManager.init();

    PageFormContainer.setInstance(formManager);

    $('#btn-create-asset').on('click', function() {

        console.log(formManager.getData());
        //Perform validations
        var report = formManager.validate();

        //Display the errors
        if (report.failed) {
            var msg = processClientErrorReport(report.form.fields);
            showAlert(msg, 'error');
            return;
        }

        var formData = formManager.getFormData();//formManager.validate();

        postData(formData);
    });

    /*
     The function is used to build a report message indicating the errors in the form
     @report: The report to be processed
     @return: An html string containing the validation issues
     */
    var processErrorReport = function(report) {
        var msg = '';
        for (var index in report) {

            for (var item in report[index]) {
                msg += report[index][item] + "<br>";
            }
        }

        return msg;
    }

    /*
     The function is used to build a report message indicating the errors in the form
     @report: The report to be processed
     @return: An html string containing the validation issues
     */
    var processClientErrorReport = function(report) {
        var msg = '';
        for (var index in report) {

            for (var item in report[index]) {
                msg += report[index][item].msg + "<br>";
            }
        }

        return msg;
    }

    /**
     * The method calls the add asset api to create a new asset
     * @param formData
     */
    var postData = function(formData) {
        var type = $('#meta-asset-type').val();

        console.log(JSON.stringify(formData));

        $.ajax({
            url: '/publisher/asset/' + type,
            type: 'POST',
            processData: false,
            contentType: false,
            dataType: 'json',
            data: formData,
            success: function(response) {
                var result = response;

                //Check if the asset was added
                if (result.ok) {
                    showAlert('Asset added successfully.', 'success');

                    // I am not conforming to the formManger behavior here. 
                    // Since roles are such precious little bundles of joy, they deserve their own
                    // special caretakers.

                    (function setupPermissions() {
                        var rolePermissions = [];
                        $('.role-permission').each(function(i, tr) {
                            var role = $(tr).attr('data-role');

                            var permissions = [];

                            $(tr).children('td').children(':checked').each(function(j, checkbox) {
                                permissions.push($(checkbox).attr('data-perm'));
                            });

                            rolePermissions.push({
                                role: role,
                                permissions: permissions
                            });
                        });


                        if (rolePermissions.length > 0) {
                            $.ajax({
                                url: '/publisher/asset/' + type + '/id/' + result.id + '/permissions',
                                type: 'POST',
                                processData: false,
                                contentType: 'application/json',
                                data: JSON.stringify(rolePermissions),
                                success: function(response) {
                                    window.location = '/publisher/assets/' + type + '/';
                                },
                                error: function(response) {
                                    showAlert('Error adding permissions.', 'error');
                                }
                            });
                        } else {
                            window.location = '/publisher/assets/' + type + '/';
                        }
                    })();
                } else {
                    var msg = processErrorReport(result.report);
                    showAlert(msg, 'error');
                }

            },
            error: function(response) {
                showAlert('Failed to add asset.', 'error');
            }
        });
    }

    // roles autocomplete
    $('#roles').tokenInput('/publisher/api/lifecycle/information/meta/' + $('#meta-asset-type').val() + '/roles', {
        theme: 'facebook',
        preventDuplicates: true,
        onAdd: function(role) {
            var permission = $('<tr class="role-permission" data-role="' + role.id + '"><td>' + role.name + '</td><td><input data-perm="GET" type="checkbox" value=""></td><td><input data-perm="PUT" type="checkbox" value=""></td><td><input data-perm="DELETE" type="checkbox" value=""></td><td><input data-perm="AUTHORIZE" type="checkbox" value=""></td></tr>')
            $('#permissionsTable > tbody').append(permission);
        },
        onDelete: function(role) {
            console.log()
            $('#permissionsTable tr[data-role="' + role.id + '"]').remove();
        }
    });
});