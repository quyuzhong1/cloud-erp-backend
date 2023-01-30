package com.erp.model.dmp.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author CLOUD
 * @version 1.0
 * @description: TODO
 * @date 2023/1/11 11:54
 */
@Data
@NoArgsConstructor
public class CleanAmountAfterVO {

    private String platformOrderId;

    private String salesRecordNumber;

    private String platformSign;

    private String id;

    private String orderId;

    private String itemId;

    private String itemName;

    private String specifics;

    private String skuNo;

    private String erpOrderItemId;

    private String amountAfter;


}
