package com.erp.server.wms.query;

import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.query.AbstractQueryHandler;
import com.erp.model.wms.enums.PrestockLinkStatusEnum;
import org.springframework.stereotype.Component;

/**
 * 预入库单高级搜索 QueryHandler
 * <p>
 * 注册 {@link com.common.business.annotation.WebAdvanceQuery} 所使用的高级查询处理器。
 * tab 页签映射说明：
 * <ul>
 *   <li>all / 空值 → 全部（不加 link_status 过滤）</li>
 *   <li>unlinked  → 未关联（link_status = UNLINKED）</li>
 *   <li>partial   → 部分关联（link_status = PARTIAL）</li>
 *   <li>linked    → 已关联（link_status = LINKED）</li>
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
            if (CharSequenceUtil.isBlank(value.toString()) || "all".equals(value.toString())) {
                return this.getQueryAllSql();
            }
            // 将 tab 名称映射到关联状态枚举值
            String linkStatus = resolveTabToLinkStatus(value.toString());
            if (CharSequenceUtil.isNotBlank(linkStatus)) {
                super.buildDefaultDTO("srp.link_status", linkStatus);
            }
        }
        return null;
    }

    /**
     * 将前端传入的 tab 标识映射为 link_status 枚举值
     */
    private String resolveTabToLinkStatus(String tab) {
        switch (tab) {
            case "unlinked":
                return PrestockLinkStatusEnum.UNLINKED.getStatus();
            case "partial":
                return PrestockLinkStatusEnum.PARTIAL.getStatus();
            case "linked":
                return PrestockLinkStatusEnum.LINKED.getStatus();
            default:
                return "";
        }
    }
}
