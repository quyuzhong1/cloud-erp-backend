package com.erp.server.bi.controller.api;

import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.enums.LogActionEnum;
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

 * @date 2022/12/16 19:20
 */
@RestController
@LogSystemModule("数据源管理")
@RequestMapping("dataSource")
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


    @LogAction(value = LogActionEnum.CUSTOM_BATCH_INSERT, desc = "批量添加数据源:数据源类型={type}")
    @PostMapping("/batchAdd")
    public ApiResult<Object> batchAddBiDataSource(@RequestBody @Validated List<BiDataSourceDTO> list) {
        boolean flag = this.biDataSourceService.batchAddBiDataSource(list);
        return flag ? success() : failure();
    }
}
