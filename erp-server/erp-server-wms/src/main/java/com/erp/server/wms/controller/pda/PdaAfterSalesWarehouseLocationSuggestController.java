package com.erp.server.wms.controller.pda;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.wms.dto.AfterSalePackDTO;
import com.erp.model.wms.dto.AfterSalesWarehouseLocationSuggestDto;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.server.wms.service.AfterSalePackService;
import com.erp.server.wms.service.AfterSalesWarehouseLocationSuggestService;
import com.erp.server.wms.service.PdaAfterSalesWarehouseMoveService;
import com.erp.server.wms.service.WarehouseService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

/**
 * PDA售后推荐仓位管理
 * @author liuchao
 * @date 2026/05/09
 */
@RestController
@RequestMapping("/pdaAfterSalesWarehouseLocationSuggest")
@LogSystemModule("PDA售后推荐仓位管理")
public class PdaAfterSalesWarehouseLocationSuggestController {

    @Resource
    private AfterSalesWarehouseLocationSuggestService afterSalesWarehouseLocationSuggestService;

    @Resource
    private WarehouseService warehouseService;

    @Resource
    private PdaAfterSalesWarehouseMoveService pdaAfterSalesWarehouseMoveService;

    @Resource
    private AfterSalePackService afterSalePackService;

    /**
     * 按箱唛号查询售后装箱详情（SKU、来源仓位等），与 {@link com.erp.server.wms.service.impl.AfterSalePackServiceImpl#viewByCode(String)} 一致
     */
    @PostMapping("/queryBoxByLabel")
    public ApiResult<AfterSalePackDTO.ViewDTO> queryBoxByLabel(
            @RequestBody @Validated AfterSalesWarehouseLocationSuggestDto.BoxLabelQueryRequestDto dto) {
        return ApiResult.success(afterSalePackService.viewByCode(CharSequenceUtil.trim(dto.getBoxLabelCode())));
    }

    /**
     * 货品上架：单 SKU + 数量，源仓位可为空仓位，目标仓位扫码录入；生成仓位移动已审核单并写库存流水
     */
    @LogAction(value = LogActionEnum.INSERT, desc = "售后PDA货品上架")
    @PostMapping("/submitGoodsShelving")
    public ApiResult<String> submitGoodsShelving(@RequestBody @Validated AfterSalesWarehouseLocationSuggestDto.PdaGoodsShelvingSubmitDto dto) {
        return ApiResult.success(pdaAfterSalesWarehouseMoveService.submitGoodsShelving(dto));
    }

    /**
     * 整箱移仓提交：前端累计箱唛查询返回的 sku 展平列表 + 目标仓位；提交前再次按箱唛校验 usageStatus，并校验即时库存可用量
     */
    @LogAction(value = LogActionEnum.INSERT, desc = "售后PDA整箱移仓")
    @PostMapping("/submitFullBoxTransfer")
    public ApiResult<String> submitFullBoxTransfer(@RequestBody @Validated AfterSalesWarehouseLocationSuggestDto.PdaFullBoxTransferSubmitDto dto) {
        return ApiResult.success(pdaAfterSalesWarehouseMoveService.submitFullBoxTransfer(dto));
    }

    /**
     * 根据sku编码获取售后推荐仓位列表
     * @param dto param
     * @return result
     * @author liuchao
     * @date 2026/05/12
     */
    @PostMapping("/getSuggestWarehouseLocationListBySkuNoAndWarehouseInfo")
    public ApiResult<List<AfterSalesWarehouseLocationSuggestDto.PdaListDto>> getSuggestWarehouseLocationListBySkuNoAndWarehouseInfo(@RequestBody AfterSalesWarehouseLocationSuggestDto.PdaSearchDto dto) {
        //当前只有一个仓库 【东莞售后仓库】
        List<WarehouseDTO.ListDTO> dtos = warehouseService.listByNames(Collections.singletonList("东莞售后仓库"));
        if (CollUtil.isEmpty(dtos)){
            throw new ServiceException("请确保存在仓库名称的默认值【东莞售后仓库】的仓库");
        }else if (dtos.size() > 1){
            throw new ServiceException("请确保存在仓库名称的默认值【东莞售后仓库】的仓库数量为1");
        }
        dto.setWarehouseId(dtos.get(0).getId());
        //当前只有一个仓位 【空仓位】 默认code为空
        dto.setWarehouseLocationCode("");
        //
        List<AfterSalesWarehouseLocationSuggestDto.PdaListDto> list  = afterSalesWarehouseLocationSuggestService.getSuggestWarehouseLocationList(dto);
        return ApiResult.success(list);
    }
}
