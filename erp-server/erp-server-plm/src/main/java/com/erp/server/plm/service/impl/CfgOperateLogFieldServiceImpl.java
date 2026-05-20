package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.model.plm.dto.MouldInfoDTO;
import com.erp.model.plm.entity.*;
import com.erp.model.plm.enums.ProductChangeFieldEnum;
import com.erp.model.plm.enums.RefundStandardEnum;
import com.erp.server.plm.mapper.CfgOperateLogFieldMapper;
import com.erp.server.plm.service.CfgOperateLogFieldService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Will
 * @version 1.0

 * @date 2022/12/5 18:18
 */
@Service
public class CfgOperateLogFieldServiceImpl extends ServiceImpl<CfgOperateLogFieldMapper, CfgOperateLogFieldEntity> implements CfgOperateLogFieldService {

    @Override
    public List<CfgOperateLogFieldEntity> listByClassPaths(List<String> classPaths) {
        LambdaQueryWrapper<CfgOperateLogFieldEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(CfgOperateLogFieldEntity::getClassPath,classPaths);
        return this.list(queryWrapper);
    }

    @Override
    public Boolean saveBatchSysLogField() {
        //用于手动添加字段对应信息，后续可添加界面添加,classPath为比较DTO路径
        List<CfgOperateLogFieldEntity> logFields = new ArrayList<>();
        String classPath = String.valueOf(ProductChangeEntity.class);;
        logFields.add(new CfgOperateLogFieldEntity().setField("skuNo").setFieldName("sku编号").setClassPath(classPath).setType(0).setEnumClass(""));
        logFields.add(new CfgOperateLogFieldEntity().setField("reason").setFieldName("变更原因").setClassPath(classPath).setType(0).setEnumClass(""));
        logFields.add(new CfgOperateLogFieldEntity().setField("billDate").setFieldName("变更日期").setClassPath(classPath).setType(0).setEnumClass(""));

        String detailClassPath = String.valueOf(ProductChangeDetailEntity.class);;
        logFields.add(new CfgOperateLogFieldEntity().setField("field").setFieldName("变更字段").setClassPath(detailClassPath).setType(1).setEnumClass("com.erp.model.plm.enums.ProductChangeFieldEnum"));
        logFields.add(new CfgOperateLogFieldEntity().setField("oldValue").setFieldName("变更原值").setClassPath(detailClassPath).setType(0).setEnumClass(""));
        logFields.add(new CfgOperateLogFieldEntity().setField("newValue").setFieldName("变更新值").setClassPath(detailClassPath).setType(0).setEnumClass(""));
        logFields.add(new CfgOperateLogFieldEntity().setField("remark").setFieldName("备注").setClassPath(detailClassPath).setType(0).setEnumClass(""));

        return this.saveBatch(logFields);
    }


}