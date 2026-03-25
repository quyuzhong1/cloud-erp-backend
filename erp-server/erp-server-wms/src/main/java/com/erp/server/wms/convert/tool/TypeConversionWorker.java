package com.erp.server.wms.convert.tool;

import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.utils.IdGeneratorUtil;
import com.common.core.utils.IdUtils;
import com.common.core.utils.UUID;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.model.wms.enums.FbaDemandTypeEnum;
import com.erp.model.wms.enums.ShipmentSourceTypeEnum;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
@Named("TypeConversionWorker")
public class TypeConversionWorker {

    /**
     * 根据仓库id获取name
     * @Author Luo_WG
     * @Date 2023/11/2 14:46
     * @param warehouseId 仓库id
     * @return java.lang.String
     **/
    @Named("warehouseIdByName")
    public String warehouseIdByName(String warehouseId, List<WarehouseEntity> warehouseEntities) {
        String name = warehouseEntities.stream().filter(req -> req.getId().equals(warehouseId)).findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
        return name;
    }

    @Named("replacePdf")
    public String replacePdf(String fileData){
        if (CharSequenceUtil.isBlank(fileData)){
            return fileData;
        }
        return fileData.replace("data:application/pdf;base64,","");
    }

    @Named("getPdfFileName")
    public String getPdfFileName(String orderCode){
        if (CharSequenceUtil.isBlank(orderCode)){
            return IdUtils.fastSimpleUUID() + ".pdf";
        }
        return orderCode + ".pdf";
    }
    @Named("getIsGift")
    public Integer getIsGift(Boolean isGift) {
        return Objects.isNull(isGift) || !isGift ? 2 : 1;
    }

    /**
     * 四位小数转换成两位小数
     *
     */
    @Named("fourDecimalToTwoDecimal")
    public BigDecimal fourDecimalToTwoDecimal(BigDecimal value){
        if (Objects.isNull(value)){
            return value;
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    @Named("decimalToPrintData")
    public String decimalToPrintData(BigDecimal value){
        if (Objects.isNull(value)){
            return "";
        }
        return value.stripTrailingZeros().toPlainString();
    }

     /**
     * 转换sourceType到demandType
     */
    @Named("convertSourceTypeToDemandType")
    public String convertSourceTypeToDemandType(String sourceType){
        if (CharSequenceUtil.isBlank(sourceType)){
            return sourceType;
        }
        if (ShipmentSourceTypeEnum.FBA.getCode().equals(sourceType)){
            return FbaDemandTypeEnum.DEMAND_PLATFORM_WAREHOUSE.getCode();
        }
        if (ShipmentSourceTypeEnum.AWD.getCode().equals(sourceType)){
            return FbaDemandTypeEnum.DEMAND_AWD_WAREHOUSE.getCode();
        }
        return sourceType;
    }
}
