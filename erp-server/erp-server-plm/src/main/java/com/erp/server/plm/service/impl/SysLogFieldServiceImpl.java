package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.common.annotation.StateEnumValue;
import com.erp.model.plm.dto.ProductInfoDTO;
import com.erp.model.plm.entity.SysLogEntity;
import com.erp.model.plm.entity.SysLogFieldEntity;
import com.erp.server.plm.mapper.SysLogFieldMapper;
import com.erp.server.plm.mapper.SysLogMapper;
import com.erp.server.plm.service.SysLogFieldService;
import com.erp.server.plm.service.SysLogService;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/5 18:18
 */
@Service
public class SysLogFieldServiceImpl extends ServiceImpl<SysLogFieldMapper, SysLogFieldEntity> implements SysLogFieldService {

    @Override
    public List<SysLogFieldEntity> listByClassPaths(List<String> classPaths) {
        LambdaQueryWrapper<SysLogFieldEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(SysLogFieldEntity::getClassPath,classPaths);
        return this.list(queryWrapper);
    }

    @Override
    public Boolean saveBatchSysLogField() {
        //用于手动添加字段对应信息，后续可添加界面添加
         String  classPath = ProductInfoDTO.class.toString();
        List<SysLogFieldEntity> logFields =  Arrays.asList(
            new SysLogFieldEntity().setField("name").setFieldName("产品名称").setClassPath(classPath).setType(0) .setEnumClass(null),
                new SysLogFieldEntity().setField("category").setFieldName("产品类别").setClassPath(classPath).setType(0) .setEnumClass(null),
                new SysLogFieldEntity().setField("property").setFieldName("产品属性").setClassPath(classPath).setType(0) .setEnumClass(null),
                new SysLogFieldEntity().setField("chargeName").setFieldName("产品负责人").setClassPath(classPath).setType(0) .setEnumClass(null),
                new SysLogFieldEntity().setField("name").setFieldName("产品名称").setClassPath(classPath).setType(0) .setEnumClass(null),
                new SysLogFieldEntity().setField("name").setFieldName("产品名称").setClassPath(classPath).setType(0) .setEnumClass(null),
                new SysLogFieldEntity().setField("name").setFieldName("产品名称").setClassPath(classPath).setType(0) .setEnumClass(null),
                new SysLogFieldEntity().setField("name").setFieldName("产品名称").setClassPath(classPath).setType(0) .setEnumClass(null),
                new SysLogFieldEntity().setField("name").setFieldName("产品名称").setClassPath(classPath).setType(0) .setEnumClass(null),
                new SysLogFieldEntity().setField("name").setFieldName("产品名称").setClassPath(classPath).setType(0) .setEnumClass(null)
        );
        return this.saveBatch(logFields);
    }
}