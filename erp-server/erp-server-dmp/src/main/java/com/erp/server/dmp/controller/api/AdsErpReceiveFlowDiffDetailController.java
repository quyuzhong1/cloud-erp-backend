package com.erp.server.dmp.controller.api;


import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.dmp.dto.AdsErpReceiveFlowDiffDetailDTO;
import com.erp.server.dmp.service.AdsErpReceiveFlowDiffService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 签收流水差异表（溯源）
 * @author will
 * @date 2026/3/9 11:43
 */
@Slf4j
@RestController
@LogSystemModule("ERP签收流水差异表")
@RequestMapping("/adsErpReceiveFlowDiffDetail")
public class AdsErpReceiveFlowDiffDetailController extends BaseController {

    @Resource
    private AdsErpReceiveFlowDiffService adsErpReceiveFlowDiffService;


    /**
     * 查询朔源信息分页（直接调拨单）
     * @author will
     * @date 2026/2/4 11:30
     * @param dto
     * @return ApiResult<PagingVO<PagingDTO>>
     */
    @PostMapping("/transferInfoPaging")
    public ApiResult<PagingVO<AdsErpReceiveFlowDiffDetailDTO.SourceTransferInfoDTO>> transferInfoPaging(@RequestBody @Validated PagingDTO<AdsErpReceiveFlowDiffDetailDTO.PagingParamDTO> dto) {
        return success(adsErpReceiveFlowDiffService.transferInfoPaging(dto));
    }

    /**
     * 导出朔源信息（直接调拨单）
     * @author will
     * @date 2026/2/5 09:18
     * @param dto
     * @return ApiResult<Object>
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出朔源信息")
    @PostMapping(value = "/exportTransferInfo")
    public ApiResult<Object>exportTransferInfo(@RequestBody AdsErpReceiveFlowDiffDetailDTO.PagingParamDTO dto) {
        Boolean flag = adsErpReceiveFlowDiffService.exportTransferInfo(dto);
        return flag == true ? success() : failure();
    }


    /**
     * 查询朔源信息分页（库存流水）
     * @author will
     * @date 2026/2/4 11:30
     * @param dto
     * @return ApiResult<PagingVO<PagingDTO>>
     */
    @PostMapping("/sourcePlatformFlowPaging")
    public ApiResult<PagingVO<AdsErpReceiveFlowDiffDetailDTO.SourcePlatformFlowDTO>> sourcePlatformFlowPaging(@RequestBody @Validated PagingDTO<AdsErpReceiveFlowDiffDetailDTO.PagingParamDTO> dto) {
        return success(adsErpReceiveFlowDiffService.sourcePlatformFlowPaging(dto));
    }

    /**
     * 导出朔源信息（库存流水）
     * @author will
     * @date 2026/2/5 09:18
     * @param dto
     * @return ApiResult<Object>
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出朔源信息")
    @PostMapping(value = "/exportPlatformFlow")
    public ApiResult<Object>exportPlatformFlow(@RequestBody AdsErpReceiveFlowDiffDetailDTO.PagingParamDTO dto) {
        Boolean flag = adsErpReceiveFlowDiffService.exportPlatformFlow(dto);
        return flag == true ? success() : failure();
    }
}
