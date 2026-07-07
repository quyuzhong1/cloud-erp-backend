package com.erp.server.wms.query;

import com.common.business.constant.SearchType;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.QueryConditionEnum;
import com.common.business.enums.QueryDataTypeEnum;
import com.common.business.query.AbstractQueryHandler;
import com.common.business.threadlocal.AdvanceQueryContext;
import com.common.business.wrapper.FeignQuery;
import com.erp.model.oms.entity.CustomerInfoEntity;
import com.erp.rpc.oms.feign.CustomerFeign;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.rpc.oms.feign.SoInfoFeign;
import com.erp.rpc.sys.feign.SysPartitionFeign;
import com.erp.rpc.tms.feign.LogisticsBillFeign;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import com.common.business.dto.AdvanceQueryContainer;
import com.common.business.dto.AdvanceQueryDTO;
import com.erp.model.sys.entity.DictPartitionEntity;

/**
 * @author liuruipeng
 * @date 2024年01月08日 9:54
 */
@Component
public class SoOutstockQueryHandler extends AbstractQueryHandler {

    @Resource
    private CustomerFeign customerFeign;

    @Resource
    private SysPartitionFeign sysPartitionFeign;

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
                super.buildSplicingSQLDTO("so.approve_status", QueryConditionEnum.EQ, ApproveStatusEnum.WAIT_SUBMIT.getStatus(), QueryDataTypeEnum.STRING);
            }
            //待审核
            if (SearchType.WAIT_APPROVE.equals(searchType)) {
                super.buildSplicingSQLDTO("so.approve_status", QueryConditionEnum.EQ, ApproveStatusEnum.APPROVE_ING.getStatus(), QueryDataTypeEnum.STRING);
            }

            //已审核
            if (ApproveStatusEnum.APPROVE.getStatus().equals(searchType)) {
                super.buildSplicingSQLDTO("so.approve_status", QueryConditionEnum.EQ, ApproveStatusEnum.APPROVE.getStatus(), QueryDataTypeEnum.STRING);

            }

            //审核不通过
            if (ApproveStatusEnum.REJECT.getStatus().equals(searchType)) {
                super.buildSplicingSQLDTO("so.approve_status", QueryConditionEnum.EQ, ApproveStatusEnum.REJECT.getStatus(), QueryDataTypeEnum.STRING);

            }
        }
        return null;
    }
}

