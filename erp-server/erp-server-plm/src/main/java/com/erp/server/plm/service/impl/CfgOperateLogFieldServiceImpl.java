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
        ProductChangeFieldEnum[] productChangeFieldEnums = ProductChangeFieldEnum.values();
        for (ProductChangeFieldEnum productChangeFieldEnum : productChangeFieldEnums) {
            String classPath = "";
            String table = productChangeFieldEnum.getTableName();
            if(table.equals("product_cost")){
                classPath = String.valueOf(ProductCostEntity.class);
            }
            if(table.equals("product_detail")){
                classPath = String.valueOf(ProductDetailEntity.class);
            }
            if(table.equals("product_info")){
                classPath = String.valueOf(ProductInfoEntity.class);
            }
            if(table.equals("product_pack")){
                classPath = String.valueOf(ProductPackEntity.class);
            }
            if(table.equals("product_purchase")){
                classPath = String.valueOf(ProductPurchaseEntity.class);
            }

            if(table.equals("product_sale")){
                classPath = String.valueOf(ProductSaleEntity.class);
            }
            if(StringUtils.isNotBlank(classPath)){
                logFields.add(new CfgOperateLogFieldEntity().setField(productChangeFieldEnum.getEntityField()).setFieldName(productChangeFieldEnum.getFieldLabel()).setClassPath(classPath).setType(0).setEnumClass(""));
            }
        }
        return this.saveBatch(logFields);
    }


}