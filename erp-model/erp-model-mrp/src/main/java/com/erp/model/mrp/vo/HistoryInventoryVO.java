package com.erp.model.mrp.vo;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class HistoryInventoryVO {

    /**
     * 日期
     */
    private List<LocalDate> dates;
    /**
     * 库存数量
     */
    private List<Integer> qty;

    public static HistoryInventoryVO buildHistoryInventoryVO(List<LocalDate> dates, List<Integer> qty) {
        HistoryInventoryVO inventoryVO = new HistoryInventoryVO();
        inventoryVO.setDates(dates);
        inventoryVO.setQty(qty);
        return inventoryVO;
    }
}
