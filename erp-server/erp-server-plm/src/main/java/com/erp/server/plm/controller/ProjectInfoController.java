package com.erp.server.plm.controller;


import com.erp.common.annotation.RequestPermissions;
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

    /**
     * 项目列表-启动项目
     * @param dto
     * @return
     */

    @PostMapping("/startProject")
    //@RequestPermissions("plm:project:startProject")
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
    //@RequestPermissions("plm:project:paging")
    public ApiResult<PagingVO<List<ProductShowDTO>>> paging(@RequestBody @Validated PagingDTO<ProductSearchDTO> dto) {
        PagingVO<List<ProductShowDTO>> pagingVO = projectInfoService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 项目列表-项目归档
     *
     * @param
     * @return void
     * @author yl
     * @date 2022-10-09 14:38
     */
    @PostMapping("/archive")
    //@RequestPermissions("plm:project:archive")
    public ApiResult archive(@RequestParam(value = "productId") String productId) {
        boolean flag = projectInfoService.archive(productId);
        return flag == true ? success() : failure();
    }



}

