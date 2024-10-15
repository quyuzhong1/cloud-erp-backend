package com.erp.server.plm.query;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.QueryConditionEnum;
import com.common.business.enums.QueryDataTypeEnum;
import com.common.business.query.AbstractQueryHandler;
import com.common.business.threadlocal.AdvanceQueryContext;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.erp.model.scm.enums.PageListTypeEnum;
import com.erp.model.workflow.vo.MyToDoTaskVO;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.plm.constant.SearchType;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProductChangeHandler extends AbstractQueryHandler {

    @Resource
    private WorkflowFeign workflowFeign;
    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if("tab".equals(field)){
            return getTabSql(value);
        }
        if ("state".equals(field)) {
            QueryConditionEnum queryConditionEnum = AdvanceQueryContext.getCompareCode();
            if (QueryConditionEnum.EQ.equals(queryConditionEnum)) {
                super.buildSplicingSQLDTO(field, QueryConditionEnum.EQ, value, QueryDataTypeEnum.NUMBER);
            } else if (QueryConditionEnum.NE.equals(queryConditionEnum)) {
                super.buildSplicingSQLDTO(field, QueryConditionEnum.NE, value, QueryDataTypeEnum.NUMBER);
            } else if (QueryConditionEnum.IN_LIST.equals(queryConditionEnum)) {
                super.buildSplicingSQLDTO(field, QueryConditionEnum.IN_LIST, value, QueryDataTypeEnum.NUMBER);
            } else if (QueryConditionEnum.NOT_IN_LIST.equals(queryConditionEnum)) {
                super.buildSplicingSQLDTO(field, QueryConditionEnum.NOT_IN_LIST, value, QueryDataTypeEnum.NUMBER);
            }
        }
        return null;
    }

    public String getTabSql (Object value) {
        // 待提交
        if (SearchType.WAIT_AUDIT.equals(value)) {
            String userId = UserContext.getDefaultLoginUser().getUid();
            //获取我的待办信息
            List<MyToDoTaskVO> myToDoTasks = workflowFeign.getMyToDoTasks(userId);
            List<String> changeIdList = myToDoTasks.stream().map(MyToDoTaskVO::getBusinessTableId).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(changeIdList)) {
                return getQueryEmptySql();
            }
            super.buildSplicingSQLDTO("id", QueryConditionEnum.IN_LIST, changeIdList, QueryDataTypeEnum.STRING);
        }
        return super.getSplicingSQL();
    }
}
