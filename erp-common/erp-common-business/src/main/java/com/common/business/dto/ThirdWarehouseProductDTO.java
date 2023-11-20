package com.common.business.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 第三方仓产品数据，转换为此类后发送mq统一消费处理
 **/
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class ThirdWarehouseProductDTO extends UniqueDto {

    /**
     * 订单日期
     */
    private LocalDate billDate;

}
