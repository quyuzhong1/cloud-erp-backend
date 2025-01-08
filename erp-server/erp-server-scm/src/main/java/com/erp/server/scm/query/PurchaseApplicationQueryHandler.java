package com.erp.server.scm.query;

import com.common.business.enums.ApproveStatusEnum;
import com.common.business.query.AbstractQueryHandler;
import com.erp.model.scm.enums.CreatePoTypeEnum;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * @author liuruipeng
 * @date 2024年01月08日 9:54
 */
@Component
public class PurchaseApplicationQueryHandler extends AbstractQueryHandler {

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if("pa.tab".equals(field)){
            String status = value.toString();
            if("waitSubmit".equals(status)){
                super.buildDefaultDTO("pa.approve_status",ApproveStatusEnum.WAIT_SUBMIT.getStatus());
            }else if ("toBeApprove".equals(status)){
                super.buildDefaultDTO("pa.approve_status",ApproveStatusEnum.APPROVE_ING.getStatus());
            }else if ("toBeCreate".equals(status)){
                super.buildDefaultDTO("pa.approve_status",ApproveStatusEnum.APPROVE.getStatus());
                super.buildDefaultDTO("pad.create_po_type", Arrays.asList(CreatePoTypeEnum.NOT_GENERATED.getStatus(),CreatePoTypeEnum.PARTIAL_GENERATED.getStatus()));
            }else if ("created".equals(status)){
                super.buildDefaultDTO("pa.approve_status",ApproveStatusEnum.APPROVE.getStatus());
                super.buildDefaultDTO("pad.create_po_type", Arrays.asList(CreatePoTypeEnum.ALL_GENERATED.getStatus()));
            }else if ("reject".equals(status)){
                super.buildDefaultDTO("pa.approve_status",ApproveStatusEnum.REJECT.getStatus());
            }else {
                return this.getQueryAllSql();
            }
        }
        if("sourceCode".equals(field)){
            return "EXISTS (select id from  (select json_array_elements(source_json::json) ->> 'code' as source_code,id from purchase_application_detail where is_deleted = false \n" +
                    "and id = pad.id ) as sj where sj.source_code " + compareCodeSplicingValueSql + ")";
        }
        return null;
    }

}

