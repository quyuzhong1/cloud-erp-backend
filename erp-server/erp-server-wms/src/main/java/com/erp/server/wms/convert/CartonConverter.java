package com.erp.server.wms.convert;

import com.erp.model.wms.dto.PackingTaskDetailDTO;
import com.erp.model.wms.dto.WmsCartonDetailDTO;
import com.erp.model.wms.dto.WmsCartonSpecDTO;
import com.erp.model.wms.entity.WmsCartonDetailEntity;
import com.erp.model.wms.entity.WmsCartonEntity;
import com.erp.model.wms.entity.WmsCartonSpecEntity;
import com.erp.server.wms.convert.tool.TypeConversionWorker;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 装箱明细转换类
 * @Author zdy
 * @Date 2024/7/2 18:55
 **/
@Mapper(uses = TypeConversionWorker.class)
@Component
public interface CartonConverter {
    CartonConverter INSTANCE = Mappers.getMapper(CartonConverter.class);


    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "createUserId", source = "createUserId"),
            @Mapping(target = "createUserName", source = "createUserName"),
            @Mapping(target = "createTime", source = "createTime"),
            @Mapping(target = "updateUserId", source = "updateUserId"),
            @Mapping(target = "updateUserName", source = "updateUserName"),
            @Mapping(target = "updateTime", source = "updateTime"),
            @Mapping(target = "version", ignore = true),
            @Mapping(target = "isDeleted", ignore = true),
            @Mapping(target = "boxSpecNo", source = "boxSpecNo"),
            @Mapping(target = "packageWeight", source = "packageWeight"),
            @Mapping(target = "weightUnit", constant = "kg"),
            @Mapping(target = "boxLength", source = "boxLength"),
            @Mapping(target = "boxWidth", source = "boxWidth"),
            @Mapping(target = "boxHeight", source = "boxHeight"),
            @Mapping(target = "sizeUnit", constant = "cm"),
            @Mapping(target = "boxQty", constant = "1"),
            @Mapping(target = "mainId", ignore = true),
            @Mapping(target = "measureSource", constant = "manual")
    })
    WmsCartonSpecEntity historyToSpec(PackingTaskDetailDTO.HistoryCartonDTO historyCartonDTO);

    /**
     *
     * @param historyCartonDTO
     * @return
     */
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "createUserId", source = "createUserId"),
            @Mapping(target = "createUserName", source = "createUserName"),
            @Mapping(target = "createTime", source = "createTime"),
            @Mapping(target = "updateUserId", source = "updateUserId"),
            @Mapping(target = "updateUserName", source = "updateUserName"),
            @Mapping(target = "updateTime", source = "updateTime"),
            @Mapping(target = "version", ignore = true),
            @Mapping(target = "isDeleted", ignore = true),
            @Mapping(target = "boxNo", source = "boxNo"),
            @Mapping(target = "packingStatus", constant = "completed"),
            @Mapping(target = "weightingStatus", constant = "success"),
            @Mapping(target = "packingUserId", source = "packingUserId"),
            @Mapping(target = "packingUserName", source = "packingUserName"),
            @Mapping(target = "packingTaskId", ignore = true)
    })
    WmsCartonEntity historyToCarton(PackingTaskDetailDTO.HistoryCartonDTO historyCartonDTO);
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "createUserId", source = "createUserId"),
            @Mapping(target = "createUserName", source = "createUserName"),
            @Mapping(target = "createTime", source = "createTime"),
            @Mapping(target = "updateUserId", source = "updateUserId"),
            @Mapping(target = "updateUserName", source = "updateUserName"),
            @Mapping(target = "updateTime", source = "updateTime"),
            @Mapping(target = "version", ignore = true),
            @Mapping(target = "isDeleted", ignore = true),
            @Mapping(target = "skuId", source = "skuId"),
            @Mapping(target = "skuNo", source = "skuNo"),
            @Mapping(target = "packQty", source = "packQty"),
            @Mapping(target = "grossWeight", source = "grossWeight"),
            @Mapping(target = "weightUnit", constant = "kg"),
            @Mapping(target = "mainId", ignore = true)
    })
    WmsCartonDetailEntity historyToCartonDetail(PackingTaskDetailDTO.HistoryCartonDTO cartonDTO);
    List<WmsCartonDetailEntity> historyToCartonDetail(List<PackingTaskDetailDTO.HistoryCartonDTO> cartonDTOList1);
    @Mappings({
            @Mapping(target = "id", source = "specId"),
            @Mapping(target = "createUserId", ignore = true),
            @Mapping(target = "createUserName", ignore = true),
            @Mapping(target = "createTime", ignore = true),
            @Mapping(target = "updateUserId", ignore = true),
            @Mapping(target = "updateUserName", ignore = true),
            @Mapping(target = "updateTime", ignore = true),
            @Mapping(target = "version", ignore = true),
            @Mapping(target = "isDeleted", ignore = true),
            @Mapping(target = "packageWeight", source = "packageWeight"),
            @Mapping(target = "boxLength", source = "boxLength"),
            @Mapping(target = "boxWidth", source = "boxWidth"),
            @Mapping(target = "boxHeight", source = "boxHeight"),
            @Mapping(target = "weightUnit", source = "weightUnit"),
            @Mapping(target = "sizeUnit", source = "sizeUnit"),
            @Mapping(target = "measureSource", source = "measureSource"),
            @Mapping(target = "boxQty", constant = "1"),
            @Mapping(target = "boxSpecNo", ignore = true),
            @Mapping(target = "mainId", ignore = true)
    })
    WmsCartonSpecEntity convertDtoToCartonSpec(WmsCartonSpecDTO.SpecSaveDTO dto);

    @Mapping(target = "productName", ignore = true)
    @Mapping(target = "detailId", source = "id")
    WmsCartonDetailDTO.BoxDetailDTO convertCartonDetailToBoxDTO(WmsCartonDetailEntity detailEntity);
    List<WmsCartonDetailDTO.BoxDetailDTO> convertCartonDetailToBoxDTO(List<WmsCartonDetailEntity> detailEntityList);
}
