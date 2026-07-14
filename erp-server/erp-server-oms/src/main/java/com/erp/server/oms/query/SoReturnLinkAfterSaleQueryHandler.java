package com.erp.server.oms.query;

import com.common.business.enums.QueryConditionEnum;
import com.common.business.enums.QueryDataTypeEnum;
import com.common.business.query.AbstractQueryHandler;
import com.common.business.utils.QueryUtils;
import com.erp.model.oms.enums.BillTypeEnum;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 预入库-关联售后单：分页查询高级查询处理类
 * <p>该接口同时服务 B2B（so_return，别名 sr）与 B2C（so_b2c_return，别名 sbr）两张表，
 * 单据类型、仓库等条件均通过高级查询传入，需在此做特殊处理（前端只传结构化的值，不传 SQL）：</p>
 * <ul>
 *   <li>type：单据类型（BillTypeEnum），驱动 B2B / B2C 分表，不作为列过滤（B2C 表无单据类型列），
 *       仅记录到上下文供仓库字段判定表别名；要求其在 warehouseId 之前处理。</li>
 *   <li>warehouseId：按单据类型手动拼接仓库过滤 —— B2B 用 so_return.warehouse_id（sr），
 *       B2C 用关联销售订单明细 so_b2c_detail.warehouse_id（sbd）。</li>
 *   <li>b2bSoCode/b2bPlatformOrderCode/b2bAfterSaleCode 与 b2cSoCode/b2cPlatformOrderCode/b2cAfterSaleCode：
 *       B2B/B2C 单号字段分属不同表列，按上下文单据类型拼到对应表列；单据类型不匹配的字段直接放行，
 *       避免把 B2C 列拼进 B2B 查询（或反之）导致列不存在报错。</li>
 * </ul>
 *
 * @author Will
 * @date 2026/7/3
 */
@Component
public class SoReturnLinkAfterSaleQueryHandler extends AbstractQueryHandler {

    /**
     * 单据类型高级查询字段名
     */
    private static final String FIELD_TYPE = "type";

    /**
     * 仓库高级查询字段名
     */
    private static final String FIELD_WAREHOUSE_ID = "warehouseId";

    /**
     * B2C 销售订单号高级查询字段名
     */
    private static final String FIELD_B2C_SO_CODE = "b2cSoCode";

    /**
     * B2C 平台订单号高级查询字段名
     */
    private static final String FIELD_B2C_PLATFORM_ORDER_CODE = "b2cPlatformOrderCode";

    /**
     * B2C 售后订单号高级查询字段名
     */
    private static final String FIELD_B2C_AFTER_SALE_CODE = "b2cAfterSaleCode";

    /**
     * B2B 销售订单号高级查询字段名
     */
    private static final String FIELD_B2B_SO_CODE = "b2bSoCode";

    /**
     * B2B 平台订单号高级查询字段名
     */
    private static final String FIELD_B2B_PLATFORM_ORDER_CODE = "b2bPlatformOrderCode";

    /**
     * B2B 售后订单号高级查询字段名
     */
    private static final String FIELD_B2B_AFTER_SALE_CODE = "b2bAfterSaleCode";

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        //单据类型：只用于分表与仓库表别名判定，不生成列过滤
        if (FIELD_TYPE.equals(field)) {
            LinkAfterSaleQueryContext.setBillType(value == null ? null : value.toString());
            return getQueryAllSql();
        }
        //仓库：前端只传仓库 id(列表)，按单据类型手动拼接到不同表的仓库列
        if (FIELD_WAREHOUSE_ID.equals(field)) {
            return buildWarehouseSql(value);
        }
        //B2B/B2C 单号：分属不同表列，按上下文单据类型拼接，单据类型不匹配的字段直接放行
        String codeSql = buildOrderCodeSql(field, compareCodeSplicingValueSql);
        if (codeSql != null) {
            return codeSql;
        }
        return null;
    }

    /**
     * B2B / B2C 销售单号、平台单号、售后单号过滤：按上下文单据类型拼接到对应表列。
     * <p>该接口一次只查一张表（B2B 查 so_return(sr)，B2C 查 so_b2c_return(sbr)），
     * 因此仅当字段所属单据类型与上下文单据类型一致时才生成列过滤；否则放行（1 = 1），
     * 避免把不存在的表别名列拼进 SQL 导致报错。</p>
     * @param field 高级查询字段名
     * @param compareCodeSplicingValueSql 比较符与值拼接后的 SQL 片段（如 = 'xxx' / like '%xxx%' / in (...)）
     * @return java.lang.String 单号过滤 SQL 片段；非单号字段返回 null 交由后续默认逻辑处理
     */
    private String buildOrderCodeSql(String field, String compareCodeSplicingValueSql) {
        boolean isB2b = BillTypeEnum.B2B.getCode().equals(LinkAfterSaleQueryContext.getBillType());
        String column;
        switch (field) {
            case FIELD_B2B_SO_CODE:
                column = isB2b ? "sr.source_code" : null;
                break;
            case FIELD_B2B_PLATFORM_ORDER_CODE:
                column = isB2b ? "sr.platform_order_code" : null;
                break;
            case FIELD_B2B_AFTER_SALE_CODE:
                column = isB2b ? "sr.code" : null;
                break;
            case FIELD_B2C_SO_CODE:
                column = isB2b ? null : "sbr.so_code";
                break;
            case FIELD_B2C_PLATFORM_ORDER_CODE:
                column = isB2b ? null : "sbr.platform_order_no";
                break;
            case FIELD_B2C_AFTER_SALE_CODE:
                column = isB2b ? null : "sbr.code";
                break;
            default:
                return null;
        }
        //命中单号字段但单据类型不匹配：放行，不生成列过滤
        if (column == null) {
            return getQueryAllSql();
        }
        return column + " " + compareCodeSplicingValueSql;
    }

    /**
     * 根据上下文单据类型手动拼接仓库过滤：B2B -> so_return.warehouse_id(sr)，B2C -> so_b2c_detail.warehouse_id(sbd)。
     * @param value 仓库 id 或 id 列表
     * @return java.lang.String 仓库过滤 SQL 片段
     */
    private String buildWarehouseSql(Object value) {
        String billType = LinkAfterSaleQueryContext.getBillType();
        String warehouseColumn = BillTypeEnum.B2B.getCode().equals(billType) ? "sr.warehouse_id" : "sbd.warehouse_id";
        if (value instanceof Collection) {
            List<String> warehouseIds = ((Collection<?>) value).stream()
                    .filter(Objects::nonNull)
                    .map(Object::toString)
                    .filter(id -> !id.isEmpty())
                    .distinct()
                    .collect(Collectors.toList());
            if (warehouseIds.isEmpty()) {
                return getQueryAllSql();
            }
            //QueryUtils.listToStringValue 会做安全引号处理，返回形如 ('id1','id2')
            return warehouseColumn + " in " + QueryUtils.listToStringValue(warehouseIds, QueryDataTypeEnum.STRING);
        }
        if (value == null || value.toString().isEmpty()) {
            return getQueryAllSql();
        }
        return warehouseColumn + " = " + QueryUtils.handleVal(value, QueryDataTypeEnum.STRING.getCode(), QueryConditionEnum.EQ);
    }
}
