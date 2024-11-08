package com.erp.server.oms.service;

import com.common.business.dto.AdvanceQueryContainer;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.ListingAdvanceQueryDTO;
import com.erp.model.oms.dto.ListingInfoParamDTO;
import com.erp.model.oms.dto.ListingInfoWithSkuMappingDTO;
import com.erp.model.oms.dto.SkuMappingDTO;
import com.erp.model.oms.entity.ListingInfoEntity;
import com.erp.model.oms.entity.SkuMappingEntity;
import com.erp.model.oms.enums.RuleTypeEnum;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.scm.dto.OperateLogDTO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * sku 对照表 服务类
 * </p>
 *
 * @author Lambda
 * @since 2023-06-28
 */
public interface SkuMappingService extends SuperService<SkuMappingEntity> {

    void downloadTemplate(String type, HttpServletResponse response);

    /**
     * 导入sku对照信息
     *
     * @param excelFile
     * @param response
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-06-29 11:01
     */
    Boolean importExcel(MultipartFile excelFile, String type, HttpServletResponse response);

    /**
     * 分页查询
     *
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.oms.dto.SkuMapingDTO.PagingViewDTO>
     * @author yl
     * @date 2023-06-29 18:07
     */
    PagingVO<SkuMappingDTO.PagingViewDTO> paging(PagingDTO<SkuMappingDTO.PagingParamDTO> dto);

    /**
     * 导出sku 对照表
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-06-30 9:35
     */
    Boolean exportPlatformSku(SkuMappingDTO.ExportDTO dto);

    /**
     * 获取tab 列表
     *
     * @param dto
     * @return java.util.List<com.erp.model.oms.dto.SkuMapingDTO.TabListDTO>
     * @author yl
     * @date 2023-06-30 9:45
     */
    List<SkuMappingDTO.TabListDTO> tabList(SkuMappingDTO.FindTabDTO dto);

    /**
     * 更改sku 对照表
     *
     * @param dto
     * @return java.lang.String
     * @author yl
     * @date 2023-06-30 10:21
     */
    String updatePlatformSku(SkuMappingDTO.UpdatePlatformDTO dto);

    /**
     * 销售订单添加客户sku
     *
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.oms.dto.SkuMapingDTO.ProductSkuInfoDTO>
     * @author yl
     * @date 2023-07-01 9:19
     */
    PagingVO<SkuMappingDTO.ProductSkuInfoDTO> listPaging(PagingDTO<SkuMappingDTO.ListParamDTO> dto);

    /**
     * 添加库存sku 对照信息
     * @author yl
     * @date 2023-08-18 16:32
     * @param dto
     * @return java.lang.String
     */
    String addWarehouseSku(SkuMappingDTO.AddWarehouseSkuDTO dto);

    /**
     * 库存sku 对照表分页
     * @author yl
     * @date 2023-08-21 9:56
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.oms.dto.SkuMappingDTO.WarehousePagingViewDTO>
     */
    PagingVO<SkuMappingDTO.WarehousePagingViewDTO> warehousePaging(PagingDTO<SkuMappingDTO.WarehousePagingParamDTO> dto);

    /**
     * 导出库存sku 对照表
     * @author yl
     * @date 2023-08-21 10:22
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean exportWarehouseSku(SkuMappingDTO.ExportWarehouseSkuDTO dto);

    /**
     * 更改库存sku 对照
     * @author yl
     * @date 2023-08-21 11:40
     * @param dto
     * @return java.lang.String
     */
    String updateWarehouseSku(SkuMappingDTO.UpdateWarehouseSkuDTO dto);
    /**
     * @description: 根据skuIds查询
     * @author Will
     * @date: 2023/8/24 18:52
     * @param list
     * @return List<ListSkuDTO>
     */
    List<SkuMappingDTO.ListSkuDTO> listBySkuNoList(List<SkuMappingDTO.ListSkuParamDTO> list);

    
    /**
     * 根据平台sku noList 获取对应的数据
     * @author yl
     * @date 2023-09-04 17:11
     * @param platformSkuNoList
     * @return java.util.List<com.erp.model.oms.dto.SkuMappingDTO.ListSkuDTO>
     */
    List<SkuMappingDTO.SkuDTO> listByPlatformSkuNoList(List<String> platformSkuNoList);

    /**
     * 根据平台sku查询sku映射信息
     * @Author Luo_WG
     * @Date 2023/11/15 15:28
     * @param listingInfoParamDTO
     * @return java.util.List<com.erp.model.oms.dto.SkuMappingDTO.SkuDTO>
     **/
    List<SkuMappingDTO.MappingSkuViewDTO> listByPlatformSkuNoAndPlatform(ListingInfoParamDTO listingInfoParamDTO);

    /**
     * 根据产品sku查询库存sku
     * @Author Luo_WG
     * @Date 2023/11/2 17:20
     * @param productSkuIdList
     * @return java.util.List<com.erp.model.oms.dto.SkuMappingDTO.listStockSkuNoByProductSkuNoView>
     **/
    List<SkuMappingDTO.ListStockSkuNoByProductSkuIdView> listStockSkuNoByProductSkuIds(List<String> productSkuIdList);


