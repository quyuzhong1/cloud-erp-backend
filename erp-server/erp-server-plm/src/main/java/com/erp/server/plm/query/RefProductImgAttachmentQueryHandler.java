package com.erp.server.plm.query;

import com.common.business.query.AbstractQueryHandler;
import org.springframework.stereotype.Component;


/**
 * 图片关联表高级查询Handler
 * @date 2025-12-30
 * @author wuhaotian
 */
@Component
public class RefProductImgAttachmentQueryHandler  extends AbstractQueryHandler {
    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        return "";
    }
}
