package com.erp.server.workflow.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.wms.entity.StocktakingProfitLossDetailEntity;
import com.erp.model.workflow.entity.CfgOperateLogFieldEntity;
import com.erp.server.workflow.mapper.CfgOperateLogFieldMapper;
import com.erp.server.workflow.service.CfgOperateLogFieldService;
import com.erp.server.workflow.service.OperateLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

/**
 * <p>
 * 日志字段配置表 服务实现类
 * </p>
 *
 * @author will
 * @since 2025-05-12
 */
@Slf4j
@Service
public class CfgOperateLogFieldServiceImpl extends SuperServiceImpl<CfgOperateLogFieldMapper, CfgOperateLogFieldEntity> implements CfgOperateLogFieldService {
    @Autowired
    private OperateLogService operateLogService;

    @Lazy
    @Resource
    private CfgOperateLogFieldService service;
    @Override
    public List<CfgOperateLogFieldEntity> listByClassPaths(List<String> classPaths) {
        LambdaQueryWrapper<CfgOperateLogFieldEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(CfgOperateLogFieldEntity::getClassPath,classPaths);
        return this.list(queryWrapper);
    }

    @Override
    public Boolean saveBatchSysLogField() {
        //用于手动添加字段对应信息，后续可添加界面添加,classPath为比较DTO路径
        String classPath = String.valueOf(StocktakingProfitLossDetailEntity.class);
        List<CfgOperateLogFieldEntity> logFields = Arrays.asList(
                new CfgOperateLogFieldEntity().setField("code").setFieldName("单号").setClassPath(classPath).setType(0).setEnumClass("")
                ,new CfgOperateLogFieldEntity().setField("warehouseName").setFieldName("仓库").setClassPath(classPath).setType(0).setEnumClass("")
                ,new CfgOperateLogFieldEntity().setField("warehouseLocation").setFieldName("库位").setClassPath(classPath).setType(0).setEnumClass("")
                ,new CfgOperateLogFieldEntity().setField("skuNo").setFieldName("SKU").setClassPath(classPath).setType(0).setEnumClass("")
                ,new CfgOperateLogFieldEntity().setField("qty").setFieldName("盘点数量").setClassPath(classPath).setType(0).setEnumClass("")
                ,new CfgOperateLogFieldEntity().setField("diffQty").setFieldName("差异数量").setClassPath(classPath).setType(0).setEnumClass("")
                ,new CfgOperateLogFieldEntity().setField("usableQty").setFieldName("可用数量").setClassPath(classPath).setType(0).setEnumClass("")
                ,new CfgOperateLogFieldEntity().setField("frozenQty").setFieldName("冻结数量").setClassPath(classPath).setType(0).setEnumClass("")
        );
        return service.saveBatch(logFields);
    }
}
