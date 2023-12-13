package com.sdk.oms.shopify.chrome.service;

import com.sdk.oms.shopify.chrome.dto.MabangOrderDTO;
import com.sdk.oms.shopify.chrome.entity.MabanIncomeExpensesEntity;
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
