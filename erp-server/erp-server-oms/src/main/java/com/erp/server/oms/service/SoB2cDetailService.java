package com.erp.server.oms.service;

import com.common.business.dto.PlatformOrderDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.service.SuperService;
import com.erp.model.oms.dto.ListingInfoWithSkuMappingDTO;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.oms.dto.SoB2cDetailDTO;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.entity.SoB2cDetailEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.plm.vo.SkuInfoSimpleVO;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.wms.dto.ReportOrderDataDTO;
import com.erp.model.wms.dto.WarehouseMappingDTO;
import org.apache.commons.math3.util.Pair;

import java.time.LocalDateTime;
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
     * @param addDTO
     * @param mainId
     * @return Boolean
     */
    Boolean add(SoB2cDTO.AddDTO addDTO, String mainId);
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
    Boolean updateWarehouseIdByMainId(String mainId, String warehouseId,Boolean isCover);

    /**
     * @description: 更新明细id
     * @author Will
     * @date: 2024/5/24 10:37
     * @param detailList
     * @param isCover
     * @return Boolean
     */
    Boolean updateWarehouseId(SoB2cEntity entity,List<SoB2cDTO.SaveSoB2cDistributionDetailDTO> detailList, Boolean isCover);
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
    List<SoB2cDetailEntity> saveOrUpdateEntity(PlatformOrderDTO dto, SoB2cEntity mainEntity, Map<String, List<ListingInfoWithSkuMappingDTO>> listingInfoWithSkuMappingDTOMap, ShopInfoEntity shopInfo, List<SkuInfoSimpleVO> skuList);

    /**
     * 消费处理明细
     *
     * @Author Jim
     * @since 2023-12-13
     * @param list
     * @param mainEntity
     */
    void consumerHandleDetailList(List<SoB2cDetailEntity> list, SoB2cEntity mainEntity, List<SkuInfoSimpleVO> skuList);

    /**
     * 通过platformSkuId查询关联关系
     *
     * @param dictPlatform            平台代码
     * @param shopId                  店铺ID
     * @param platformOrderCreateTime 生效日期（查询所有=传空）
     * @param isExpire                是否过期（查询所有=传空）
     * @return
     */
    Map<String, List<ListingInfoWithSkuMappingDTO>> mapListingByPlatformSkuId(List<String> platformSkuIdList, List<String> platformSpuList, String dictPlatform, String shopId, LocalDateTime platformOrderCreateTime, Boolean isExpire);


    /**
     * @description: 更新明细的是否匹配仓库规则字段
     * @author Will
     * @date: 2023/12/14 9:21
     * @param mainId
     * @param detailIdList
     * @return Boolean
     */
    Boolean updateIsMatchWarehouseRule(String mainId,List<String> detailIdList);

    /**
     *
     * @description
     * @param mainId
     * @author Lambda
     * @return
     * @create 2023-12-13 20:15
     */
    List<SoB2cDetailDTO.OutstockDTO> listOutstockByMainId(String mainId);
    /**
     * @description: 查询待发货数量
     * @author Will
     * @date: 2024/1/31 12:03
     * @param paramDTO
     * @return List<WaitDeliveryQtyDTO>
     */
    List<SoB2cDetailDTO.WaitDeliveryQtyDTO> listWaitDeliveryQty(SoB2cDetailDTO.WaitDeliveryParamDTO paramDTO);

    /**
     * 速卖通修改仓库信息
     * @Author Luo_WG
     * @Date 2024/2/1 17:24
     * @param viewDTO
     * @return java.lang.Boolean
     **/
    Boolean updateWarehouseByMapping(WarehouseMappingDTO.MappingViewDTO viewDTO,String soId,List<String> skuIdList);

    List<SoB2cDetailEntity> listContainDeleted(List<String> ids);

    void updateContainDeleted(List<String> revertDetailIds);

    List<SoB2cDetailEntity> listBySplitId(List<String> detailIds);

    /**
     * 根据主表id修改平台包裹号
     * @param platformPackageId
     * @param mainId
     * @return
     */
    Boolean updatePlatformPackageIdByMainId(String platformPackageId, String mainId);

    /**
     * 根据匹配规则进行明细更新
     * @param entity
     * @param updateWarehouseList
     */
    void updateWarehouse(SoB2cEntity entity, List<Pair<SoB2cDetailEntity,String>> updateWarehouseList);
    void updateSignShippedByDetailId(List<String> detailIdList);

    List<SoB2cDetailDTO.PropertyDTO> handlePropertyDTOList(SkuVO.PropertyDTO skuPropertyDTO);
    /**
     * 查询所有虚拟仓B2C销售订单数据
     * @author will
     * @date 2024/9/26 17:11
     * @return List<ViewDTO>
     */
    List<ReportOrderDataDTO.ViewDTO> listAllVirtualSoB2cDetail();

    /**
     * 更换发货SKU
     *
     * @param targetId
     * @param entity
     * @param detail
     * @param skuVO
     * @return
     */
    BatchResultDTO changeDeliverySku(String targetId, SoB2cEntity entity, SoB2cDetailEntity detail, SkuVO skuVO);
}
