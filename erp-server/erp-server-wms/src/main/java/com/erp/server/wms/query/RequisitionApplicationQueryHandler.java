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
public class RequisitionApplicationQueryHandler extends AbstractQueryHandler {

    @Resource
    private SkuMappingFeign skuMappingFeign;

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        //第三方仓SKU
        if("platformProductId".equals(field) || "platformSku".equals(field)
        ||"fnSku".equals(field) || "thirdWarehouseSku".equals(field)){
            String queryField = "li.platform_sku_no";
            if("platformProductId".equals(field)){
                queryField = "li.platform_spu_no";
            }
            if("fnSku".equals(field)){
                queryField = "li.platform_fn_sku";
            }
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
                super.buildSplicingSQLDTO("rad.sku_id",QueryConditionEnum.IN_LIST,skuIds,QueryDataTypeEnum.STRING);

                //过滤指定店铺
                if(!"thirdWarehouseSku".equals(field)){
                    List<String> shopIds = listingAdvanceQueryDTOList.stream().map(ListingAdvanceQueryDTO::getShopId).distinct().collect(Collectors.toList());
                    if (CollectionUtils.isEmpty(shopIds)) {
                        return getQueryEmptySql();
                    }
                    super.buildDefaultDTO("ra.channel_id",shopIds);
                }else{
                    List<String> platformList = listingAdvanceQueryDTOList.stream().map(ListingAdvanceQueryDTO::getPlatform).distinct().collect(Collectors.toList());
                    if(CollectionUtils.isNotEmpty(platformList)){
                        super.buildDefaultDTO("op.code",platformList);
                    }
                }
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
                super.buildSplicingSQLDTO("rad.sku_id",QueryConditionEnum.NOT_IN_LIST,skuIds,QueryDataTypeEnum.STRING);
            }
            if("thirdWarehouseSku".equals(field)){
                super.buildDefaultDTO("ra.type","overseasWarehouse");
            }else{
                super.buildDefaultDTO("ra.type","salesPlatform");
            }

        }
        if("deliveryCode".equals(field)){
            return "EXISTS (SELECT id FROM first_mile_delivery fd WHERE fd.source_id = odp.id AND fd.code " + compareCodeSplicingValueSql + ")";
        }
        if("isChange".equals(field)){
            if((Boolean) value){
                return "EXISTS (SELECT 1 FROM requisition_application_change rac" +
                        " INNER JOIN requisition_application_change_detail racd on rac.id = racd.main_id WHERE rac.business_id = ra.id  and rac.is_deleted = false and rac.invalid_status = false and rac.approve_status != 'approve' and racd.business_detail_id = rad.id)";
            }else{
                return "NOT EXISTS (SELECT 1 FROM requisition_application_change rac" +
                        " INNER JOIN requisition_application_change_detail racd on rac.id = racd.main_id WHERE rac.business_id = ra.id  and rac.is_deleted = false and rac.invalid_status = false and rac.approve_status != 'approve' and racd.business_detail_id = rad.id)";
            }
        }
        return null;
    }
}

