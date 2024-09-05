package com.erp.server.mrp.mapper;

import com.erp.model.mrp.dto.ReplenishmentResultDTO;
import com.erp.model.tms.entity.LogisticsBillEntity;
import com.erp.model.tms.entity.LogisticsChannelEntity;
import com.erp.model.wms.dto.FirstMileDeliveryDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface InventoryMapper {


    int getFbaUsable(@Param("result") ReplenishmentResultDTO replenishmentResultDTO,@Param("code") String code, @Param("tableName") String tableName);

    List<ReplenishmentResultDTO.FbaInTransitDetailDTO> getFbaShipment(@Param("result") ReplenishmentResultDTO replenishmentResultDTO, @Param("tableName") String tableName, @Param("tableDetailName") String tableDetailName);

    List<ReplenishmentResultDTO.FbaInTransitDetailDTO> getFbaDelivery(@Param("result") ReplenishmentResultDTO replenishmentResultDTO, @Param("tableName") String tableName, @Param("tableDetailName") String tableDetailName,@Param("shipmentName") String shipmentName, @Param("shipmentDetailName") String shipmentDetailName);

    List<LogisticsBillEntity> getLogisticsBillBySourceIds(@Param("sourceIds") List<String> firstMileDeliveryIds, @Param("tableName") String tableName);

    List<LogisticsChannelEntity> listLogisticsChannelByIds(@Param("channelIds") List<String> channelIds, @Param("tableName") String tableName);

    List<FirstMileDeliveryDTO.FbaShipmentDTO> listFirstMileDelivery(@Param("codes") List<String> codes, @Param("tableName") String tableName, @Param("tableDetailName") String tableDetailName);
}
