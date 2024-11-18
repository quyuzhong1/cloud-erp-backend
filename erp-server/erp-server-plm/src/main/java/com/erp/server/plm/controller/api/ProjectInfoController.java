package com.erp.server.plm.controller.api;


import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.plm.dto.*;
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
@LogSystemModule("产品开发管理")
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
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "项目列表-启动项目:负责人id={chargeId},产品id={productId},项目id={projectId}")
    @PostMapping("/startProject")
    public ApiResult<Object> startProject(@RequestBody @Validated StartProjectDTO dto) {
        Boolean flag = projectInfoService.startProject(dto);
        return flag == true ? success() : failure();
    }


    /**
     * 产品开发管理-项目启动  【PLM1.3】
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "项目列表-批量启动项目:负责人id={chargeId},项目ids={projectIdList}")
    @PostMapping("/batchStartProject")
    public ApiResult<Object> batchStartProject(@RequestBody @Validated StartProjectDTO.BatchStartProjectDTO dto) {
        Boolean flag = projectInfoService.batchStartProject(dto);
        return flag ? success() : failure();
    }

    /**
     * 启动项目时候 来源树形结构
     *
     * @return
     */
    @GetMapping("/startItemList")
    public ApiResult<Object> getList() {
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
    public ApiResult<List<BasicDTO>> listProjectInfo(@RequestBody @Validated ProductSearchDTO.PagingParamDTO dto) {
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
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "项目列表-项目归档：产品id={productId}")
    @PostMapping("/archive")
    //@RequestPermissions("plm:project:archive")
    public ApiResult<Object> archive(@RequestParam(value = "productId") String productId) {
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
    public ApiResult<Object> projectReportForms(@RequestParam(value = "productId") String productId) {
        boolean flag = projectInfoService.archive(productId);
        return flag == true ? success() : failure();
    }


    /**
     * 产品开发管理-重新启动项目  ids为产品id集合【PLM1.3】
     *
     * @return
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "项目列表-重新启动项目:ids={ids}")
    @PostMapping("/restart")
    public ApiResult<Object> restart(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        Boolean flag = projectInfoService.restart(dto.getIds());
        return flag ? success() : failure();
    }


    /**
     * 产品开发管理-暂停项目  ids为产品id集合【PLM1.3】
     *
     * @return
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "项目列表-暂停项目:ids={ids}")
    @PostMapping("/suspend")
    public ApiResult<Object> suspend(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        Boolean flag = projectInfoService.suspend(dto.getIds());
        return flag ? success() : failure();
    }

    /**
     * 产品开发管理-终止项目  ids为项目id集合【PLM1.3】
     *
     * @return
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "项目列表-终止项目:ids={ids}")
    @PostMapping("/stop")
    public ApiResult<Object> terminate(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        Boolean flag = projectInfoService.terminate(dto.getIds());
        return flag ? success() : failure();
    }

    /**
     * 产品开发管理-项目结项  ids为产品id集合【PLM1.3】
     *
     * @return
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "项目列表-项目结项:ids={ids}")
    @PostMapping("/finish")
    public ApiResult<Object> finish(@RequestBody @Valid ProductInfoDTO.IdsDateDto dto) {
        Boolean flag = projectInfoService.finish(dto);
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
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "项目列表-批量项目归档:ids={ids}")
    @PostMapping("/batchArchive")
    public ApiResult<Object> batchArchive(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        boolean flag = projectInfoService.batchArchive(dto.getIds());
        return flag ? success() : failure();
    }



}

