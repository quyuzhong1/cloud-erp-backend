package com.erp.model.oms.dto;

import com.common.business.dto.ReceiverDTO;
import com.common.business.dto.UniqueDto;
import com.erp.model.oms.entity.SoB2cDetailEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.entity.SoB2cLogisticsEntity;
import com.erp.model.wms.dto.OverseasProviderWarehouseDTO;
import com.erp.model.wms.dto.third.ThirdWarehouseAuth;
import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.List;

/**
 * @author liuruipeng
 * @date 2023年11月17日 10:22
 */

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class ThirdWarehouseCreateOutboundPushDTO extends UniqueDto {
    private String soId;
    private String soCode;
    private SoB2cEntity entity;
    private String warehouseId;
    private String warehouseManageType;
    private SoB2cLogisticsEntity logisticsEntity;
    private OverseasProviderWarehouseDTO.ViewDTO overseasProviderWarehouse;
    private List<SoB2cDetailEntity> detailList;
    private String newChannelId;
}
