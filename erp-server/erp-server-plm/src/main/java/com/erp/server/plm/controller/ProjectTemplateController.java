package com.erp.server.plm.controller;


import com.erp.common.controller.BaseController;
import com.erp.common.dto.base.ApiResult;
import com.erp.common.dto.base.BaseSearchDTO;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.plm.dto.ProjectTemplateDTO;
import com.erp.model.plm.dto.ProjectTemplateSaveOrUpdateDTO;
import com.erp.model.plm.dto.ProjectTemplateUpdateStatusDTO;
import com.erp.server.plm.service.ProjectTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 模板管理
 *
 * @author yl
 * @since 2022-09-13
 */
@RestController
@RequestMapping("/plm/template")
public class ProjectTemplateController extends BaseController {

    @Autowired
    private ProjectTemplateService projectTemplateService;

    /**
     * 模板管理列表查询
     *
     * @author Will
     * @date: 2022/11/11 14:53
     * @param dto
     * @return ApiResult<PagingVO<ProjectTemplateDTO>>
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<ProjectTemplateDTO>> paging(@RequestBody  PagingDTO<BaseSearchDTO> dto) {
        PagingVO<ProjectTemplateDTO> pagingVO = projectTemplateService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 新增或修改模板
     *
     * @author Will
     * @date: 2022/11/11 15:33
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/saveOrUpdate")
    public ApiResult saveOrUpdate(@RequestBody @Validated ProjectTemplateSaveOrUpdateDTO dto) {
        Boolean flag = projectTemplateService.saveOrUpdate(dto);
        return flag ? success() : failure();
    }

    /**
     * 修改模板状态
     *
     * @author Will
     * @date: 2022/11/11 15:33
     * @param dto
     * @return ApiResult
     */
    @PutMapping("/updateStatus")
    public ApiResult updateTemplateStatus(@RequestBody @Validated ProjectTemplateUpdateStatusDTO dto) {
        Boolean flag = projectTemplateService.updateTemplateStatus(dto);
        return flag ? success() : failure();
    }


}

