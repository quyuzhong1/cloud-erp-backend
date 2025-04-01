package com.erp.server.mrp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.utils.ApplicationContextUtils;
import com.erp.model.mrp.entity.CfgOperateLogFieldEntity;
import com.erp.model.mrp.entity.CfgRuleExpireTimeEntity;
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
        String  classPath = String.valueOf(CfgRuleExpireTimeEntity.class);
        List<CfgOperateLogFieldEntity> logFields =  Arrays.asList(
                new CfgOperateLogFieldEntity().setField("purchaseApproveDays").setFieldName("采购审批天数（天）").setClassPath(classPath).setType(0) .setEnumClass(""),
                new CfgOperateLogFieldEntity().setField("productionDays").setFieldName("生产周期天数（天）").setClassPath(classPath).setType(0) .setEnumClass(""),

                new CfgOperateLogFieldEntity().setField("supplierDeliveryDays").setFieldName("供应商发货天数（天）").setClassPath(classPath).setType(0) .setEnumClass(""),

                new CfgOperateLogFieldEntity().setField("qcDays").setFieldName("质检入库天数（天）").setClassPath(classPath).setType(0) .setEnumClass(""),

                new CfgOperateLogFieldEntity().setField("purchaseCycleDays").setFieldName("采购频率天数（天）").setClassPath(classPath).setType(0) .setEnumClass(""),
                new CfgOperateLogFieldEntity().setField("platformInstockDays").setFieldName("FBA入库天数（天）").setClassPath(classPath).setType(0) .setEnumClass(""),
                new CfgOperateLogFieldEntity().setField("overseasInstockDays").setFieldName("海外入库天数（天）").setClassPath(classPath).setType(0) .setEnumClass("")


        );
        return ApplicationContextUtils.getBean(CfgOperateLogFieldServiceImpl.class).saveBatch(logFields);

    }
}
