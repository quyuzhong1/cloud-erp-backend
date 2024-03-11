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
public class FirstMileDeliveryQueryHandler extends AbstractQueryHandler {

    @Resource
    private SkuMappingFeign skuMappingFeign;

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        //第三方仓SKU
        if("thirdWarehouseSku".equals(field)){
            String queryField = "li.platform_sku_no";
            List<AdvanceQueryDTO> advanceQueryDTOList = new ArrayList<>();
            QueryConditionEnum queryConditionEnum = AdvanceQueryContext.getCompareCode();
            if(queryConditionEnum.equals(QueryConditionEnum.EQ) || queryConditionEnum.equals(QueryConditionEnum.IN_LIST) || queryConditionEnum.equals(QueryConditionEnum.CONTAINS)
            || queryConditionEnum.equals(QueryConditionEnum.STARTS_WITH) ||  queryConditionEnum.equals(QueryConditionEnum.ENDS_WITH)){
                AdvanceQueryDTO advanceQueryDTO = AdvanceQueryDTO.buildSplicingSQLDTO(queryField,queryConditionEnum,value,QueryDataTypeEnum.STRING);
                advanceQueryDTOList.add(advanceQueryDTO);
                AdvanceQueryContainer advanceQueryContainer = AdvanceQueryContainer.builder().advanceQueryDTOList(advanceQueryDTOList).build();

                //查询sku mapping
                List<ListingAdvanceQueryDTO> listingAdvanceQueryDTOList = skuMappingFeign.advanceQuerySku(advanceQueryContainer);
                List<String> skuIds = listingAdvanceQueryDTOList.stream().map(ListingAdvanceQueryDTO::getSkuId).distinct().collect(Collectors.toList());
                if (CollectionUtils.isEmpty(skuIds)) {
                    return getQueryEmptySql();
                }
                super.buildSplicingSQLDTO("fdd.sku_id",QueryConditionEnum.IN_LIST,skuIds,QueryDataTypeEnum.STRING);

            }

            if(queryConditionEnum.equals(QueryConditionEnum.NE) || queryConditionEnum.equals(QueryConditionEnum.NOT_IN_LIST) || queryConditionEnum.equals(QueryConditionEnum.NOT_CONTAINS)){
                AdvanceQueryDTO advanceQueryDTO = AdvanceQueryDTO.buildSplicingSQLDTO(queryField,QueryConditionEnum.IN_LIST,value,QueryDataTypeEnum.STRING);
                advanceQueryDTOList.add(advanceQueryDTO);
                AdvanceQueryContainer advanceQueryContainer = AdvanceQueryContainer.builder().advanceQueryDTOList(advanceQueryDTOList).build();
                List<ListingAdvanceQueryDTO> listingAdvanceQueryDTOList = skuMappingFeign.advanceQuerySku(advanceQueryContainer);
                List<String> skuIds = listingAdvanceQueryDTOList.stream().map(ListingAdvanceQueryDTO::getSkuId).distinct().collect(Collectors.toList());
                if (CollectionUtils.isEmpty(skuIds)) {
                    return getQueryAllSql();
                }
                super.buildSplicingSQLDTO("fdd.sku_id",QueryConditionEnum.NOT_IN_LIST,skuIds,QueryDataTypeEnum.STRING);
            }

            super.buildDefaultDTO("fd.demand_type","demandOverseasWarehouse");
        }
        if("isGenerateOverseasInbound".equals(field)){
            String stringValue = value.toString();
            boolean booleanValue = Boolean.parseBoolean(stringValue);
            if(booleanValue){
                return " EXISTS (SELECT id FROM overseas_warehouse_inbound owi WHERE owi.source_id = fd.id AND owi.is_deleted = FALSE)";
            }else{
                return " NOT EXISTS (SELECT id FROM overseas_warehouse_inbound owi WHERE owi.source_id = fd.id AND owi.is_deleted = FALSE)";
            }
        }
        return null;
    }
}

