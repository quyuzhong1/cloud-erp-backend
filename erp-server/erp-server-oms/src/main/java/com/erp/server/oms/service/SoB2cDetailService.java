package com.erp.server.oms.service;

import com.common.business.dto.PlatformOrderDTO;
import com.common.business.service.SuperService;
import com.erp.model.oms.dto.ListingInfoWithSkuMappingDTO;
import com.erp.model.oms.dto.SoB2cDetailDTO;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.entity.SoB2cDetailEntity;
import com.erp.model.oms.entity.SoB2cEntity;

import java.util.List;
import java.util.Map;


/**
 * <p>
 * B2C销售订单明细表 服务类
 * </p>
 *
 * @author Will
 * @since 2023-08-18
 */
public interface SoB2cDetailService extends SuperService<SoB2cDetailEntity> {

    /**
     * @description: 新增
     * @author Will
     * @date: 2023/8/21 17:13
     * @param detailList
     * @param mainId
     * @return Boolean
     */
    Boolean add(List<SoB2cDetailDTO.AddDTO> detailList, String mainId);
    /**
     * @description: 修改
     * @author Will
     * @date: 2023/8/21 17:13
     * @param detailList
     * @param mainId
     * @return Boolean
     */
    Boolean update(List<SoB2cDetailDTO.UpdateDTO> detailList, String mainId);

    /**
     * @description: 根据主表id查询
     * @author Will
     * @date: 2023/8/22 11:04
     * @param mainId
     * @return List<SoB2cDetailEntity>
     */
    List<SoB2cDetailEntity> listByMainId(String mainId);

    /**
     * @description: 根据主表ids查询
     * @author Will
     * @date: 2023/8/22 11:04
     * @param mainIds
     * @return List<SoB2cDetailEntity>
     */
    List<SoB2cDetailEntity> listByMainIds(List<String> mainIds);
    /**
     * @description: 根据明细id更新仓库id
     * @author Will
     * @date: 2023/8/22 15:40
     * @param mainId
     * @param warehouseId
     * @return Boolean
     */
    Boolean updateWarehouseIdByMainId(String mainId, String warehouseId);
    /**
     * @description: 根据主表id删除
     * @author Will
     * @date: 2023/8/23 12:28
     * @param mainIds
     * @return Boolean
     */
    Boolean deleteByMainIds(List<String> mainIds);
    /**
     * @description: 更新仓库信息
     * @author Will
     * @date: 2023/9/11 10:02
     * @param detailList
     * @return Boolean
     */
    Boolean updateWarehouse(List<SoB2cDetailEntity> detailList);


    /**
     * 平台订单明细更新或保存
     *
     * @Author Jim
     * @since 2023-11-10
     **/
    List<SoB2cDetailEntity> saveOrUpdateEntity(PlatformOrderDTO dto, SoB2cEntity mainEntity, Map<String, ListingInfoWithSkuMappingDTO> listingInfoWithSkuMappingDTOMap, ShopInfoEntity shopInfo);

    /**
     * 通过platformSkuNo查询关联关系
     *
     * @Author Jim
     * @since 2023-11-28
     **/
    Map<String, ListingInfoWithSkuMappingDTO> mapListingByPlatformSkuNo(List<String> platformSkuList, String dictPlatform, String shopId);

    /**
     * @description: 更新明细的是否匹配仓库规则字段
     * @author Will
     * @date: 2023/12/14 9:21
     * @param mainId
     * @return Boolean
     */
    Boolean updateIsMatchWarehouseRule(String mainId);
}
