package com.erp.server.oms.controller.api;


import cn.hutool.core.util.ObjectUtil;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.validator.ValidList;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.oms.dto.SkuMappingDTO;
import com.erp.model.oms.entity.SkuMappingEntity;
import com.erp.model.scm.dto.OperateLogDTO;
import com.erp.model.workflow.dto.ProcessManagementDTO;
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


    /**
     * 获取 tab列表
     *
     * @return
     */
    @PostMapping("/tabList")
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
    public ApiResult add(@RequestBody @Validated SkuMappingDTO.AddWarehouseSkuDTO dto) {
        String id = skuMappingService.addWarehouseSku(dto);
        return StringUtils.isNotBlank(id) ? success() : failure();
    }


    /**
     * 平台分页列表
     *
     * @param dto
     * @return
     */
    @PostMapping("/platformPaging")
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
    public ApiResult<PagingVO<SkuMappingDTO.WarehousePagingViewDTO>> queryWarehouseByPage(@RequestBody @Validated PagingDTO<SkuMappingDTO.WarehousePagingParamDTO> dto) {
        PagingVO<SkuMappingDTO.WarehousePagingViewDTO> pagingVO = skuMappingService.warehousePaging(dto);
        return success(pagingVO);
    }


    /**
     * 导入平台sku对照表
     *
     * @return
     */
    @PostMapping("/importFile")
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
    public ApiResult exportPlatformSku(@RequestBody @Valid SkuMappingDTO.ExportDTO dto, HttpServletResponse response) {
        Boolean result = skuMappingService.exportPlatformSku(dto, response);
        return result ? success() : failure();
    }

    /**
     * 导出库存sku 对照表
     *
     * @return
     */
    @PostMapping("/exportWarehouseSku")
    public ApiResult exportWarehouseSku(@RequestBody @Valid SkuMappingDTO.ExportWarehouseSkuDTO dto, HttpServletResponse response) {
        Boolean result = skuMappingService.exportWarehouseSku(dto, response);
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
    public ApiResult updatePlatformSku(@RequestBody @Valid SkuMappingDTO.UpdatePlatformDTO dto) {
        String id = skuMappingService.updatePlatformSku(dto);
        return StringUtils.isNotBlank(id) ? success() : failure();
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
    @GetMapping("/autoMatch")
    public ApiResult<?> autoMatch() {
        skuMappingRuleService.handleSkuMapping();
        return success();
    }


    /**
     * 查询操作日志
     *
     * @return
     */
    @PostMapping("/getLog")
    public ApiResult<PagingVO<OperateLogDTO.ListDTO>> getLog(@RequestBody @Validated PagingDTO<BaseIdDTO> dto) {
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
}
