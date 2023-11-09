package com.erp.model.tms.vo.request;

import com.erp.model.tms.entity.LogisticsAuthEntity;
import com.erp.model.tms.entity.LogisticsChannelEntity;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;
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
@Builder
public class LogisticsOrderVO implements Serializable {

    //渠道id
    private String channelId;

    //渠道code
    private String channelCode;

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

    //税号
    private String iossCode;

    /**
     * 发货单号
     */
    private String deliveryNo;
    /**
     * 收货人信息
     */
    private ReceiverInfoVO receiverInfoVO;
    /**
     * 发货人信息
     */
    private SenderInfo senderInfo;

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
    private LogisticsAuthEntity logisticsAuthEntity;
    /**
     * 渠道信息
     */
    private LogisticsChannelEntity logisticsChannelEntity;


}
