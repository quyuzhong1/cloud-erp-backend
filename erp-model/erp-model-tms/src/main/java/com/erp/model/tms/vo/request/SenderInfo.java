package com.erp.model.tms.vo.request;

import com.erp.model.tms.entity.LogisticsAddressEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 物流发件人信息
 */
@Data
@NoArgsConstructor
public class SenderInfo extends LogisticsAddressEntity {
        //发件人id
        private String actId;
        //发件人税号
        private String taxNumber;
}
