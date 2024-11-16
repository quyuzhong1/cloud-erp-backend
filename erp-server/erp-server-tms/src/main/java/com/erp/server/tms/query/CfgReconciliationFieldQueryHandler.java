package com.erp.server.tms.query;

import com.common.business.enums.QueryConditionEnum;
import com.common.business.enums.QueryDataTypeEnum;
import com.common.business.query.AbstractQueryHandler;
import com.common.business.threadlocal.AdvanceQueryContext;
import com.erp.model.tms.dto.CfgReconciliationFieldDTO;
import com.erp.server.tms.service.CfgReconciliationFieldService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 高级查询处理器
 *
 * @author Jim
 * {@code @date:}  2024-03-25
 */
@Component
public class CfgReconciliationFieldQueryHandler extends AbstractQueryHandler {

    @Resource
    private CfgReconciliationFieldService cfgReconciliationFieldService;

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if ("erpFieldName".equalsIgnoreCase(field) && null != value) {
            //查询ERO
            List<CfgReconciliationFieldDTO.ErpFieldDropDownDTO> erpFieldList = cfgReconciliationFieldService.erpFieldList(null);
            if (CollectionUtils.isEmpty(erpFieldList)) {
                return getQueryEmptySql();
            }
            List<String> sourceIds = new LinkedList<>();
            QueryConditionEnum queryConditionEnum = AdvanceQueryContext.getCompareCode();
            if (queryConditionEnum.equals(QueryConditionEnum.IS_NULL)) {
                return getQueryEmptySql();
            }
            if (queryConditionEnum.equals(QueryConditionEnum.NOT_NULL)) {
                sourceIds = erpFieldList.stream()
                        .map(CfgReconciliationFieldDTO.ErpFieldDropDownDTO::getSourceId)
                        .distinct()
                        .collect(Collectors.toList());
            }
            if (queryConditionEnum.equals(QueryConditionEnum.EQ)) {
                sourceIds = erpFieldList.stream()
                        .filter(e -> e.getErpFieldName().equals(value.toString()))
                        .map(CfgReconciliationFieldDTO.ErpFieldDropDownDTO::getSourceId)
                        .distinct()
                        .collect(Collectors.toList());
            }
            if (queryConditionEnum.equals(QueryConditionEnum.CONTAINS)) {
                sourceIds = erpFieldList.stream()
                        .filter(e -> e.getErpFieldName().contains(value.toString()))
                        .map(CfgReconciliationFieldDTO.ErpFieldDropDownDTO::getSourceId)
                        .distinct()
                        .collect(Collectors.toList());
            }
            if (queryConditionEnum.equals(QueryConditionEnum.IN_LIST)) {
                List<String> quertList = Arrays.stream((value.toString().split(","))).collect(Collectors.toList());
                sourceIds = erpFieldList.stream()
                        .filter(e -> quertList.contains(e.getErpFieldName()))
                        .map(CfgReconciliationFieldDTO.ErpFieldDropDownDTO::getSourceId)
                        .distinct()
                        .collect(Collectors.toList());
            }
            if (queryConditionEnum.equals(QueryConditionEnum.STARTS_WITH)) {
                sourceIds = erpFieldList.stream()
                        .filter(e -> e.getErpFieldName().startsWith(value.toString()))
                        .map(CfgReconciliationFieldDTO.ErpFieldDropDownDTO::getSourceId)
                        .distinct()
                        .collect(Collectors.toList());
            }
            if (queryConditionEnum.equals(QueryConditionEnum.ENDS_WITH)) {
                sourceIds = erpFieldList.stream()
                        .filter(e -> e.getErpFieldName().endsWith(value.toString()))
                        .map(CfgReconciliationFieldDTO.ErpFieldDropDownDTO::getSourceId)
                        .distinct()
                        .collect(Collectors.toList());
            }
            if (queryConditionEnum.equals(QueryConditionEnum.NE)) {
                sourceIds = erpFieldList.stream()
                        .filter(e -> !e.getErpFieldName().equals(value.toString()))
                        .map(CfgReconciliationFieldDTO.ErpFieldDropDownDTO::getSourceId)
                        .distinct()
                        .collect(Collectors.toList());
            }
            if (queryConditionEnum.equals(QueryConditionEnum.NOT_CONTAINS)) {
                sourceIds = erpFieldList.stream()
                        .filter(e -> !e.getErpFieldName().contains(value.toString()))
                        .map(CfgReconciliationFieldDTO.ErpFieldDropDownDTO::getSourceId)
                        .distinct()
                        .collect(Collectors.toList());
            }
            if (queryConditionEnum.equals(QueryConditionEnum.NOT_IN_LIST)) {
                List<String> quertList = Arrays.stream((value.toString().split(","))).collect(Collectors.toList());
                sourceIds = erpFieldList.stream()
                        .filter(e -> !quertList.contains(e.getErpFieldName()))
                        .map(CfgReconciliationFieldDTO.ErpFieldDropDownDTO::getSourceId)
                        .distinct()
                        .collect(Collectors.toList());
            }
            if(CollectionUtils.isEmpty(sourceIds)){
                return this.getQueryEmptySql();
            }
            if (queryConditionEnum.equals(QueryConditionEnum.EQ) || queryConditionEnum.equals(QueryConditionEnum.IN_LIST) || queryConditionEnum.equals(QueryConditionEnum.CONTAINS)
                    || queryConditionEnum.equals(QueryConditionEnum.STARTS_WITH) || queryConditionEnum.equals(QueryConditionEnum.ENDS_WITH)) {
                super.buildSplicingSQLDTO("crf.source_id", QueryConditionEnum.IN_LIST, sourceIds, QueryDataTypeEnum.STRING);
            } else {
                super.buildSplicingSQLDTO("crf.source_id", QueryConditionEnum.NOT_IN_LIST, sourceIds, QueryDataTypeEnum.STRING);
            }
        }
        return null;
    }
}

