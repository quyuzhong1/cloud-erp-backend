package com.erp.server.plm.query;

import com.common.business.enums.BaseStatusEnum;
import com.common.business.enums.QueryConditionEnum;
import com.common.business.enums.QueryDataTypeEnum;
import com.common.business.query.AbstractQueryHandler;
import com.common.business.threadlocal.AdvanceQueryContext;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.enums.BomStateEnum;
import com.erp.model.workflow.vo.MyToDoTaskVO;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.plm.constant.SearchType;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


@Component
public class ProjectScheduleHandler extends AbstractQueryHandler {

    @Resource
    private WorkflowFeign workflowFeign;

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if ("tab".equals(field)) {
            return getTabSql(value);
        }
        return null;
    }

    public String getTabSql(Object value) {
        if ("all".equals(value)){
            return getQueryAllSql();
        }
        // 待提交
        if (SearchType.WAIT_AUDIT.equals(value)) {
            List<String> statusList = new ArrayList<>();
            String userId = UserContext.getDefaultLoginUser().getUid();
            //获取我的待办信息
            List<MyToDoTaskVO> myToDoTasks = workflowFeign.getMyToDoTasks(userId);
            // ids
            List<String> idList = myToDoTasks.stream().map(MyToDoTaskVO::getBusinessTableId).filter(Objects::nonNull).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(idList)) {
                return super.getQueryEmptySql();
            }
            statusList.add(BaseStatusEnum.WAIT_AUDIT.getStatus());
            statusList.add(BaseStatusEnum.AUDIT_ING.getStatus());
            super.buildSplicingSQLDTO("pp.id", QueryConditionEnum.IN_LIST, idList, QueryDataTypeEnum.STRING);
            super.buildSplicingSQLDTO("pp.status", QueryConditionEnum.IN_LIST, statusList, QueryDataTypeEnum.STRING);
        }
        return null;
    }
}
