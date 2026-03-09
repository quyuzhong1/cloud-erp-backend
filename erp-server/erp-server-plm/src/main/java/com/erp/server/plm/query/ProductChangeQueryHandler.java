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
public class ProductChangeQueryHandler extends AbstractQueryHandler {

    @Resource
    private WorkflowFeign workflowFeign;

    @Resource
    private CommonService commonService;

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if ("tab".equals(field)) {
            return getTabSql(value);
        }

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


    /**
     * @description: tabSql拼接
     * @author Will
     * @date: 2025/12/03 19:58
     * @param value
     * @return String
     */
    public String getTabSql (Object value) {

        //待提交
        if(ApproveStatusEnum.WAIT_SUBMIT.getCode().equals(value)){
            super.buildDefaultDTO("pc.invalid_status", Collections.singletonList(InvalidStatusEnum.NOT_VOIDED.getStatus()));
            super.buildDefaultDTO("pc.approve_status", Collections.singletonList(ApproveStatusEnum.WAIT_SUBMIT.getCode()));
        }
        //待提交
        if(ApproveStatusEnum.APPROVE_ING.getCode().equals(value)){
            super.buildDefaultDTO("pc.invalid_status", Collections.singletonList(InvalidStatusEnum.NOT_VOIDED.getStatus()));
            super.buildDefaultDTO("pc.approve_status", Collections.singletonList(ApproveStatusEnum.APPROVE_ING.getCode()));
        }
        //待我审核
//        if(ApproveStatusEnum.APPROVE_ING.getCode().equals(value)){
//            super.buildDefaultDTO("pc.approve_status", Collections.singletonList(ApproveStatusEnum.APPROVE_ING.getStatus()));
//            //需要审核的业务ids
//            List<String> businessIds = commonService.listProcessCurBusinessIds(SourceTypeEnum.PRODUCT_CHANGE.getCode());
//            if (CollectionUtils.isNotEmpty(businessIds)) {
//                super.buildDefaultDTO("pc.id", businessIds);
//            }else{
//                //返回空结果
//                return this.getQueryEmptySql();
//            }
//        }

        //待提交
        if(ApproveStatusEnum.WAIT_SUBMIT.getCode().equals(value)){
            super.buildDefaultDTO("pc.invalid_status", Collections.singletonList(InvalidStatusEnum.NOT_VOIDED.getStatus()));
            super.buildDefaultDTO("pc.approve_status", Collections.singletonList(ApproveStatusEnum.WAIT_SUBMIT.getCode()));
        }


        //审核不通过
        if(ApproveStatusEnum.REJECT.getCode().equals(value)){
            super.buildDefaultDTO("pc.invalid_status", Collections.singletonList(InvalidStatusEnum.NOT_VOIDED.getStatus()));
            super.buildDefaultDTO("pc.approve_status", Collections.singletonList(ApproveStatusEnum.REJECT.getCode()));
        }
        return super.getSplicingSQL();
    }
}
