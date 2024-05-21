package com.erp.server.wms.wdt;

import cn.wangdian.erp.sdk.api.wms.stockin.dto.CreateOtherStockinRequest;
import com.erp.model.wms.entity.OtherInstockEntity;

import javax.annotation.Nullable;
import java.util.List;

/**
 * 同步其他入库单到旺店通
 *
 * @author tanmujin
 * @date 2024-05-15
 */
public interface SyncWdtOtherInStockService {

    /**
     * 将ERP其他入库单推送到旺店通
     * @param entity 其他入库单
     * @return void
     * @date: 2024-05-20
     * @author: tanmujin
     */
    void syncDataToWdt(OtherInstockEntity entity);

    /**
     * 推送其他入库单到旺店通
     * @param goodsList   商品明细列表 CreateOtherStockoutRequest.GoodsList
     * @param warehouseId 仓库ID
     * @param outerNo 单据编码, 若为空则自动生成
     * @return void
     * @date: 2024-05-17
     * @author: tanmujin
     */
    void syncDataToWdt(List<CreateOtherStockinRequest.GoodsList> goodsList, String warehouseId, @Nullable String outerNo);
}
