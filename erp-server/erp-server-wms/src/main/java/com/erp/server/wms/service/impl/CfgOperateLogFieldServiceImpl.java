package com.erp.server.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.plm.entity.ProductCostEntity;
import com.erp.model.plm.enums.ProductChangeFieldEnum;
import com.erp.model.wms.entity.CfgOperateLogFieldEntity;
import com.erp.server.wms.mapper.CfgOperateLogFieldMapper;
import com.erp.server.wms.service.CfgOperateLogFieldService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
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
        List<CfgOperateLogFieldEntity> logFields = new ArrayList<>();
        ProductChangeFieldEnum[] productChangeFieldEnums = ProductChangeFieldEnum.values();
        for (ProductChangeFieldEnum productChangeFieldEnum : productChangeFieldEnums) {
            String classPath = "";
            String table = productChangeFieldEnum.getTableName();
            if(table.equals("product_cost")){
                classPath = String.valueOf(ProductCostEntity.class);
            }
            if(table.equals("product_cost")){
                classPath = String.valueOf(ProductCostEntity.class);
            }
            if(table.equals("product_cost")){
                classPath = String.valueOf(ProductCostEntity.class);
            }
            if(table.equals("product_cost")){
                classPath = String.valueOf(ProductCostEntity.class);
            }
            if(table.equals("product_cost")){
                classPath = String.valueOf(ProductCostEntity.class);
            }
            logFields.add(new CfgOperateLogFieldEntity().setField(productChangeFieldEnum.getCode()).setFieldName(productChangeFieldEnum.getName()).setClassPath(classPath).setType(0).setEnumClass(""));
        }
        return service.saveBatch(logFields);
    }
}
