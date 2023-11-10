package com.common.business.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.common.business.enums.ApproveStatusEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 订单DTO 所有平台订单通用数据，转换为此类后发送mq统一消费处理
 *
 * @Author Cloud
 * @Date 2023/8/31 16:01
 **/
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class PlatformOrderDTO extends UniqueDto {

    /**
     * 订单日期
     */
    private LocalDate billDate;

    /**
     * 平台订单号
     */
    private String platformCode;

    /**
     * 销售平台
     */
    private String dictPlatform;

    /**
     * 店铺
     */
    private String shopId;

    /**
     * 作废状态（false未作废，true已作废）
     */
    private Boolean invalidStatus;

    /**
     * 作废类型（manual手动作废，automatic自动作废）
     */
    private String invalidType;

    /**
     * 作废原因
     */
    private String invalidRemark;

    /**
     * 订单状态
     */
    private String billStatus;

    /**
     * 付款状态（待付款、已付款）
     */
    private String payStatus;

    /**
     * 订单金额
     */
    private BigDecimal amount;

    /**
     * 币别（原币）
     */
    private String currency;

    /**
     * 汇率
     */
    private BigDecimal exchangeRate;

    /**
     * 运费收入
     */
    private BigDecimal shippingFee;

    /**
     * 付款时间
     */
    private LocalDateTime payTime;

    /**
     * 付款金额
     */
    private BigDecimal payAmount;

    /**
     * 付款方式
     */
    private String dictPayMethod;

    /**
     * 买家备注
     */
    private String buyerRemark;

    /**
     * 订单备注
     */
    private String remark;

    /**
     * 销售组织id
     */
    private String orgId;

    /**
     * 销售组织名称
     */
    private String orgName;

    /**
     * 是否拦截
     */
    private Boolean isIntercept;

    /**
     * 拦截备注
     */
    private String interceptRemark;

    /**
     * 来源类型
     */
    private String sourceType;

    /**
     * 来源id
     */
    private String sourceId;

    /**
     * 来源编码
     */
    private String sourceCode;

    /**
     * 标签json
     */
    private String labelJson;

    /**
     * 异常原因（1、订单规则审核不通过；2、配货规则匹配失败；3、人工审核不通过）
     */
    private String abnormalType;

    /**
     * 同步金蝶状态（默认0无需同步,1待同步,2同步中,3同步成功,4同步失败）
     */
    private String syncKingdeeStatus;

    /**
     * 同步时间
     */
    private LocalDateTime syncKingdeeTime;

    /**
     * 金蝶数据id
     */
    private String syncKingdeeId;

    /**
     * 同步操作
     */
    private String syncOperate;

    /**
     * 数据下载状态
     * 0 详情数据需要更新
     * 1 详情数据已更新
     */
    private Integer downloadStatus;

    /**
     * 数据下载时间
     */
    private String downloadTime;

    /**
     * 订单明细
     */
    private List<PlatformOrderDetailDTO> details;

    /**
     * 订单财务信息
     */
    private List<PlatformOrderFinanceDTO> financesList;

    /**
     * 订单物流信息
     */
    private List<PlatformOrderLogisticsDTO> logisticsList;

    /**
     * 订单买家信息
     */
    private List<PlatformOrderReceiverDTO> receiverList;
}
