package com.erp.server.scm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.scm.entity.AssetNoticeDetailEntity;
import com.erp.model.scm.entity.AssetNoticeEntity;
import com.erp.model.scm.entity.CfgModuleOperateLogFieldEntity;
import com.erp.server.scm.mapper.CfgModuleOperateLogFieldMapper;
import com.erp.server.scm.service.CfgModuleOperateLogFieldService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
        String  classPath = String.valueOf(AssetNoticeEntity.class);
        List<CfgModuleOperateLogFieldEntity> logFields = new ArrayList<>();
//        logFields.addAll(Arrays.asList(
//                new CfgModuleOperateLogFieldEntity().setField("applyUserName").setFieldName("申请人名称").setClassPath(classPath).setType(0) .setEnumClass(""),
//                new CfgModuleOperateLogFieldEntity().setField("applyDeptName").setFieldName("申请部门名称").setClassPath(classPath).setType(0) .setEnumClass(""),
//                new CfgModuleOperateLogFieldEntity().setField("remark").setFieldName("备注").setClassPath(classPath).setType(0) .setEnumClass(""),
//                new CfgModuleOperateLogFieldEntity().setField("applyDate").setFieldName("申请日期").setClassPath(classPath).setType(0) .setEnumClass("")
//                ));
        String  detailClassPath = String.valueOf(AssetNoticeDetailEntity.class);
        logFields.addAll(Arrays.asList(
                new CfgModuleOperateLogFieldEntity().setField("assetName").setFieldName("资产名称").setClassPath(detailClassPath).setType(0) .setEnumClass(""),
                new CfgModuleOperateLogFieldEntity().setField("tag").setFieldName("标识(首套模、复制模)").setClassPath(detailClassPath).setType(0) .setEnumClass(""),
                new CfgModuleOperateLogFieldEntity().setField("isUrgent").setFieldName("是否加急").setClassPath(detailClassPath).setType(1) .setEnumClass("").setBooleanValue("否|是"),
                new CfgModuleOperateLogFieldEntity().setField("planDeliveryDate").setFieldName("计划交期").setClassPath(detailClassPath).setType(0) .setEnumClass(""),
                new CfgModuleOperateLogFieldEntity().setField("applyQty").setFieldName("申请数量").setClassPath(detailClassPath).setType(0) .setEnumClass(""),
                new CfgModuleOperateLogFieldEntity().setField("remark").setFieldName("备注").setClassPath(detailClassPath).setType(0) .setEnumClass(""),
                new CfgModuleOperateLogFieldEntity().setField("supplierName").setFieldName("供应商名称").setClassPath(detailClassPath).setType(0) .setEnumClass("")
        ));
        return this.saveBatch(logFields);
    }
}
