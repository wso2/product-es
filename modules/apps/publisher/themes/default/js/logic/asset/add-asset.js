$(function () {
    var formManager = new FormManager('form-asset-create');


    console.log(JSON.stringify(formManager.formMap));
    console.log(JSON.stringify(formManager.fieldMap));

    formManager.init();

    $('#btn-create-asset').on('click',function(){
         var report=formManager.validate();
        console.log(JSON.stringify(report));
         console.log('Submit clicked');
    });
});