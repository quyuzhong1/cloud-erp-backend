package com.erp.server.plm.controller;


import com.common.business.annotation.DataPermission;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.enums.ProjectStateEnum;
import com.erp.server.plm.service.ProjectInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
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
     * 项目列表-普通分页列表【优化3】
     * @param dto
     * @return
     */
    @PostMapping("/paging")
    //@RequestPermissions("plm:project:paging")
    @DataPermission(operationType = DataAttributeEnum.LIST, tableField = "charge_id", menuCode = "plm:project:paging", tableAlias = "pt")
    public ApiResult<PagingVO<List<ProductShowDTO>>> paging(@RequestBody @Validated PagingDTO<ProductSearchDTO> dto) {
        PagingVO<List<ProductShowDTO>> pagingVO = projectInfoService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 项目列表-无分页
     * @param dto
     * @return
     */
    @PostMapping("/list")
    @DataPermission(operationType = DataAttributeEnum.LIST, tableField = "charge_id", menuCode = "plm:project:paging", tableAlias = "pt")
    public ApiResult<List<BasicDTO>> listProjectInfo(@RequestBody @Validated ProductSearchDTO dto) {
        List<BasicDTO> list = projectInfoService.listProjectInfo(dto);
        return success(list);
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

    /**
     * 项目列表-项目状态下拉框
     * @author Will
     * @date: 2022/11/24 10:26
     * @return ApiResult
     */
    @GetMapping("/getProjectStatusSelect")
    public ApiResult<List<SelectShowDTO>> getProjectStatusSelect() {
        List<SelectShowDTO> list = new ArrayList<>();
        Arrays.stream(ProjectStateEnum.values()).forEach(obj->{
            SelectShowDTO dto = new SelectShowDTO();
            dto.setValue(obj.getState());
            dto.setLabel(obj.getName());
            list.add(dto);
        });
        return success(list);
    }

}

