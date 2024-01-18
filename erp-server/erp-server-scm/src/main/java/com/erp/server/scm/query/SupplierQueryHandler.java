package com.erp.server.scm.query;

import com.common.business.enums.SourceTypeEnum;
import com.common.business.query.AbstractQueryHandler;
import com.erp.model.scm.enums.SupplierTabEnum;
import com.erp.server.scm.service.CommonService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

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
    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if("tab".equals(field)){
            if (SupplierTabEnum.TO_ME_CHECK_TASK.getCode().equalsIgnoreCase(field)){
                List<String> businessIds = commonService.listProcessCurBusinessIds(SourceTypeEnum.SUPPLIER.getCode());
                if (CollectionUtils.isNotEmpty(businessIds)){
                    super.buildDefaultDTO("id", businessIds);
                }else {

                    return this.getQueryEmptySql();
                }

            }
            if (SupplierTabEnum.APPROVE.getCode().equalsIgnoreCase(field)){
                super.buildDefaultDTO("approve_status", SupplierTabEnum.APPROVE.getCode());
            }
            if (SupplierTabEnum.REJECT.getCode().equalsIgnoreCase(field)){
                super.buildDefaultDTO("approve_status", SupplierTabEnum.REJECT.getCode());
            }
        }
        if("contactPerson".equals(field)){
            return "id IN (SELECT supplier_id  FROM  supplier_contact  WHERE  is_deleted=FALSE   AND  person ILIKE '%"+field+"%' )";
        }
        if("contactTelNumber".equals(field)){
            return "id IN (SELECT supplier_id  FROM  supplier_contact  WHERE  is_deleted=FALSE   AND  tel_number ILIKE '%"+field+"%' )";
        }
        if("returnConfirmRule".equals(field)){

        }
        if("orderAcceptRule".equals(field)){

        }
        return null;
    }
}
