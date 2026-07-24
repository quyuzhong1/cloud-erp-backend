package com.erp.server.oms.query;

import com.common.business.enums.QueryConditionEnum;
import com.common.business.enums.QueryDataTypeEnum;
import com.common.business.query.AbstractQueryHandler;
import com.common.business.utils.QueryUtils;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.oms.dto.SoReturnDTO;
import com.erp.model.oms.enums.BillTypeEnum;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 预入库-关联售后单：分页查询高级查询处理类
 * <p>该接口同时服务 B2B（so_return，别名 sr）与 B2C（so_b2c_return，别名 sbr）两张表，
 * 单据类型、仓库等条件均通过高级查询传入，需在此做特殊处理（前端只传结构化的值，不传 SQL）：</p>
 * <ul>
 *   <li>type：单据类型，仅允许 B2B / B2C（单一值），驱动分表，不作为列过滤（B2C 表无单据类型列），
 *       非法值或集合多值直接拒绝，禁止默认路由到 B2C；要求其在 warehouseId / 单号字段之前处理。</li>
 *   <li>warehouseId：按单据类型手动拼接仓库过滤 —— B2B 用 so_return.warehouse_id（sr），
 *       B2C 用关联销售订单明细 so_b2c_detail.warehouse_id（sbd）。</li>
 *   <li>剩余应退货数量：通过 {@link #fillRemainReturnQtySql} 生成入库实退 LEFT JOIN、SELECT 表达式
 *       及 &gt;0 过滤。{@code so_return_instock}/{@code so_return_instock_detail} 为 WMS 库
 *       postgres_fdw 外表，入库汇总必须用非关联子查询（禁止 LATERAL/逐行关联子查询）。</li>
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

    /**
     * 入库实退按售后明细维度预聚合子查询（WMS FDW 外表 so_return_instock / so_return_instock_detail）。
     * <p>必须保持非关联：先用与主查询一致的本地条件算出候选售后明细 id，再 {@code IN} 收窄后一次 GROUP BY，
     * 避免全表聚合 FDW，也避免 LATERAL 导致逐行远程查询。</p>
     * <ul>
     *   <li>B2B 作废口径对齐 {@code listDetailBySoReturnDetailIds}：{@code COALESCE(invalid_status,false)=false}</li>
     *   <li>B2C 作废口径对齐 {@code getSoReturnInstockByReturnIds}：{@code invalid_status=false}（NULL 不计入）</li>
     *   <li>B2B/B2C 均限定 {@code so_return_detail_id} 属于「未做剩余应退过滤」的候选明细；
     *       对主查询结果行，与按候选售后主单拉入库再按明细汇总的 JOIN 口径等价</li>
     * </ul>
     */
    private static String buildInStockAggSubquery(boolean isB2b, SoReturnDTO.LinkAfterSalePagingParam params) {
        String invalidFilter = isB2b
                ? "COALESCE(a.invalid_status, FALSE) = FALSE"
                : "a.invalid_status = FALSE";
        String candidateDetailIdsSql = buildCandidateDetailIdsSubquery(isB2b, params);
        StringBuilder sql = new StringBuilder();
        sql.append(" (SELECT b.so_return_detail_id, COALESCE(SUM(b.real_qty), 0) AS instock_qty ")
                .append(" FROM so_return_instock_detail b ")
                .append(" INNER JOIN so_return_instock a ON a.id = b.main_id AND a.is_deleted = FALSE ")
                .append(" WHERE b.is_deleted = FALSE ")
                .append(" AND b.so_return_detail_id IS NOT NULL AND b.so_return_detail_id <> '' ")
                .append(" AND ").append(invalidFilter).append(" ")
                // 用候选明细收窄 FDW 扫描；相对原 B2B 全表聚合，对主查询行 JOIN 结果等价
                .append(" AND b.so_return_detail_id IN (").append(candidateDetailIdsSql).append(") ");
        if (!isB2b) {
            // 对齐原 getSoReturnInstockByReturnIds：入库主单 so_return_id 须属于候选明细对应的售后主单
            sql.append(" AND a.so_return_id IN (")
                    .append(" SELECT sbrd.main_id FROM so_b2c_return_detail sbrd ")
                    .append(" WHERE sbrd.is_deleted = FALSE ")
                    .append(" AND sbrd.id IN (").append(candidateDetailIdsSql).append(")")
                    .append(") ");
        }
        sql.append(" GROUP BY b.so_return_detail_id) ").append(INSTOCK_AGG_ALIAS).append(" ");
        return sql.toString();
    }

    /**
     * 候选售后明细 id 子查询：条件与主查询一致（高级查询 / skuNoList / permissionSql / 软删与作废），
     * 但不含剩余应退过滤，供 FDW 入库汇总 {@code IN} 收窄使用。
     *
     * @param isB2b  是否 B2B
     * @param params 分页入参
     * @return 仅投影明细主键的 SELECT SQL
     */
    private static String buildCandidateDetailIdsSubquery(boolean isB2b, SoReturnDTO.LinkAfterSalePagingParam params) {
        String advanceSql = resolveAdvanceDefaultSql(params);
        StringBuilder sql = new StringBuilder();
        if (isB2b) {
            sql.append(" SELECT srd.id FROM so_return_detail srd ")
                    .append(" INNER JOIN so_return sr ON srd.main_id = sr.id AND srd.is_deleted = FALSE ")
                    .append(" WHERE sr.is_deleted = FALSE AND sr.invalid_status = FALSE AND ").append(advanceSql);
            if (params != null && CollectionUtils.isNotEmpty(params.getSkuNoList())) {
                sql.append(" AND srd.sku_no IN ")
                        .append(QueryUtils.listToStringValue(params.getSkuNoList(), QueryDataTypeEnum.STRING));
            }
        } else {
            sql.append(" SELECT sbrd.id FROM so_b2c_return_detail sbrd ")
                    .append(" INNER JOIN so_b2c_return sbr ON sbrd.main_id = sbr.id AND sbrd.is_deleted = FALSE ")
                    .append(" LEFT JOIN so_b2c_detail sbd ON sbrd.so_detail_id = sbd.id AND sbd.is_deleted = FALSE ")
                    .append(" WHERE sbr.is_deleted = FALSE AND ").append(advanceSql);
            if (params != null && CollectionUtils.isNotEmpty(params.getSkuNoList())) {
                sql.append(" AND sbrd.sku_no IN ")
                        .append(QueryUtils.listToStringValue(params.getSkuNoList(), QueryDataTypeEnum.STRING));
            }
        }
        if (params != null && StringUtils.isNotBlank(params.getPermissionSql())) {
            sql.append(params.getPermissionSql());
        }
        return sql.toString();
    }

    /**
     * 读取高级查询默认分组 SQL；未生成时退化为恒真，避免拼接出非法 WHERE。
     *
     * @param params 分页入参
     * @return {@code sqlMap.default} 或 {@code 1 = 1}
     */
    private static String resolveAdvanceDefaultSql(SoReturnDTO.LinkAfterSalePagingParam params) {
        if (params == null) {
            return "1 = 1";
        }
        Map<String, String> sqlMap = params.getSqlMap();
        if (sqlMap != null && StringUtils.isNotBlank(sqlMap.get("default"))) {
            return sqlMap.get("default");
        }
        return "1 = 1";
    }

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        //单据类型：只用于分表与仓库表别名判定，不生成列过滤；仅允许单一合法 B2B/B2C
        if (FIELD_TYPE.equals(field)) {
            LinkAfterSaleQueryContext.setBillType(resolveAndValidateBillType(value));
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
     * 解析并校验关联售后单查询的单据类型。
     * <p>仅允许 {@link BillTypeEnum#B2B} / {@link BillTypeEnum#B2C}；高级查询集合入参必须恰好包含一个合法值，
     * 禁止多类型混传，也禁止把非法值静默当成 B2C。</p>
     *
     * @param value 高级查询 type 字段原值（字符串或集合）
     * @return 合法单据类型编码；值为空时返回 null（由调用方决定是否必填）
     */
    public static String resolveAndValidateBillType(Object value) {
        if (value == null) {
            return null;
        }
        List<String> types;
        if (value instanceof Collection) {
            types = new ArrayList<>();
            for (Object item : (Collection<?>) value) {
                if (item == null) {
                    continue;
                }
                String code = item.toString().trim();
                if (StringUtils.isNotBlank(code) && !types.contains(code)) {
                    types.add(code);
                }
            }
        } else {
            String code = value.toString().trim();
            types = StringUtils.isBlank(code) ? Collections.emptyList() : Collections.singletonList(code);
        }
        if (types.isEmpty()) {
            return null;
        }
        if (types.size() > 1) {
            throw new ServiceException(ApiError.SO_RETURN_LINK_AFTER_SALE_BILL_TYPE_MULTI_FORBIDDEN);
        }
        String billType = types.get(0);
        if (!BillTypeEnum.B2B.getCode().equals(billType) && !BillTypeEnum.B2C.getCode().equals(billType)) {
            throw new ServiceException(ApiError.SO_RETURN_LINK_AFTER_SALE_BILL_TYPE_INVALID);
        }
        return billType;
    }

    /**
     * 读取并校验上下文中的单据类型，避免 Service 校验被绕过时仓库/单号条件默认走 B2C。
     *
     * @return 合法的 B2B / B2C 编码
     */
    private static String requireContextBillType() {
        String billType = LinkAfterSaleQueryContext.getBillType();
        if (StringUtils.isBlank(billType)) {
            throw new ServiceException(ApiError.COMMON_PARAM_REQUIRED, "单据类型");
        }
        if (!BillTypeEnum.B2B.getCode().equals(billType)
                && !BillTypeEnum.B2C.getCode().equals(billType)) {
            throw new ServiceException(ApiError.SO_RETURN_LINK_AFTER_SALE_BILL_TYPE_INVALID);
        }
        return billType;
    }

    /**
     * 为关联售后单分页 SQL 填充剩余应退货数量相关片段：入库实退 LEFT JOIN、SELECT 表达式、&gt;0 过滤。
     * 口径对齐改动前 Service 逻辑：
     * <ul>
     *   <li>剩余应退货数量 = max(原退货数量 - 有效入库实退汇总, 0)</li>
     *   <li>仅返回剩余应退货数量 &gt; 0 的明细</li>
     *   <li>B2B 入库汇总口径同 {@code listDetailBySoReturnDetailIds}</li>
     *   <li>B2C 入库汇总口径同 {@code getSoReturnInstockByReturnIds}（候选明细集合内按明细汇总）</li>
     * </ul>
     *
     * @param params 分页入参（须已写入 {@code sqlMap.default} 与 {@code permissionSql}，与主查询条件一致）
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
     * 构建入库实退汇总 LEFT JOIN：按售后明细 id 关联「非关联」预聚合子查询（FDW 安全）。
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
     * @param params         候选明细收窄入参（含 sqlMap / skuNoList / permissionSql）；可空（空则候选条件仅软删等）
     * @return LEFT JOIN SQL 片段
     */
    public static String buildInstockJoinSql(boolean isB2b, String detailIdColumn, SoReturnDTO.LinkAfterSalePagingParam params) {
        return " LEFT JOIN " + buildInStockAggSubquery(isB2b, params)
                + " ON " + INSTOCK_AGG_ALIAS + ".so_return_detail_id = " + detailIdColumn + " ";
    }

    /**
     * 构建剩余应退货数量 SELECT 表达式。
     * <p>GREATEST(..., 0) 是「关联售后候选」筛选口径（还能再关联的量，且配合 &gt;0 过滤）。
     * 售后单明细编辑视图 {@code SoB2cReturnServiceImpl#listAddDetailView} 保留原始差值（可负）用于展示超额入库，
     * 两处场景不同，勿要求改成与详情 Java 同一公式。</p>
     *
     * @param returnQtyColumn 原退货数量列（含表别名）
     * @return 剩余应退货数量表达式（不含 AS 别名）
     */
    public static String buildRemainReturnQtyExpr(String returnQtyColumn) {
        return " GREATEST(COALESCE(" + returnQtyColumn + ", 0) - COALESCE(" + INSTOCK_AGG_ALIAS + ".instock_qty, 0), 0) ";
    }

    /**
     * 构建剩余应退货数量 &gt; 0 的 WHERE 过滤条件（无可关联量的明细不返回）。
     * <p>与 {@code GREATEST(return_qty - instock_qty, 0) &gt; 0} 等价，但避免 WHERE 中重复计算 GREATEST，
     * 便于优化器做简单比较。</p>
     *
     * @param returnQtyColumn 原退货数量列（含表别名）
     * @return WHERE 条件片段
     */
    public static String buildRemainReturnQtyPositiveFilter(String returnQtyColumn) {
        return " COALESCE(" + returnQtyColumn + ", 0) > COALESCE(" + INSTOCK_AGG_ALIAS + ".instock_qty, 0) ";
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
        // 非单号字段直接放行，避免在 type 尚未写入上下文时误触发单据类型校验
        boolean isOrderCodeField = FIELD_B2B_SO_CODE.equals(field)
                || FIELD_B2B_PLATFORM_ORDER_CODE.equals(field)
                || FIELD_B2B_AFTER_SALE_CODE.equals(field)
                || FIELD_B2C_SO_CODE.equals(field)
                || FIELD_B2C_PLATFORM_ORDER_CODE.equals(field)
                || FIELD_B2C_AFTER_SALE_CODE.equals(field);
        if (!isOrderCodeField) {
            return null;
        }
        boolean isB2b = BillTypeEnum.B2B.getCode().equals(requireContextBillType());
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
        String billType = requireContextBillType();
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
