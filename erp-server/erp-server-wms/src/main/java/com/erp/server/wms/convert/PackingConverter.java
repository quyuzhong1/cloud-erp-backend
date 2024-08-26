package com.erp.server.wms.convert;

import com.common.business.utils.MD5Util;
import com.common.business.utils.StringUtil;
import com.erp.model.wms.dto.*;
import com.erp.model.wms.entity.*;
import com.erp.server.wms.convert.tool.TypeConversionWorker;
import com.sdk.wms.iml.dto.response.ImlProductResp;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 装箱任务转换类
 * @Author zdy
 * @Date 2024/7/2 18:55
 **/
@Mapper(uses = TypeConversionWorker.class)
@Component
public interface PackingConverter {
    PackingConverter INSTANCE = Mappers.getMapper(PackingConverter.class);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "code", ignore = true),
            @Mapping(target = "createUserId", ignore = true),
            @Mapping(target = "createUserName", ignore = true),
            @Mapping(target = "createTime", ignore = true),
            @Mapping(target = "updateUserId", ignore = true),
            @Mapping(target = "updateUserName", ignore = true),
            @Mapping(target = "updateTime", ignore = true),
            @Mapping(target = "version", ignore = true),
            @Mapping(target = "isDeleted", ignore = true),
            @Mapping(target = "sourceId", source = "id"),
            @Mapping(target = "sourceCode", source = "code"),
            @Mapping(target = "sourceType", constant = "B2B"),
            @Mapping(target = "warehouseId", source = "warehouseId"),
            @Mapping(target = "warehouseName", source = "warehouseName"),
            @Mapping(target = "deliveryQty", constant = "0"),
            @Mapping(target = "weightingStatus", constant = "unweighed"),
            @Mapping(target = "packingStatus", constant = "unpacked")
    })
    PackingTaskEntity b2bDeliveryToPackingTask(SoDeliveryNoticeEntity soDeliveryNoticeEntity);
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "mainId", ignore = true),
            @Mapping(target = "createUserId", ignore = true),
            @Mapping(target = "createUserName", ignore = true),
            @Mapping(target = "createTime", ignore = true),
            @Mapping(target = "updateUserId", ignore = true),
            @Mapping(target = "updateUserName", ignore = true),
            @Mapping(target = "updateTime", ignore = true),
            @Mapping(target = "version", ignore = true),
            @Mapping(target = "isDeleted", ignore = true),
            @Mapping(target = "skuId", source = "detailEntity.skuId"),
            @Mapping(target = "skuNo", source = "detailEntity.skuNo"),
            @Mapping(target = "deliveryQty", source = "detailEntity.deliveryQty"),
            @Mapping(target = "sourceDetailId", source = "detailEntity.id"),
            @Mapping(target = "fnSku", ignore = true)
    })
    PackingTaskDetailEntity b2bDeliveryDetailToPackingTaskDetail(SoDeliveryNoticeDetailEntity detailEntity);
    List<PackingTaskDetailEntity> b2bDeliveryDetailToPackingTaskDetail(List<SoDeliveryNoticeDetailEntity> detailEntityList);
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "code", ignore = true),
            @Mapping(target = "createUserId", ignore = true),
            @Mapping(target = "createUserName", ignore = true),
            @Mapping(target = "createTime", ignore = true),
            @Mapping(target = "updateUserId", ignore = true),
            @Mapping(target = "updateUserName", ignore = true),
            @Mapping(target = "updateTime", ignore = true),
            @Mapping(target = "version", ignore = true),
            @Mapping(target = "isDeleted", ignore = true),
            @Mapping(target = "sourceId", source = "firstMileDeliveryEntity.id"),
            @Mapping(target = "sourceCode", source = "firstMileDeliveryEntity.code"),
            @Mapping(target = "sourceType", source = "sourceType"),
            @Mapping(target = "warehouseId", source = "firstMileDeliveryEntity.deliveryWarehouseId"),
            @Mapping(target = "warehouseName", source = "firstMileDeliveryEntity.deliveryWarehouseName"),
            @Mapping(target = "deliveryQty", constant = "0"),
            @Mapping(target = "weightingStatus", constant = "unweighed"),
            @Mapping(target = "packingStatus", constant = "unpacked")
    })
    PackingTaskEntity firstMileDeliveryToPackingTask(FirstMileDeliveryEntity firstMileDeliveryEntity,String sourceType);
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "mainId", ignore = true),
            @Mapping(target = "createUserId", ignore = true),
            @Mapping(target = "createUserName", ignore = true),
            @Mapping(target = "createTime", ignore = true),
            @Mapping(target = "updateUserId", ignore = true),
            @Mapping(target = "updateUserName", ignore = true),
            @Mapping(target = "updateTime", ignore = true),
            @Mapping(target = "version", ignore = true),
            @Mapping(target = "isDeleted", ignore = true),
            @Mapping(target = "skuId", source = "detailEntity.skuId"),
            @Mapping(target = "skuNo", source = "detailEntity.skuNo"),
            @Mapping(target = "deliveryQty", source = "detailEntity.deliveryQty"),
            @Mapping(target = "sourceDetailId", source = "detailEntity.id"),
