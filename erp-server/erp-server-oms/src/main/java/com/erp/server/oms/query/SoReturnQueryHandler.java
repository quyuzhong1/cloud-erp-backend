package com.erp.server.oms.query;

import com.common.business.enums.ApproveStatusEnum;
import com.common.business.query.AbstractQueryHandler;
import com.erp.model.oms.enums.SoReturnChangeListTypeEnum;
import com.erp.model.oms.enums.SoReturnInstockStatusEnum;
import org.springframework.stereotype.Component;

/**
 * @author Will
 * @date: 2024/2/28 9:49
 */
@Component
public class SoReturnQueryHandler extends AbstractQueryHandler {

    /**
     * 退货入库明细汇总子查询（非关联）：so_return_instock / so_return_instock_detail 为 wms 库的
     * postgres_fdw 外部表，禁止使用关联子查询（会退化为逐行远程查询导致分页/统计卡死）。
     * 子查询内部 join 本地表 so_return_detail 取 return_qty，按 so_return_detail_id 汇总 real_qty，
     * 只需扫描/下推一次外部表；口径与列表展示一致：非删除、入库主单未作废。
     * having 占位由各状态拼接后，以 srd.id in (...) 或 NOT EXISTS 使用。
     */
    private static final String IN_STOCK_QTY_SUB_PREFIX =
            " (select b.so_return_detail_id "
            + " from so_return_instock_detail b "
            + " inner join so_return_instock a on a.id = b.main_id "
            + " inner join so_return_detail d on d.id = b.so_return_detail_id "
            + " where b.is_deleted = false and a.is_deleted = false and d.is_deleted = false "
            + " and coalesce(a.invalid_status, false) = false "
            + " and b.so_return_detail_id is not null and b.so_return_detail_id <> '' "
            + " group by b.so_return_detail_id, d.return_qty having ";

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if("tab".equals(field)){
            return getTabSql(value);
        }
        if ("instockStatus".equals(field)) {
            return getInstockStatusSql(value);
        }
        return null;
    }

    /**
     * 入库状态过滤：not 未入库 / partial 部分入库 / instocked 已入库 / beyond 超出退货。
     * 已入库/部分/超出采用非关联 srd.id in 子查询；未入库采用 NOT EXISTS 关联预聚合结果，
     * 内层子查询仍只扫描一次 FDW 外部表，避免 NOT IN 的三值逻辑问题。
     * @param value 状态编码
     * @return String
     */
    public String getInstockStatusSql(Object value) {
        if (value == null) {
            return null;
        }
        String type = value.toString();
        if (SoReturnInstockStatusEnum.NOT.getCode().equals(type)) {
            // 无入库明细或汇总实退数量为 0：不在“汇总实退>0”的明细集合中
            return " not exists ( select 1 from " + IN_STOCK_QTY_SUB_PREFIX
                    + " sum(b.real_qty) > 0 ) instocked_ids where instocked_ids.so_return_detail_id = srd.id ) ";
        } else if (SoReturnInstockStatusEnum.PARTIAL.getCode().equals(type)) {
            return " srd.id in " + IN_STOCK_QTY_SUB_PREFIX + " sum(b.real_qty) > 0 and sum(b.real_qty) < d.return_qty ) ";
        } else if (SoReturnInstockStatusEnum.INSTOCKED.getCode().equals(type)) {
            return " srd.id in " + IN_STOCK_QTY_SUB_PREFIX + " sum(b.real_qty) > 0 and sum(b.real_qty) = d.return_qty ) ";
        } else if (SoReturnInstockStatusEnum.BEYOND.getCode().equals(type)) {
            return " srd.id in " + IN_STOCK_QTY_SUB_PREFIX + " sum(b.real_qty) > d.return_qty ) ";
        }
        return null;
    }


    /**
     * @description: tabSql
     * @author Will
     * @date: 2024/2/26 15:55
     * @param value
     * @return String
     */
    public String getTabSql (Object value) {
        //待审核
        if (SoReturnChangeListTypeEnum.TO_BE_APPROVE.getCode().equals(value)) {
            super.buildDefaultDTO("sr.approve_status", ApproveStatusEnum.APPROVE_ING.getCode());
        }
        //已审核
        if (SoReturnChangeListTypeEnum.APPROVE.getCode().equals(value)) {
            super.buildDefaultDTO("sr.approve_status", ApproveStatusEnum.APPROVE.getCode());
        }
        //不通过
        if (SoReturnChangeListTypeEnum.REJECT.getCode().equals(value)) {
            super.buildDefaultDTO("sr.approve_status", ApproveStatusEnum.REJECT.getCode());
        }
        return super.getSplicingSQL();
    }
}

