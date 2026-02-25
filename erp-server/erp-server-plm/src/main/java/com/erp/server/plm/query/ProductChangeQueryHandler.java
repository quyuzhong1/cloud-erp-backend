package com.erp.server.plm.query;

import cn.hutool.core.collection.CollUtil;
import com.common.business.enums.QueryConditionEnum;
import com.common.business.enums.QueryDataTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.query.AbstractQueryHandler;
import com.common.business.threadlocal.AdvanceQueryContext;
import com.common.business.validator.ValidList;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.plm.enums.ProductChangeFieldEnum;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.rpc.workflow.WorkflowFeign;
import jodd.util.StringUtil;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProductChangeQueryHandler extends AbstractQueryHandler {

    @Resource
    private WorkflowFeign workflowFeign;

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {

        if("field".equals(field)){
            List<String> valueList = com.common.business.utils.CollectionUtils.convertStrClzToList(value);
            List<String> newValueList = new ArrayList<>();
            for (String val : valueList) {
                ProductChangeFieldEnum productChangeFieldEnum = ProductChangeFieldEnum.getByFieldLabel(StringUtil.toString(val));
                if(productChangeFieldEnum != null){
                    newValueList.add(val);
                }
            }
            if(CollUtil.isEmpty(newValueList)){
                return this.getQueryEmptySql();
            }
            super.buildSplicingSQLDTO("pc.field", AdvanceQueryContext.getCompareCode(), newValueList, QueryDataTypeEnum.STRING);
        }
        return null;
    }
}
