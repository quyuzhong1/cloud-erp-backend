package com.erp.server.oms.service;

import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.ListingInfoDTO;
import com.erp.model.oms.dto.ListingInfoParamDTO;
import com.erp.model.oms.dto.ListingInfoWithSkuMappingDTO;
import com.erp.model.oms.entity.ListingInfoEntity;
import com.common.business.service.SuperService;
import com.erp.model.oms.entity.SkuMappingEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.wms.dto.FbaShipmentDTO;
import org.apache.commons.math3.util.Pair;

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
     * 添加库存sku
     * @param warehouseSkuNo
     * @param warehouseProductName
     * @return
     */
    String addWarehouseSku(String warehouseSkuNo, String warehouseProductName);

    /**
     * 根据平台sku 获取到对应的list
     *
     * @param platform
     * @param platformSkuNo
     * @param authId
     * @return com.erp.model.oms.entity.ListingInfoEntity
     * @author yl
     * @date 2023-08-21 11:57
     */
    ListingInfoEntity getByPlatformSkuNo(String platform, String platformSkuNo, String authId);
    List<ListingInfoEntity> listByParam(String type,String platform,List<String> skuNoList);
    /**
     * 根据类型获取到对应数据
     * @author yl
     * @date 2023-08-24 14:54
     * @param type
     * @return java.util.List<com.erp.model.oms.dto.ListingInfoDTO.ListDTO>
     */
    List<ListingInfoDTO.ListDTO> listByType(String type);


    /**
     * 新增映射skuNo
     * @Author Luo_WG
     * @Date 2023/11/2 19:24
     * @param dto
     * @return java.lang.Boolean
     **/
    Boolean skuMapping(FbaShipmentDTO.SkuMappingParamDTO dto);

    /**
     * 新增映射skuNo
     * @Author Jim
     * @Date 2023/11/20
     **/
    List<ListingInfoDTO.BaseDropDownDTO> listByTypeWithFieldName(ListingInfoDTO.BaseDropDownParamDTO dto);

    /**
     * 查询listing 信息
     * @author yl
     * @date 2023-12-08 10:04
     * @param platformSkuNo
     * @return 
     */
    ListingInfoEntity getByPlatformSkuNoAndSpu(String platformSkuNo, String platformSpuNo, String typeCode);

    void updateMatchResult(String listingId, Boolean matchResult);

    PagingVO<ListingInfoDTO.PageDTO> paging(PagingDTO<ListingInfoDTO.PagingParamDTO> dto);

    Boolean warehouseSkuMapping(ListingInfoDTO.WarehouseSkuMappingParamDTO dto);

    void saveBatchImport(List<ListingInfoEntity> addListingInfoEntityList, List<SkuMappingEntity> updateSkuMappingList, List<ListingInfoEntity> updateListingInfoList, List<SkuMappingEntity> addSkuMappingList,List<Pair<String, String>> addLogPairList, List<Pair<String, String>> updateLogPairList);
    void handleAliExpress(SkuMappingEntity lastestSkuMapping, SkuVO skuVO, ListingInfoEntity listingInfoEntity);

    /**
     * 检查和更新FnSku
     */
    List<ListingInfoWithSkuMappingDTO> checkAndUpdateFnsku(ListingInfoParamDTO dto);
}
