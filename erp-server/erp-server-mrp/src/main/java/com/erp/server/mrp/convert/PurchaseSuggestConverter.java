package com.erp.server.mrp.convert;

import com.erp.model.mrp.dto.PurchaseSuggestMergeDTO;
import com.erp.model.mrp.dto.ReplenishmentResultDTO;
import com.erp.model.mrp.entity.PurchaseSuggestMergeEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component
public interface PurchaseSuggestConverter {

    PurchaseSuggestConverter INSTANCE = Mappers.getMapper(PurchaseSuggestConverter.class);

    /**
     * 复制对象
     */
    ReplenishmentResultDTO.PurchaseSuggestDTO copyPurchaseSuggest(ReplenishmentResultDTO.PurchaseSuggestDTO suggestDTO);

    /**
     * 采购源数据转换为新增对象
     */
    List<PurchaseSuggestMergeDTO.AddOrUpdateDTO> purchaseSuggestToMergeAdd(List<ReplenishmentResultDTO.PurchaseSuggestDTO> suggestList);
    PurchaseSuggestMergeDTO.AddOrUpdateDTO purchaseSuggestToMergeAdd(ReplenishmentResultDTO.PurchaseSuggestDTO suggestDTO);
    /**
     * 新增对象转换为实体类
     */
    PurchaseSuggestMergeEntity copyToMergeEntity(PurchaseSuggestMergeDTO.AddOrUpdateDTO addOrUpdateDTO);
}
