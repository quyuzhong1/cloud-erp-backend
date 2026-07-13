package com.erp.server.wms.query;

import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.enums.QueryConditionEnum;
import com.common.business.enums.QueryDataTypeEnum;
import com.common.business.query.AbstractQueryHandler;
import com.erp.model.wms.enums.PrestockLinkStatusEnum;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * 预入库单高级搜索 QueryHandler
 * <p>
 * 注册 {@link com.common.business.annotation.WebAdvanceQuery} 所使用的高级查询处理器。
 * tab 页签映射说明：
 * <ul>
 *   <li>all / 空值 → 全部（不加 link_status 过滤）</li>
 *   <li>linked    → 已关联（link_status = LINKED）</li>
 *   <li>unlinked  → 未关联（link_status IN (UNLINKED, PARTIAL)，即未关联和部分关联）</li>
 * </ul>
 * </p>
 *
 * @author auto
 * @since 2026-06-30
 */
@Component
public class SoReturnPrestockQueryHandler extends AbstractQueryHandler {

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if ("tab".equals(field)) {
            String tab = value == null ? "" : value.toString();
            if (CharSequenceUtil.isBlank(tab) || "all".equals(tab)) {
                return this.getQueryAllSql();
            }
            if ("linked".equals(tab)) {
                super.buildDefaultDTO("srp.link_status", PrestockLinkStatusEnum.LINKED.getStatus());
            } else if ("unlinked".equals(tab)) {
                super.buildSplicingSQLDTO("srp.link_status", QueryConditionEnum.IN_LIST,
                        Arrays.asList(PrestockLinkStatusEnum.UNLINKED.getStatus(),
                                PrestockLinkStatusEnum.PARTIAL.getStatus()),
                        QueryDataTypeEnum.STRING);
            }
        }
        return null;
    }
}
