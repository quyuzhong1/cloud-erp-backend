package com.erp.model.oms.dto;

import com.erp.model.oms.entity.SoB2cDetailEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.wms.dto.OverseasProviderWarehouseDTO;
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
public class GenerateDeliveryAndOutStockDTO implements Serializable {

    private SoB2cEntity entity;

    private List<SoB2cDetailEntity> detailEntityList;

    private SoB2cDTO.DeliveryWithNotOutboundDTO dto;

    private OverseasProviderWarehouseDTO.ViewDTO overseasWarehouseDto;

}