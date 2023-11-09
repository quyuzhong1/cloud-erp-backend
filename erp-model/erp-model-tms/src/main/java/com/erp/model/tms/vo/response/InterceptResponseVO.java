package com.erp.model.tms.vo.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author zdy
 * @ClassName InterceptResponseVO
 * @description: 订单拦截结果
 * @date 2023年11月09日
 * @version: 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterceptResponseVO implements Serializable {
    /**
     * 运单号
     */
    String transportNo;
    /**
     * 跟踪单号
     */
    String trackNo;
    /**
     * 发货单号，erp传的
     */
    String deliveryNo;
    //成功 Success
    String status;
    //失败原因
    String errors;
}
