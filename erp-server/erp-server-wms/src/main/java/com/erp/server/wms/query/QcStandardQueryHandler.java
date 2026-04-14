package com.erp.server.wms.query;

import com.common.business.enums.QueryConditionEnum;
import com.common.business.enums.QueryDataTypeEnum;
import com.common.business.query.AbstractQueryHandler;
import org.springframework.stereotype.Component;

/**
 * 质检标准 高级查询处理器
 *
 * @author jack
 * @since 2026-03-22
 */
@Component
public class QcStandardQueryHandler extends AbstractQueryHandler {

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        // 根据前端需要处理虚拟 tab 标签或其他复杂条件
        if ("tab".equals(field)) {
            if ("all".equals(value)|| "".equals(value)){
                return getQueryAllSql();
            }
            if ("enable".equals(value)) {
                super.buildSplicingSQLDTO("qs.disabled", QueryConditionEnum.EQ,false, QueryDataTypeEnum.BOOLEAN);
            } else if ("disabled".equals(value)) {
                super.buildSplicingSQLDTO("qs.disabled", QueryConditionEnum.EQ,true, QueryDataTypeEnum.BOOLEAN);
            }
            return super.getSplicingSQL();
        }
        return null; // 返回 null 交给底层默认处理
    }
}
