package com.erp.server.tms.query;

import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.QueryConditionEnum;
import com.common.business.enums.QueryDataTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.query.AbstractQueryHandler;
import com.common.business.threadlocal.AdvanceQueryContext;
import com.common.business.threadlocal.UserContext;
import com.erp.model.oms.enums.KolB2bApplicationTableEnum;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.model.workflow.entity.ProcessTaskManagementEntity;
import com.erp.rpc.workflow.WorkflowFeign;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author jack
 * @date 2025-12-04
 */
@Component
public class CfgLogisticsCostImportQueryHandler extends AbstractQueryHandler {


    @Resource
    private WorkflowFeign workflowFeign;

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if ("tab".equals(field)) {
            return getTabSql(value);
        }
        QueryConditionEnum queryConditionEnum = AdvanceQueryContext.getCompareCode();
        if ("logisticsSuppler".equals(field) || "platform".equals(field)) {
            return " clci.dict_platform " + compareCodeSplicingValueSql;
        }
        return null;
    }

    private String getTabSql(Object value) {
        if ("all".equals(value)|| "".equals(value)){
            return getQueryAllSql();
        }
        if ("f".equals(value)){
            super.buildDefaultDTO("clci.disabled", Boolean.FALSE);
            return " clci.disabled = false ";
        }else if("t".equals(value)){
            return " clci.disabled = true ";
        }
        return null;
    }
}

