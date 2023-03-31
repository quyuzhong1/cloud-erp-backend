package com.erp.server.scm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.service.SuperServiceImpl;
import com.erp.model.scm.entity.CfgModuleOperateLogFieldEntity;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.server.scm.mapper.CfgModuleOperateLogFieldMapper;
import com.erp.server.scm.service.CfgModuleOperateLogFieldService;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * <p>
 * 日志字段配置表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-03-17
 */
@Service
public class CfgModuleOperateLogFieldServiceImpl extends SuperServiceImpl<CfgModuleOperateLogFieldMapper, CfgModuleOperateLogFieldEntity> implements CfgModuleOperateLogFieldService {

    @Override
    public List<CfgModuleOperateLogFieldEntity> listByClassPaths(List<String> classPaths) {
        LambdaQueryWrapper<CfgModuleOperateLogFieldEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(CfgModuleOperateLogFieldEntity::getClassPath,classPaths);
        return this.list(queryWrapper);
    }

    @Override
    public Boolean saveBatchSysLogField() {
        //用于手动添加字段对应信息，后续可添加界面添加,classPath为比较DTO路径
        String  classPath = String.valueOf(SupplierEntity.class);
        List<CfgModuleOperateLogFieldEntity> logFields =  Arrays.asList(
                new CfgModuleOperateLogFieldEntity().setField("name").setFieldName("供应商名称").setClassPath(classPath).setType(0) .setEnumClass(""),
                new CfgModuleOperateLogFieldEntity().setField("categoryName").setFieldName("分类名").setClassPath(classPath).setType(0) .setEnumClass(""),

                new CfgModuleOperateLogFieldEntity().setField("gradeId").setFieldName("等级").setClassPath(classPath).setType(3) .setEnumClass(""),

                new CfgModuleOperateLogFieldEntity().setField("purchaseUserName").setFieldName("采购员").setClassPath(classPath).setType(0) .setEnumClass(""),

                new CfgModuleOperateLogFieldEntity().setField("companyWebsite").setFieldName("公司网址").setClassPath(classPath).setType(0) .setEnumClass(""),
                new CfgModuleOperateLogFieldEntity().setField("disabled").setFieldName("禁用状态").setClassPath(classPath).setType(1) .setEnumClass(""),
                new CfgModuleOperateLogFieldEntity().setField("payMethodId").setFieldName("付款方式").setClassPath(classPath).setType(3) .setEnumClass(""),
                new CfgModuleOperateLogFieldEntity().setField("payCurrency").setFieldName("付款币种").setClassPath(classPath).setType(0) .setEnumClass(""),

                new CfgModuleOperateLogFieldEntity().setField("companyAddress").setFieldName("公司地址").setClassPath(classPath).setType(0) .setEnumClass("")


                );
        return this.saveBatch(logFields);
    }
}
