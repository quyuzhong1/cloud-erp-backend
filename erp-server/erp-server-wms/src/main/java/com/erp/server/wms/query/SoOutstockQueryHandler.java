package com.erp.server.wms.query;

import com.common.business.constant.SearchType;
import com.common.business.dto.AdvanceQueryContainer;
import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.QueryConditionEnum;
import com.common.business.enums.QueryDataTypeEnum;
import com.common.business.query.AbstractQueryHandler;
import com.common.business.threadlocal.AdvanceQueryContext;
import com.erp.model.oms.entity.CustomerInfoEntity;
import com.erp.rpc.oms.feign.CustomerFeign;
import com.erp.rpc.tms.feign.LogisticsBillFeign;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author liuruipeng
 * @date 2024年01月08日 9:54
 */
@Component
public class SoOutstockQueryHandler extends AbstractQueryHandler {

    @Resource
    private CustomerFeign customerFeign;

    @Resource
    private LogisticsBillFeign logisticsBillFeign;

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if("so.country_id".equals(field)){
            List<String> list = com.common.business.utils.CollectionUtils.convertStrClzToList(value);
            List<CustomerInfoEntity> customerList = customerFeign.listByCountryIdList(list);
            if (CollectionUtils.isEmpty(customerList)) {
                return getQueryAllSql();
            }
            List<String> customerIds = customerList.stream().map(v->v.getId()).collect(Collectors.toList());
            super.buildDefaultDTO("so.customer_id", customerIds);
        }
        if("so.tab".equals(field)){
            String searchType = value.toString();
            if ("all".equals(searchType)) {
                return getQueryAllSql();
            }
            // 待提交
            super.buildSplicingSQLDTO("so.invalid_status", QueryConditionEnum.EQ,false, QueryDataTypeEnum.BOOLEAN);
            if (SearchType.WAIT_SUBMIT.equals(searchType)) {
                super.buildDefaultDTO("so.approve_status", ApproveStatusEnum.WAIT_SUBMIT.getStatus());
            }
            //待审核
            if (SearchType.WAIT_APPROVE.equals(searchType)) {
                super.buildDefaultDTO("so.approve_status", ApproveStatusEnum.APPROVE_ING.getStatus());
            }

            //已审核
            if (ApproveStatusEnum.APPROVE.getStatus().equals(searchType)) {
                super.buildDefaultDTO("so.approve_status", ApproveStatusEnum.APPROVE.getStatus());
            }

            //审核不通过
            if (ApproveStatusEnum.REJECT.getStatus().equals(searchType)) {
                super.buildDefaultDTO("so.approve_status", ApproveStatusEnum.REJECT.getStatus());
            }
        }
        return null;
    }
}

