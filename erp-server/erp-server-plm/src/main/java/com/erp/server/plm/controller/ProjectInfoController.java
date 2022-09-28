package com.erp.server.plm.controller;


import com.erp.common.dto.base.ApiResult;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.plm.dto.ProductSearchDTO;
import com.erp.model.plm.dto.StartProjectDTO;
import com.erp.server.plm.service.ProjectInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.erp.common.controller.BaseController;

/**
 * <p>
 * 产品项目表 前端控制器
 * </p>
 *
 * @author yl
 * @since 2022-09-13
 */
@RestController
@RequestMapping("plm/project")
public class ProjectInfoController extends BaseController {

    @Autowired
    private ProjectInfoService projectInfoService;


    @PostMapping("/startProject")
    public ApiResult startProject(@RequestBody @Validated StartProjectDTO dto) {
        Boolean flag = projectInfoService.startProject(dto);
        return flag == true ? success() : failure();
    }


    @GetMapping("/list")
    public ApiResult getList() {
        return success(projectInfoService.listMap());
    }

    //普通分页
    @PostMapping("/paging")
    public ApiResult paging(@RequestBody @Validated PagingDTO<ProductSearchDTO> dto) {
        PagingVO pagingVO = projectInfoService.paging(dto);
        return success(pagingVO);
    }


}

