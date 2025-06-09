package com.erp.server.wms.listener;

import cn.hutool.core.text.CharSequenceUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.erp.model.wms.dto.excel.WarehouseLocationExcelDto;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * 仓位信息导入监听
 * @date 2024-05-31
 * @author tanmujin
 */
@Getter
public class WarehouseLocationExcelListener extends AnalysisEventListener<LinkedHashMap<Integer, String>> {

    private List<WarehouseLocationExcelDto> errorList = new ArrayList<>();
    private List<WarehouseLocationExcelDto> successList = new ArrayList<>();

    @Override
    public void invoke(LinkedHashMap<Integer, String> data, AnalysisContext context) {
        Integer rowIndex = context.readRowHolder().getRowIndex();
        WarehouseLocationExcelDto dto = new WarehouseLocationExcelDto();
        dto.setWarehouseName(data.get(0));
        dto.setWarehouseAreaName(data.get(1));
        dto.setWarehouseLocationCode(data.get(2));
        dto.setWarehouseLocationName(data.get(3));
        dto.setRemark(data.get(4));
        verifyField(dto);
        if(CharSequenceUtil.isNotBlank(dto.getErrorMsg())){
            errorList.add(dto);
        }else {
            successList.add(dto);
        }
    }

    private void verifyField(WarehouseLocationExcelDto dto) {
        if(CharSequenceUtil.isBlank(dto.getWarehouseName())){
            dto.setErrorMsg("仓库不能为空，");
            return;
        }
        if(CharSequenceUtil.isBlank(dto.getWarehouseAreaName())){
            dto.setErrorMsg("库区不能为空");
            return;
        }
        if(CharSequenceUtil.isBlank(dto.getWarehouseLocationCode())){
            dto.setErrorMsg("仓位编码不能为空");
            return;
        }
        if(CharSequenceUtil.isBlank(dto.getWarehouseLocationName())){
            dto.setErrorMsg("仓位名称不能为空");
            return;
        }
        if(dto.getWarehouseLocationCode().length() > 32){
            dto.setErrorMsg("仓位编码过长");
            return;
        }
        if(dto.getWarehouseLocationName().length() > 200){
            dto.setErrorMsg("仓位名称过长");
            return;
        }
        if(dto.getRemark() != null && dto.getRemark().length() > 200){
            dto.setErrorMsg("备注过长");
            return;
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {

    }
}
