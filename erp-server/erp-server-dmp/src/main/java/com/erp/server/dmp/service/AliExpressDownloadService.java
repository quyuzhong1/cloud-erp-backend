package com.erp.server.dmp.service;

import java.util.List;

import com.erp.model.dmp.entity.PlatformApiTaskEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.oms.aliexpress.dto.PlatformAliExpressOrderDTO;

/**
 * 速卖通下载服务
 * @author Jim
 * @date 2024/6/11 16:14
 */
public interface AliExpressDownloadService {


    /**
     * 地址下载
     */
    void handlerAddressDetail(String groupId, List<PlatformApiTaskEntity> list, String platform, String category, String business, Integer size);

    /**
     * 查询下载
     */
    List<PlatformAliExpressOrderDTO> mongoListPlatformOrder(List<String> shopIds,
                                                            Integer downloadStatus,
                                                            Integer downloadAddressStatus,
                                                            Integer downloadDeliveryStatus,
                                                            Integer downloadDeliveryDetailStatus,
                                                            Boolean hasPlatformWarehouseOrder,
                                                            int currentPage,
                                                            int pageSize);

    /**
     * 所有订单明细下载
     */
    void handlerOrderDetailDownload(List<PlatformApiTaskEntity> taskList, Integer size, String platform, String category, List<ShopInfoEntity> list);


    /**
     * 单明细下载
     */
    void singleHandlerOrderDetailDownload(String platform, String category, PlatformAliExpressOrderDTO dto);


    /**
     * 所有发货单下载
     */
    void handlerSoDeliveryDownload(List<PlatformApiTaskEntity> value, Integer size, String platform, String category, List<ShopInfoEntity> list);

    /**
     * 单发货单下载
     */

    void singleHandlerSoDeliveryDownload(String shopId, String platform, String category, List<PlatformAliExpressOrderDTO> dtoList);


    /**
     * 所有发货单明细下载
     */
    void handlerSoDeliveryDetailDownload(List<PlatformApiTaskEntity> value, Integer size, String platform, String category, List<ShopInfoEntity> list);

    /**
     * 单发货单明细下载
     */
    void singleHandlerSoDeliveryDetailDownload(String platform, String category, PlatformAliExpressOrderDTO dto);

}
