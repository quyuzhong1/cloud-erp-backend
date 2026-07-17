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
import com.erp.model.wms.dto.WegoSkuSyncDTO;
import com.erp.wms.aliexpress.model.product.AliexpressProductDTO;
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
     *
     * @param warehouseSkuNo
     * @param warehouseProductName
     * @param thirdBarcode
     * @param authId
     * @param platform
     * @return
     */
    String addWarehouseSku(String warehouseSkuNo, String warehouseProductName, String thirdBarcode, String authId, String platform,String platformStatus);

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
    List<ListingInfoEntity> listByAuth(String type,List<String> platformSkuNoList,List<String> authIdList);
    List<ListingInfoEntity> listByParam(String type,String platform,List<String> skuNoList);
    /**
     * 根据平台集合、sku集合查询
     * @author will
     * @date 2025/4/8 14:48
     * @param type
     * @param platformList
     * @param platformSkuNoList
     * @return List<ListingInfoEntity>
     */
    List<ListingInfoEntity> listByParams(String type,List<String> platformList,List<String> platformSkuNoList);

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


    PagingVO<ListingInfoDTO.PageDTO> paging(PagingDTO<ListingInfoDTO.PagingParamDTO> dto);

    Boolean warehouseSkuMapping(ListingInfoDTO.WarehouseSkuMappingParamDTO dto);

    void saveBatchImport(List<ListingInfoEntity> addListingInfoEntityList, List<SkuMappingEntity> updateSkuMappingList, List<ListingInfoEntity> updateListingInfoList, List<SkuMappingEntity> addSkuMappingList,List<Pair<String, String>> addLogPairList, List<Pair<String, String>> updateLogPairList);
    void handleAliExpress(SkuMappingEntity lastestSkuMapping, SkuVO skuVO, ListingInfoEntity listingInfoEntity);

    /**
     * 检查和更新FnSku
     */
    List<ListingInfoWithSkuMappingDTO> checkAndUpdateFnsku(ListingInfoParamDTO dto);

    List<ListingInfoDTO.SearchResultDTO> searchByKey(ListingInfoDTO.SearchParamDTO dto);

    List<ListingInfoEntity> listByAuthIds(List<String> authIds);

    void updateLabelInfo(String id, String labelUrl, String labelSourceType, String labelFileName);

    List<ListingInfoEntity> listInfoByPlatformSkuNo(ListingInfoDTO.QueryDTO queryDTO);

    AliexpressProductDTO convertAliexpressProductDTO(ListingInfoEntity productDetailEntity);

    boolean updatePlatformSkuId(String listingId, String platformSkuId);

    /**
     * 同步三方仓SKU到未匹配对照表，并按源端状态做禁用回收：
     * 除新增/更新未匹配对照表外，对本次传入 SKU 中源端状态非启用（{@code SkuItemDTO.status} 非 active）的记录，
     * 若已存在启用中的映射关系（{@code matchResult=TRUE}），置为禁用。
     * <p>
     * 禁用判断仅依赖本次传入 SKU 各自携带的状态，不需要"完整快照"用于比对是否有 SKU 消失
     * （三方仓通常会把已下架/停用的 SKU 继续保留在拉取结果里，只是状态变化，不会整条消失），
     * 因此可以按任意批次（不要求携带全量数据）调用，调用方无需为了触发禁用而单独攒批。
     * 未回传状态的调用方（如历史 WEGO 调用不传 {@code status}）不会触发禁用分支，行为与此前一致。
     * <p>
     * 禁用后不会被本方法自动重新置为启用，需人工在 SKU 对照表页面手动恢复。
     * 平台无关实现（按 authId 维度处理），本轮仅接入爱亚，后续其它三方仓可直接复用。
     *
     * @param dto 三方仓 SKU 同步参数（服务商、平台、本次批次的 SKU 及其可选的源端状态）；
     *            {@code skuList} 为空时跳过处理
     * @return 本次处理统计结果（新增/禁用数量）
     */
    WegoSkuSyncDTO.ReconcileResultDTO syncWarehouseNotMatchSku(WegoSkuSyncDTO.SyncReqDTO dto);
}
