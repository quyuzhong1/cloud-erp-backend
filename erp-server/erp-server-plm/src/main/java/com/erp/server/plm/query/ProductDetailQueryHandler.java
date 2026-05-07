package com.erp.server.plm.query;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.QueryConditionEnum;
import com.common.business.enums.QueryDataTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.query.AbstractQueryHandler;
import com.common.business.threadlocal.AdvanceQueryContext;
import com.common.business.utils.QueryUtils;
import com.common.business.wrapper.FeignQuery;
import com.common.core.entity.BaseEntity;
import com.erp.model.dmp.dto.CfgOperateLogFieldDTO;
import com.erp.model.plm.entity.ProductRefBuEntity;
import com.erp.model.plm.vo.ProductRefLabelVO;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.scm.enums.PageListTypeEnum;
import com.erp.server.plm.service.CommonService;
import com.erp.server.plm.service.ProductRefBuService;
import com.erp.server.plm.service.ProductRefLabelService;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProductDetailQueryHandler extends AbstractQueryHandler {

    @Resource
    private ProductRefLabelService productRefLabelService;

    @Resource
    private ProductRefBuService productRefBuService;

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        QueryConditionEnum queryConditionEnum = AdvanceQueryContext.getCompareCode();
        if ("label".equals(field)) {
            if (null == value){
                return this.getQueryAllSql();
            }
            // 值
            List<String> sourceValueIds = QueryUtils.parseValueToStrList(value, queryConditionEnum);
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
            if (!org.apache.commons.collections4.CollectionUtils.isEmpty(labelProductIds)){
                if (QueryConditionEnum.EQ.equals(queryConditionEnum) || QueryConditionEnum.IN_LIST.equals(queryConditionEnum)) {
                    super.buildSplicingSQLDTO("pi.id", QueryConditionEnum.IN_LIST, labelProductIds, QueryDataTypeEnum.STRING);
                } else {
                    super.buildSplicingSQLDTO("pi.id", QueryConditionEnum.NOT_IN_LIST, labelProductIds, QueryDataTypeEnum.STRING);
                }
            }
            return super.getSplicingSQL();
        }

        if ("ps.sale_state".equals(field)){
            if (QueryConditionEnum.IS_NULL.equals(queryConditionEnum)){
                return "ps.sale_state is null";
            }
            if (QueryConditionEnum.NOT_NULL.equals(queryConditionEnum)){
                return "ps.sale_state is not null";
            }
            return " ps.sale_state " + compareCodeSplicingValueSql;
        }
        if ("pd.status".equals(field)){
            if (QueryConditionEnum.IS_NULL.equals(queryConditionEnum)){
                return "pd.status is null";
            }
            if (QueryConditionEnum.NOT_NULL.equals(queryConditionEnum)){
                return "pd.status is not null";
            }
            return " pd.status " + compareCodeSplicingValueSql;
        }
        if ("pi.pirate_risk".equals(field)){
            if (QueryConditionEnum.IS_NULL.equals(queryConditionEnum)){
                return "pi.pirate_risk is null";
            }
            if (QueryConditionEnum.NOT_NULL.equals(queryConditionEnum)){
                return "pi.pirate_risk is not null";
            }
            return " pi.pirate_risk " + compareCodeSplicingValueSql;
        }
        if("pi.application_category_id".equals(field)){

            List<String> valueList = com.common.business.utils.CollectionUtils.convertStrClzToList(value);
            StringBuilder sb = new StringBuilder();
            //是否是第一个，否则需要加连接符
            boolean isFirst = true;
            sb.append(" ( ");
            if(queryConditionEnum.equals(QueryConditionEnum.EQ) || queryConditionEnum.equals(QueryConditionEnum.IN_LIST) ){
                for(String valueStr : valueList) {
                    if (!isFirst) {
                        sb.append(" or ");
                    }
                    isFirst = false;
                    sb.append(" pi.application_category_id = '").append(valueStr).append("'");
                }
            }

            if(queryConditionEnum.equals(QueryConditionEnum.NE) || queryConditionEnum.equals(QueryConditionEnum.NOT_IN_LIST) ){
                for(String valueStr : valueList) {
                    if (!isFirst) {
                        sb.append(" and ");
                    }
                    isFirst = false;
                    sb.append(" pi.application_category_id != '").append(valueStr).append("'");
                }
            }
            sb.append(" ) ");
            return sb.toString();
        }

        if("firstCertificateJson".equals(field)){
            List<String> valueList = com.common.business.utils.CollectionUtils.convertStrClzToList(value);
            String arrayValue = valueList.stream()
                    .map(v -> "'" + v.replace("'", "''") + "'")
                    .collect(Collectors.joining(","));


            List<SupplierEntity> supplierEntityList = FeignQuery.create(SupplierEntity.class)
                    .last("AND EXISTS (SELECT 1 FROM jsonb_array_elements_text(certificate_json) AS cert WHERE cert IN (" + arrayValue + "))").list();
            List<String> supplierIds = supplierEntityList.stream().map(BaseEntity::getId).collect(Collectors.toList());
            //是否是第一个，否则需要加连接符
            if(queryConditionEnum.equals(QueryConditionEnum.EQ) || queryConditionEnum.equals(QueryConditionEnum.IN_LIST) ){
                if(CollectionUtils.isEmpty(supplierIds)){
                    return this.getQueryEmptySql();
                }
                super.buildDefaultDTO("pp.main_supplier", supplierIds);
            }

            if(queryConditionEnum.equals(QueryConditionEnum.NE) || queryConditionEnum.equals(QueryConditionEnum.NOT_IN_LIST) ){
                if(CollectionUtils.isEmpty(supplierIds)){
                    return this.getQueryAllSql();
                }
                super.buildSplicingSQLDTO("pp.main_supplier",QueryConditionEnum.NOT_IN_LIST,supplierIds,QueryDataTypeEnum.STRING);
            }
            return null;
        }

        if("buCode".equals(field)){
            return "exists (SELECT 1 from product_ref_bu a inner join  basic_product_bu b on a.bu_id = b.id where a.is_deleted = false and b.is_deleted = false and a.product_id = pi.id and b.name "+compareCodeSplicingValueSql+")";
        }

        return null;
    }

}

