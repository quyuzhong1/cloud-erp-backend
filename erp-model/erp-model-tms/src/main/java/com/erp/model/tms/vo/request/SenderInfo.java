package com.erp.model.tms.vo.request;

import com.erp.model.tms.entity.LogisticsAddressEntity;
import lombok.Builder;
import lombok.Data;

/**
 * 物流发件人信息
 */
@Data
@Builder
public class SenderInfo extends LogisticsAddressEntity {

}
