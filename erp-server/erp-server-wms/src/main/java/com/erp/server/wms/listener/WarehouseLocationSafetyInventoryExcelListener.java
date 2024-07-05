package com.erp.server.wms.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.wms.dto.WarehouseLocationSafetyInventoryDTO;
import lombok.Getter;
import org.apache.commons.lang3.ObjectUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 仓位安全库存导入监听
 * @date 2024-06-21
 * @author tanmujin
 */
@Getter
public class WarehouseLocationSafetyInventoryExcelListener extends AnalysisEventListener<WarehouseLocationSafetyInventoryDTO.importExcelDTO> {

    private List<WarehouseLocationSafetyInventoryDTO.importExcelDTO> successList = new ArrayList<>();
    private List<WarehouseLocationSafetyInventoryDTO.importExcelDTO> errorList = new ArrayList<>();

    @Override
    public void invoke(WarehouseLocationSafetyInventoryDTO.importExcelDTO data, AnalysisContext context) {
        List<String> errorMsgList = FieldValidUtil.fieldValid(data);
        if(data.getSafetyQty() < 0){
            data.setErrorInfo(data.getErrorInfo() + "，安全库存不能小于0");
        }
        if(data.getMaxQty() < 0){
            data.setErrorInfo(data.getErrorInfo() + "，最大库存不能小于0");
        }
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
