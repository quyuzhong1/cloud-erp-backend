package com.erp.server.dmp.query;

import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.query.AbstractQueryHandler;
import com.erp.model.dmp.enums.DmpInputTaskStatusEnum;
import com.erp.model.dmp.enums.DmpInputTaskTaskTypeEnum;
import com.erp.model.scm.enums.PageListTypeEnum;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 *
 */
@Component
public class DmpCfgOutputDetailQueryHandler extends AbstractQueryHandler {


    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if ("tab".equals(field)) {
            if ("all".equals(value)) {
                return getQueryAllSql();
            }
            //待我审核
            if (DmpInputTaskStatusEnum.FINISH.getCode().equals(value)) {
                approveStatusList.add(ApproveStatusEnum.APPROVE_ING.getStatus());
                //需要审核的业务ids
                List<String> businessIds = commonService.listProcessCurBusinessIds(SourceTypeEnum.PRODUCT_LOGISTICS.getCode());
                if (CollectionUtils.isEmpty(businessIds)) {
                    return null;
                }
                super.buildDefaultDTO("pl.id", businessIds);
            }
        }
        return null;
    }
}

