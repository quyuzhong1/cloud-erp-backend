package com.erp.server.bi.controller;

import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.bi.dto.AdvanceSearchDTO;
import com.erp.model.bi.dto.BiDataSourceDTO;
import com.erp.server.bi.service.BiDataSourceService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 数据源管理
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/16 19:20
 */
@RestController
@RequestMapping("bi/dataSource")
public class BiDataSourceController extends BaseController {

    @Resource
    private BiDataSourceService biDataSourceService;

    /**
     * 数据源列表查询
     * @author Will
     * @date: 2022/12/19 9:51
     * @param dto
     * @return ApiResult<PagingVO<BiDataSourceDTO>>
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<BiDataSourceDTO>> queryByPage(@RequestBody @Validated PagingDTO<AdvanceSearchDTO> dto) {
        PagingVO<BiDataSourceDTO> pagingVO = biDataSourceService.paging(dto);
        return success(pagingVO);
    }


    @PostMapping("/batchAdd")
    public ApiResult batchAddBiDataSource(@RequestBody @Validated List<BiDataSourceDTO> list) {
        Boolean flag = this.biDataSourceService.batchAddBiDataSource(list);
        return flag == true ? success() : failure();
    }
}
