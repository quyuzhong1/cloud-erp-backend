package com.erp.server.wms.controller.api;


import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.wms.dto.VirtualInventoryDTO;
import com.erp.model.wms.dto.VirtualTransFlowDTO;
import com.erp.server.wms.service.VirtualTransFlowService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 虚拟库存交易流水表
 *
 * @author will
 * @since 2024-06-03
 */
@Slf4j
@RestController
@LogSystemModule("虚拟库存交易流水表")
@RequestMapping("/virtualTransFlow")
public class VirtualTransFlowController extends BaseController {

    @Resource
    private VirtualTransFlowService virtualTransFlowService;

    /**
     * 虚拟库存交易流水列表
     * @author will
     * @date 2024/6/3 17:07
     * @param dto
     * @return ApiResult<PagingVO<ListDTO>>
     */
    @PostMapping("/paging")
    @WebAdvanceQuery
    public ApiResult<PagingVO<VirtualTransFlowDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<VirtualTransFlowDTO.SearchParamDTO> dto) {
        return success(virtualTransFlowService.paging(dto));
    }


}
