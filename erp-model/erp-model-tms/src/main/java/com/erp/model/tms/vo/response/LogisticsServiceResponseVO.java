package com.erp.model.tms.vo.response;

import lombok.*;

import java.io.Serializable;

/**
 * 物流服务响应实体
 * @author Lambda
 * @Classname LogisticsServiceResponseVO
 * @Date 2024-03-04 15:13
 * @Created by yl
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString(callSuper = true)
public class LogisticsServiceResponseVO  implements Serializable {

    /**
     * 服务名
     */
    private  String serviceName;

    private String logisticsType;
}
