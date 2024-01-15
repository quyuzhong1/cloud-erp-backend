package com.erp.tms.batong.model.label.response;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author Lambda
 * @Classname LabelResponse
 * @Description TODO
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
    @JSONField(name = "lable_file_type")
    private String labelFileType;

    /**
     * 标签内容类型 1 ：运单标签 2 ：报关单/发票
     */
    @JSONField(name = "lable_content_type")
    private String labelContentType;

    /**
     * 标签文件url地址
     */
    @JSONField(name = "lable_file")
    private String labelFile;

}
