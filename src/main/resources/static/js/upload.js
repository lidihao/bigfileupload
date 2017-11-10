/**
 * Created by Administrator on 2017/11/4.
 */
var beginDate;
var endDate;
var success=0;
function getUuid() {
    var uuid="";
    $.ajax({
        url: "/getUuid",
        type: "POST",
        async: true,        //异步
        processData: false,  //很重要，告诉jquery不要对form进行处理
        contentType: false,
        success: function (data) {
            console.log(data);
            uuid=data.extend.uuid;
        }
    });
    console.log(uuid);
    return uuid;
}

function uploadPart(fileName,uuid,data,blockNum,total,fail) {
    if (fail==3)
        return "FAIL";
    var form =new FormData();
    var r =new FileReader();
    r.readAsBinaryString(data);
    $(r).load(function (e) {
        var blob=e.target.result;
        var md51=hex_md5(blob);
        form.append("uuid",uuid);
        form.append("fileName",fileName);
        form.append("md5",md51);
        form.append("blockNum",blockNum);
        form.append("data",data);
        form.append("total",total);
        $.ajax({
            url: "/upload",
            type: "POST",
            data: form,
            async: true,        //异步
            processData: false,  //很重要，告诉jquery不要对form进行处理
            contentType: false,
            success: function (data) {
                if(data.extend.status=="FAIL")
                    uploadPart(fileName,uuid,data,blockNum,total,fail+1);
                else
                    success++;
            }
        })
    });
    return "SUCCESS";
}

function uplaodFile(file,fileName,uuid,shardCout,shardSize) {
    for(var i=0;i<shardCout;i++){
        var begin=i*shardSize;
        var end=file.size>begin+shardSize?begin+shardSize:file.size;
        var data=file.slice(begin,end);
        if(uploadPart(fileName,uuid,data,i,shardCout,0)=="FAIL")
            return "FAIL";
    }
    return "SUCCESS";
}
$(function(){
    //去首页
    $("#upload").click(function () {
        var file=$("#uploadfile")[0].files[0];
        var s = file.size;        //总大小
        var shardSize =5*1024 * 1024,    //以5MB为一个分片
            shardCount = Math.ceil(s / shardSize);  //总片数
        var uuid="";
        $.ajax({
            url: "/getUuid",
            type: "POST",
            async: true,        //异步
            processData: false,  //很重要，告诉jquery不要对form进行处理
            contentType: false,
            success: function (data) {
                uuid=data.extend.uuid;
                uplaodFile(file,file.name,uuid,shardCount,shardSize);
            }
        })
    })});