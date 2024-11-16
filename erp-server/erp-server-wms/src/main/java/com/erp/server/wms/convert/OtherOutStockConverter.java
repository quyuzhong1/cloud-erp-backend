package com.erp.server.wms.convert;

import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.erp.model.oms.dto.CustomerDTO;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.model.wms.dto.OtherOutstockCustomerDTO;
import com.erp.model.wms.dto.OtherOutstockDTO;
import com.erp.model.wms.dto.OtherOutstockDetailDTO;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.dto.excel.OtherOutStockImportExcelDTO;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.InventoryDirectionEnum;
import com.erp.model.wms.enums.OutstockTypeEnum;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * 其他出库实体映射工具
 **/
@Mapper
@Component
public interface OtherOutStockConverter {

    OtherOutStockConverter INSTANCE = Mappers.getMapper(OtherOutStockConverter.class);


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
            @Mapping(target = "customerId", source = "id"),
            @Mapping(target = "customerCode", source = "code"),
            @Mapping(target = "name", source = "name"),
            @Mapping(target = "receiveAddress", source = "receiveAddress"),
            @Mapping(target = "receiverName", source = "receiverName"),
            @Mapping(target = "telNumber", source = "telNumber"),
    })
    OtherOutstockCustomerEntity convertCustomerEntity(CustomerDTO.ReceiveInfoDTO customerDTO);

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
            @Mapping(target = "remark", source = "remark"),
            @Mapping(target = "inventoryDirection", source = "inventoryDirectionEnum.code"),
            @Mapping(target = "warehouseKeeperId", constant = ""),
            @Mapping(target = "warehouseKeeperName", constant = ""),
            @Mapping(target = "receiverId", expression = "java(null == userDTO ? \"\" : userDTO.getUserId())"),
            @Mapping(target = "receiverName", expression = "java(null == userDTO ? \"\" : userDTO.getUserName())"),
            @Mapping(target = "warehouseId", source = "warehouseDTO.id"),
            @Mapping(target = "warehouseName", source = "warehouseDTO.name"),
            @Mapping(target = "inventoryOrgId", source = "warehouseDTO.orgId"),
            @Mapping(target = "inventoryOrgName", source = "warehouseDTO.orgName"),
            @Mapping(target = "receiveOrgId", source = "orgDTO.id"),
            @Mapping(target = "receiveOrgName", source = "orgDTO.name"),
            @Mapping(target = "type", source = "type"),
            @Mapping(target = "outType", source = "outType"),
            @Mapping(target = "deptId", source = "departmentDTO.id"),
            @Mapping(target = "deptName", source = "departmentDTO.name"),
            @Mapping(target = "code", source = "code"),
            @Mapping(target = "approveStatus", constant = "waitSubmit"),
    })
    OtherOutstockEntity combineAddEntity(OtherOutStockImportExcelDTO importExcelDTO,
                                         String type,
                                         String outType,
                                         LocalDate billDate,
                                         InventoryDirectionEnum inventoryDirectionEnum,
                                         WarehouseDTO.ListDTO warehouseDTO,
                                         WarehouseLocationEntity locationEntity,
                                         SysDepartmentDTO departmentDTO,
                                         FindUserDTO userDTO,
                                         BaseIdDTO orgDTO,
                                         String code,
                                         String remark
    );

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
    OtherOutstockDetailEntity combineDetailEntity(OtherOutStockImportExcelDTO importExcelDTO,
                                                  SkuVO skuVO,
                                                  WarehouseLocationEntity locationEntity,
                                                  Integer actualQty);
}
