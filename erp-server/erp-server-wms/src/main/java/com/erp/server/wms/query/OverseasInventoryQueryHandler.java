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
                return "( ( oi.dict_platform = 'fbt' AND ( fw.id IS NULL OR fw.id = '' ) ) " +
                        "OR ( oi.dict_platform = 'AliExpress' AND ( w.warehouseName IS NULL OR w.warehouseName = '' ) ) " +
                        "OR ( oi.dict_platform != 'AliExpress' AND oi.dict_platform != 'fbt' AND ( opw.warehouse_id IS NULL OR opw.warehouse_id = '' ) ) )";
            }
            if (queryConditionEnum.equals(QueryConditionEnum.NOT_NULL)) {
                return "( ( oi.dict_platform = 'fbt' AND fw.id IS NOT NULL AND fw.id != '' ) " +
                        "OR ( oi.dict_platform = 'AliExpress' AND w.warehouseName IS NOT NULL AND w.warehouseName != '' ) " +
                        "OR ( oi.dict_platform != 'AliExpress' AND oi.dict_platform != 'fbt' AND opw.warehouse_id IS NOT NULL AND opw.warehouse_id != '' ) )";
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
            List<String> codeList = warehouseDTOList.stream()
                    .map(OverseasProviderDTO.WarehouseDTO::getWarehouseId)
                    .filter(StringUtils::isNotBlank)
                    .distinct()
                    .collect(Collectors.toList());
            List<String> warehouseNameList = warehouseEntities.stream().map(WarehouseEntity::getName).distinct().collect(Collectors.toList());
            List<String> fbtWarehouseIdList = buildFbtWarehouseIdList(warehouseEntities);

            if(queryConditionEnum.equals(QueryConditionEnum.NE) || queryConditionEnum.equals(QueryConditionEnum.NOT_IN_LIST) || queryConditionEnum.equals(QueryConditionEnum.NOT_CONTAINS)){
                return buildWarehouseFilterSql(codeList, warehouseNameList, fbtWarehouseIdList, true);
            } else {
                return buildWarehouseFilterSql(codeList, warehouseNameList, fbtWarehouseIdList, false);
            }
        }
        return null;
    }

    private List<String> buildFbtWarehouseIdList(List<WarehouseEntity> warehouseEntities) {
        Set<String> warehouseIdSet = new LinkedHashSet<>();
        for (WarehouseEntity warehouseEntity : warehouseEntities) {
            if (Objects.isNull(warehouseEntity)) {
                continue;
            }
            if (StringUtils.isNotBlank(warehouseEntity.getId())) {
                warehouseIdSet.add(warehouseEntity.getId());
            }
            if (StringUtils.isNotBlank(warehouseEntity.getOnwayWarehouseId())) {
                warehouseIdSet.add(warehouseEntity.getOnwayWarehouseId());
            }
        }
        return new ArrayList<>(warehouseIdSet);
    }

    private String buildWarehouseFilterSql(List<String> providerWarehouseIdList,
                                           List<String> warehouseNameList,
                                           List<String> fbtWarehouseIdList,
                                           boolean negative) {
        List<String> sqlList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(providerWarehouseIdList)) {
            String valueStr = QueryUtils.listToStringValue(providerWarehouseIdList, QueryDataTypeEnum.STRING);
            sqlList.add("( oi.dict_platform != 'AliExpress' AND oi.dict_platform != 'fbt' AND opw.warehouse_id IN " + valueStr + " )");
        }
        if (CollectionUtils.isNotEmpty(warehouseNameList)) {
            String valueStr = QueryUtils.listToStringValue(warehouseNameList, QueryDataTypeEnum.STRING);
            sqlList.add("( oi.dict_platform = 'AliExpress' AND w.warehouseName IN " + valueStr + " )");
        }
        if (CollectionUtils.isNotEmpty(fbtWarehouseIdList)) {
            String valueStr = QueryUtils.listToStringValue(fbtWarehouseIdList, QueryDataTypeEnum.STRING);
            sqlList.add("( oi.dict_platform = 'fbt' AND fw.id IN " + valueStr + " )");
        }
        if (CollectionUtils.isEmpty(sqlList)) {
            return null;
        }
        String sql = sqlList.size() == 1 ? sqlList.get(0) : "( " + String.join(" OR ", sqlList) + " )";
        return negative ? "NOT " + sql : sql;
    }
}

