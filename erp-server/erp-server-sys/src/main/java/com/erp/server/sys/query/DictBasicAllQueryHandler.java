package com.erp.server.sys.query;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.common.business.query.AbstractQueryHandler;
import com.erp.rpc.sys.feign.AuthDataFeign;


@Component
public class DictBasicAllQueryHandler extends AbstractQueryHandler {

    @Resource
    private AuthDataFeign authDataFeign;


    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
    	if ("tab".equals(field)) {
            String searchType = value.toString();
            if ("all".equals(searchType)) {
                return getQueryAllSql();
            }

            if ("able".equals(searchType)) {
                return " t.status = true ";
            }else {
                return " t.status = false ";
            }
        }

        return null;
    }
}
