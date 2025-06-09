package com.erp.server.wms.query;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.AdvanceQueryContainer;
import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.QueryConditionEnum;
import com.common.business.enums.QueryDataTypeEnum;
import com.common.business.query.AbstractQueryHandler;
import com.common.business.threadlocal.AdvanceQueryContext;
import com.common.business.utils.QueryUtils;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.ListingAdvanceQueryDTO;
import com.erp.model.tms.dto.CfgReconciliationFieldDTO;
import com.erp.model.wms.dto.OverseasProviderDTO;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.server.wms.service.OverseasProviderService;
import com.erp.server.wms.service.WarehouseService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 */
@Component
public class OverseasInventoryQueryHandler extends AbstractQueryHandler {
    @Resource
    private OverseasProviderService overseasProviderService;
    @Resource
    private WarehouseService warehouseService;

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if("warehouseId".equals(field)){
            if (null == value){
                return null;
            }
            QueryConditionEnum queryConditionEnum = AdvanceQueryContext.getCompareCode();
            if (queryConditionEnum.equals(QueryConditionEnum.IS_NULL)) {
                return " oi.warehouse_code IS NULL or oi.warehouse_code IS NULL";
            }
            if (queryConditionEnum.equals(QueryConditionEnum.NOT_NULL)) {
                return " oi.warehouse_code IS NOT NULL or oi.warehouse_code IS NOT NULL";
            }
            List<String> warehouseIds;
            if (queryConditionEnum.equals(QueryConditionEnum.IN_LIST) || queryConditionEnum.equals(QueryConditionEnum.NOT_IN_LIST)){
                warehouseIds = JSONArray.parseArray(JSON.toJSONString(value)).stream().map(Object::toString).collect(Collectors.toList());
            } else {
                warehouseIds = Collections.singletonList(value.toString());
            }
            if (CollectionUtils.isEmpty(warehouseIds)){
                return null;
            }
            List<OverseasProviderDTO.WarehouseDTO> warehouseDTOList = overseasProviderService.listProviderWarehouseByIds(warehouseIds);
            List<WarehouseEntity> warehouseEntities = warehouseService.listByIds(warehouseIds);
            if (CollectionUtils.isEmpty(warehouseDTOList) && CollectionUtils.isEmpty(warehouseEntities)){
                return null;
            }
            List<String> codeList = warehouseDTOList.stream().map(OverseasProviderDTO.WarehouseDTO::getPlatformWarehouseCode).distinct().collect(Collectors.toList());
            List<String> warehouseNameList = warehouseEntities.stream().map(WarehouseEntity::getName).distinct().collect(Collectors.toList());

            if(queryConditionEnum.equals(QueryConditionEnum.NE) || queryConditionEnum.equals(QueryConditionEnum.NOT_IN_LIST) || queryConditionEnum.equals(QueryConditionEnum.NOT_CONTAINS)){
                if (CollectionUtils.isNotEmpty(codeList) && CollectionUtils.isEmpty(warehouseNameList)){
                    buildSplicingSQLDTO("opw.warehouse_id", QueryConditionEnum.NOT_IN_LIST, warehouseIds, QueryDataTypeEnum.STRING);
                    return null;
                }
                if (CollectionUtils.isEmpty(codeList) && CollectionUtils.isNotEmpty(warehouseNameList)){
                    buildSplicingSQLDTO("w.warehouseName", QueryConditionEnum.NOT_IN_LIST, warehouseNameList, QueryDataTypeEnum.STRING);
                    return null;
                }
                String codeListValueStr = QueryUtils.listToStringValue(warehouseIds, QueryDataTypeEnum.STRING);
                String warehouseNameListValueStr = QueryUtils.listToStringValue(warehouseNameList, QueryDataTypeEnum.STRING);
                return "( opw.warehouse_id not in " + codeListValueStr + " or w.warehouseName not in " + warehouseNameListValueStr + ")";
            } else {
                if (CollectionUtils.isNotEmpty(codeList) && CollectionUtils.isEmpty(warehouseNameList)){
                    buildSplicingSQLDTO("opw.warehouse_id", QueryConditionEnum.IN_LIST, warehouseIds, QueryDataTypeEnum.STRING);
                    return null;
                }
                if (CollectionUtils.isEmpty(codeList) && CollectionUtils.isNotEmpty(warehouseNameList)){
                    buildSplicingSQLDTO("w.warehouseName", QueryConditionEnum.IN_LIST, warehouseNameList, QueryDataTypeEnum.STRING);
                    return null;
                }
                String codeListValueStr = QueryUtils.listToStringValue(warehouseIds, QueryDataTypeEnum.STRING);
                String warehouseNameListValueStr = QueryUtils.listToStringValue(warehouseNameList, QueryDataTypeEnum.STRING);
                return "( opw.warehouse_id in " + codeListValueStr + " or w.warehouseName in " + warehouseNameListValueStr + ")";
            }
        }
        return null;
    }
}

