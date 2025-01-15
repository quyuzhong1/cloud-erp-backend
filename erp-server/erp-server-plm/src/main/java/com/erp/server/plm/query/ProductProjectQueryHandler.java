package com.erp.server.plm.query;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.common.business.enums.QueryConditionEnum;
import com.common.business.enums.QueryDataTypeEnum;
import com.common.business.query.AbstractQueryHandler;
import com.common.business.threadlocal.AdvanceQueryContext;
import com.common.business.utils.QueryUtils;
import com.erp.model.plm.vo.ProductRefLabelVO;
import com.erp.server.plm.service.ProductInfoService;
import com.erp.server.plm.service.ProductRefLabelService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class ProductProjectQueryHandler extends AbstractQueryHandler {

    @Resource
    private ProductRefLabelService productRefLabelService;
    
    @Lazy
    @Resource
    private ProductInfoService productInfoService;
    

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if ("sku_no".equals(field)) {
            return "exists ( SELECT id FROM product_detail pd WHERE pd.sku_no "+ compareCodeSplicingValueSql + ")";
        }
        // 类型
        QueryConditionEnum queryConditionEnum = AdvanceQueryContext.getCompareCode();
        // 值
        List<String> sourceValueIds = QueryUtils.parseValueToStrList(value, queryConditionEnum);
        if ("label".equals(field)) {
            if (null == value){
                return this.getQueryAllSql();
            }
            List<String> labelProductIds = null;
            if (!CollectionUtils.isEmpty(sourceValueIds)) {
                List<ProductRefLabelVO> productRefLabelVOS = productRefLabelService.getLabelListByIds(null, new HashSet<>(sourceValueIds), null);
                if (!CollectionUtils.isEmpty(productRefLabelVOS)) {
                    labelProductIds = productRefLabelVOS.stream().map(ProductRefLabelVO::getProductId).collect(Collectors.toList());
                } else {
                    labelProductIds = new ArrayList<>();
                    labelProductIds.add("-1");
                }
            }
            if (!CollectionUtils.isEmpty(labelProductIds)){
                if (QueryConditionEnum.EQ.equals(queryConditionEnum) || QueryConditionEnum.IN_LIST.equals(queryConditionEnum)) {
                    super.buildSplicingSQLDTO("pi.id", QueryConditionEnum.IN_LIST, labelProductIds, QueryDataTypeEnum.STRING);
                } else {
                    super.buildSplicingSQLDTO("pi.id", QueryConditionEnum.NOT_IN_LIST, labelProductIds, QueryDataTypeEnum.STRING);
                }
            }
        }

        // 项目经理部门下人员
        if ("projectChargeDeptId".equals(field) && CollectionUtils.isNotEmpty(sourceValueIds)) {
            List<String> projectChargeDeptUserIdList =  productInfoService.handleDept(sourceValueIds);
            if (CollectionUtils.isEmpty(projectChargeDeptUserIdList)) {
                projectChargeDeptUserIdList = Collections.singletonList("-1");
            }
            if (QueryConditionEnum.EQ.equals(queryConditionEnum) || QueryConditionEnum.IN_LIST.equals(queryConditionEnum)) {
                super.buildSplicingSQLDTO("p.project_charge_id", QueryConditionEnum.IN_LIST, projectChargeDeptUserIdList, QueryDataTypeEnum.STRING);
            } else {
                super.buildSplicingSQLDTO("p.project_charge_id", QueryConditionEnum.NOT_IN_LIST, projectChargeDeptUserIdList, QueryDataTypeEnum.STRING);
            }
        }

        //产品经理部门下人员
        if ("productChargeDeptId".equals(field) && CollectionUtils.isNotEmpty(sourceValueIds)) {
            List<String> productChargeDeptUserIdList =  productInfoService.handleDept(sourceValueIds);
            if (CollectionUtils.isEmpty(productChargeDeptUserIdList)) {
                productChargeDeptUserIdList = Collections.singletonList("-1");
            }
            if (QueryConditionEnum.EQ.equals(queryConditionEnum) || QueryConditionEnum.IN_LIST.equals(queryConditionEnum)) {
                super.buildSplicingSQLDTO("p.charge_id", QueryConditionEnum.IN_LIST, productChargeDeptUserIdList, QueryDataTypeEnum.STRING);
            } else {
                super.buildSplicingSQLDTO("p.charge_id", QueryConditionEnum.NOT_IN_LIST, productChargeDeptUserIdList, QueryDataTypeEnum.STRING);
            }
        }

        //团队成员部门下人员
        if ("teamChargeDeptId".equals(field) && CollectionUtils.isNotEmpty(sourceValueIds)) {
            List<String> teamChargeDeptUserIdList =  productInfoService.handleDept(sourceValueIds);
            if (CollectionUtils.isEmpty(teamChargeDeptUserIdList)) {
                teamChargeDeptUserIdList = Collections.singletonList("-1");
            }
            if (QueryConditionEnum.EQ.equals(queryConditionEnum) || QueryConditionEnum.IN_LIST.equals(queryConditionEnum)) {
                super.buildSplicingSQLDTO("exists ( select id from project_members where product_id = p.id and  is_deleted = false and ", QueryConditionEnum.IN_LIST, teamChargeDeptUserIdList, QueryDataTypeEnum.STRING);
            } else {
                super.buildSplicingSQLDTO("exists ( select id from project_members where product_id = p.id and  is_deleted = false and ", QueryConditionEnum.NOT_IN_LIST, teamChargeDeptUserIdList, QueryDataTypeEnum.STRING);
            }
        }

        //创建人部门下人员
        if ("createChargeDeptId".equals(field) && CollectionUtils.isNotEmpty(sourceValueIds)) {
            List<String> createChargeDeptUserIdList =  productInfoService.handleDept(sourceValueIds);
            if (CollectionUtils.isEmpty(createChargeDeptUserIdList)) {
                createChargeDeptUserIdList = Collections.singletonList("-1");
            }
            if (QueryConditionEnum.EQ.equals(queryConditionEnum) || QueryConditionEnum.IN_LIST.equals(queryConditionEnum)) {
                super.buildSplicingSQLDTO("p.create_user_id", QueryConditionEnum.IN_LIST, createChargeDeptUserIdList, QueryDataTypeEnum.STRING);
            } else {
                super.buildSplicingSQLDTO("p.create_user_id", QueryConditionEnum.NOT_IN_LIST, createChargeDeptUserIdList, QueryDataTypeEnum.STRING);
            }
        }
        return null;
    }

}

