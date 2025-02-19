package com.erp.server.oms.query;

import com.common.business.constant.SearchType;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.query.AbstractQueryHandler;
import com.common.business.threadlocal.UserContext;
import org.springframework.stereotype.Component;

/**
 * @author liuruipeng
 * @date 2024年01月08日 9:54
 */
@Component
public class CustomerInfoQueryHandler extends AbstractQueryHandler {


    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if("cbs.approve_status".equals(field)){
            String searchType = value.toString();
            if ("all".equals(searchType)) {
                return getQueryAllSql();
            }else if (ApproveStatusEnum.APPROVE_ING.getStatus().equals(searchType)) {
                    return "cbs.approve_status = 'approveIng' and  exists (\n" +
                            "\t\t\t\tSELECT 1  \n" +
                            "\t\t\t\tFROM foreign_process_management pm\n" +
                            "        inner JOIN foreign_process_task_management ptm ON pm.process_instance_id = ptm.process_instance_id AND ptm.is_deleted = false AND ptm.task_status = 'approveIng'\n" +
                            "\t\t\t\twhere ptm.cur_approve_id = '"+ UserContext.getDefaultLoginUser().getUid()+"' and pm.business_key = 'customerB2bChangeSeller'\n" +
                            "\t\t\t\tand pm.business_id = ci.id\n" +
                            "\t\t\t\t)";
            }
            super.buildDefaultDTO("cbs.approve_status", value.toString());
        }
        if("ci.tab".equals(field)){
            String searchType = value.toString();
            if ("all".equals(searchType)) {
                return getQueryAllSql();
            }
            if (SearchType.WAIT_SUBMIT.equals(searchType)) {
                super.buildDefaultDTO("ci.approve_status", ApproveStatusEnum.WAIT_SUBMIT.getStatus());
            }
            //待审核
            if (SearchType.WAIT_APPROVE.equals(searchType)) {
                return "ci.approve_status = 'approveIng' and  exists (\n" +
                        "\t\t\t\tSELECT 1  \n" +
                        "\t\t\t\tFROM foreign_process_management pm\n" +
                        "        inner JOIN foreign_process_task_management ptm ON pm.process_instance_id = ptm.process_instance_id AND ptm.is_deleted = false AND ptm.task_status = 'approveIng'\n" +
                        "\t\t\t\twhere ptm.cur_approve_id = '"+ UserContext.getDefaultLoginUser().getUid()+"' and pm.business_key = 'customerInfo'\n" +
                        "\t\t\t\tand pm.business_id = ci.id\n" +
                        "\t\t\t\t)";
            }

            //已审核
            if (ApproveStatusEnum.APPROVE.getStatus().equals(searchType)) {
                super.buildDefaultDTO("ci.approve_status", ApproveStatusEnum.APPROVE.getStatus());
            }

            //审核不通过
            if (ApproveStatusEnum.REJECT.getStatus().equals(searchType)) {
                super.buildDefaultDTO("ci.approve_status", ApproveStatusEnum.REJECT.getStatus());
            }
        }
        return null;
    }
}

