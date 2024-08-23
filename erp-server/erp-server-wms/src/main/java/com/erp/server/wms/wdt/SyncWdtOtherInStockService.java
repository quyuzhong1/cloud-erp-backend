package com.erp.server.wms.wdt;

import com.common.business.enums.SyncOperateEnum;
import org.apache.commons.math3.util.Pair;
import com.common.business.dto.DmpPushTaskFeignDTO;
import com.sdk.wangdian.sdk.api.wms.stockin.dto.CreateOtherStockinRequest;

import java.util.List;

/**
 * 同步其他入库单到旺店通
 *
 * @author tanmujin
 * @date 2024-05-15
 */
public interface SyncWdtOtherInStockService {

    /**
     * 保存推送旺店通其他入库单任务
     *
     * @param goodsList          SKU明细列表
     * @param operateCode        操作方向: 审核/反审核
     * @param sourceCode         来源单据编号
     * @param detailId           明细ID
     * @param outerCode          外部单号
     * @param thirdWarehouseCode 第三方仓库编码
     * @param checkOuterCode     是否校验外部单号重复
     * @param sysWarehouseId     ERP仓库ID
     * @return DmpPushTaskEntity 由DMP返回的任务实体
     * @date: 2024-05-25
     * @author: tanmujin
     */
//    DmpPushTaskFeignDTO generateTask(List<CreateOtherStockinRequest.GoodsList> goodsList, String operateCode, String sourceCode, String detailId, String outerCode, String thirdWarehouseCode, boolean checkOuterCode, String sysWarehouseId);

    /**
     * 根据sku和仓位合并明细
     * @param goodsList 明细列表
     * @return 合并后的列表
     * @date: 2024-07-31
     * @author: tanmujin
     */
//    List<CreateOtherStockinRequest.GoodsList> sumBySkuAndPositionNo(List<CreateOtherStockinRequest.GoodsList> goodsList);

    /**
     * 拆分sku明细，存在仓位映射关系的一组，不存在映射关系的放在另一组
     * @param sysWarehouseId ERP仓位ID
     * @param goodsList 原始明细列表
     * @return Pair：需要推送的列表，不需要推送的列表
     * @date: 2024-08-14
     * @author: tanmujin
     */
//    Pair<List<CreateOtherStockinRequest.GoodsList>, List<CreateOtherStockinRequest.GoodsList>> splitGoodsList(String sysWarehouseId, List<CreateOtherStockinRequest.GoodsList> goodsList);

    /**
     * 转换为其它入库单
     * @param operateEnum 操作类型
     * @param sourceCode 来源单据号
     * @param goodsList sku明细
     * @return
     * @date: 2024-08-15
     * @author: tanmujin
     */
//    void transferToInStock(SyncOperateEnum operateEnum, String sourceCode, List<CreateOtherStockinRequest.GoodsList> goodsList);
}