//            @Mapping(target = "fnSku", expression = "java(PackingConverter.getFnSkuByDeliveryDetail(detailEntity))"),
    })
    PackingTaskDetailEntity firstMileDeliveryDetailToPackingTaskDetail(FirstMileDeliveryDetailEntity detailEntity);
    List<PackingTaskDetailEntity> firstMileDeliveryDetailToPackingTaskDetail(List<FirstMileDeliveryDetailEntity> detailEntityList);

    @Mappings({
            @Mapping(target = "skuId", source = "skuId"),
            @Mapping(target = "skuNo", source = "skuNo"),
            @Mapping(target = "packQty", source = "packQty"),
            @Mapping(target = "grossWeight", source = "grossWeight"),
            @Mapping(target = "weightUnit", source = "weightUnit")
    })
    WmsCartonSpecDTO.CartonDetailDTO cartonDetailToDTO(WmsCartonDetailEntity detailEntity);
    List<WmsCartonSpecDTO.CartonDetailDTO> cartonDetailToDTO(List<WmsCartonDetailEntity> detailEntityList);

    /**
     * 调整装箱
     * @param adjustDetailDTO
     * @param cartonId
     * @return
     */
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "mainId", source = "cartonId"),
            @Mapping(target = "createUserId", ignore = true),
            @Mapping(target = "createUserName", ignore = true),
            @Mapping(target = "createTime", ignore = true),
            @Mapping(target = "updateUserId", ignore = true),
            @Mapping(target = "updateUserName", ignore = true),
            @Mapping(target = "updateTime", ignore = true),
            @Mapping(target = "version", ignore = true),
            @Mapping(target = "isDeleted", ignore = true),
            @Mapping(target = "skuId", source = "adjustDetailDTO.skuId"),
            @Mapping(target = "skuNo", source = "adjustDetailDTO.skuNo"),
            @Mapping(target = "packQty", ignore = true),
            @Mapping(target = "grossWeight", ignore = true),
            @Mapping(target = "weightUnit", constant = "kg"),
    })
    WmsCartonDetailEntity cartonDtoToDetail(WmsCartonDTO.AdjustDetailDTO adjustDetailDTO, String cartonId);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "code", ignore = true),
            @Mapping(target = "createUserId", ignore = true),
            @Mapping(target = "createUserName", ignore = true),
            @Mapping(target = "createTime", ignore = true),
            @Mapping(target = "updateUserId", ignore = true),
            @Mapping(target = "updateUserName", ignore = true),
            @Mapping(target = "updateTime", ignore = true),
            @Mapping(target = "version", ignore = true),
            @Mapping(target = "isDeleted", ignore = true),
            @Mapping(target = "sourceId", source = "requisitionApplicationEntity.id"),
            @Mapping(target = "sourceCode", source = "requisitionApplicationEntity.code"),
            @Mapping(target = "sourceType", source = "sourceType"),
            @Mapping(target = "warehouseId", source = "requisitionApplicationEntity.requisitionWarehouseId"),
            @Mapping(target = "warehouseName", source = "requisitionApplicationEntity.requisitionWarehouseName"),
            @Mapping(target = "deliveryQty", constant = "0"),
            @Mapping(target = "weightingStatus", constant = "unweighed"),
            @Mapping(target = "packingStatus", constant = "unpacked")
    })
    PackingTaskEntity requisitionToPackingTask(RequisitionApplicationEntity requisitionApplicationEntity, String sourceType);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "mainId", ignore = true),
            @Mapping(target = "createUserId", ignore = true),
            @Mapping(target = "createUserName", ignore = true),
            @Mapping(target = "createTime", ignore = true),
            @Mapping(target = "updateUserId", ignore = true),
            @Mapping(target = "updateUserName", ignore = true),
            @Mapping(target = "updateTime", ignore = true),
            @Mapping(target = "version", ignore = true),
            @Mapping(target = "isDeleted", ignore = true),
            @Mapping(target = "skuId", source = "detailEntity.skuId"),
            @Mapping(target = "skuNo", source = "detailEntity.skuNo"),
            @Mapping(target = "deliveryQty", source = "detailEntity.pickingQty"),
            @Mapping(target = "sourceDetailId", source = "detailEntity.id"),
//            @Mapping(target = "fnSku", expression = "java(PackingConverter.getFnSkuByReqDetail(detailEntity))")
    })
    PackingTaskDetailEntity requisitionDetailToPackingTaskDetail(RequisitionApplicationDetailEntity detailEntity);
    List<PackingTaskDetailEntity> requisitionDetailToPackingTaskDetail(List<RequisitionApplicationDetailEntity> detailEntityList);

    static String getFnSkuByReqDetail(RequisitionApplicationDetailEntity detailEntity){
        return StringUtils.isBlank(detailEntity.getPlatformFnSku())?detailEntity.getPlatformSku():detailEntity.getPlatformFnSku();
    }
    static String getFnSkuByDeliveryDetail(FirstMileDeliveryDetailEntity detailEntity){
        return StringUtils.isBlank(detailEntity.getFnSku())?detailEntity.getPlatformSkuNo():detailEntity.getFnSku();
    }

}
