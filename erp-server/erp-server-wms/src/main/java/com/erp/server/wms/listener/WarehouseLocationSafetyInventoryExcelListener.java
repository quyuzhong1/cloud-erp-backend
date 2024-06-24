package com.erp.server.wms.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.wms.dto.WarehouseLocationSafetyInventoryDto;
import com.erp.model.wms.entity.WarehouseLocationSafetyInventoryEntity;
import lombok.Getter;
import org.apache.commons.lang3.ObjectUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 仓位安全库存导入监听
 * @date 2024-06-21
 * @author tanmujin
 */
@Getter
public class WarehouseLocationSafetyInventoryExcelListener extends AnalysisEventListener<WarehouseLocationSafetyInventoryDto.importExcelDto> {

    private List<WarehouseLocationSafetyInventoryDto.importExcelDto> successList = new ArrayList<>();
    private List<WarehouseLocationSafetyInventoryDto.importExcelDto> errorList = new ArrayList<>();

    @Override
    public void invoke(WarehouseLocationSafetyInventoryDto.importExcelDto data, AnalysisContext context) {
        /*if(ObjectUtils.isEmpty(data.getSkuNo())){
            data.setErrorInfo("SKU不能为空");
        }
        if(ObjectUtils.isEmpty(data.getWarehouseLocation())){
            data.setErrorInfo("仓位编码不能为空");
        }
        if(ObjectUtils.isEmpty(data.getWarehouseName())){
            data.setErrorInfo("所属仓库不能为空");
        }*/
        List<String> errorMsgList = FieldValidUtil.fieldValid(data);
        if(ObjectUtils.isEmpty(errorMsgList)){
            successList.add(data);
        }else {
            data.setErrorInfo(String.join(", ", errorMsgList));
            errorList.add(data);
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {

    }
}
