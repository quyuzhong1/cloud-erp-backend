package com.erp.server.wms.controller.pda;

import cn.hutool.core.collection.CollUtil;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.dto.AfterSalesWarehouseLocationSuggestDto;
import com.erp.server.wms.service.AfterSalesWarehouseLocationSuggestService;
import com.erp.server.wms.service.WarehouseService;
import org.springframework.web.bind.annotation.GetMapping;
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


    @GetMapping("/getSuggestWarehouseLocationListBySkuNoAndWarehouseInfo")
    public ApiResult<List<AfterSalesWarehouseLocationSuggestDto.PdaListDto>> getSuggestWarehouseLocationListBySkuNoAndWarehouseInfo(AfterSalesWarehouseLocationSuggestDto.PdaSearchDto dto) {
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
