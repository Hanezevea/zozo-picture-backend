package com.zozo.zozopicturebackend.utils;

import com.zozo.zozopicturebackend.exception.BusinessException;
import com.zozo.zozopicturebackend.exception.ErrorCode;

public class ColorTransformUtils {

    private ColorTransformUtils() {
        // 工具类不需要实例化
    }

    /**
     * 获取标准颜色格式，补充前导零
     * 示例： 0xFF00 -> 0x00FF00
     * @param color
     * @return
     */
    public static String normalizeHexColor(String color) {
        // 去除空输入或null的情况
        if (color == null || color.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"颜色值不能为空");
        }

        // 去除可能的 "0x" 或 "#" 前缀
        String cleanHex = color.trim().toLowerCase();
        if (cleanHex.startsWith("0x")) {
            cleanHex = cleanHex.substring(2);
        } else if (cleanHex.startsWith("#")) {
            cleanHex = cleanHex.substring(1);
        }

        // 检查是否为合法的十六进制字符
        if (!cleanHex.matches("^[0-9a-fA-F]+$")) {
            throw new IllegalArgumentException("颜色值包含非法字符，仅允许0-9和a-f");
        }

        // 检查长度并补齐前导零
        int length = cleanHex.length();
        if (length > 6) {
            throw new IllegalArgumentException("颜色值长度超过6位，无法解析");
        } else if (length == 6) {
            return "0x" + cleanHex; // 已经是6位，添加 "0x" 前缀后返回
        } else {
            // 补齐前导零
            StringBuilder paddedHex = new StringBuilder(cleanHex);
            while (paddedHex.length() < 6) {
                // 在最前面插入 '0'
                paddedHex.insert(0, '0');
            }
            return "0x" + paddedHex.toString();
        }
    }
}