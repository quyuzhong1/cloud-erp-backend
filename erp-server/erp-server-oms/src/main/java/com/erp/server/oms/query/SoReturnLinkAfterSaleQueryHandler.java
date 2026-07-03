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
        return null;
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
