package com.erp.tms.aliexpress.model.handover.response;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;

/**
 * @author zdy
 * @ClassName PdfResponse
 * @description: 通过国际订单号获取的body内容用base64转码后生成运单标签的pdf字节流。
 * @date 2024年02月05日
 * @version: 1.0
 */
@Data
public class PdfResponse implements Serializable {

    private String errorDesc;

    @JSONField(name = "Content-Disposition")
    private String content;

    @JSONField(name = "body")
    private String body;

    @JSONField(name = "filename")
    private String filename;

    @JSONField(name = "StatusCode")
    private String statusCode;

    @JSONField(name = "Content-Type")
    private String contentType;
}
