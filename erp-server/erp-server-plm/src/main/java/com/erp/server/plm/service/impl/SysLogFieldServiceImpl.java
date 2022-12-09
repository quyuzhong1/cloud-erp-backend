package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.common.annotation.StateEnumValue;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.entity.SysLogEntity;
import com.erp.model.plm.entity.SysLogFieldEntity;
import com.erp.server.plm.mapper.SysLogFieldMapper;
import com.erp.server.plm.mapper.SysLogMapper;
import com.erp.server.plm.service.SysLogFieldService;
import com.erp.server.plm.service.SysLogService;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
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
        //用于手动添加字段对应信息，后续可添加界面添加
         String  classPath = ProductSkuBaseInfoDTO.class.toString();
        List<SysLogFieldEntity> logFields =  Arrays.asList(
            new SysLogFieldEntity().setField("skuNo").setFieldName("SKU").setClassPath(classPath).setType(0) .setEnumClass(null)
                /*new SysLogFieldEntity().setField("netWeight").setFieldName("净重").setClassPath(classPath).setType(0) .setEnumClass(null),
                new SysLogFieldEntity().setField("boxSize").setFieldName("箱规").setClassPath(classPath).setType(0) .setEnumClass(null),
                new SysLogFieldEntity().setField("boxWeight").setFieldName("单箱重量").setClassPath(classPath).setType(0) .setEnumClass(null),
                new SysLogFieldEntity().setField("boxQty").setFieldName("单箱数量").setClassPath(classPath).setType(0) .setEnumClass(null)
                               new SysLogFieldEntity().setField("isFinishedVideo").setFieldName("视频是否完成").setClassPath(classPath).setType(1) .setEnumClass(null),
 new SysLogFieldEntity().setField("englishMaterial").setFieldName("英文材质").setClassPath(classPath).setType(0) .setEnumClass(null),
                new SysLogFieldEntity().setField("englishUsage").setFieldName("英文用途").setClassPath(classPath).setType(1) .setEnumClass(null)
                new SysLogFieldEntity().setField("saleState").setFieldName("销售状态").setClassPath(classPath).setType(0) .setEnumClass(null),

                new SysLogFieldEntity().setField("dataUrl").setFieldName("产品上市（含培训）资料链接").setClassPath(classPath).setType(0) .setEnumClass(null),
                new SysLogFieldEntity().setField("targetSalesQty").setFieldName("首季度目标销量").setClassPath(classPath).setType(1) .setEnumClass(null),
                new SysLogFieldEntity().setField("salesPlatform").setFieldName("销售平台").setClassPath(classPath).setType(0) .setEnumClass(null),
                new SysLogFieldEntity().setField("isMarketable").setFieldName("是否可销售").setClassPath(classPath).setType(1) .setEnumClass(null)
                new SysLogFieldEntity().setField("saleMethod").setFieldName("销售方式").setClassPath(classPath).setType(0) .setEnumClass(null),
                new SysLogFieldEntity().setField("entrustedDevelopCost").setFieldName("委托开发成本").setClassPath(classPath).setType(0) .setEnumClass(null),
                new SysLogFieldEntity().setField("moldCost").setFieldName("模具成本").setClassPath(classPath).setType(0) .setEnumClass(null),
                new SysLogFieldEntity().setField("sampleFee").setFieldName("样品费用").setClassPath(classPath).setType(0) .setEnumClass(null),
                new SysLogFieldEntity().setField("salesChannel").setFieldName("销售渠道").setClassPath(classPath).setType(0) .setEnumClass(null),
                new SysLogFieldEntity().setField("isCustomized").setFieldName("是否客户定制").setClassPath(classPath).setType(1) .setEnumClass(null)*/
        );
        return this.saveBatch(logFields);
    }


}