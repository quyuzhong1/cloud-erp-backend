package com.erp.server.scm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.service.SuperServiceImpl;
import com.erp.model.scm.entity.CfgModuleOperateLogFieldEntity;
import com.erp.model.scm.entity.PurchaseOrderDetailEntity;
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
        String  classPath = String.valueOf(PurchaseOrderDetailEntity.class);
        List<CfgModuleOperateLogFieldEntity> logFields =  Arrays.asList(
                new CfgModuleOperateLogFieldEntity().setField("skuNo").setFieldName("sku编码").setClassPath(classPath).setType(0) .setEnumClass(""),
                new CfgModuleOperateLogFieldEntity().setField("taxPrice").setFieldName("含税单价").setClassPath(classPath).setType(0) .setEnumClass(""),

                new CfgModuleOperateLogFieldEntity().setField("currency").setFieldName("币别").setClassPath(classPath).setType(0) .setEnumClass(""),

                new CfgModuleOperateLogFieldEntity().setField("purchaseQty").setFieldName("采购数量").setClassPath(classPath).setType(0) .setEnumClass(""),

                new CfgModuleOperateLogFieldEntity().setField("planDeliveryDate").setFieldName("计划交期").setClassPath(classPath).setType(0) .setEnumClass(""),

                new CfgModuleOperateLogFieldEntity().setField("receiveOrgName").setFieldName("收料组织名称").setClassPath(classPath).setType(0) .setEnumClass(""),
                new CfgModuleOperateLogFieldEntity().setField("isGift").setFieldName("是否是赠品").setClassPath(classPath).setType(1) .setEnumClass(""),
                new CfgModuleOperateLogFieldEntity().setField("isUrgent").setFieldName("是否加急").setClassPath(classPath).setType(1) .setEnumClass(""),

                new CfgModuleOperateLogFieldEntity().setField("remark").setFieldName("备注").setClassPath(classPath).setType(0) .setEnumClass("")

                );
        return this.saveBatch(logFields);
    }
}
