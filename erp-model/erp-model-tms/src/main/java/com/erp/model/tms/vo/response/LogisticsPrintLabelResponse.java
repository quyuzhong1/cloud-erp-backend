package com.erp.model.tms.vo.response;

import lombok.*;

import java.io.Serializable;
import java.util.List;

/**
 * 打印标签响应实体
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString(callSuper = true)
public class LogisticsPrintLabelResponse extends LogisticsBaseResponseVO implements Serializable {
    /**
     * 运单号
     */
    private List<String> transportNoList;
    /**
     * 跟踪单号
     */
    private List<String> trackNoList;
    /**
     * 发货单号
     */
    private List<String> deliveryNoList;
    /**
     * 是否存在子单
     */
    private boolean more;
    /**
     * 主运单列表
     */
    private List<LogisticsPrintLabelResponse> logisticsPrintLabelResponses;
    /**
     * 文件base64编码
     */
    private String base64;
}
