package com.cloud.erp.chrome.service;

import com.cloud.erp.chrome.dto.MabangOrderDTO;
import com.cloud.erp.chrome.entity.MabanIncomeExpensesEntity;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author yl
 * @since 2022-08-24
 */
public interface MabanService extends IService<MabanIncomeExpensesEntity> {

    void importIncomeExpensesCsv(MabangOrderDTO dto);
}
