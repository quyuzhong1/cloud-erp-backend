package com.erp.server.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.service.SuperServiceImpl;
import com.erp.model.wms.entity.CfgOperateLogFieldEntity;
import com.erp.model.wms.entity.MachineSubComponentsEntity;
import com.erp.server.wms.mapper.CfgOperateLogFieldMapper;
import com.erp.server.wms.service.CfgOperateLogFieldService;
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
        String  classPath = String.valueOf(MachineSubComponentsEntity.class);
        List<CfgOperateLogFieldEntity> logFields =  Arrays.asList(
                new CfgOperateLogFieldEntity().setField("qty").setFieldName("加工数量").setClassPath(classPath).setType(0) .setEnumClass(""),
                new CfgOperateLogFieldEntity().setField("warehouseLocation").setFieldName("库位").setClassPath(classPath).setType(0) .setEnumClass(""),

                new CfgOperateLogFieldEntity().setField("warehouseName").setFieldName("仓库").setClassPath(classPath).setType(3) .setEnumClass("")

                );
        return this.saveBatch(logFields);
    }
}
