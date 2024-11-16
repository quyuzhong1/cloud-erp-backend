package com.erp.model.tms.vo.request;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * @author zdy
 * @ClassName OrderQueryVO

 * @date 2023年11月08日
 * @version: 1.0
 */
@Data
public class OrderQueryVO implements Serializable {
    /**
     * 客户单号(业务单号如出库单号)
     */
    String refNo;
    /**
     * 运单号
     */
    String transportNo;
    /**
     * 跟踪号
     */
    String trackNoList;
    /**
     * 授权信息
     */
    Map<String, String> authMap;

}
