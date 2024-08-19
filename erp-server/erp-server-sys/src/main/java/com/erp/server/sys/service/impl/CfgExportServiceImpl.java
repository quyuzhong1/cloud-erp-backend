package com.erp.server.sys.service.impl;

import com.alibaba.excel.annotation.ExcelProperty;
import com.common.business.annotation.MenuCode;
import com.common.core.exception.ServiceException;
import com.erp.model.sys.dto.CfgExportFieldDTO;
import com.erp.server.sys.service.CfgExportService;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class CfgExportServiceImpl implements CfgExportService {

    @Override
    public List<CfgExportFieldDTO> getExportField(String className) {
        List<CfgExportFieldDTO> cfgExportFieldList = new ArrayList<>();

        try {
            // 根据类名获取 Class 对象
            Class<?> clazz = Class.forName(className);

            // 获取类中的所有字段
            Field[] fields = FieldUtils.getAllFields(clazz);

            for (Field field : fields) {
                // 检查字段是否有 @ExcelProperty 注解
                ExcelProperty excelProperty = field.getAnnotation(ExcelProperty.class);
                if (excelProperty != null) {
                    MenuCode menuCodeAnnotation = field.getAnnotation(MenuCode.class);
                    String menuCode = menuCodeAnnotation != null ? menuCodeAnnotation.value() : null;

                    // 创建 CfgExportFieldDTO 对象并设置属性
                    CfgExportFieldDTO cfgExportFieldDTO = new CfgExportFieldDTO();
                    cfgExportFieldDTO.setField(field.getName());
                    cfgExportFieldDTO.setFieldName(excelProperty.value()[0]);
                    cfgExportFieldDTO.setMenuCode(menuCode);

                    // 添加到结果列表
                    cfgExportFieldList.add(cfgExportFieldDTO);
                }
            }
        } catch (ClassNotFoundException e) {
            throw new ServiceException("找不到对应导出类");
        }

        return cfgExportFieldList;
    }

}
