package com.erp.server.tms.convert;

import com.erp.model.oms.dto.SplitSkuDTO;
import com.erp.model.oms.dto.TransferDeclareProductDTO;
import com.erp.model.plm.dto.LogisticsProductDTO;
import com.erp.model.tms.dto.FirstMileSkuCostAllocationDetailDTO;
import com.erp.model.tms.dto.transfer.TransferLogisticsCreateOrderReq;
import com.erp.model.tms.entity.FirstMileSkuCostAllocationDetailEntity;
import com.erp.model.tms.entity.TransferDeclareProductEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component
public interface FirstMileCostAllocationConverter {
    FirstMileCostAllocationConverter INSTANCE = Mappers.getMapper(FirstMileCostAllocationConverter.class);

    @Mappings({
            @Mapping(target = "skuNo", source = "skuNo"),
            @Mapping(target = "qty", source = "qty"),
            @Mapping(target = "productTitleEn", source = "declareEnglishName"),
            @Mapping(target = "purposeDeclaredValue", source = "toDeclarePrice"),
    })
    TransferLogisticsCreateOrderReq.ProductDetail declareProductEntityToCreateOrderReq(TransferDeclareProductEntity declareProductEntity);

    FirstMileSkuCostAllocationDetailDTO.ViewDTO detailToViewDTO(FirstMileSkuCostAllocationDetailEntity detailEntity);
    List<FirstMileSkuCostAllocationDetailDTO.ViewDTO> detailToViewDTO(List<FirstMileSkuCostAllocationDetailEntity> detailEntityList1);
}
