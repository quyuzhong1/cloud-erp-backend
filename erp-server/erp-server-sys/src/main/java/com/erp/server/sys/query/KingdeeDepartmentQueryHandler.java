package com.erp.server.sys.query;

import com.common.business.query.AbstractQueryHandler;
import com.common.business.threadlocal.AdvanceQueryContext;
import com.erp.model.sys.entity.KingdeeDepartmentEntity;
import com.erp.server.sys.service.KingdeeDepartmentService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Lambda
 * @Classname KingdeeDepartmentQueryHandler
 * @Description TODO
 * @Date 2024-03-11 18:04
 * @Created by yl
 */
@Component
public class KingdeeDepartmentQueryHandler extends AbstractQueryHandler {

    @Resource
    private KingdeeDepartmentService kingdeeDepartmentService;
    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {

        if("parentDeptName".equals(field)){
        	return " EXISTS (SELECT 1 from kingdee_department pkd where pkd.is_deleted = false and pkd.id = kd.parent_id and pkd.kingdee_dept_name "+ compareCodeSplicingValueSql +" ) ";
        }

        return null;
    }
}
