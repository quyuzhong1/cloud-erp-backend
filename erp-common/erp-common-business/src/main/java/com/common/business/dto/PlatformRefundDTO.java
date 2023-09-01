package com.common.business.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 退款DTO 所有平台订单通用数据，转换为此类后发送mq统一消费处理
 *
 * @Author Cloud
 * @Date 2023/8/31 16:01
 **/
@Data
@NoArgsConstructor
public class PlatformRefundDTO extends UniqueDto {
}
