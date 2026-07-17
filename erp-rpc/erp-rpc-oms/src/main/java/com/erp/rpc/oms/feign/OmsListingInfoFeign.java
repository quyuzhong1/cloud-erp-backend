package com.erp.rpc.oms.feign;

import com.common.business.config.FeignErrorDecoder;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.ListingInfoDTO;
import com.erp.model.oms.dto.ListingInfoParamDTO;
import com.erp.model.oms.dto.ListingInfoWithSkuMappingDTO;
import com.erp.model.oms.dto.SkuMappingDTO;
import com.erp.model.oms.entity.ListingInfoEntity;
import com.erp.model.wms.dto.FbaShipmentDTO;
import com.erp.model.wms.dto.WegoSkuSyncDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "erp-oms", contextId = "omsListingInfoFeign",configuration = {FeignErrorDecoder.class})
public interface OmsListingInfoFeign {

    /**
     * 根据产品sku查询库存sku
     * @Author Luo_WG
     * @Date 2023/11/2 17:24
     * @param productSkuIdList
     * @return java.util.List<com.erp.model.oms.dto.SkuMappingDTO.listStockSkuNoByProductSkuNoView>
     **/
    @PostMapping("feign/listing/listStockSkuNoByProductSkuIds")
    List<SkuMappingDTO.ListStockSkuNoByProductSkuIdView> listStockSkuNoByProductSkuIds(@RequestBody List<String> productSkuIdList);

    /**
     * sku映射
     * @Author Luo_WG
     * @Date 2023/11/2 11:19
     * @param dto
     * @return java.lang.Boolean
     **/
    @PostMapping("feign/listing/skuMapping")
    Boolean skuMapping(@RequestBody @Validated FbaShipmentDTO.SkuMappingParamDTO dto);


    /**
     * 检查和更新FnSku
     **/
    @PostMapping("feign/listing/checkAndUpdateFnsku")
    List<ListingInfoWithSkuMappingDTO> checkAndUpdateFnsku(@RequestBody @Validated ListingInfoParamDTO dto);

    /**
     * 根据参数查询sku映射记录
     * @Author zdy
     * @Date 2024/11/25 17:24
     * @param queryDTO
     * @return java.util.List<com.erp.model.oms.dto.SkuMappingDTO.SkuMappingViewDTO>
     **/
    @PostMapping("feign/listing/listSkuMappingByParams")
    public List<SkuMappingDTO.SkuMappingViewDTO> listSkuMappingByParams(@RequestBody ListingInfoDTO.QueryDTO queryDTO);

    /**
     * listing 分页
     **/
    @PostMapping("feign/listing/paging")
    PagingVO<ListingInfoDTO.PageDTO> paging(@RequestBody @Validated PagingDTO<ListingInfoDTO.PagingParamDTO> dto);

    /**
     * 根据参数查询sku映射记录
     * @Author zdy
     * @Date 2024/11/25 17:24
     * @param queryDTO
     * @return java.util.List<com.erp.model.oms.dto.SkuMappingDTO.SkuMappingViewDTO>
     **/
    @PostMapping("feign/listing/listInfoByPlatformSkuNo")
    List<ListingInfoEntity> listInfoByPlatformSkuNo(@RequestBody ListingInfoDTO.QueryDTO queryDTO);

    @PostMapping("feign/listing/updatePlatformSkuId")
    boolean updatePlatformSkuId(@RequestParam(value = "listingId") String listingId, @RequestParam(value = "platformSkuId") String platformSkuId);

    /**
     * 同步三方仓SKU到未匹配对照表
     *
     * @param dto 三方仓 SKU 同步参数
     * @return 本次新增未匹配记录数量
     */
    @PostMapping("feign/listing/syncWarehouseNotMatchSku")
    Integer syncWarehouseNotMatchSku(@RequestBody @Validated WegoSkuSyncDTO.SyncReqDTO dto);

    /**
     * 三方仓SKU全量快照回收：新增/更新未匹配对照表，并删除源端已消失的未映射记录，
     * 禁用源端已消失或已停用的已映射记录。禁用后不会自动恢复启用，需人工在SKU对照表页面处理。
     *
     * @param dto 三方仓 SKU 全量快照（服务商、平台、本次拉取到的全部 SKU 及其源端状态）
     * @return 本次回收统计结果（新增/删除/禁用数量）
     */
    @PostMapping("feign/listing/reconcileWarehouseSkuSnapshot")
    WegoSkuSyncDTO.ReconcileResultDTO reconcileWarehouseSkuSnapshot(@RequestBody @Validated WegoSkuSyncDTO.SyncReqDTO dto);
}
