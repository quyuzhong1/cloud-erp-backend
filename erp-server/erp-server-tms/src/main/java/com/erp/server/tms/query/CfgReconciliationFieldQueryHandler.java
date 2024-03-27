package com.erp.server.tms.query;

import com.common.business.constant.SearchType;
import com.common.business.dto.AdvanceQueryContainer;
import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.QueryConditionEnum;
import com.common.business.enums.QueryDataTypeEnum;
import com.common.business.query.AbstractQueryHandler;
import com.common.business.threadlocal.AdvanceQueryContext;
import com.erp.model.oms.dto.ListingAdvanceQueryDTO;
import com.erp.model.plm.enums.ProductDetailStatusEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.vo.SupplierRefUserVO;
import com.erp.model.tms.dto.CfgReconciliationFieldDTO;
import com.erp.model.wms.entity.SoOutstockEntity;
import com.erp.server.tms.controller.api.CfgReconciliationFieldController;
import com.erp.server.tms.service.CfgReconciliationFieldService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        if ("erpFieldName".equals(field) && null != value) {
            //查询ERO
            List<CfgReconciliationFieldDTO.ErpFieldDropDownDTO> erpFieldList = cfgReconciliationFieldService.erpFieldList(null);
            if (CollectionUtils.isEmpty(erpFieldList)) {
                return getQueryEmptySql();
            }
            List<String> sourceIds = new LinkedList<>();
            QueryConditionEnum queryConditionEnum = AdvanceQueryContext.getCompareCode();
            Stream<CfgReconciliationFieldDTO.ErpFieldDropDownDTO> stream = erpFieldList.stream();
            if (queryConditionEnum.equals(QueryConditionEnum.IS_NULL)) {
                return getQueryEmptySql();
            }
            if (queryConditionEnum.equals(QueryConditionEnum.NOT_NULL)) {
                sourceIds = stream
                        .map(CfgReconciliationFieldDTO.ErpFieldDropDownDTO::getSourceId)
                        .distinct()
                        .collect(Collectors.toList());
            }
            if (queryConditionEnum.equals(QueryConditionEnum.EQ)) {
                sourceIds = stream
                        .filter(e -> e.getErpFieldName().equals(value.toString()))
                        .map(CfgReconciliationFieldDTO.ErpFieldDropDownDTO::getSourceId)
                        .distinct()
                        .collect(Collectors.toList());
            }
            if (queryConditionEnum.equals(QueryConditionEnum.CONTAINS)) {
                sourceIds = stream
                        .filter(e -> e.getErpFieldName().contains(value.toString()))
                        .map(CfgReconciliationFieldDTO.ErpFieldDropDownDTO::getSourceId)
                        .distinct()
                        .collect(Collectors.toList());
            }
            if (queryConditionEnum.equals(QueryConditionEnum.IN_LIST)) {
                List<String> quertList = Arrays.stream(value.toString().split(",")).collect(Collectors.toList());
                sourceIds = stream
                        .filter(e -> quertList.contains(e.getErpFieldName()))
                        .map(CfgReconciliationFieldDTO.ErpFieldDropDownDTO::getSourceId)
                        .distinct()
                        .collect(Collectors.toList());
            }
            if (queryConditionEnum.equals(QueryConditionEnum.STARTS_WITH)) {
                sourceIds = stream
                        .filter(e -> e.getErpFieldName().startsWith(value.toString()))
                        .map(CfgReconciliationFieldDTO.ErpFieldDropDownDTO::getSourceId)
                        .distinct()
                        .collect(Collectors.toList());
            }
            if (queryConditionEnum.equals(QueryConditionEnum.ENDS_WITH)) {
                sourceIds = stream
                        .filter(e -> e.getErpFieldName().endsWith(value.toString()))
                        .map(CfgReconciliationFieldDTO.ErpFieldDropDownDTO::getSourceId)
                        .distinct()
                        .collect(Collectors.toList());
            }
            if (queryConditionEnum.equals(QueryConditionEnum.NE)) {
                sourceIds = stream
                        .filter(e -> !e.getErpFieldName().equals(value.toString()))
                        .map(CfgReconciliationFieldDTO.ErpFieldDropDownDTO::getSourceId)
                        .distinct()
                        .collect(Collectors.toList());
            }
            if (queryConditionEnum.equals(QueryConditionEnum.NOT_CONTAINS)) {
                sourceIds = stream
                        .filter(e -> !e.getErpFieldName().contains(value.toString()))
                        .map(CfgReconciliationFieldDTO.ErpFieldDropDownDTO::getSourceId)
                        .distinct()
                        .collect(Collectors.toList());
            }
            if (queryConditionEnum.equals(QueryConditionEnum.NOT_IN_LIST)) {
                List<String> quertList = Arrays.stream(value.toString().split(",")).collect(Collectors.toList());
                sourceIds = stream
                        .filter(e -> !quertList.contains(e.getErpFieldName()))
                        .map(CfgReconciliationFieldDTO.ErpFieldDropDownDTO::getSourceId)
                        .distinct()
                        .collect(Collectors.toList());
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

