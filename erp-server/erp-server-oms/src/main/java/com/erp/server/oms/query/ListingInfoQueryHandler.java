package com.erp.server.oms.query;

import com.common.business.dto.AdvanceQueryContainer;
import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.enums.QueryConditionEnum;
import com.common.business.enums.QueryDataTypeEnum;
import com.common.business.query.AbstractQueryHandler;
import com.common.business.threadlocal.AdvanceQueryContext;
import com.erp.model.plm.enums.ProductDetailStatusEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author liuruipeng
 * @date 2024年01月08日 9:54
 */
@Component
public class ListingInfoQueryHandler extends AbstractQueryHandler {

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if("sm.product_name".equals(field)){
            String queryField = "pd.name";
            String skuStatusField = "pd.status";
            List<AdvanceQueryDTO> advanceQueryDTOList = new ArrayList<>();
            AdvanceQueryDTO filterStatus = AdvanceQueryDTO.buildSplicingSQLDTO(skuStatusField,QueryConditionEnum.EQ, ProductDetailStatusEnum.APPROVAL_PASS.getCode(),QueryDataTypeEnum.STRING);
            advanceQueryDTOList.add(filterStatus);
            QueryConditionEnum queryConditionEnum = AdvanceQueryContext.getCompareCode();
            if(queryConditionEnum.equals(QueryConditionEnum.EQ) || queryConditionEnum.equals(QueryConditionEnum.IN_LIST) || queryConditionEnum.equals(QueryConditionEnum.CONTAINS)
                    || queryConditionEnum.equals(QueryConditionEnum.STARTS_WITH) ||  queryConditionEnum.equals(QueryConditionEnum.ENDS_WITH)){
                AdvanceQueryDTO advanceQueryDTO = AdvanceQueryDTO.buildSplicingSQLDTO(queryField,queryConditionEnum,value,QueryDataTypeEnum.STRING);
                advanceQueryDTOList.add(advanceQueryDTO);
                AdvanceQueryContainer advanceQueryContainer = AdvanceQueryContainer.builder().advanceQueryDTOList(advanceQueryDTOList).build();
                List<SkuVO> skuVOS = plmTaskFeign.getSkuInfoAdvanceQuery(advanceQueryContainer);
                List<String> skuIds = skuVOS.stream().map(SkuVO::getSkuId).distinct().collect(Collectors.toList());
                if(CollectionUtils.isEmpty(skuIds)){
                    return this.getQueryEmptySql();
                }
                super.buildSplicingSQLDTO("sm.product_sku_id",QueryConditionEnum.IN_LIST,skuIds, QueryDataTypeEnum.STRING);
            }
            if(queryConditionEnum.equals(QueryConditionEnum.NE) || queryConditionEnum.equals(QueryConditionEnum.NOT_IN_LIST) || queryConditionEnum.equals(QueryConditionEnum.NOT_CONTAINS)){
                AdvanceQueryDTO advanceQueryDTO = AdvanceQueryDTO.buildSplicingSQLDTO(queryField,QueryConditionEnum.IN_LIST,value,QueryDataTypeEnum.STRING);
                advanceQueryDTOList.add(advanceQueryDTO);
                AdvanceQueryContainer advanceQueryContainer = AdvanceQueryContainer.builder().advanceQueryDTOList(advanceQueryDTOList).build();
                List<SkuVO> skuVOS = plmTaskFeign.getSkuInfoAdvanceQuery(advanceQueryContainer);
                List<String> skuIds = skuVOS.stream().map(SkuVO::getSkuId).distinct().collect(Collectors.toList());
                if(CollectionUtils.isEmpty(skuIds)){
                    return this.getQueryAllSql();
                }
                super.buildSplicingSQLDTO("sm.product_sku_id",QueryConditionEnum.NOT_IN_LIST,skuIds,QueryDataTypeEnum.STRING);
            }
        }
        return null;
    }
}

