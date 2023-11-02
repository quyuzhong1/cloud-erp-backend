package com.erp.server.oms.service;

import com.erp.model.oms.dto.ListingInfoDTO;
import com.erp.model.oms.dto.ListingInfoParamDTO;
import com.erp.model.oms.entity.ListingInfoEntity;
import com.common.business.service.SuperService;

import java.util.List;

/**
 * <p>
 * 对应平台sku 表 服务类
 * </p>
 *
 * @author Lambda
 * @since 2023-08-18
 */
public interface ListingInfoService extends SuperService<ListingInfoEntity> {

    /**
     * 根据 sku 获取到listing 数据
     * @author yl
     * @date 2023-08-18 16:35
     * @param skuNo
     * @return com.erp.model.oms.entity.ListingInfoEntity
     */
    ListingInfoEntity getBySkuNo(String skuNo,String type);

    /**
     * 添加库存sku
     * @param warehouseSkuNo
     * @param warehouseProductName
     * @return
     */
    String addWarehouseSku(String warehouseSkuNo, String warehouseProductName);

    /**
     * 根据平台sku 获取到对应的list
     * @author yl
     * @date 2023-08-21 11:57
     * @param platformSkuNo
     * @param platform
     * @return com.erp.model.oms.entity.ListingInfoEntity
     */
    ListingInfoEntity getByPlatformSkuNo(String platform,String platformSkuNo);
    /**
     * 根据类型获取到对应数据
     * @author yl
     * @date 2023-08-24 14:54
     * @param type
     * @return java.util.List<com.erp.model.oms.dto.ListingInfoDTO.ListDTO>
     */
    List<ListingInfoDTO.ListDTO> listByType(String type);

    /**
     * 通过条件查询ListingInfoEntity列表
     *
     * @author  Jim
     * @date 2023/11/2
     */
    List<ListingInfoEntity> findList(ListingInfoParamDTO dto);
}
