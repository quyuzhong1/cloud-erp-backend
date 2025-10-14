package com.erp.server.oms.controller.api;


import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.AdvanceQueryContainer;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.validator.AddGroup;
import com.common.business.validator.UpdateGroup;
import com.common.business.validator.ValidList;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.enums.LogActionEnum;
import com.erp.model.dmp.dto.DmpPushTaskDTO;
import com.erp.model.oms.dto.OperateLogDTO;
import com.erp.model.oms.dto.SkuMappingDTO;
import com.erp.model.oms.entity.ListingInfoEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.entity.SkuMappingEntity;
import com.erp.model.oms.enums.RuleTypeEnum;
import com.erp.server.oms.service.ListingInfoService;
import com.erp.server.oms.service.ShopInfoService;
import com.erp.server.oms.service.SkuMappingRuleService;
import com.erp.server.oms.service.SkuMappingService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * SKU对照表管理
 *
 * @author Lambda
 * @since 2023-06-28
 */
@Slf4j
@RestController
@LogSystemModule("sku对照表")
@RequestMapping("/skuMaping")
public class SkuMappingController extends BaseController {


    @Resource
    private SkuMappingService skuMappingService;

    @Resource
    private SkuMappingRuleService skuMappingRuleService;
    @Resource
    private ListingInfoService listingInfoService;
    @Resource
    private ShopInfoService shopInfoService;

