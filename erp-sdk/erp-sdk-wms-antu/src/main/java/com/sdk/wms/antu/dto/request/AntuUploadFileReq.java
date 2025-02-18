package com.sdk.wms.antu.dto.request;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class AntuUploadFileReq {

    //文件类型
    @NotBlank(message = "文件类型不能为空")
    @JSONField(name = "file_type")
    private String fileType;

    //文件base64数据  需要去掉 pdf文件流前缀  data:application/pdf;base64,
    @NotBlank(message = "文件base64数据不能为空")
    @JSONField(name = "file_data")
    private String fileData;

    //模块类型 	支持order_attach（订单附件）和order_label （订单标签），other_documents_carton(其它附件箱唛), other_documents_invoice(其它附件发票), 如果是pdf格式的标签，请传order_label
    @NotBlank(message = "模块类型不能为空")
    @JSONField(name = "module")
    private String module;

    //文件说明
    @JSONField(name = "file_note")
    private String fileNote;

    //文件url， 使用file_url时，file_data可不填写
    @JSONField(name = "file_url")
    private String fileUrl;
}
