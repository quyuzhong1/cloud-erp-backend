package com.erp.server.plm.query;

import cn.hutool.core.collection.CollUtil;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.QueryConditionEnum;
import com.common.business.enums.QueryDataTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.query.AbstractQueryHandler;
import com.common.business.threadlocal.AdvanceQueryContext;
import com.common.business.validator.ValidList;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.oms.enums.KolB2bApplicationTableEnum;
import com.erp.model.plm.enums.ProductChangeFieldEnum;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.plm.service.CommonService;
import jodd.util.StringUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class SkuStdRetailPriceQueryHandler extends AbstractQueryHandler {

    @Resource
    private WorkflowFeign workflowFeign;

    @Resource
    private CommonService commonService;

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
    	if ("tab".equals(field)) {
            String searchType = value.toString();
            if ("all".equals(searchType)) {
                return getQueryAllSql();
            }

            if ("in".equals(searchType)) {
                return " g.id is not null ";
            }else {
                return " g.id is null ";
            }
        }
        if("t.id".equals(field)){
            return " ( g.id " + compareCodeSplicingValueSql +" or t.id " + compareCodeSplicingValueSql+" ) ";
        }
        return null;
    }

}
