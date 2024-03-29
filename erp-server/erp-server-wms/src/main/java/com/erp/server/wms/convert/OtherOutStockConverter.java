package com.erp.server.wms.convert;

import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.core.anno.StateEnumValue;
import com.erp.model.oms.dto.CustomerDTO;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.model.sys.dto.SysUserDeptDTO;
import com.erp.model.wms.dto.OtherOutstockCustomerDTO;
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
            @Mapping(target = "warehouseLocation", expression = "java(null == locationEntity ? \"\" : locationEntity.getCode())"),
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
            @Mapping(target = "receiverId", expression = "java(null == userDTO ? \"\" : userDTO.getUserId())"),
            @Mapping(target = "warehouseId", source = "warehouseDTO.id"),
            @Mapping(target = "receiveOrgId", source = "orgDTO.id"),
            @Mapping(target = "type", source = "outstockTypeEnum.code"),
            @Mapping(target = "typeName", source = "outstockTypeEnum.name"),
            @Mapping(target = "deptId", source = "departmentDTO.id"),
            @Mapping(target = "processApplyCode", source = "importExcelDTO.processApplyCode"),
            @Mapping(target = "otherOutstockCustomer", source = "addCustomerDTO"),
            @Mapping(target = "detailList", source = "detailList"),
    })
    OtherOutstockDTO.AddDTO combineAddDTO(OtherOutStockImportExcelDTO importExcelDTO,
                                          OutstockTypeEnum outstockTypeEnum,
                                          LocalDate billDate,
                                          InventoryDirectionEnum inventoryDirectionEnum,
                                          WarehouseDTO.ListDTO warehouseDTO,
                                          WarehouseLocationEntity locationEntity,
                                          FindUserDTO userDTO,
                                          SysDepartmentDTO departmentDTO,
                                          BaseIdDTO orgDTO,
                                          OtherOutstockCustomerDTO.AddDTO addCustomerDTO,
                                          List<OtherOutstockDetailDTO.AddDTO> detailList);


    @Mappings({
            @Mapping(target = "customerId", source = "id"),
            @Mapping(target = "customerCode", source = "code"),
            @Mapping(target = "name", source = "name"),
            @Mapping(target = "receiveAddress", source = "receiveAddress"),
            @Mapping(target = "receiverName", source = "receiverName"),
            @Mapping(target = "telNumber", source = "telNumber"),
    })
    OtherOutstockCustomerDTO.AddDTO convertAddDTO(CustomerDTO.ReceiveInfoDTO customerDTO);
}
