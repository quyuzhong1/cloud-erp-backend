package com.erp.server.oms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.oms.dto.CfgRuleOrderHandleDTO;
import com.erp.model.oms.entity.CfgOperateLogFieldEntity;
import com.erp.server.oms.mapper.CfgOperateLogFieldMapper;
import com.erp.server.oms.service.CfgOperateLogFieldService;
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
 * @since 2023-05-08
 */
@Service
public class CfgOperateLogFieldServiceImpl extends SuperServiceImpl<CfgOperateLogFieldMapper, CfgOperateLogFieldEntity> implements CfgOperateLogFieldService {

    @Lazy
    @Resource
    private CfgOperateLogFieldService cfgOperateLogFieldService;

    @Override
    public List<CfgOperateLogFieldEntity> listByClassPaths(List<String> classPaths) {
        LambdaQueryWrapper<CfgOperateLogFieldEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(CfgOperateLogFieldEntity::getClassPath,classPaths);
        return this.list(queryWrapper);
    }

    @Override
    public Boolean saveBatchSysLogField() {
        //用于手动添加字段对应信息，后续可添加界面添加,classPath为比较DTO路径
        String  classPath = String.valueOf(CfgRuleOrderHandleDTO.ReceiveHandleContent.class);
        List<CfgOperateLogFieldEntity> logFields =  Arrays.asList(

                new CfgOperateLogFieldEntity().setField("receiveEmptyFillSwitch").setFieldName("收货人为空填充开关").setClassPath(classPath).setType(1) .setEnumClass(""),
                new CfgOperateLogFieldEntity().setField("handleReceiveEmptyFillRule").setFieldName("处理收货人为空的规则").setClassPath(classPath).setType(2) .setEnumClass("com.erp.model.oms.enums.RuleOrderHandleEnum.ReceiveFillRuleContentEnum"),
                new CfgOperateLogFieldEntity().setField("receiveFillText").setFieldName("收货人为空填充文本").setClassPath(classPath).setType(0) .setEnumClass(""),
                new CfgOperateLogFieldEntity().setField("filterReceiveTextList").setFieldName("收货人过滤特殊符号").setClassPath(classPath).setType(0) .setEnumClass("")
        );
        return cfgOperateLogFieldService.saveBatch(logFields);

    }
}
