package com.erp.server.wms.controller.api;


import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.OverseasInventoryAgeDetailDTO;
import com.erp.server.wms.service.OverseasInventoryAgeDetailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 海外仓库存库龄明细表
 *
 * @author jack
 * @since 2024-12-06
 */
@Slf4j
@RestController
@LogSystemModule("海外仓库存库龄明细表")
@RequestMapping("/overseasInventoryAgeDetail")
public class OverseasInventoryAgeDetailController extends BaseController {
    @Resource
    private OverseasInventoryAgeDetailService overseasInventoryAgeDetailService;


    /**
     * 列表查询
     * @author jack
     * @date:  2024-12-09
     * @return ApiResult
     */
    @PostMapping("/pagingSelect")
    public ApiResult<PagingVO<OverseasInventoryAgeDetailDTO.ListDTO>> pagingSelect(@RequestBody @Validated PagingDTO<OverseasInventoryAgeDetailDTO.PagingParamDTO> dto) {
        return success(overseasInventoryAgeDetailService.pagingSelect(dto));
    }

}
