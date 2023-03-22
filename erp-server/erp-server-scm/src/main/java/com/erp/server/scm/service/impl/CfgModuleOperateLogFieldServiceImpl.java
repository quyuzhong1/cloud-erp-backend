package com.erp.server.scm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.erp.model.plm.entity.SysLogFieldEntity;
import com.erp.model.plm.enums.SysLogClassPathEnum;
import com.erp.model.scm.dto.SalesDemandDTO;
import com.erp.model.scm.entity.CfgModuleOperateLogFieldEntity;
import com.erp.server.scm.mapper.CfgModuleOperateLogFieldMapper;
import com.erp.server.scm.service.CfgModuleOperateLogFieldService;
import com.common.core.serveice.SuperServiceImpl;
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
        String  classPath = String.valueOf(SalesDemandDTO.UpdateDTO.class);
        List<CfgModuleOperateLogFieldEntity> logFields =  Arrays.asList(
                new CfgModuleOperateLogFieldEntity().setField("applyDate").setFieldName("申请日期").setClassPath(classPath).setType(2) .setEnumClass("")
        );
        return this.saveBatch(logFields);
    }
}
