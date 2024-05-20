package com.erp.server.wms.wdt;

import cn.wangdian.erp.sdk.api.wms.stockout.dto.CreateOtherStockoutRequest;
import com.erp.model.wms.entity.OtherInstockEntity;
import com.erp.model.wms.entity.OtherOutstockEntity;

import javax.annotation.Nullable;
import java.util.List;

/**
 * 旺店通其他出库单
 * @author tanmujin
 * @date 2024-05-16
 */
public interface SyncWdtOtherOutStockService {

    /**
     * 将ERP的其他出库单同步到旺店通
     * @param entity 其他出库单
     * @return void
     * @date: 2024-05-20
     * @author: tanmujin
     */
    void syncDataToWdt(OtherOutstockEntity entity);

    /**
     * 推送其他入库单到旺店通
     * @param goodsList SKU明细列表 CreateOtherStockoutRequest.GoodsList
     * @param warehouseId 仓库ID
     * @param outerNo ERP单据编码, 若为空则自动生成
     * @return void
     * @date: 2024-05-17
     * @author: tanmujin
     */
    void syncDataToWdt(List<CreateOtherStockoutRequest.GoodsList> goodsList, String warehouseId, @Nullable String outerNo);
}
