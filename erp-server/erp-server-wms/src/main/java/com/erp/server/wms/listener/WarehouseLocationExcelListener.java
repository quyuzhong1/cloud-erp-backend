package com.erp.server.wms.listener;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.read.metadata.holder.ReadRowHolder;
import com.erp.model.wms.dto.excel.WarehouseLocationExcelDto;
import lombok.Data;
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

    private List<String> errorMsgList = new ArrayList<>();
    private List<WarehouseLocationExcelDto> successList = new ArrayList<>();

    @Override
    public void invoke(LinkedHashMap<Integer, String> data, AnalysisContext context) {
        Integer rowIndex = context.readRowHolder().getRowIndex();
        WarehouseLocationExcelDto dto = new WarehouseLocationExcelDto();
        dto.setWarehouseName(data.get(0));
        dto.setWarehouseAreaCode(data.get(1));
        dto.setWarehouseLocationCode(data.get(2));
        dto.setWarehouseLocationName(data.get(3));
        dto.setRemark(data.get(4));
        if(verifyNotBlank(dto)){
            successList.add(dto);
        }else {
            errorMsgList.add(String.format("第%s行的必填字段不能为空", rowIndex));
        }
    }

    private boolean verifyNotBlank(WarehouseLocationExcelDto dto) {
        if(StringUtils.isBlank(dto.getWarehouseName())){
            return false;
        }
        if(StringUtils.isBlank(dto.getWarehouseAreaCode())){
            return false;
        }
        if(StringUtils.isBlank(dto.getWarehouseLocationCode())){
            return false;
        }
        if(StringUtils.isBlank(dto.getWarehouseLocationName())){
            return false;
        }
        return true;
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {

    }
}
