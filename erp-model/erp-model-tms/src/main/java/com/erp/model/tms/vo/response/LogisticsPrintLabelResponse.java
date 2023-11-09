package com.erp.model.tms.vo.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 打印标签响应实体
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LogisticsPrintLabelResponse implements Serializable {
    /**
     * 运单号
     */
    private List<String> transportNo;
    /**
     * 跟踪单号
     */
    private List<String> trackNo;
    /**
     * 发货单号
     */
    private List<String> deliveryNo;
    /**
     * 文件base64编码
     */
    private String base64;
}
