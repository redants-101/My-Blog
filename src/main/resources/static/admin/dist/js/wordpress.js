$(function () {
    var loadid;
    new AjaxUpload('#uploadButton', {
        action: '/admin/upload/xml',
        name: 'file',
        autoSubmit: true,
        responseType: "json",
        onSubmit: function (file, extension) {
            if (!(extension && /^(xml)$/.test(extension.toLowerCase()))) {
                xtip.alert('只支持xml格式的文件！','e')
                return false;
            }
            loadid = xtip.load("导入时间较长，请耐心等待....");
        },
        onComplete: function (file, r) {
            if(null != loadid){
                xtip.close(loadid);
            }

            if (r != null && r.resultCode == 200) {
                console.log(r.data);
                xtip.alert('导入成功','s')
                return false;
            } else {
                xtip.alert('导入失败','e')
            }
        }
    });
});