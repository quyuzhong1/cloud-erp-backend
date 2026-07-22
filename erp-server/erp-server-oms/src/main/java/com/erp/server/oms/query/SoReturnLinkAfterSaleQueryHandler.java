package com.erp.server.oms.query;

import com.common.business.enums.QueryConditionEnum;
import com.common.business.enums.QueryDataTypeEnum;
import com.common.business.query.AbstractQueryHandler;
import com.common.business.utils.QueryUtils;
import com.erp.model.oms.dto.SoReturnDTO;
import com.erp.model.oms.enums.BillTypeEnum;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
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
 *   <li>剩余应退货数量：通过 {@link #fillRemainReturnQtySql} 生成入库实退 LEFT JOIN、SELECT 表达式
 *       及 &gt;0 过滤（OMS 本地 so_return_instock / so_return_instock_detail）。</li>
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
     * 入库实退按售后明细维度预聚合子查询（OMS 本地 so_return_instock / so_return_instock_detail）。
     * B2B 作废口径对齐 {@code listDetailBySoReturnDetailIds}：{@code COALESCE(invalid_status,false)=false}；
     * B2C 作废口径对齐 {@code getSoReturnInstockByReturnIds}：{@code invalid_status=false}（NULL 不计入）。
     * B2C 另限定 {@code a.so_return_id} 属于「未做剩余应退过滤」的候选售后主表，对齐改动前按 mainIdList 拉入库再汇总。
     */
    private static String buildInStockAggSubquery(boolean isB2b, SoReturnDTO.LinkAfterSalePagingParam params) {
        String invalidFilter = isB2b
                ? "COALESCE(a.invalid_status, FALSE) = FALSE"
                : "a.invalid_status = FALSE";
        StringBuilder sql = new StringBuilder();
        sql.append(" (SELECT b.so_return_detail_id, COALESCE(SUM(b.real_qty), 0) AS instock_qty ")
                .append(" FROM so_return_instock_detail b ")
                .append(" INNER JOIN so_return_instock a ON a.id = b.main_id AND a.is_deleted = FALSE ")
                .append(" WHERE b.is_deleted = FALSE ")
                .append(" AND b.so_return_detail_id IS NOT NULL AND b.so_return_detail_id <> '' ")
                .append(" AND ").append(invalidFilter).append(" ");
        if (!isB2b) {
            sql.append(" AND a.so_return_id IN (").append(buildB2cCandidateReturnIdsSubquery(params)).append(") ");
        }
        sql.append(" GROUP BY b.so_return_detail_id) link_instock_agg ");
        return sql.toString();
    }

    /**
     * B2C 候选售后主表 id 子查询：与 {@code pagingLinkAfterSaleB2C} 主查询共用表别名（sbr/sbrd/sbd），
     * 复用高级查询 {@code sqlMap.default} 与 skuNoList，但不包含剩余应退过滤。
     */
    private static String buildB2cCandidateReturnIdsSubquery(SoReturnDTO.LinkAfterSalePagingParam params) {
        String advanceSql = "1 = 1";
        if (params != null) {
            Map<String, String> sqlMap = params.getSqlMap();
            if (sqlMap != null && StringUtils.isNotBlank(sqlMap.get("default"))) {
                advanceSql = sqlMap.get("default");
            }
        }
        StringBuilder sql = new StringBuilder();
        sql.append(" SELECT DISTINCT sbr.id FROM so_b2c_return_detail sbrd ")
                .append(" INNER JOIN so_b2c_return sbr ON sbrd.main_id = sbr.id AND sbrd.is_deleted = FALSE ")
                .append(" LEFT JOIN so_b2c_detail sbd ON sbrd.so_detail_id = sbd.id AND sbd.is_deleted = FALSE ")
                .append(" WHERE sbr.is_deleted = FALSE AND ").append(advanceSql);
        if (params != null && CollectionUtils.isNotEmpty(params.getSkuNoList())) {
            sql.append(" AND sbrd.sku_no IN ")
                    .append(QueryUtils.listToStringValue(params.getSkuNoList(), QueryDataTypeEnum.STRING));
        }
        if (params != null && StringUtils.isNotBlank(params.getPermissionSql())) {
            sql.append(params.getPermissionSql());
        }
        return sql.toString();
    }

    /**
     * 剩余应退货数量汇总 JOIN 别名
     */
    private static final String INSTOCK_AGG_ALIAS = "link_instock_agg";

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
     * 为关联售后单分页 SQL 填充剩余应退货数量相关片段：入库实退 LEFT JOIN、SELECT 表达式、&gt;0 过滤。
     * 口径对齐改动前 Service 逻辑：
     * <ul>
     *   <li>剩余应退货数量 = max(原退货数量 - 有效入库实退汇总, 0)</li>
     *   <li>仅返回剩余应退货数量 &gt; 0 的明细</li>
     *   <li>B2B 入库汇总口径同 {@code listDetailBySoReturnDetailIds}</li>
     *   <li>B2C 入库汇总口径同 {@code getSoReturnInstockByReturnIds}（候选 mainIdList + 按明细汇总）</li>
     * </ul>
     *
     * @param params 分页入参（须已写入 {@code sqlMap.default}，与主查询高级条件一致）
     * @param isB2b  是否 B2B 售后单
     */
    public static void fillRemainReturnQtySql(SoReturnDTO.LinkAfterSalePagingParam params, boolean isB2b) {
        if (params == null) {
            return;
        }
        String detailIdColumn = isB2b ? "srd.id" : "sbrd.id";
        String returnQtyColumn = isB2b ? "srd.return_qty" : "sbrd.return_qty";
        params.setInstockJoinSql(buildInstockJoinSql(isB2b, detailIdColumn, params));
        params.setRemainReturnQtyExpr(buildRemainReturnQtyExpr(returnQtyColumn));
        params.setRemainReturnQtyFilter(buildRemainReturnQtyPositiveFilter(returnQtyColumn));
    }

    /**
     * 构建入库实退汇总 LEFT JOIN：按售后明细 id 关联预聚合子查询。
     *
     * @param isB2b          是否 B2B（决定入库主单作废过滤口径）
     * @param detailIdColumn 售后明细 id 列（含表别名，如 srd.id / sbrd.id）
     * @return LEFT JOIN SQL 片段
     */
    public static String buildInstockJoinSql(boolean isB2b, String detailIdColumn) {
        return buildInstockJoinSql(isB2b, detailIdColumn, null);
    }

    /**
     * 构建入库实退汇总 LEFT JOIN。
     *
     * @param isB2b          是否 B2B
     * @param detailIdColumn 售后明细 id 列
     * @param params         B2C 候选集范围入参；B2B 可传 null
     * @return LEFT JOIN SQL 片段
     */
    public static String buildInstockJoinSql(boolean isB2b, String detailIdColumn, SoReturnDTO.LinkAfterSalePagingParam params) {
        return " LEFT JOIN " + buildInStockAggSubquery(isB2b, params)
                + " ON " + INSTOCK_AGG_ALIAS + ".so_return_detail_id = " + detailIdColumn + " ";
    }

    /**
     * 构建剩余应退货数量 SELECT 表达式。
     *
     * @param returnQtyColumn 原退货数量列（含表别名）
     * @return 剩余应退货数量表达式（不含 AS 别名）
     */
    public static String buildRemainReturnQtyExpr(String returnQtyColumn) {
        return " GREATEST(COALESCE(" + returnQtyColumn + ", 0) - COALESCE(" + INSTOCK_AGG_ALIAS + ".instock_qty, 0), 0) ";
    }

    /**
     * 构建剩余应退货数量 &gt; 0 的 WHERE 过滤条件（无可关联量的明细不返回）。
     *
     * @param returnQtyColumn 原退货数量列（含表别名）
     * @return WHERE 条件片段
     */
    public static String buildRemainReturnQtyPositiveFilter(String returnQtyColumn) {
        return buildRemainReturnQtyExpr(returnQtyColumn) + " > 0 ";
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
