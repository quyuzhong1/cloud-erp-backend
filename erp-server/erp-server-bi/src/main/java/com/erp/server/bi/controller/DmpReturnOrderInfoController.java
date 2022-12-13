package com.erp.server.bi.controller;

import com.erp.common.controller.BaseController;
import com.erp.common.dto.base.ApiResult;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.bi.dto.DmpReturnOrderInfoDTO;
import com.erp.model.bi.dto.DmpReturnOrderInfoSearchDTO;
import com.erp.server.bi.service.DmpReturnOrderInfoService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 销售数据
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/13 15:15
 */
@RestController
@RequestMapping("bi/dmpRefundInfo")
public class DmpReturnOrderInfoController extends BaseController {

    @Resource
    private DmpReturnOrderInfoService dmpReturnOrderInfoService;

    /**
     * 分页查询
     *
     * @return 查询结果
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<DmpReturnOrderInfoDTO>> queryByPage(@RequestBody @Validated PagingDTO<DmpReturnOrderInfoSearchDTO> dto) {
        PagingVO<DmpReturnOrderInfoDTO> pagingVO = dmpReturnOrderInfoService.paging(dto);
        return success(pagingVO);
    }
}
