package com.erp.model.oms.dto;

import com.common.business.dto.PlatformDeliveryDetailDTO;
import com.erp.model.wms.dto.SoOutstockDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlatformGenerateSoOutstockDTO implements Serializable {

    /**
     * 平台发货明细
     */
    private List<PlatformDeliveryDetailDTO> platformDeliveryDetailDTOList;

    private SoOutstockDTO.GenerateB2cDTO generateB2cDTO;
    /**
     * 第三方编号
     */
    private String thirdCode;

}
