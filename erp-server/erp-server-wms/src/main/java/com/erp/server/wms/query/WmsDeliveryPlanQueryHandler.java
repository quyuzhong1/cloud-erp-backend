package com.erp.server.wms.query;

import com.common.business.dto.AdvanceQueryContainer;
import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.enums.QueryConditionEnum;
import com.common.business.enums.QueryDataTypeEnum;
import com.common.business.query.AbstractQueryHandler;
import com.common.business.threadlocal.AdvanceQueryContext;
import com.erp.model.oms.dto.ListingAdvanceQueryDTO;
import com.erp.rpc.oms.feign.SkuMappingFeign;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author liuruipeng
 * @date 2024年01月08日 9:54
 */
@Component
public class WmsDeliveryPlanQueryHandler extends AbstractQueryHandler {

    @Resource
    private SkuMappingFeign skuMappingFeign;

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        //第三方仓SKU
        if("platformSku".equals(field)){
            List<AdvanceQueryDTO> advanceQueryDTOList = new ArrayList<>();
            QueryConditionEnum queryConditionEnum = AdvanceQueryContext.getCompareCode();
            if(queryConditionEnum.equals(QueryConditionEnum.EQ) || queryConditionEnum.equals(QueryConditionEnum.IN_LIST) || queryConditionEnum.equals(QueryConditionEnum.CONTAINS)
            || queryConditionEnum.equals(QueryConditionEnum.STARTS_WITH) ||  queryConditionEnum.equals(QueryConditionEnum.ENDS_WITH)){
                AdvanceQueryDTO advanceQueryDTO = AdvanceQueryDTO.buildSplicingSQLDTO("li.platform_sku_no",queryConditionEnum,value,QueryDataTypeEnum.STRING);
                advanceQueryDTOList.add(advanceQueryDTO);
                AdvanceQueryContainer advanceQueryContainer = AdvanceQueryContainer.builder().advanceQueryDTOList(advanceQueryDTOList).build();
                List<ListingAdvanceQueryDTO> listingAdvanceQueryDTOList = skuMappingFeign.advanceQuerySku(advanceQueryContainer);
                List<String> skuIds = listingAdvanceQueryDTOList.stream().map(ListingAdvanceQueryDTO::getSkuId).distinct().collect(Collectors.toList());
                if (CollectionUtils.isEmpty(skuIds)) {
                    return getQueryEmptySql();
                }
                super.buildSplicingSQLDTO("odpd.sku_id",QueryConditionEnum.IN_LIST,skuIds,QueryDataTypeEnum.STRING);
            }

            if(queryConditionEnum.equals(QueryConditionEnum.NE) || queryConditionEnum.equals(QueryConditionEnum.NOT_IN_LIST) || queryConditionEnum.equals(QueryConditionEnum.NOT_CONTAINS)){
                AdvanceQueryDTO advanceQueryDTO = AdvanceQueryDTO.buildSplicingSQLDTO("li.platform_sku_no",QueryConditionEnum.IN_LIST,value,QueryDataTypeEnum.STRING);
                advanceQueryDTOList.add(advanceQueryDTO);
                AdvanceQueryContainer advanceQueryContainer = AdvanceQueryContainer.builder().advanceQueryDTOList(advanceQueryDTOList).build();
                List<ListingAdvanceQueryDTO> listingAdvanceQueryDTOList = skuMappingFeign.advanceQuerySku(advanceQueryContainer);
                List<String> skuIds = listingAdvanceQueryDTOList.stream().map(ListingAdvanceQueryDTO::getSkuId).distinct().collect(Collectors.toList());
                if (CollectionUtils.isEmpty(skuIds)) {
                    return getQueryAllSql();
                }
                super.buildSplicingSQLDTO("odpd.sku_id",QueryConditionEnum.NOT_IN_LIST,skuIds,QueryDataTypeEnum.STRING);
            }

        }
        if("deliveryCode".equals(field)){
            return "EXISTS (SELECT id FROM first_mile_delivery fd WHERE fd.source_id = odp.id AND fd.code " + compareCodeSplicingValueSql + ")";
        }
        if("sourceCode".equals(field)){
            return "EXISTS (select source_code ,id from  (select json_array_elements(source_json::json) ->> 'sourceCode' as source_code,id from wms_delivery_plan_detail where is_deleted = false \n" +
                    "and id = odpd.id ) as sj where sj.source_code " + compareCodeSplicingValueSql + ")";
        }
        return null;
    }
}

