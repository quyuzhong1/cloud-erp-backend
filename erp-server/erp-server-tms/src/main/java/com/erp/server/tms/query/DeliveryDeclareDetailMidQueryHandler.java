package com.erp.server.tms.query;

import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.query.AbstractQueryHandler;
import com.erp.model.tms.enums.DeliveryDeclareDetailMidGenerateStatusEnum;
import org.springframework.stereotype.Component;

/**
 * 报关明细中间表高级查询处理器
 *
 * @author jack
 * @date 2026-04-30
 */
@Component
public class DeliveryDeclareDetailMidQueryHandler extends AbstractQueryHandler {

    /**
     * 处理高级查询虚拟字段转译
     *
     * @param field 查询字段
     * @param value 查询值
     * @param compareCodeSplicingValueSql 比较条件SQL片段
     * @return SQL片段
     * @throws RuntimeException 当前方法不主动抛出业务异常
     */
    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if ("tab".equals(field)) {
            return getTabSql(value);
        }
        return null;
    }

    /**
     * 构建页签查询SQL
     *
     * @param value 页签值
     * @return SQL片段
     * @throws RuntimeException 当前方法不主动抛出业务异常
     */
    private String getTabSql(Object value) {
        if (value == null || CharSequenceUtil.equals("", value.toString()) || CharSequenceUtil.equals("all", value.toString())) {
            return this.getQueryAllSql();
        }
        if (CharSequenceUtil.equals(DeliveryDeclareDetailMidGenerateStatusEnum.WAIT.getCode(), value.toString())
                || CharSequenceUtil.equals(DeliveryDeclareDetailMidGenerateStatusEnum.FINISH.getCode(), value.toString())) {
            this.buildDefaultDTO("dddm.generate_status", value.toString());
            return super.getSplicingSQL();
        }
        return this.getQueryEmptySql();
    }
}
