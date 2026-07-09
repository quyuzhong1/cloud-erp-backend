package com.erp.server.wms.convert;

import com.erp.model.wms.dto.OverseasWarehouseInboundDTO;
import com.erp.model.wms.entity.OverseasWarehouseInboundReceivedEntity;
import com.erp.server.wms.convert.tool.TypeConversionWorker;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

/**
 * 海外仓入库api相关
 **/
@Mapper(uses = TypeConversionWorker.class)
@Component
public interface WmsOverseasWarehouseInboundConverter {

    /**
     * 不良品标识中文名称：true → 不良品
     */
    String DEFECTIVE_PRODUCT_FLAG_NAME_TRUE = "不良品";

    /**
     * 不良品标识中文名称：false / null → 可用
     */
    String DEFECTIVE_PRODUCT_FLAG_NAME_FALSE = "可用";

    WmsOverseasWarehouseInboundConverter INSTANCE = Mappers.getMapper(WmsOverseasWarehouseInboundConverter.class);

    @Mappings({
            @Mapping(target = "sourceTypeName", expression = "java(com.erp.model.wms.enums.SignSourceTypeEnum.getName(entity.getSourceType()))"),
            @Mapping(target = "defectiveProductFlagName",
                    source = "defectiveProductFlag",
                    qualifiedByName = "defectiveProductFlagToName")
    })
    OverseasWarehouseInboundDTO.ReceiveRecordView receivedEntityToView(OverseasWarehouseInboundReceivedEntity entity);

    /**
     * 不良品标识 → 中文名称：true=不良品，false/null=可用。
     * <p>
     * 历史数据 {@code defective_product_flag} 字段可能为 null（早期接入平台未回写），统一按可用处理，
     * 避免前端展示空白；新增数据由 wego 等平台显式回传，按业务语义渲染。
     */
    @Named("defectiveProductFlagToName")
    default String defectiveProductFlagToName(Boolean defectiveProductFlag) {
        return Boolean.TRUE.equals(defectiveProductFlag)
                ? DEFECTIVE_PRODUCT_FLAG_NAME_TRUE
                : DEFECTIVE_PRODUCT_FLAG_NAME_FALSE;
    }

}
