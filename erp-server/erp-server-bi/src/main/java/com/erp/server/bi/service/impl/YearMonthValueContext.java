package com.erp.server.bi.service.impl;


import com.erp.model.bi.dto.BiTargetYearDTO;
import com.erp.server.bi.service.ListYearMonthValueStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Description 年月目标值上下文
 * @Author yl
 * @Date 2023-09-18 15:26
 */
@Component
public class YearMonthValueContext {

    @Autowired
    private List<ListYearMonthValueStrategy> list;

    public <T extends ListYearMonthValueStrategy> T getBean(Class<T> tclass) {
        for (ListYearMonthValueStrategy strategy : list) {
            if (strategy.getClass() == tclass) {
                return (T) strategy;
            }
        }
        return null;
    }
}
