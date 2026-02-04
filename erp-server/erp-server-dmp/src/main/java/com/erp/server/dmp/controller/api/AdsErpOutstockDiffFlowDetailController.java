package com.erp.server.dmp.controller.api;

import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.dmp.dto.AdsErpOutstockDiffFlowDetailDTO;
import com.erp.server.dmp.service.AdsErpOutstockDiffFlowService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Slf4j
@RestController
@LogSystemModule("第三方仓出库单据差异明细表")
@RequestMapping("/adsErpOutstockDiffDetailFlow")
public class AdsErpOutstockDiffFlowDetailController extends BaseController {

    @Resource
    private AdsErpOutstockDiffFlowService adsErpOutstockDiffFlowService;

    /**
     * 查询朔源信息分页
     * @author will
     * @date 2026/2/4 11:30
     * @param dto
     * @return ApiResult<PagingVO<PagingDTO>>
     */
    @PostMapping("/sourceSelfPaging")
    public ApiResult<PagingVO<AdsErpOutstockDiffFlowDetailDTO.SourceSelfDTO>> sourceSelfPaging(@RequestBody @Validated PagingDTO<AdsErpOutstockDiffFlowDetailDTO.PagingParamDTO> dto) {
        return success(adsErpOutstockDiffFlowService.sourceSelfPaging(dto));
    }

    /**
     * 查询朔源信息分页
     * @author will
     * @date 2026/2/4 11:30
     * @param dto
     * @return ApiResult<PagingVO<PagingDTO>>
     */
    @PostMapping("/sourcePlatformPaging")
    public ApiResult<PagingVO<AdsErpOutstockDiffFlowDetailDTO.SourcePlatformDTO>> sourcePlatformPaging(@RequestBody @Validated PagingDTO<AdsErpOutstockDiffFlowDetailDTO.PagingParamDTO> dto) {
        return success(adsErpOutstockDiffFlowService.sourcePlatformPaging(dto));
    }
}
