package com.erp.server.plm.controller.api;


import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.plm.dto.BasicDTO;
import com.erp.model.plm.dto.ProductSearchDTO;
import com.erp.model.plm.dto.SelectShowDTO;
import com.erp.model.plm.dto.StartProjectDTO;
import com.erp.model.plm.enums.ProjectStateEnum;
import com.erp.server.plm.service.ProjectInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
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
@RequestMapping("project")
public class ProjectInfoController extends BaseController {

    @Autowired
    private ProjectInfoService projectInfoService;

    /**
     * 项目列表-启动项目
     *
     * @param dto
     * @return
     */

    @PostMapping("/startProject")
    public ApiResult startProject(@RequestBody @Validated StartProjectDTO dto) {
        Boolean flag = projectInfoService.startProject(dto);
        return flag == true ? success() : failure();
    }


    /**
     * 启动项目时候 来源树形结构
     *
     * @return
     */
    @GetMapping("/startItemList")
    public ApiResult getList() {
        return success();
    }



    /**
     * 项目列表-无分页
     *
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
     *
     * @return ApiResult
     * @author Will
     * @date: 2022/11/24 10:26
     */
    @GetMapping("/getProjectStatusSelect")
    public ApiResult<List<SelectShowDTO>> getProjectStatusSelect() {
        List<SelectShowDTO> list = new ArrayList<>();
        Arrays.stream(ProjectStateEnum.values()).forEach(obj -> {
            SelectShowDTO dto = new SelectShowDTO();
            dto.setValue(obj.getState());
            dto.setLabel(obj.getName());
            list.add(dto);
        });
        return success(list);
    }


    /**
     * 项目报表
     *
     * @param
     * @return com.common.core.controller.vo.ApiResult
     * @Author Luo_WG
     * @Date 2023/6/12 19:08
     **/
    @PostMapping("/projectReportForms")
    //@RequestPermissions("plm:project:archive")
    public ApiResult projectReportForms(@RequestParam(value = "productId") String productId) {
        boolean flag = projectInfoService.archive(productId);
        return flag == true ? success() : failure();
    }


    /**
     * 产品开发管理-重新启动项目  ids为产品id集合【PLM1.3】
     *
     * @return
     */
    @PostMapping("/restart")
    public ApiResult restart(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        Boolean flag = projectInfoService.restart(dto.getIds());
        return flag ? success() : failure();
    }


    /**
     * 产品开发管理-暂停项目  ids为产品id集合【PLM1.3】
     *
     * @return
     */
    @PostMapping("/suspend")
    public ApiResult suspend(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        Boolean flag = projectInfoService.suspend(dto.getIds());
        return flag ? success() : failure();
    }

    /**
     * 产品开发管理-终止项目  ids为项目id集合【PLM1.3】
     *
     * @return
     */
    @PostMapping("/stop")
    public ApiResult stop(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        Boolean flag = projectInfoService.stop(dto.getIds());
        return flag ? success() : failure();
    }

    /**
     * 产品开发管理-项目结项  ids为项目id集合【PLM1.3】
     *
     * @return
     */
    @PostMapping("/finish")
    public ApiResult finish(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        Boolean flag = projectInfoService.finish(dto.getIds());
        return flag ? success() : failure();
    }

    /**
     * 产品开发管理-项目归档  ids为产品id集合【PLM1.3】
     *
     * @param
     * @return void
     * @author yl
     * @date 2022-10-09 14:38
     */
    @PostMapping("/batchArchive")
    public ApiResult batchArchive(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        boolean flag = projectInfoService.batchArchive(dto.getIds());
        return flag ? success() : failure();
    }

    /**
     * 产品开发管理-启动项目  ids为产品id集合【PLM1.3】
     *
     * @param
     * @return void
     * @author yl
     * @date 2022-10-09 14:38
     */
//    @PostMapping("/batchStartProject")
//    public ApiResult batchStartProject(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
//        boolean flag = projectInfoService.batchArchive(dto.getIds());
//        return flag ? success() : failure();
//    }

}

