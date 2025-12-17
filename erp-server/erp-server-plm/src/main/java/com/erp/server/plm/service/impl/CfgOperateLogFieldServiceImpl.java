package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.model.plm.dto.MouldInfoDTO;
import com.erp.model.plm.entity.CfgOperateLogFieldEntity;
import com.erp.model.plm.enums.RefundStandardEnum;
import com.erp.server.plm.mapper.CfgOperateLogFieldMapper;
import com.erp.server.plm.service.CfgOperateLogFieldService;
import org.springframework.stereotype.Service;

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
         String  classPath = String.valueOf(MouldInfoDTO.LogDetailDTO.class);
        List<CfgOperateLogFieldEntity> logFields =  Arrays.asList(
            new CfgOperateLogFieldEntity().setField("thirdMouldNo").setFieldName("外部模具编号(供应商)").setClassPath(classPath).setType(0) .setEnumClass(null),
                new CfgOperateLogFieldEntity().setField("typeName").setFieldName("模具类型").setClassPath(classPath).setType(0) .setEnumClass(null),
                 new CfgOperateLogFieldEntity().setField("mouldHoles").setFieldName("模具穴数").setClassPath(classPath).setType(0) .setEnumClass(null),
                new CfgOperateLogFieldEntity().setField("length").setFieldName("模具长").setClassPath(classPath).setType(0) .setEnumClass(null),
                new CfgOperateLogFieldEntity().setField("width").setFieldName("模具宽").setClassPath(classPath).setType(0) .setEnumClass(null),
                new CfgOperateLogFieldEntity().setField("height").setFieldName("模具高").setClassPath(classPath).setType(0) .setEnumClass(null),
                new CfgOperateLogFieldEntity().setField("material").setFieldName("模具材质").setClassPath(classPath).setType(0) .setEnumClass(null),
                new CfgOperateLogFieldEntity().setField("lifeCycle").setFieldName("模具寿命(万)(啤)").setClassPath(classPath).setType(0) .setEnumClass(null),
                new CfgOperateLogFieldEntity().setField("developCycle").setFieldName("开模周期(自然日)").setClassPath(classPath).setType(0) .setEnumClass(null),
                new CfgOperateLogFieldEntity().setField("enableDate").setFieldName("启用时间").setClassPath(classPath).setType(0) .setEnumClass(null),
                new CfgOperateLogFieldEntity().setField("supplierName").setFieldName("供应商").setClassPath(classPath).setType(0) .setEnumClass(null),
                new CfgOperateLogFieldEntity().setField("remark").setFieldName("备注").setClassPath(classPath).setType(0) .setEnumClass(null),
                new CfgOperateLogFieldEntity().setField("qty").setFieldName("数量").setClassPath(classPath).setType(0) .setEnumClass(null),
                new CfgOperateLogFieldEntity().setField("taxPrice").setFieldName("含税单价").setClassPath(classPath).setType(0) .setEnumClass(null),
                new CfgOperateLogFieldEntity().setField("taxRate").setFieldName("税率").setClassPath(classPath).setType(0) .setEnumClass(null),
                new CfgOperateLogFieldEntity().setField("payMethodName").setFieldName("结算方式").setClassPath(classPath).setType(0) .setEnumClass(null),
                new CfgOperateLogFieldEntity().setField("paymentConditionName").setFieldName("付款条件").setClassPath(classPath).setType(0) .setEnumClass(null),
                new CfgOperateLogFieldEntity().setField("isNeedRefund").setFieldName("是否费用返还").setClassPath(classPath).setType(1) .setEnumClass(null),
                new CfgOperateLogFieldEntity().setField("refundStandard").setFieldName("返还标准").setClassPath(classPath).setType(2) .setEnumClass(String.valueOf(RefundStandardEnum.class)),
                new CfgOperateLogFieldEntity().setField("refundOrderQty").setFieldName("退款单量").setClassPath(classPath).setType(0) .setEnumClass(null),
                new CfgOperateLogFieldEntity().setField("refundAmount").setFieldName("返还金额").setClassPath(classPath).setType(0) .setEnumClass(null),
                new CfgOperateLogFieldEntity().setField("productList").setFieldName("产品信息").setClassPath(classPath).setType(0) .setEnumClass(null),
                new CfgOperateLogFieldEntity().setField("refProductList").setFieldName("关联产品").setClassPath(classPath).setType(0) .setEnumClass(null)
        );
        return this.saveBatch(logFields);
    }


}