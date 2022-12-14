package com.erp.server.bi.controller;

import com.erp.common.controller.BaseController;
import com.erp.common.dto.base.ApiResult;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.bi.dto.DmpShopInfoDTO;
import com.erp.model.bi.dto.DmpShopInfoSearchDTO;
import com.erp.server.bi.service.DmpShopInfoService;
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
 * @date 2022/12/14 14:42
 */
@RestController
@RequestMapping("bi/dmpRefundInfo")
public class DmpShopInfoController extends BaseController {

    @Resource
    private DmpShopInfoService dmpShopInfoService;

    /**
     * 分页查询
     *
     * @return 查询结果
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<DmpShopInfoDTO>> queryByPage(@RequestBody @Validated PagingDTO<DmpShopInfoSearchDTO> dto) {
        PagingVO<DmpShopInfoDTO> pagingVO = dmpShopInfoService.paging(dto);
        return success(pagingVO);
    }
}
