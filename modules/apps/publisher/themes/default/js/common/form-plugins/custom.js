$('INPUT[ext]').change(function () {
    var ext = this.value.match(/\.(.+)$/)[1];
    var incoming = $(this).attr('ext');

   if(incoming){
           if($.inArray(ext, [incoming,""]) == -1) {
           	      alert('Invalid file extension type. Please upload a "'+ incoming+'" file.');
           	      this.value = '';
           	    }
    }
});