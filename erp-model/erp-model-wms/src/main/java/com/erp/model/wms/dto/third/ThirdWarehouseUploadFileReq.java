package com.erp.model.wms.dto.third;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotBlank;

/**
 * @author liuruipeng
 * @date 2023年11月17日 10:22
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class ThirdWarehouseUploadFileReq extends ThirdWarehouseAuth{

    private String orderCode;
    //文件类型
    private String fileType;

    //文件base64数据  需要去掉 pdf文件流前缀  data:application/pdf;base64,
    @NotBlank(message = "文件base64数据不能为空")
    @JSONField(name = "file_data")
    private String fileData;

    //模块类型 	支持order_attach（订单附件）和order_label （订单标签），other_documents_carton(其它附件箱唛), other_documents_invoice(其它附件发票), 如果是pdf格式的标签，请传order_label
    private String module;

    //文件说明
    private String fileNote;

    //文件url， 使用file_url时，file_data可不填写
    private String fileUrl;
    //文件名称
    private String fileName;
}
