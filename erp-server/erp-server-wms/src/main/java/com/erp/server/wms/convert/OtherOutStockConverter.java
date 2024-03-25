package com.erp.server.wms.convert;

import com.common.business.dto.base.BaseIdDTO;
import com.common.core.anno.StateEnumValue;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.model.sys.dto.SysUserDeptDTO;
import com.erp.model.wms.dto.OtherOutstockDTO;
import com.erp.model.wms.dto.OtherOutstockDetailDTO;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.dto.excel.OtherOutStockImportExcelDTO;
import com.erp.model.wms.entity.WarehouseLocationEntity;
import com.erp.model.wms.enums.InventoryDirectionEnum;
import com.erp.model.wms.enums.OutstockTypeEnum;
import jnr.ffi.annotations.In;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

import javax.validation.constraints.*;
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
            @Mapping(target = "skuId", source = "skuVO.skuId"),
            @Mapping(target = "skuNo", source = "skuVO.skuNo"),
            @Mapping(target = "actualQty", source = "actualQty"),
            @Mapping(target = "warehouseLocation", expression = "java(null == locationEntity ? \"\" : locationEntity.getId())"),
            @Mapping(target = "remark", source = "importExcelDTO.remark"),
    })
    OtherOutstockDetailDTO.AddDTO combineDetailDTO(OtherOutStockImportExcelDTO importExcelDTO,
                                                   SkuVO skuVO,
                                                   WarehouseLocationEntity locationEntity,
                                                   Integer actualQty
    );


    @Mappings({
            @Mapping(target = "billDate", source = "billDate"),
            @Mapping(target = "inventoryDirection", source = "inventoryDirectionEnum.code"),
            @Mapping(target = "warehouseKeeperId", constant = ""),
            @Mapping(target = "receiverId", source = "userDeptDTO.uid"),
            @Mapping(target = "warehouseId", source = "warehouseDTO.id"),
            @Mapping(target = "receiveOrgId", source = "userDeptDTO.deptId"),
            @Mapping(target = "type", source = "outstockTypeEnum.code"),
            @Mapping(target = "typeName", source = "outstockTypeEnum.name"),
            @Mapping(target = "deptId", source = "departmentDTO.id"),
            @Mapping(target = "processApplyCode", source = "importExcelDTO.processApplyCode"),
            @Mapping(target = "detailList", source = "detailList"),
    })
    OtherOutstockDTO.AddDTO combineAddDTO(OtherOutStockImportExcelDTO importExcelDTO,
                                          OutstockTypeEnum outstockTypeEnum,
                                          LocalDate billDate,
                                          InventoryDirectionEnum inventoryDirectionEnum,
                                          WarehouseDTO.ListDTO warehouseDTO,
                                          WarehouseLocationEntity locationEntity,
                                          SysUserDeptDTO userDeptDTO,
                                          SysDepartmentDTO departmentDTO,
                                          BaseIdDTO orgDTO,
                                          List<OtherOutstockDetailDTO.AddDTO> detailList);
}
