package com.sdk.wms.goodcang.dto.request;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author zdy
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class GoodCangUploadFileReq {
    /**
     * 文件base64内容。
     * 必填
     */
    @NotBlank(message = "文件base64内容不能为空")
    @JSONField(name = "file")
    protected String file;
    /**
     * 用途
     * 必填
     */
    @NotBlank(message = "用途不能为空")
    @JSONField(name = "use_for")
    protected String useFor;

    /**
     * 完整文件名
     * 必填
     */
    @NotBlank(message = "完整文件名不能为空")
    @JSONField(name = "file_name")
    protected String fileName;
}
