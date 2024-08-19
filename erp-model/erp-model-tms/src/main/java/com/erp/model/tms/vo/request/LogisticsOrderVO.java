package com.erp.model.tms.vo.request;


import com.erp.model.tms.entity.LogisticsChannelEntity;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author zdy
 * @ClassName LogisticsOrderVO
 * @description: 创建订单请求实体
 * @date 2023年11月06日
 * @version: 1.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LogisticsOrderVO implements Serializable {
    /**
     * 速卖通 ISV用户唯一标识，一般为userId,最大长度为16个字符
     */
    private String topUserKey;
    /**
     *订单对应收货地址OAID
     */
    private String oaid;
    /**
     * 顺丰月结卡号
     */
    private String monthlyCard;
    //订单来源
    private String orderSource;
    /**
     * 订单来源 取值 so_b2c.source_type
     */
    private String orderType;

    /**
     * 预约时间，格式为：yyyy-MM-dd
     */
    private String reserveTime;

    //是否退回,包裹无人签收时是否退回，1-退回，0-不退回，默认 0
    private Boolean returnOption;

    //备注
    private String remark;
    //材质
    private String material;

    //税号
    private String taxId;

    //IOSS编号
    private String iossCode;
    /**
     * VOEC税号
     */
    private String voecTaxNo;
    /**
     * 收货国家
     */
    private String country;

    //是否已税
    private Boolean isTaxed;

    //运费
    private Integer transportCost;

    //护照号
    private String passportNumber;
    //发货方式
    private String pickupType;

    /**
     * 发货单号
     * 没有就订单id
     */
    private String deliveryNo;

    /**
     * 平台订单号
     */
    private String platformCode;

    /**
     * 来源id(订单id)
     */
    private String sourceId;
    /**
     * 物流单号
     */
    private String trackNo;
    /**
     * 收货人信息
     */
    private ReceiverInfoVO receiverInfoVO;
    /**
     * 发货人信息
     */
    private SenderInfo senderInfo;
    /**
     * 退货地址
     */
    private SenderInfo returnInfo;
    /**
     * 上门揽收
     */
    private SenderInfo pickUpInfo;

    /**
     * 包裹信息
     */
    private ParceInfoVO parceInfoVO;

    /**
     * 产品物流信息
     */
    private List<LogisticsProductVO> logisticsProductVOList;
    /**
     * 授权信息
     */
    private Map<String, String> authMap;
    /**
     * 渠道信息
     */
    private LogisticsChannelEntity logisticsChannelEntity;
    /**
     * 原始渠道
     */
    private LogisticsSaleChannelEntity logisticsSaleChannel;
}
