package com.erp.model.tms.vo.request;

import com.erp.model.plm.entity.ProductLogisticsEntity;
import com.erp.model.tms.entity.LogisticsAddressEntity;
import com.erp.model.tms.entity.LogisticsAuthEntity;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author zdy
 * @ClassName LogisticsOrderVO
 * @description: 创建订单请求实体
 * @date 2023年11月06日
 * @version: 1.0
 */
@Data
public class LogisticsOrderVO implements Serializable {

    /**
     * 预约时间，格式为：yyyy-MM-dd
     */
    private String reserveTime;
    /**
     * 发货单号
     */
    private String deliveryNo;
    /**
     * 收货人信息
     */
    ReceiverInfoVO receiverInfoVO;
    /**
     * 发货人信息
     */
    LogisticsAddressEntity addressEntity;
    /**
     * 产品物流信息
     */
    List<ProductLogisticsEntity> productLogisticsEntities;
    /**
     * 授权信息
     */
    LogisticsAuthEntity logisticsAuthEntity;
}
