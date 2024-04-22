package com.erp.server.tms.convert;

import com.baomidou.mybatisplus.annotation.TableField;
import com.erp.model.tms.dto.CfgReconciliationFieldDTO;
import com.erp.model.tms.dto.excel.CfgReconciliationFieldImportExcelDTO;
import com.erp.model.tms.entity.CfgReconciliationFieldEntity;
import com.erp.model.tms.entity.DictBasicEntity;
import com.erp.model.tms.entity.LogisticsSupplierEntity;
import com.erp.model.tms.entity.LogisticsTrackEntity;
import com.erp.model.tms.enums.CfgReconciliationTypeEnum;
import com.sdk.tms.track123.dto.PlatformTrackDetail;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 转换
 * @author Jim
 */
@Mapper
@Component
public interface CfgReconciliationFieldConverter {

    CfgReconciliationFieldConverter INSTANCE = Mappers.getMapper(CfgReconciliationFieldConverter.class);



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
            @Mapping(target = "reconciliationType", source = "cfgReconciliationTypeEnum.code"),
            @Mapping(target = "thirdName", source = "supplierEntity.supplierName"),
            @Mapping(target = "thirdCode", source = "supplierEntity.id"),
            @Mapping(target = "thirdFieldName", source = "importExcelDTO.thirdFieldName"),
            @Mapping(target = "sourceType", source = "erpFieldDTO.sourceType"),
            @Mapping(target = "sourceId", source = "erpFieldDTO.sourceId"),
            @Mapping(target = "status", ignore = true)
    })
    CfgReconciliationFieldEntity combineAddEntity(CfgReconciliationFieldImportExcelDTO importExcelDTO,
                                                  CfgReconciliationTypeEnum cfgReconciliationTypeEnum,
                                                  LogisticsSupplierEntity supplierEntity,
                                                  CfgReconciliationFieldDTO.ErpFieldDropDownDTO erpFieldDTO);
}
