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
        if("packingStatus".equals(field)){
            String stringValue = value.toString();
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
        if ("businessCodes".equals(field)){
            this.buildDefaultDTO("concat(fdd.fba_shipment_code,owi.code)",value);
        }
        return null;
    }
}

