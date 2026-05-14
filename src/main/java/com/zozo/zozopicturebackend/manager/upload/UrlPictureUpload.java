package com.zozo.zozopicturebackend.manager.upload;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.zozo.zozopicturebackend.exception.ErrorCode;
import com.zozo.zozopicturebackend.exception.ThrowUtils;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class UrlPictureUpload extends PictureUploadTemplate {

    /**
     * 获取文件类型
     * @param inputSource
     * @return
     */
    @Override
    protected String getFileType(Object inputSource) {
        //因为在获取文件类型这个方法执行前已经进行过校验了，无法通过校验也不会执行本方法，因此在这里不再进行判断
        String fileUrl = (String) inputSource;
        HttpResponse httpResponse = null;
        try {
            httpResponse = HttpUtil.createRequest(Method.HEAD, fileUrl).execute();
            //如果无法通过请求头获取类型，默认设置为jpg格式
            if (httpResponse.getStatus() != HttpStatus.HTTP_OK) {
                return "jpg";
            }
            //返回文件类型
            String contentType = httpResponse.header("Content-Type");
            if (contentType != null && contentType.startsWith("image/")) {
                contentType = contentType.substring("image/".length());
                return contentType;
            } else {
                // 处理不符合预期格式的情况，例如设置默认图片类型
                return "jpg";
            }
        } finally {
            //如果建立了连接，则执行完毕后需要关闭连接
            if (httpResponse != null) {
                httpResponse.close();
            }
        }
    }

    @Override
    protected void validPicture(Object inputSource) {  
        String fileUrl = (String) inputSource;  
        ThrowUtils.throwIf(StrUtil.isBlank(fileUrl), ErrorCode.PARAMS_ERROR, "文件地址不能为空");
        // ... 跟之前的校验逻辑保持一致  
    }  
  
    @Override  
    protected String getOriginFilename(Object inputSource) {  
        String fileUrl = (String) inputSource;  
        // 从 URL 中提取文件名
        //  todo 修改文件没有后缀名
        // 原 FileUtil.mainName()
        return FileUtil.mainName(fileUrl);
    }  
  
    @Override  
    protected void processFile(Object inputSource, File file) throws Exception {
        String fileUrl = (String) inputSource;  
        // 下载文件到临时目录  
        HttpUtil.downloadFile(fileUrl, file);
    }


}
