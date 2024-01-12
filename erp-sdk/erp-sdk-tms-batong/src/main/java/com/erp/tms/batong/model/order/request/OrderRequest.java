package com.erp.tms.batong.model.order.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @author Lambda
 * @Classname OrderRequest
 * @Description TODO
 * @Date 2024-01-10 16:42
 * @Created by yl
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest implements Serializable {

    /**
     * 客户端的订单唯一标识
     */
    @NotBlank(message = "客户端的订单唯一标识不能为空")
    private String referenceNo;

    /**
     * 运输方式代码
     * 就是服务商代码
     */
    @NotBlank(message = "运输方式代码不能为空")
    private String shippingMethod;

    /**
     * 服务商单号
     *
     */
    private String shippingMethodNo;




}
