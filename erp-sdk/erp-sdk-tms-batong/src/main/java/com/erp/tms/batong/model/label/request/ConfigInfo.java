package com.erp.tms.batong.model.label.request;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author Lambda
 * @Classname ConfigInfo
 * @Description
 * @Date 2024-01-15 11:12
 * @Created by yl
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfigInfo  implements Serializable {

    /**
     * 标签文件类型
     * 1：PNG文件
     * 2：PDF文件
     */
    @JSONField(name = "lable_file_type")
    @NotBlank(message = "标签文件类型不能为空")
    private String labelFileType;


    /**
     * 纸张类型
     * 1：标签纸
     * 2：A4纸
     */
    @JSONField(name = "lable_paper_type")
    @NotBlank(message = "纸张类型不能为空")
    private String labelPaperType;

    /**
     * 标签内容类型代码
     * 1：标签
     * 2：报关单
     * 3：配货单
     * 4：标签+报关单
     * 5：标签+配货单
     * 6：标签+报关单+配货单
     */
    @JSONField(name = "lable_content_type")
    @NotBlank(message = "标签内容类型代码不能为空")
    private String labelContentType;

    @JSONField(name = "additional_info")
    @NotNull(message = "附加配置信息不能为空")
    private AdditionalInfo additionalInfo;



}
