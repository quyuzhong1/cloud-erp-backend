package com.erp.server.dmp.query;

import com.common.business.constant.SearchType;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.query.AbstractQueryHandler;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * @author liuruipeng
 * @date 2024年01月08日 9:54
 */
@Component
public class DmpTaskQueryHandler extends AbstractQueryHandler {


    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if("tab".equals(field)){
            String searchType = value.toString();
            if ("all".equals(searchType)) {
                return getQueryAllSql();
            }
            if ("2".equals(searchType)) {
                super.buildDefaultDTO("status", Arrays.asList("2","1"));
            }else{
                super.buildDefaultDTO("status",searchType);
            }
        }
        return null;
    }
}

