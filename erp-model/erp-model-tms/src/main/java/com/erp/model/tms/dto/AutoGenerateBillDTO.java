package com.erp.model.tms.dto;

import com.common.business.enums.SourceTypeEnum;
import com.erp.model.tms.enums.BillGenerateTimingEnum;
import com.erp.model.wms.entity.FirstMileDeliveryEntity;
import com.erp.model.wms.entity.SoOutstockEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author liuruipeng
 * @date 2024年04月23日 11:39
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AutoGenerateBillDTO {

    /**
     * id
     */
    private String id;

    private SourceTypeEnum sourceTypeEnum;

    private BillGenerateTimingEnum billGenerateTimingEnum;

    private FirstMileDeliveryEntity firstMileDeliveryEntity;

    private SoOutstockEntity soOutstockEntity;

    private Boolean checkCfg;

}
