package com.erp.server.dmp.controller.api;


import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.dmp.dto.AdsErpInventoryDiffFlowDetailDTO;
import com.erp.server.dmp.service.AdsErpInventoryDiffFlowService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 平台仓流水差异朔源表
 * @author will
 * @date 2026/2/5 09:23
 */
@Slf4j
@RestController
@LogSystemModule("平台仓流水差异朔源表")
@RequestMapping("/adsErpInventoryDiffFlowDetail")
public class AdsErpInventoryDiffFlowDetailController extends BaseController {

    @Resource
    private AdsErpInventoryDiffFlowService adsErpInventoryDiffFlowService;

    /**
     * 查询朔源信息分页（每日库存）
     * @author will
     * @date 2026/2/4 11:30
     * @param dto
     * @return ApiResult<PagingVO<PagingDTO>>
     */
    @PostMapping("/sourcePlatformPaging")
    public ApiResult<PagingVO<AdsErpInventoryDiffFlowDetailDTO.SourcePlatformDTO>> sourcePlatformPaging(@RequestBody @Validated PagingDTO<AdsErpInventoryDiffFlowDetailDTO.PagingParamDTO> dto) {
        return success(adsErpInventoryDiffFlowService.sourcePlatformPaging(dto));
    }

    /**
     * 导出朔源信息（每日库存）
     * @author will
     * @date 2026/2/5 09:18
     * @param dto
     * @return ApiResult<Object>
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出朔源信息")
    @PostMapping(value = "/exportSourcePlatform")
    public ApiResult<Object>exportSourcePlatform(@RequestBody AdsErpInventoryDiffFlowDetailDTO.PagingParamDTO dto) {
        Boolean flag = adsErpInventoryDiffFlowService.exportSourcePlatform(dto);
        return flag == true ? success() : failure();
    }


    /**
     * 查询朔源信息分页（库存流水）
     * @author will
     * @date 2026/2/4 11:30
     * @param dto
     * @return ApiResult<PagingVO<PagingDTO>>
     */
    @PostMapping("/sourceSelfPaging")
    public ApiResult<PagingVO<AdsErpInventoryDiffFlowDetailDTO.SourceSelfDTO>> sourceSelfPaging(@RequestBody @Validated PagingDTO<AdsErpInventoryDiffFlowDetailDTO.PagingParamDTO> dto) {
        return success(adsErpInventoryDiffFlowService.sourceSelfPaging(dto));
    }

    /**
     * 导出朔源信息（库存流水）
     * @author will
     * @date 2026/2/5 09:18
     * @param dto
     * @return ApiResult<Object>
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出朔源信息")
    @PostMapping(value = "/exportSourceSelf")
    public ApiResult<Object>exportSourceSelf(@RequestBody AdsErpInventoryDiffFlowDetailDTO.PagingParamDTO dto) {
        Boolean flag = adsErpInventoryDiffFlowService.exportSourceSelf(dto);
        return flag == true ? success() : failure();
    }
}
