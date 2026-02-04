package com.erp.server.dmp.controller.api;

import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.dmp.dto.AdsErpOutstockDiffFlowDTO;
import com.erp.server.dmp.query.AdsErpOutstockDiffFlowQueryHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@LogSystemModule("第三方仓出库单据差异明细表")
@RequestMapping("/adsErpOutstockDiffDetailFlow")
public class AdsErpOutstockDiffFlowDetailController extends BaseController {



    @PostMapping("/paging")
    public ApiResult<PagingVO<AdsErpOutstockDiffFlowDTO.PagingDTO>> paging(@RequestBody @Validated PagingDTO<AdsErpOutstockDiffFlowDTO.PagingParamDTO> dto) {
        return success(adsErpOutstockDiffFlowService.paging(dto));
    }
}
