package com.zozo.zozopicturebackend.controller;


import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.COSObjectInputStream;

import com.qcloud.cos.utils.IOUtils;
import com.zozo.zozopicturebackend.annotaion.AuthCheck;
import com.zozo.zozopicturebackend.common.BaseResponse;
import com.zozo.zozopicturebackend.common.ResultUtils;
import com.zozo.zozopicturebackend.constant.UserConstant;
import com.zozo.zozopicturebackend.exception.BusinessException;
import com.zozo.zozopicturebackend.exception.ErrorCode;
import com.zozo.zozopicturebackend.manager.CosManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;


@Slf4j
@RestController
@RequestMapping("/file")
public class FileController {


    @Resource
    private CosManager cosManager;


    /**
     * 测试上传文件
     *
     * @param multipartFile 文件
     * @return 文件路径
     */
    @PostMapping("/test/upload")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<String> testUploadFile(@RequestPart("file") MultipartFile multipartFile) {
        //文件目录
        String filename = multipartFile.getOriginalFilename();
        String filepath = String.format("/test/%s", filename);

        //创建需要保存本地的文件,并把本地文件上传到本地
        File file = null;
        try {
            file = File.createTempFile(filepath, null);
            // 将文件写入到临时文件中
            multipartFile.transferTo(file);
            // 上传文件到COS
            cosManager.putObject(filepath, file);

            //返回可访问的url
            return ResultUtils.success(filepath);
        } catch (Exception e) {
            log.error("upload file error, filepath = " + filepath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            if (file != null) {

                // 删除临时文件
                boolean delete = file.delete();
                if (!delete) {
                    log.error("delete file error, filepath ={}", filepath);
                }
            }

        }
    }


    /**
     * 测试文件下载
     *
     * @param filepath 文件路径
     * @param response 响应对象
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @GetMapping("/test/download/")
    public void testDownloadFile(String filepath, HttpServletResponse response) throws IOException {
        COSObjectInputStream cosObjectInput = null;
        try {

            //获取cosObject对象
            COSObject cosObject = cosManager.getObject(filepath);
            cosObjectInput = cosObject.getObjectContent();
            // 处理下载到的流
            byte[] bytes = IOUtils.toByteArray(cosObjectInput);
            // 设置响应头
            response.setContentType("application/octet-stream;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=" + filepath);
            // 写入响应
            response.getOutputStream().write(bytes);
            response.getOutputStream().flush();
        } catch (Exception e) {
            log.error("file download error, filepath = " + filepath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "下载失败");
        } finally {
            if (cosObjectInput != null) {
                cosObjectInput.close();
            }
        }
    }


}