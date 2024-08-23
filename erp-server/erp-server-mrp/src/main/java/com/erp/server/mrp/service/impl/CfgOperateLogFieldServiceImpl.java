package com.erp.server.mrp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.mrp.entity.CfgOperateLogFieldEntity;
import com.erp.server.mrp.mapper.CfgOperateLogFieldMapper;
import com.erp.server.mrp.service.CfgOperateLogFieldService;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * <p>
 * 日志字段配置表 服务实现类
 * </p>
 *
 * @author will
 * @since 2023-05-08
 */
@Service
public class CfgOperateLogFieldServiceImpl extends SuperServiceImpl<CfgOperateLogFieldMapper, CfgOperateLogFieldEntity> implements CfgOperateLogFieldService {


    @Override
    public List<CfgOperateLogFieldEntity> listByClassPaths(List<String> classPaths) {
        LambdaQueryWrapper<CfgOperateLogFieldEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(CfgOperateLogFieldEntity::getClassPath,classPaths);
        return this.list(queryWrapper);
    }

    @Override
    public Boolean saveBatchSysLogField() {
        //用于手动添加字段对应信息，后续可添加界面添加,classPath为比较DTO路径
        String  classPath = String.valueOf(CfgOperateLogFieldEntity.class);
        List<CfgOperateLogFieldEntity> logFields =  Arrays.asList(
                new CfgOperateLogFieldEntity().setField("name").setFieldName("供应商名称").setClassPath(classPath).setType(0) .setEnumClass(""),
                new CfgOperateLogFieldEntity().setField("categoryName").setFieldName("分类名").setClassPath(classPath).setType(0) .setEnumClass(""),

                new CfgOperateLogFieldEntity().setField("gradeId").setFieldName("等级").setClassPath(classPath).setType(3) .setEnumClass(""),

                new CfgOperateLogFieldEntity().setField("purchaseUserName").setFieldName("采购员").setClassPath(classPath).setType(0) .setEnumClass(""),

                new CfgOperateLogFieldEntity().setField("companyWebsite").setFieldName("公司网址").setClassPath(classPath).setType(0) .setEnumClass(""),
                new CfgOperateLogFieldEntity().setField("disabled").setFieldName("禁用状态").setClassPath(classPath).setType(1) .setEnumClass(""),
                new CfgOperateLogFieldEntity().setField("payMethodId").setFieldName("付款方式").setClassPath(classPath).setType(3) .setEnumClass(""),
                new CfgOperateLogFieldEntity().setField("payCurrency").setFieldName("付款币种").setClassPath(classPath).setType(0) .setEnumClass(""),

                new CfgOperateLogFieldEntity().setField("companyAddress").setFieldName("公司地址").setClassPath(classPath).setType(0) .setEnumClass("")


        );
        return this.saveBatch(logFields);

    }
}
