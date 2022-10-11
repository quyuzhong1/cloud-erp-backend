package com.erp.server.plm.controller;


import com.erp.common.dto.base.ApiResult;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.plm.dto.ProductSearchDTO;
import com.erp.model.plm.dto.ProductShowDTO;
import com.erp.model.plm.dto.StartItemSourceDTO;
import com.erp.model.plm.dto.StartProjectDTO;
import com.erp.server.plm.service.ProjectInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.erp.common.controller.BaseController;

import java.util.List;

/**
 * 产品开发管理
 * <p>
 *
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




    /**
     * 启动项目时候 来源树形结构
     * @return
     */
    @GetMapping("/startItemList")
    public ApiResult<List<StartItemSourceDTO>> getList() {
        List<StartItemSourceDTO> resultList=projectInfoService.getStartItemSourceList();
        return success(resultList);
    }

    /**
     * 项目列表-普通分页列表
     * @param dto
     * @return
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<List<ProductShowDTO>>> paging(@RequestBody @Validated PagingDTO<ProductSearchDTO> dto) {
        PagingVO<List<ProductShowDTO>> pagingVO = projectInfoService.paging(dto);
        return success(pagingVO);
    }


}