    /**
     * 根据listingId查询sku映射表
     * @Author Luo_WG
     * @Date 2023/11/15 15:03
     * @param listingIds
     * @return java.util.List<com.erp.model.oms.entity.SkuMappingEntity>
     **/
    List<SkuMappingEntity> listByListingIds(List<String> listingIds);


    /**
     * 通过属性查询 SkuMappingEntity
     * @author Jim
     * @date 2023-11-17 11:40
     */
    SkuMappingEntity getByAttribute(String productSkuId, String warehouseId, RuleTypeEnum warehouseType);

    /**
     * 修改
     * @param dto
     * @return
     */
    Boolean updateSkuMapping(SkuMappingDTO.UpdateSkuMappingDTO dto);


    /**
     * 根据条件查询映射
     * @param dto
     * @return
     */
    List<ListingInfoWithSkuMappingDTO> findListDto(ListingInfoParamDTO dto);

    /**
     * 删除映射 SkuMappingEntity
     * @author Jim
     * @date 2023-12-21
     */
    BatchResultDTO delete(String id);

    /**  获取到sku 对应的信息
     * @description
     * @param listSkuParamList
     * @author Lambda
     * @return 
     * @create 2023-12-25 15:47
     */
    List<SkuMappingDTO.ListSkuResultDTO> listBySkuList(List<SkuMappingDTO.ListingSkuParamDTO> listSkuParamList,String dictPlatform,String type);

    /**
     * 逻辑删除映射 SkuMappingEntity
     * @author Jim
     * @date 2023-12-25
     */
    void deleteAll(String id, ListingInfoEntity listingInfoEntity);

    PagingVO<OperateLogDTO.ListDTO> getLog(PagingDTO<BaseIdDTO> dto);


    List<ListingInfoWithSkuMappingDTO> listByErpSkuIdAndType(List<String> erpSkuIdList,String provideCode,String warehouseId,String shopId);

    List<ListingAdvanceQueryDTO> advanceQuerySku(AdvanceQueryContainer advanceQueryContainer);

    /**
     * 检查历史映射关系
     */
    void checkHistory(String id, String listingId, String shopId, String productSkuId);

    /**
     * 通过platformSkuNo查询关联关系
     *
     * @Author Jim
     * @since 2023-11-28
     * @param platformSkuList 平台SKU列表
     * @param dictPlatform 平台代码
     * @param shopId 店铺ID
     * @param platformOrderCreateTime 生效日期（查询所有=传空）
     * @param isExpire 是否过期（查询所有=传空）
     * @return Map<平台SKU, SKU映射和Listing列表>
     */
    Map<String, List<ListingInfoWithSkuMappingDTO>> mapListingByPlatformSkuNo(List<String> platformSkuList, List<String> platformSpuList, String dictPlatform, String shopId, LocalDateTime platformOrderCreateTime, Boolean isExpire);

    /**
     * 检查和获取映射关系
     *
     * @Author Jim
     * @since 2023-11-28
     **/
    ListingInfoWithSkuMappingDTO checkAndMappingDTO(List<ListingInfoWithSkuMappingDTO> mappingDTOList, String platformSpuNo, String dictPlatform);

    /**
     * 根据平台sku记录获取变更历史记录
     * @param id
     * @return
     */
    List<SkuMappingEntity> listHistoryByListingId(String id);
    List<SkuMappingDTO.WarehouseSkuDTO> listByWarehouseAndPlatformSku(String warehouseId,List<String> platformSkuNoList);

    PagingVO<SkuMappingDTO.PagingViewDTO> exportPlatformSku(PagingDTO<SkuMappingDTO.ExportDTO> dto);

    PagingVO<SkuMappingDTO.WarehousePagingViewDTO> exportWarehouseSku(PagingDTO<SkuMappingDTO.ExportWarehouseSkuDTO> dto);
    /**
     * 构建查询映射关系参数DTO
     * @param platformSkuList 平台sku列表
     * @param platformSpuList 平台spu列表
     * @param dictPlatform 销售平台
     * @param shopIdList   店铺ID列表
     * @param platformOrderCreateTime 平台订单创建时间=映射生效日期(空=按最新映射关系)
     * @param isExpire 映射是否已过期
     * @return 请求参数DTO
     */
    ListingInfoParamDTO constructDto(List<String> platformSkuList,
                                     List<String> platformSpuList,
                                     String dictPlatform,
                                     List<String> shopIdList,
                                     LocalDateTime platformOrderCreateTime,
                                     Boolean isExpire);

    void updateNotMatch(SkuMappingDTO.UpdateNotMatchDTO dto);

    PagingVO<SkuMappingDTO.SyncPlatformProductView> syncPlatformProductView(PagingDTO<AdvanceQueryContainer> advanceQueryDTO);

    PagingVO<SkuMappingDTO.SyncWarehouseProductView> syncWarehouseProductView(PagingDTO<AdvanceQueryContainer> advanceQueryDTO);

    List<SkuMappingDTO.ProductSkuInfoDTO> listSkuBySkuNos(SkuMappingDTO.SkuParamDTO skuParamDTO);

    List<BomChildrenSkuDTO> checkBomByPlatformSkuNos(SkuMappingDTO.SkuParamDTO skuParamDTO);
}
