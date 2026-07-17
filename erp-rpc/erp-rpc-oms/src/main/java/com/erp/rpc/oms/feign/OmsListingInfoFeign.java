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
     * 同步三方仓SKU到未匹配对照表，并对本次传入 SKU 中源端状态非启用的已映射记录置为禁用。
     * 判断仅依赖本次传入 SKU 各自的状态，不要求携带完整快照，可按任意批次调用；
     * 未回传状态的调用方（如WEGO）不会触发禁用分支。
     * 禁用后不会自动恢复启用，需人工在SKU对照表页面处理。
     *
     * @param dto 三方仓 SKU 同步参数（服务商、平台、本次批次的 SKU 及其可选的源端状态）
     * @return 本次处理统计结果（新增/禁用数量）
     */
    @PostMapping("feign/listing/syncWarehouseNotMatchSku")
    WegoSkuSyncDTO.ReconcileResultDTO syncWarehouseNotMatchSku(@RequestBody @Validated WegoSkuSyncDTO.SyncReqDTO dto);
}
