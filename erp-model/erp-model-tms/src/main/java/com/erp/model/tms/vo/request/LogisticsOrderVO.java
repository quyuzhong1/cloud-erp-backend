package com.erp.model.tms.vo.request;

import com.alibaba.fastjson.annotation.JSONField;
import com.erp.model.oms.entity.SoInfoEntity;
import com.erp.model.tms.entity.LogisticsAuthEntity;
import com.erp.model.tms.entity.LogisticsChannelEntity;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;
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

    //是否已税
    private Boolean isTaxed;

    //运费
    private Number transportCost;

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
