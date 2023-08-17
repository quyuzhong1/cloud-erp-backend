package com.erp.server.plm.controller.api;

import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.plm.dto.BomCombinationDTO;
import com.erp.server.plm.service.BomCombinationService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 组合产品
 * @author Will
 * @version 1.0
 * @date 2023/8/16 9:39
 */
@RestController
@RequestMapping("bomCombination")
public class BomCombinationController extends BaseController {
    @Resource
    private BomCombinationService bomCombinationService;

    /**
     * 列表查询
     * @author Will
     * @date: 2023/8/16 10:02
     * @param dto
     * @return ApiResult<PagingVO<ListDTO>>
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<BomCombinationDTO.ListDTO>> queryByPage(@RequestBody @Validated PagingDTO<BomCombinationDTO.SearchParamDTO> dto) {
        PagingVO<BomCombinationDTO.ListDTO> pagingVO = bomCombinationService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 新增
     * @author Will
     * @date: 2023/8/16 10:08
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/add")
    public ApiResult add(@RequestBody @Validated BomCombinationDTO.AddDTO dto) {
        Boolean flag = this.bomCombinationService.add(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 修改
     * @author Will
     * @date: 2023/8/16 10:16
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/update")
    public ApiResult update(@RequestBody @Validated BomCombinationDTO.UpdateDTO dto) {
        Boolean flag = this.bomCombinationService.update(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 查看详情
     * @author Will
     * @date: 2023/8/16 10:26
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/view")
    public ApiResult<BomCombinationDTO.ViewDTO> update(@RequestBody @Validated BaseIdDTO dto) {
        BomCombinationDTO.ViewDTO viewDTO = this.bomCombinationService.view(dto);
        return success(viewDTO);
    }

}
