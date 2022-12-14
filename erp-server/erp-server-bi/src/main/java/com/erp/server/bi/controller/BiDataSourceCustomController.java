package com.erp.server.bi.controller;

import com.erp.common.controller.BaseController;
import com.erp.common.dto.base.ApiResult;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.bi.dto.BiDataSourceCustomDTO;
import com.erp.model.bi.dto.BiDataSourceCustomSearchDTO;
import com.erp.server.bi.service.BiDataSourceCustomService;
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
 * @date 2022/12/14 16:33
 */
@RestController
@RequestMapping("bi/dataSourceCustom")
public class BiDataSourceCustomController extends BaseController {

    @Resource
    private BiDataSourceCustomService biDataSourceCustomService;

    /**
     * 分页查询
     *
     * @return 查询结果
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<BiDataSourceCustomDTO>> queryByPage(@RequestBody @Validated PagingDTO<BiDataSourceCustomSearchDTO> dto) {
        PagingVO<BiDataSourceCustomDTO> pagingVO = biDataSourceCustomService.paging(dto);
        return success(pagingVO);
    }
}
