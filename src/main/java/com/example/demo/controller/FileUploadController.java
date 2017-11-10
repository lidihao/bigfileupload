package com.example.demo.controller;


import com.example.demo.common.Msg;
import com.example.demo.util.FileMd5Util;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.nio.channels.FileChannel;
import java.util.UUID;

/**
 * 文件上传的Controller
 *
 * @author 单红宇(CSDN CATOOP)
 * @create 2017年3月11日
 */
@Controller
public class FileUploadController {


    @RequestMapping(value = "/upload", method = RequestMethod.GET)
    public String upload() {
        return "/upload1";
    }

    /**
     * 文件上传具体实现方法（单文件上传）
     *
     * @param file
     * @return
     * @author 单红宇(CSDN CATOOP)
     * @create 2017年3月11日
     */
    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    @ResponseBody
    public Msg upload(HttpServletRequest request,@RequestParam("data") MultipartFile file) throws IOException {
        try {
            String fileName = request.getParameter("fileName");
            Integer total = Integer.parseInt(request.getParameter("total"));
            String uuid = request.getParameter("uuid");
            String md5 = request.getParameter("md5");
            Integer blockNum = Integer.parseInt(request.getParameter("blockNum"));

            String basePath = "E\\temp";
            String savePath = basePath + File.separator + uuid;
            File saveDir = new File(savePath);
            if (!saveDir.exists()) {
                saveDir.mkdir();
            }
            System.out.println(saveDir.getAbsolutePath());
            File filePart = new File(saveDir, uuid + "_" + blockNum);
            if (filePart.exists()) {
                String fileMd5 = FileMd5Util.getFileMD5(filePart);
                if (fileMd5.equals(md5)) {
                    return Msg.success().add("status", "SUCCESS");
                } else {
                    filePart.delete();
                }
            }
            BufferedOutputStream buff = new BufferedOutputStream(new FileOutputStream(filePart));
            buff.write(file.getBytes());
            buff.flush();
            buff.close();
            String fileMd5 = FileMd5Util.getFileMD5(filePart);
            if (!fileMd5.equals(md5))
                return Msg.success().add("status", "FAIL");
            if (saveDir.isDirectory()) {
                File[] fileArray = saveDir.listFiles();
                if (fileArray != null) {
                    if (fileArray.length == total) {
                        //分块全部上传完毕,合
                        File outputFile = new File("E:\\", fileName);
                        FileChannel outChannel = new FileOutputStream(outputFile).getChannel();

                        //合并
                        FileChannel inChannel;
                        for (int i = 0; i < total; i++) {
                            File f = new File(saveDir, uuid + "_" + i);
                            inChannel = new FileInputStream(f).getChannel();
                            inChannel.transferTo(0, inChannel.size(), outChannel);
                            inChannel.close();
                            System.out.println("append: " + f.getName());
                            //删除分片
                            if (!f.delete()) {
                                System.out.println("删除失败");
                            }
                        }
                        outChannel.close();
                    }
                }
            }
            return Msg.success().add("status", "SUCCESS");
        }catch (Exception e){
            e.printStackTrace();
            return Msg.success().add("status","FAIL");
        }
    }
    @ResponseBody
    @PostMapping("/getUuid")
    public Msg getUuid(){
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        return Msg.success().add("uuid",uuid);
    }
    @ResponseBody
    @PostMapping("/savefile")
    public Msg savechunk(HttpServletRequest request,@RequestParam("file")MultipartFile file) throws IOException {
        BufferedOutputStream buff = null;
        try {
            int index = Integer.parseInt(request.getParameter("chunk"));
            String uuid = request.getParameter("uuid");
            String basePath = "E\\temp";
            String savePath = basePath + File.separator + uuid;
            File saveDir = new File(savePath);
            if (!saveDir.exists()) {
                saveDir.mkdirs();
            }
            System.out.println(saveDir.getAbsolutePath());
            File filePart = new File(saveDir, uuid + "_" + index);
            buff = new BufferedOutputStream(new FileOutputStream(filePart));
            buff.write(file.getBytes());
            buff.flush();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (buff != null)
                buff.close();
        }
        return Msg.success().add("status", "SUCCESS");
    }
}