    /**
     * 获取 tab列表
     *
     * @return
     */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            shopTableField = "sm.shop_id",
            menuCode = "oms:skuMaping:platformPaging"
    )
    public ApiResult<List<SkuMappingDTO.TabListDTO>> tabList(@Validated @RequestBody SkuMappingDTO.FindTabDTO dto) {
        List<SkuMappingDTO.TabListDTO> list = skuMappingService.tabList(dto);
        return success(list);
    }

    
    
    /**
     * 添加库存对应sku
     * @author yl
     * @date 2023-08-18 16:31
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     */
    @PostMapping("/addWarehouseSku")
    @LogAction(value = LogActionEnum.INSERT, desc = "添加库存对应sku")
    public ApiResult add(@RequestBody @Validated SkuMappingDTO.AddWarehouseSkuDTO dto) {
        String id = skuMappingService.addWarehouseSku(dto);
        return StringUtils.isNotBlank(id) ? success() : failure();
    }


    /**
     * B2C平台分页列表
     *
     * @param dto
     * @return
     */
    @PostMapping("/platformPaging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            shopTableField = "sm.shop_id",
            menuCode = "oms:skuMaping:platformPaging"
    )
    @WebAdvanceQuery
    public ApiResult<PagingVO<SkuMappingDTO.PagingViewDTO>> queryByPage(@RequestBody @Validated PagingDTO<SkuMappingDTO.PagingParamDTO> dto) {
        PagingVO<SkuMappingDTO.PagingViewDTO> pagingVO = skuMappingService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 库存SKU 分页
     *
     * @param dto
     * @return
     */
    @PostMapping("/warehousePaging")
    @WebAdvanceQuery
    public ApiResult<PagingVO<SkuMappingDTO.WarehousePagingViewDTO>> queryWarehouseByPage(@RequestBody @Validated PagingDTO<SkuMappingDTO.WarehousePagingParamDTO> dto) {
        PagingVO<SkuMappingDTO.WarehousePagingViewDTO> pagingVO = skuMappingService.warehousePaging(dto);
        return success(pagingVO);
    }
    /**
     * 客户SKU 分页
     *
     * @param dto
     * @return
     */
    @PostMapping("/customerPaging")
    @WebAdvanceQuery
    public ApiResult<PagingVO<SkuMappingDTO.CustomerPagingViewDTO>> customerPaging(@RequestBody @Validated PagingDTO<SkuMappingDTO.CustomerPagingParamDTO> dto) {
        PagingVO<SkuMappingDTO.CustomerPagingViewDTO> pagingVO = skuMappingService.customerPaging(dto);
        return success(pagingVO);
    }

   /**
    * B2B平台分页列表
    * @author will
    * @date 2025/8/26 16:59
    * @param dto
    * @return ApiResult<PagingVO<PagingViewDTO>>
    */
    @PostMapping("/b2bPlatformPaging")
    @WebAdvanceQuery
    public ApiResult<PagingVO<SkuMappingDTO.PagingViewDTO>> b2bPlatformPaging(@RequestBody @Validated PagingDTO<SkuMappingDTO.PagingParamDTO> dto) {
        PagingVO<SkuMappingDTO.PagingViewDTO> pagingVO = skuMappingService.b2bPlatformPaging(dto);
        return success(pagingVO);
    }


    /**
     * 查看b2b平台sku同步
     * @author will
     * @date 2025/8/27 10:15
     * @return ApiResult<PagingVO<PagingViewDTO>>
     */
    @GetMapping("/viewB2bPlatformSyncSku")
    public ApiResult<DmpPushTaskDTO.LastPullDTO> viewB2bPlatformSyncSku() {
        DmpPushTaskDTO.LastPullDTO viewDTO = skuMappingService.viewB2bPlatformSyncSku();
        return success(viewDTO);
    }

    /**
     * b2b平台sku同步
     * @author will 
     * @date 2025/8/27 10:26
     * @param dto 
     * @return ApiResult<PagingViewDTO>
     */
    @PostMapping("/b2bPlatformSyncSku")
    public ApiResult<?> b2bPlatformSyncSku(@RequestBody @Validated SkuMappingDTO.SyncSkuDTO dto) {
        return success(skuMappingService.b2bPlatformSyncSku(dto));
    }


    /**
     * b2b平台sku列表导出
     * @author will
     * @date 2025/8/27 16:06
     * @param dto 
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出b2b的sku对照表")
    @PostMapping("/exportB2bPlatformSku")
    @WebAdvanceQuery
    public ApiResult exportB2bPlatformSku(@RequestBody @Valid SkuMappingDTO.ExportDTO dto) {
        Boolean result = skuMappingService.exportB2bPlatformSku(dto);
        return result ? success() : failure();
    }

    /**
     * 新增客户SKU
     *
     * @param dto
     * @return
     */
    @PostMapping("/addCustomer")
    @LogAction(value = LogActionEnum.INSERT, desc = "新增客户SKU")
    public ApiResult<String> addCustomer(@ModelAttribute @Validated(value = {AddGroup.class}) SkuMappingDTO.AddCustomerRequest dto) {
        return success(skuMappingService.addCustomer(dto));
    }
    /**
     * 更新客户SKU
     *
     * @param dto
     * @return
     */
    @PostMapping("/updateCustomer")
    @LogAction(value = LogActionEnum.UPDATE, desc = "更新客户SKU")
    public ApiResult<String> updateCustomer(@ModelAttribute @Validated(value = {UpdateGroup.class}) SkuMappingDTO.AddCustomerRequest dto) {
        return success(skuMappingService.updateCustomer(dto));
    }
    /**
     * 批量更新客户SKU标签
     */
    @PostMapping("/batchUpdateCustomerLabel")
    @LogAction(value = LogActionEnum.UPDATE, desc = "批量更新客户SKU标签")
    public ApiResult<List<BatchResultDTO>> batchUpdateCustomerLabel(@RequestBody @Validated ValidList<SkuMappingDTO.CustomerLabelDTO> dtoList) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dtoList.size());
        for (SkuMappingDTO.CustomerLabelDTO dto : dtoList){
            try {
                BatchResultDTO resultDTO = skuMappingService.updateCustomerLabel(dto);
                resultDTOS.add(resultDTO);
            }catch (Exception e){
                resultDTOS.add(BatchResultDTO.fail(dto.getPlatformSkuNo(),dto.getPlatformSkuNo(),e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }
    /**
     * 批量生成客户条码
     */
    @PostMapping("/batchGenerateCustomerLabel")
    @LogAction(value = LogActionEnum.CUSTOM_BATCH_UPDATE, desc = "批量生成客户条码")
    public ApiResult<List<BatchResultDTO>> batchGenerateCustomerLabel(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<SkuMappingEntity> mappingEntityList = skuMappingService.listByIds(dto.getIds());
        List<String> listingIds = mappingEntityList.stream().map(SkuMappingEntity::getListingId).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<ListingInfoEntity> listingInfoEntityList = listingInfoService.listByIds(listingIds);
        for (String id : dto.getIds()) {
            SkuMappingEntity skuMapping = mappingEntityList.stream().filter(e -> e.getId().equals(id)).findFirst().orElse(null);
            if (Objects.isNull(skuMapping)) {
                resultDTOS.add(BatchResultDTO.fail(id, id, "映射信息不存在"));
                continue;
            }
            ListingInfoEntity listingInfo = listingInfoEntityList.stream().filter(e -> e.getId().equals(skuMapping.getListingId())).findFirst().orElse(null);
            if (Objects.isNull(listingInfo)) {
                resultDTOS.add(BatchResultDTO.fail(id, skuMapping.getListingId(), "listing信息不存在"));
                continue;
            }
            try {
                BatchResultDTO resultDTO = skuMappingService.generateCustomerLabel(listingInfo, skuMapping);
                resultDTOS.add(resultDTO);
            } catch (Exception e) {
                resultDTOS.add(BatchResultDTO.fail(listingInfo.getPlatformSkuNo(), listingInfo.getLabelUrl(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 导入平台sku对照表
     *
     * @return
     */
    @PostMapping("/importFile")
    @LogAction(value = LogActionEnum.IMPORT, desc = "导入平台sku对照表")
    public ApiResult importExcel(@RequestParam(value = "excelFile") MultipartFile excelFile, @RequestParam(value = "type") String type,HttpServletResponse response) {
        Boolean result = skuMappingService.importExcel(excelFile,type, response);
        return result ? success() : failure();
    }


    /**
     * 下载平台sku对照模板
     *
     * @return
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "下载模板sku对照表")
    @GetMapping("/downloadTemplate")
    public ApiResult downloadTemplate(@RequestParam(value = "importType") String type, HttpServletResponse response) {
        skuMappingService.downloadTemplate(type,response);
        return success();
    }



    /**
     * 导出平台sku 对照表
     *
     * @return
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出sku对照表")
    @PostMapping("/exportPlatformSku")
    @WebAdvanceQuery
    public ApiResult exportPlatformSku(@RequestBody @Valid SkuMappingDTO.ExportDTO dto) {
        Boolean result = skuMappingService.exportPlatformSku(dto);
        return result ? success() : failure();
    }

    /**
     * 导出库存sku 对照表
     *
     * @return
     */
    @PostMapping("/exportWarehouseSku")
    @WebAdvanceQuery
    public ApiResult exportWarehouseSku(@RequestBody @Valid SkuMappingDTO.ExportWarehouseSkuDTO dto) {
        Boolean result = skuMappingService.exportWarehouseSku(dto);
        return result ? success() : failure();
    }
    /**
     * 导出客户sku 对照表
     *
     * @return
     */
    @PostMapping("/exportCustomerSku")
    @WebAdvanceQuery
    public ApiResult exportCustomerSku(@RequestBody @Valid SkuMappingDTO.CustomerPagingParamDTO dto) {
        Boolean result = skuMappingService.exportCustomerSku(dto);
        return result ? success() : failure();
    }

    /**
     * 更改库存sku 对照表
     *
     * @return
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "更改sku对照表:id={id},产品sku={productSkuId}")
    @PostMapping("/updateWarehouseSku")
    public ApiResult updateWarehouseSku(@RequestBody @Valid SkuMappingDTO.UpdateWarehouseSkuDTO dto) {
        String id = skuMappingService.updateWarehouseSku(dto);
        return StringUtils.isNotBlank(id) ? success() : failure();
    }

    /**
     * 更改平台 对照表
     *
     * @return
     */
    @PostMapping("/updatePlatformSku")
    public ApiResult<List<BatchResultDTO>> updatePlatformSku(@RequestBody @Valid SkuMappingDTO.UpdatePlatformDTO dto) {
        List<BatchResultDTO> resultDTOList = new ArrayList<>();
        //通账号同平台SKU批量更新
        String id = dto.getId();
        SkuMappingEntity skuMapping = skuMappingService.getById(id);
        if (Objects.isNull(skuMapping)) {
            resultDTOList.add(BatchResultDTO.fail(id,id,ApiError.ERROR_92051.msg));
            return failure(resultDTOList);
        }
        ListingInfoEntity listing = listingInfoService.getById(skuMapping.getListingId());
        if (Objects.isNull(listing)) {
            resultDTOList.add(BatchResultDTO.fail(id,skuMapping.getListingId(),"listing记录不存在"));
            return failure(resultDTOList);
        }
        String shopId = dto.getShopId();
        ShopInfoEntity shopInfo = shopInfoService.getById(shopId);
        if (Objects.isNull(shopInfo) && !RuleTypeEnum.B2B_PLATFORM.getCode().equals(listing.getType())) {
            resultDTOList.add(BatchResultDTO.fail(id,id,"店铺信息不存在"));
            return failure(resultDTOList);
        }
        String name = ObjectUtil.isEmpty(shopInfo) ? "" : shopInfo.getName();

        try {
            BatchResultDTO resultDTO = skuMappingService.updatePlatformSku(dto,skuMapping,listing,shopId);
            resultDTOList.add(resultDTO);
        }catch (Exception e){
            resultDTOList.add(BatchResultDTO.fail(skuMapping.getId(),skuMapping.getId(),CharSequenceUtil.format("店铺【{}】更新异常:{}", name,e.getMessage())));
        }
        return resultDTOList.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOList) : failure(resultDTOList);
    }
 
   /**
    * 对应销售订单 添加客户sku
    * @author yl
    * @date 2023-07-01 9:13
    * @param dto
    * @return com.common.core.controller.vo.ApiResult<com.common.business.vo.PagingVO<com.erp.model.oms.dto.SkuMapingDTO.ProductSkuInfoDTO>>
    */
    @PostMapping("/list")
    public ApiResult<PagingVO<SkuMappingDTO.ProductSkuInfoDTO>> list(@RequestBody @Validated PagingDTO<SkuMappingDTO.ListParamDTO> dto) {
        PagingVO<SkuMappingDTO.ProductSkuInfoDTO> pagingVO = skuMappingService.listPaging(dto);
        return success(pagingVO);
    }


    /**
     * 根据sku集合查询库存sku
     * @author Will
     * @date: 2023/8/24 19:13
     * @param list
     * @return ApiResult<List<ListSkuDTO>>
     */
    @PostMapping("/listBySkuNoList")
    public ApiResult<List<SkuMappingDTO.ListSkuDTO>> listBySkuNoList(@RequestBody @Validated ValidList<SkuMappingDTO.ListSkuParamDTO> list) {
        List<SkuMappingDTO.ListSkuDTO> resultList = skuMappingService.listBySkuNoList(list.getList());
        return success(resultList);
    }

    /**
     * 根据产品sku查询库存sku
     * @Author Luo_WG
     * @Date 2023/11/2 17:24
     * @param productSkuIdList
     * @return java.util.List<com.erp.model.oms.dto.SkuMappingDTO.listStockSkuNoByProductSkuNoView>
     **/
    @PostMapping("/listStockSkuNoByProductSkuIds")
    public ApiResult<List<SkuMappingDTO.ListStockSkuNoByProductSkuIdView>> listStockSkuNoByProductSkuIds(@RequestBody ValidList<String> productSkuIdList) {
        List<SkuMappingDTO.ListStockSkuNoByProductSkuIdView> list = skuMappingService.listStockSkuNoByProductSkuIds(productSkuIdList.getList());
        return success(list);
    }

    /**
     * 删除
     * @author Jim
     * @date:  2023-12-20
     * @param dto ids
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/delete")
    @LogAction(value = LogActionEnum.DELETE, desc = "SKU映射删除")
    public ApiResult<List<BatchResultDTO>> delete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = skuMappingService.delete(id);
            }catch (Exception e){
                log.error("SKU映射删除失败:{}", e.getMessage());
                SkuMappingEntity entity = skuMappingService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "SKU映射不存在, 删除失败");
                    resultDTOS.add(deleteResult);
                    continue;
                }
                deleteResult = BatchResultDTO.fail(entity.getId(), entity.getProductSkuNo(), e.getMessage());
            }
            resultDTOS.add(deleteResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }


    /**
     * 执行自动匹配规则
     */
    @PostMapping("/autoMatch")
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "执行自动匹配规则")
    public ApiResult<?> autoMatch(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        skuMappingRuleService.handleSkuMapping(dto.getIds());
        return success();
    }


    /**
     * 查询操作日志
     *
     * @return
     */
    @PostMapping("/getLog")
    public ApiResult<PagingVO<OperateLogDTO.ListDTO>> getLog(@RequestBody @Validated PagingDTO<BaseIdDTO.SearchDTO> dto) {
        PagingVO<OperateLogDTO.ListDTO> pagingVO = skuMappingService.getLog(dto);
        return success(pagingVO);
    }

    /**
     * 根据平台sku记录获取变更历史记录
     * @param dto
     * @return
     */
    @PostMapping("/listHistoryByListingId")
    public ApiResult<List<SkuMappingEntity>> listHistoryByListingId(@RequestBody @Validated BaseIdDTO dto){
        List<SkuMappingEntity> list = skuMappingService.listHistoryByListingId(dto.getId());
        return success(list);
    }
    /**
     * 无需匹配
     * @param dto
     * @return
     */
    @PostMapping("/updateNotMatch")
    public ApiResult<Boolean> updateNotMatch(@RequestBody @Validated SkuMappingDTO.UpdateNotMatchDTO dto){
        skuMappingService.updateNotMatch(dto);
        return success();
    }

    /**
     * 平台同步平台商品view
     * @return
     */
    @PostMapping("/syncPlatformProductView")
    @WebAdvanceQuery
    public ApiResult<PagingVO<SkuMappingDTO.SyncPlatformProductView>> syncPlatformProductView(@RequestBody PagingDTO<AdvanceQueryContainer> advanceQueryDTO){
        return success(skuMappingService.syncPlatformProductView(advanceQueryDTO));
    }

    /**
     * 仓库同步平台商品view
     * @return
     */
    @PostMapping("/syncWarehouseProductView")
    @WebAdvanceQuery
    public ApiResult<PagingVO<SkuMappingDTO.SyncWarehouseProductView>> syncWarehouseProductView(@RequestBody PagingDTO<AdvanceQueryContainer> advanceQueryDTO){
        return success(skuMappingService.syncWarehouseProductView(advanceQueryDTO));
    }
    /**
     * 平台同步平台商品
     * @return
     */
    @PostMapping("/syncPlatformProduct")
    public ApiResult<Boolean> syncPlatformProduct(@RequestBody @Validated BaseIdsDTO.IdsDTO dto){
        skuMappingService.syncPlatformProduct(dto.getIds());
        return success();
    }

    /**
     * 仓库同步平台商品
     * @return
     */
    @PostMapping("/syncWarehouseProduct")
    public ApiResult<Boolean> syncWarehouseProduct(@RequestBody @Validated BaseIdsDTO.IdsDTO dto){
        skuMappingService.syncWarehouseProduct(dto.getIds());
        return success();
    }
    /**
     * 根据customerId和skuno 关联查询平台sku
     * @author jack
     * @date: 2024-11-07
     * @param skuParamDTO
     * @return ApiResult<List<ProductDetailShowDTO>>
     */
    @PostMapping("/listSkuBySkuNos")
    public ApiResult<List<SkuMappingDTO.ProductSkuInfoDTO>> listSkuBySkuNos(@RequestBody SkuMappingDTO.SkuParamDTO skuParamDTO) {
        return this.success(skuMappingService.listSkuBySkuNos(skuParamDTO));
    }

    /**
     * 填写客户sku返回匹配的erp sku 和对应的实体仓实际库存-虚拟仓冻结库存
     */
    @PostMapping("/getErpSkuByCustomerSku")
    public ApiResult<List<SkuMappingDTO.CustomerInventorySkuInfoDTO>> getErpSkuByCustomerSku(@RequestBody @Validated SkuMappingDTO.CustomerInventorySkuParamDTO skuParamDTO) {
        return this.success(skuMappingService.getErpSkuByCustomerSku(skuParamDTO));
    }


    /**
     * 推送商品
     */
    @PostMapping("/pushProduct")
    public ApiResult<List<BatchResultDTO>> pushProduct(@RequestBody @Validated BaseIdsDTO.IdsDTO dto ) {
        List<BatchResultDTO> resultDTOS = skuMappingService.pushProduct(dto.getIds());
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

//    /**
//     * 根据customerId和平台sku 查询是否存在套装bom
//     * @author jack
//     * @date: 2024-11-07
//     * @param skuParamDTO
//     * @return ApiResult<List<BomChildrenSkuDTO>>
//     */
//    @PostMapping("/checkBomByPlatformSkuNos")
//    public ApiResult<List<BomChildrenSkuDTO>> checkBomByPlatformSkuNos(@RequestBody SkuMappingDTO.SkuParamDTO skuParamDTO) {
//        return this.success(skuMappingService.checkBomByPlatformSkuNos(skuParamDTO));
//    }
}
