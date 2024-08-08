package com.erp.model.tms.vo.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author zdy
 * @ClassName RegisterResponseVO
 * @description: 注册结果
 * @date 2023年11月21日
 * @version: 1.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterResponseVO implements Serializable {
    /**
     * 运单号/跟踪号
     */
    private String trackNo;
    /**
     * 已注册
     */
    private Boolean trackStatus;
    private String code;
    private String msg;


    /**
     * 平台订单生成单号
     */
    private String orderNo;
}
