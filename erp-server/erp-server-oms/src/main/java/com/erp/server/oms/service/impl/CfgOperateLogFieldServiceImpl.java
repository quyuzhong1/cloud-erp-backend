package com.erp.server.oms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.oms.entity.CfgOperateLogFieldEntity;
import com.erp.model.oms.entity.ListingInfoEntity;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.server.oms.mapper.CfgOperateLogFieldMapper;
import com.erp.server.oms.service.CfgOperateLogFieldService;
import org.springframework.stereotype.Service;

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


    @Override
    public List<CfgOperateLogFieldEntity> listByClassPaths(List<String> classPaths) {
        LambdaQueryWrapper<CfgOperateLogFieldEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(CfgOperateLogFieldEntity::getClassPath,classPaths);
        return this.list(queryWrapper);
    }

    @Override
    public Boolean saveBatchSysLogField() {
        //用于手动添加字段对应信息，后续可添加界面添加,classPath为比较DTO路径
        String  classPath = String.valueOf(ListingInfoEntity.class);
        List<CfgOperateLogFieldEntity> logFields =  Arrays.asList(

                new CfgOperateLogFieldEntity().setField("platformSpuNo").setFieldName("平台产品(spu) no").setClassPath(classPath).setType(0) .setEnumClass(""),
                new CfgOperateLogFieldEntity().setField("productImageUrl").setFieldName("产品图片 url").setClassPath(classPath).setType(0) .setEnumClass(""),
                new CfgOperateLogFieldEntity().setField("productSpec").setFieldName("产品规格信息").setClassPath(classPath).setType(3) .setEnumClass(""),
                new CfgOperateLogFieldEntity().setField("productPacking").setFieldName("产品包装信息").setClassPath(classPath).setType(0) .setEnumClass(""),
                new CfgOperateLogFieldEntity().setField("platformFnSku").setFieldName("平台SKU额外关联的FNSKU").setClassPath(classPath).setType(0) .setEnumClass(""),
                new CfgOperateLogFieldEntity().setField("platformSkuName").setFieldName("平台产品Sku名称").setClassPath(classPath).setType(1) .setEnumClass("")

        );
        return this.saveBatch(logFields);

    }
}
