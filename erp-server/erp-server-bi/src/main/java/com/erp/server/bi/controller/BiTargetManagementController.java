package com.erp.server.bi.controller;

import com.erp.common.controller.BaseController;
import com.erp.common.dto.base.ApiResult;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.bi.dto.AdvanceSearchDTO;
import com.erp.model.bi.dto.BiTargetManagementShowDTO;
import com.erp.model.bi.dto.DmpOrderInfoDTO;
import com.erp.server.bi.service.BiTargetManagementService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/21 17:32
 */
@RestController
@RequestMapping("bi/targetManagement")
public class BiTargetManagementController extends BaseController {

    @Resource
    private BiTargetManagementService biTargetManagementService;


    @PostMapping("/paging")
    public ApiResult<PagingVO<BiTargetManagementShowDTO>> queryByPage(@RequestBody @Validated PagingDTO<AdvanceSearchDTO> dto) {
        PagingVO<BiTargetManagementShowDTO> pagingVO = biTargetManagementService.paging(dto);
        return success(pagingVO);
    }
}
