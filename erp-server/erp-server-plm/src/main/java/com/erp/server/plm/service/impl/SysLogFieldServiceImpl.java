package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.model.plm.entity.SysLogFieldEntity;
import com.erp.server.plm.enums.SysLogClassPathEnum;
import com.erp.server.plm.mapper.SysLogFieldMapper;
import com.erp.server.plm.service.SysLogFieldService;
import org.springframework.stereotype.Service;

import java.util.Arrays;
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
        //用于手动添加字段对应信息，后续可添加界面添加,classPath为比较DTO路径
         String  classPath = SysLogClassPathEnum.PROJECTTASKENTITY.getDesc();
        List<SysLogFieldEntity> logFields =  Arrays.asList(
            new SysLogFieldEntity().setField("planStartTime").setFieldName("开始时间").setClassPath(classPath).setType(0) .setEnumClass(null),
                new SysLogFieldEntity().setField("planEndTime").setFieldName("结束时间").setClassPath(classPath).setType(0) .setEnumClass(null),
                new SysLogFieldEntity().setField("name").setFieldName("任务名称").setClassPath(classPath).setType(0) .setEnumClass(null),
                new SysLogFieldEntity().setField("chargeName").setFieldName("任务负责人").setClassPath(classPath).setType(0) .setEnumClass(null)
                 /*new SysLogFieldEntity().setField("saleMethod").setFieldName("销售方式").setClassPath(classPath).setType(0) .setEnumClass(null),
                new SysLogFieldEntity().setField("entrustedDevelopCost").setFieldName("委托开发成本").setClassPath(classPath).setType(0) .setEnumClass(null),
                new SysLogFieldEntity().setField("moldCost").setFieldName("模具成本").setClassPath(classPath).setType(0) .setEnumClass(null),
                new SysLogFieldEntity().setField("sampleFee").setFieldName("样品费用").setClassPath(classPath).setType(0) .setEnumClass(null),
                new SysLogFieldEntity().setField("salesChannel").setFieldName("销售渠道").setClassPath(classPath).setType(0) .setEnumClass(null),
                new SysLogFieldEntity().setField("isCustomized").setFieldName("是否客户定制").setClassPath(classPath).setType(1) .setEnumClass(null)*/
        );
        return this.saveBatch(logFields);
    }


}