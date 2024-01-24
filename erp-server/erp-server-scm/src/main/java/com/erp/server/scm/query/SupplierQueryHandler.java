package com.erp.server.scm.query;

import com.common.business.enums.QueryConditionEnum;
import com.common.business.enums.QueryDataTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.query.AbstractQueryHandler;
import com.erp.model.scm.enums.SupplierTabEnum;
import com.erp.model.srm.dto.CfgSettingDTO;
import com.erp.model.srm.dto.OrderAcceptDTO;
import com.erp.model.srm.dto.ReturnConfirmDTO;
import com.erp.model.srm.enums.ConfigKeyEnum;
import com.erp.rpc.srm.feign.SrmCfgSettingFeign;
import com.erp.server.scm.service.CommonService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author zdy
 * @ClassName SupplierQueryHandler
 * @description: TODO
 * @date 2024年01月18日
 * @version: 1.0
 */
@Component
public class SupplierQueryHandler extends AbstractQueryHandler {
    @Resource
    private CommonService commonService;
    @Resource
    private SrmCfgSettingFeign srmCfgSettingFeign;

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if ("tab".equals(field)) {
            if (SupplierTabEnum.TO_ME_CHECK_TASK.getCode().equals(value)) {
                List<String> businessIds = commonService.listProcessCurBusinessIds(SourceTypeEnum.SUPPLIER.getCode());
                if (CollectionUtils.isNotEmpty(businessIds)) {
                    super.buildDefaultDTO("id", businessIds);
                } else {

                    return this.getQueryEmptySql();
                }

            }
            if (SupplierTabEnum.APPROVE.getCode().equals(value)) {
                super.buildDefaultDTO("approve_status", SupplierTabEnum.APPROVE.getCode());
            }
            if (SupplierTabEnum.REJECT.getCode().equals(value)) {
                super.buildDefaultDTO("approve_status", SupplierTabEnum.REJECT.getCode());
            }
        }
        if ("contactPerson".equals(field)) {
            return "id IN (SELECT supplier_id  FROM  supplier_contact  WHERE  is_deleted=FALSE   AND  person ILIKE '%" + field + "%' )";
        }
        if ("contactTelNumber".equals(field)) {
            return "id IN (SELECT supplier_id  FROM  supplier_contact  WHERE  is_deleted=FALSE   AND  tel_number ILIKE '%" + field + "%' )";
        }
        if ("returnConfirmRule".equals(field)) {
            //通过获取srm中供应商配置进行筛选符合条件数据
            List<CfgSettingDTO.ViewDTO> viewDTOS = srmCfgSettingFeign.listByKey(ConfigKeyEnum.RETURN_AUTO_CONFIRM.getCode());
            if (value instanceof Boolean) {
                List<ReturnConfirmDTO> returnConfirmDTOS = viewDTOS.stream().map(CfgSettingDTO.ViewDTO::getReturnConfirmDTO).collect(Collectors.toList());
                List<String> supplierIds = returnConfirmDTOS.stream().map(ReturnConfirmDTO::getSupplierId).collect(Collectors.toList());
                Boolean returnConfirmRule = (Boolean) value;
                //启用退货规则
                if (returnConfirmRule) {
                    if (CollectionUtils.isNotEmpty(supplierIds)){
                        super.buildDefaultDTO("id", supplierIds);
                    }else {
                        super.getQueryEmptySql();
                    }
                } else {
                    if (CollectionUtils.isNotEmpty(supplierIds)){
                        super.buildSplicingSQLDTO("id", QueryConditionEnum.NOT_IN_LIST,supplierIds, QueryDataTypeEnum.STRING);
                    }else {
                        super.getQueryAllSql();
                    }
                }
            }
        }
        if ("orderAcceptRule".equals(field)) {
            //通过获取srm中供应商配置进行筛选符合条件数据
            List<CfgSettingDTO.ViewDTO> viewDTOS = srmCfgSettingFeign.listByKey(ConfigKeyEnum.ORDER_AUTO_ACCEPT.getCode());
            if (value instanceof Boolean) {
                List<OrderAcceptDTO> orderAcceptDTOS = viewDTOS.stream().map(CfgSettingDTO.ViewDTO::getOrderAcceptDTO).collect(Collectors.toList());
                List<String> supplierIds = orderAcceptDTOS.stream().map(OrderAcceptDTO::getSupplierId).collect(Collectors.toList());
                Boolean orderAcceptRule = (Boolean) value;
                //启用退货规则
                if (orderAcceptRule) {
                    if (CollectionUtils.isNotEmpty(supplierIds)){
                        super.buildDefaultDTO("id", supplierIds);
                    }else {
                        super.getQueryEmptySql();
                    }
                } else {
                    if (CollectionUtils.isNotEmpty(supplierIds)){
                        super.buildSplicingSQLDTO("id", QueryConditionEnum.NOT_IN_LIST,supplierIds, QueryDataTypeEnum.STRING);
                    }else {
                        super.getQueryAllSql();
                    }
                }
            }
        }
        return null;
    }
}
