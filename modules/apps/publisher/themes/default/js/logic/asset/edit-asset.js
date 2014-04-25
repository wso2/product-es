$(function() {
    var formManager = new FormManager('form-asset-edit');

    formManager.init();

    PageFormContainer.setInstance(formManager);

    // let's fill all the permissions
    $.each($('.perm-check'), function () {
        // var checkbox = $(checkbox);

        if($(this).attr('data-permissions').indexOf($(this).attr('data-perm')) > -1) {
            $(this).attr('checked', true);
        } else {
            $(this).attr('checked', false);
        }
    });

    $('#editAssetButton').on('click', function() {
        var data = formManager.getData();
        var report = formManager.validate();

        console.log(formManager.formMap);

        //Check if there are any validation failures
        if (report.failed) {
            var output = processClientErrorReport(report.form.fields);
            showAlert(output, 'error');
            return;
        }

        var assetId = formManager.formMap.meta.assetId;
        var assetType = formManager.formMap.meta.assetType;
        var formData = formManager.getFormData();

        updateAsset(assetId, assetType, formData);
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
    };


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
    };

    /**
     * The function makes a backend call to update the asset
     * @param assetId The id of the current asset
     * @param assetType  The current assets type
     * @param formData An object containing the data to be updated
     */
    var updateAsset = function(assetId, assetType, formData) {
        var url = '/publisher/api/asset/' + assetType + '/' + assetId;
        $.ajax({
            type: 'POST',
            url: url,
            data: formData,
            dataType: 'json',
            processData: false,
            contentType: false,
            success: function(response) {
                var result = response;

                //Check if the asset was added
                if (result.ok) {
                    showAlert('Asset updated successfully.', 'success');

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
                                url: '/publisher/asset/' + assetType + '/id/' + assetId + '/permissions',
                                type: 'POST',
                                processData: false,
                                contentType: 'application/json',
                                data: JSON.stringify(rolePermissions),
                                error: function(response) {
                                    showAlert('Error adding permissions.', 'error');
                                }
                            });
                        }
                    })();
                }
            },
            error: function(err) {
                showAlert('Failed to update asset', 'error');
            }
        })
    };

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