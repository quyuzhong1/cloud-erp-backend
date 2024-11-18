package com.erp.server.wms.convert;

import com.common.business.dto.FindUserDTO;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.model.wms.dto.OtherInstockDTO;
import com.erp.model.wms.dto.OtherInstockDetailDTO;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.dto.excel.OtherInStockImportExcelDTO;
import com.erp.model.wms.entity.OtherInstockDetailEntity;
import com.erp.model.wms.entity.OtherInstockEntity;
import com.erp.model.wms.entity.WarehouseLocationEntity;
import com.erp.model.wms.enums.InstockTypeEnum;
import com.erp.model.wms.enums.InventoryDirectionEnum;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * 其他入库实体映射工具
 **/
@Mapper
@Component
public interface OtherInStockConverter {

    OtherInStockConverter INSTANCE = Mappers.getMapper(OtherInStockConverter.class);


    @Mappings({
            @Mapping(target = "skuId", source = "skuVO.skuId"),
            @Mapping(target = "skuNo", source = "skuVO.skuNo"),
            @Mapping(target = "actualQty", source = "actualQty"),
            @Mapping(target = "warehouseLocation", expression = "java(null == locationEntity ? \"\" : locationEntity.getCode())"),
            @Mapping(target = "remark", source = "importExcelDTO.remark"),
    })
    OtherInstockDetailDTO.AddDTO combineDetailDTO(OtherInStockImportExcelDTO importExcelDTO,
                                                  SkuVO skuVO,
                                                  WarehouseLocationEntity locationEntity,
                                                  Integer actualQty
    );


    @Mappings({
            @Mapping(target = "billDate", source = "billDate"),
            @Mapping(target = "inventoryDirection", source = "inventoryDirectionEnum.code"),
            @Mapping(target = "warehouseKeeperId", constant = ""),
            // 入库单无领料人
            @Mapping(target = "receiverId", expression = "java(null == userDTO ? \"\" : userDTO.getUserId())"),
            @Mapping(target = "warehouseId", source = "warehouseDTO.id"),
            @Mapping(target = "type", source = "inStockTypeEnum.code"),
            @Mapping(target = "typeName", source = "inStockTypeEnum.name"),
            @Mapping(target = "deptId", source = "departmentDTO.id"),
            @Mapping(target = "detailList", source = "detailList"),
            @Mapping(target = "remark", ignore = true),
    })
    OtherInstockDTO.AddDTO combineAddDTO(OtherInStockImportExcelDTO importExcelDTO,
                                         InstockTypeEnum inStockTypeEnum,
                                         LocalDate billDate,
                                         InventoryDirectionEnum inventoryDirectionEnum,
                                         WarehouseDTO.ListDTO warehouseDTO,
                                         WarehouseLocationEntity locationEntity,
                                         SysDepartmentDTO departmentDTO,
                                         FindUserDTO userDTO,
                                         List<OtherInstockDetailDTO.AddDTO> detailList);


    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "createTime", ignore = true),
            @Mapping(target = "createUserId", ignore = true),
            @Mapping(target = "createUserName", ignore = true),
            @Mapping(target = "updateTime", ignore = true),
            @Mapping(target = "updateUserId", ignore = true),
            @Mapping(target = "updateUserName", ignore = true),
            @Mapping(target = "isDeleted", ignore = true),
            @Mapping(target = "version", ignore = true),
            @Mapping(target = "billDate", source = "billDate"),
            @Mapping(target = "inventoryDirection", source = "inventoryDirectionEnum.code"),
            @Mapping(target = "warehouseKeeperId", constant = ""),
            @Mapping(target = "warehouseKeeperName", constant = ""),
            // 入库单无领料人
            @Mapping(target = "receiverId", expression = "java(null == userDTO ? \"\" : userDTO.getUserId())"),
            @Mapping(target = "receiverName", expression = "java(null == userDTO ? \"\" : userDTO.getUserName())"),
            @Mapping(target = "warehouseId", source = "warehouseDTO.id"),
            @Mapping(target = "warehouseName", source = "warehouseDTO.name"),
            @Mapping(target = "orgId", source = "warehouseDTO.orgId"),
            @Mapping(target = "orgName", source = "warehouseDTO.orgName"),
            @Mapping(target = "type", source = "inStockTypeEnum.code"),
            @Mapping(target = "deptId", source = "departmentDTO.id"),
            @Mapping(target = "deptName", source = "departmentDTO.name"),
            @Mapping(target = "code", source = "code"),
            @Mapping(target = "approveStatus", constant = "waitSubmit"),
            @Mapping(target = "remark", ignore = true),
    })
    OtherInstockEntity combineAddEntity(OtherInStockImportExcelDTO importExcelDTO,
                                        InstockTypeEnum inStockTypeEnum,
                                        LocalDate billDate,
                                        InventoryDirectionEnum inventoryDirectionEnum,
                                        WarehouseDTO.ListDTO warehouseDTO,
                                        WarehouseLocationEntity locationEntity,
                                        SysDepartmentDTO departmentDTO,
                                        FindUserDTO userDTO,
                                        String code);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "createTime", ignore = true),
            @Mapping(target = "createUserId", ignore = true),
            @Mapping(target = "createUserName", ignore = true),
            @Mapping(target = "updateTime", ignore = true),
            @Mapping(target = "updateUserId", ignore = true),
            @Mapping(target = "updateUserName", ignore = true),
            @Mapping(target = "isDeleted", ignore = true),
            @Mapping(target = "version", ignore = true),

            @Mapping(target = "skuId", source = "skuVO.skuId"),
            @Mapping(target = "skuNo", source = "skuVO.skuNo"),
            @Mapping(target = "unit", expression = "java(cn.hutool.core.text.CharSequenceUtil.isBlank(skuVO.getUnitName()) ? \"\" : skuVO.getUnitName())"),
            @Mapping(target = "actualQty", source = "actualQty"),
            @Mapping(target = "warehouseLocation", expression = "java(null == locationEntity ? \"\" : locationEntity.getCode())"),
            @Mapping(target = "remark", source = "importExcelDTO.remark"),
    })
    OtherInstockDetailEntity combineDetailEntity(OtherInStockImportExcelDTO importExcelDTO,
                                                 SkuVO skuVO,
                                                 WarehouseLocationEntity locationEntity,
                                                 Integer actualQty);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "createTime", ignore = true),
            @Mapping(target = "createUserId", ignore = true),
            @Mapping(target = "createUserName", ignore = true),
            @Mapping(target = "updateTime", ignore = true),
            @Mapping(target = "updateUserId", ignore = true),
            @Mapping(target = "updateUserName", ignore = true),
            @Mapping(target = "isDeleted", ignore = true),
            @Mapping(target = "version", ignore = true),
            @Mapping(target = "code", ignore = true),
    })
    OtherInstockEntity copy(OtherInstockEntity dbOtherInstockEntity);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "createTime", ignore = true),
            @Mapping(target = "createUserId", ignore = true),
            @Mapping(target = "createUserName", ignore = true),
            @Mapping(target = "updateTime", ignore = true),
            @Mapping(target = "updateUserId", ignore = true),
            @Mapping(target = "updateUserName", ignore = true),
            @Mapping(target = "isDeleted", ignore = true),
            @Mapping(target = "version", ignore = true),
            @Mapping(target = "mainId", ignore = true),
    })
    OtherInstockDetailEntity copyDetail(OtherInstockDetailEntity dbDetail);
    List<OtherInstockDetailEntity> copyDetailList(List<OtherInstockDetailEntity> dbDetailList);
}
