package com.erp.tms.batong.model.label.response;

import cn.hutool.core.annotation.Alias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author Lambda
 * @Classname LabelResponse

 * @Date 2024-01-15 11:38
 * @Created by yl
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LabelResponse implements Serializable {

    /**
     * 标签文件类型 1 ：PNG文件 2 ：PDF文件
     */
    @Alias("lable_file_type")
    private String labelFileType;

    /**
     * 标签内容类型 1 ：运单标签 2 ：报关单/发票
     */
    @Alias("lable_content_type")
    private String labelContentType;

    /**
     * 标签文件url地址
     */
    @Alias("lable_file")
    private String labelUrl;


    private String base64;

}
