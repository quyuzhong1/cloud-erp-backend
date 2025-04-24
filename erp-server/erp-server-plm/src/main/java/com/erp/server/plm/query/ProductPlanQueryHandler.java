package com.erp.server.plm.query;

import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.query.AbstractQueryHandler;
import com.erp.model.scm.enums.PageListTypeEnum;
import com.erp.server.plm.service.CommonService;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
public class ProductPlanQueryHandler extends AbstractQueryHandler {


    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if ("tab".equals(field)) {
            if ("all".equals(value)) {
                return this.getQueryAllSql();
            }
            if ("1".equals(value)) {
                super.buildDefaultDTO("pp.product_status", Arrays.asList("", "1"));
            }
            if ("2".equals(value)) {
                super.buildDefaultDTO("pp.product_status", Arrays.asList("3", "4", "5", "6"));
            }
            if ("3".equals(value)) {
                super.buildDefaultDTO("pp.product_status", Arrays.asList("4", "5", "6"));
            }
            return super.getSplicingSQL();
        }
        return null;
    }

}

