package com.erp.server.plm.controller;

import com.erp.common.controller.BaseController;
import com.erp.common.dto.base.ApiResult;
import com.erp.common.dto.base.BaseIdDTO;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.plm.dto.AddChangeDTO;
import com.erp.model.plm.dto.BomSearchPagingDTO;
import com.erp.model.plm.dto.ProductChangeListSearchDTO;
import com.erp.model.plm.vo.ProductChangePagingVO;
import com.erp.server.plm.service.ProductChangeService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 变更信息表(ProductChange)表控制层
 *
 * @author yl
 * @since 2023-01-11 14:05:03
 */
@RestController
@RequestMapping("plm/change")
public class ProductChangeController extends BaseController {


    @Resource
    private ProductChangeService productChangeService;


    @PostMapping("/add")
    public ApiResult add(@RequestBody @Validated AddChangeDTO dto) {
        Boolean result = productChangeService.add(dto);
        return result == true ? success() : failure();
    }

    @PostMapping("/paging")
    public ApiResult<PagingVO<List<ProductChangePagingVO>>> queryByPage(@RequestBody @Validated PagingDTO<BomSearchPagingDTO> dto) {
        PagingVO<List<ProductChangePagingVO>> pagingVO = productChangeService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 详情
     *
     * @param dto
     * @return
     */
    @PostMapping("/view")
    public ApiResult details(@RequestBody @Validated BaseIdDTO dto) {
        return success();
    }

    /**
     * 作废
     *
     * @param dto
     * @return
     */
    @PostMapping("/cancellation")
    public ApiResult cancellation(@RequestBody @Validated BaseIdDTO dto) {
        Boolean result = productChangeService.cancellation(dto.getId());
        return result == true ? success() : failure();
    }

    /**
     * 获取变更的信息
     *
     * @param
     * @return
     */
    @PostMapping("/list")
    public ApiResult<List<BaseIdDTO>> list(ProductChangeListSearchDTO  dto) {
        List<BaseIdDTO> list = productChangeService.getChangeByType(dto.getType(),dto.getSearchKeyword());
        return success(list);
    }

}

