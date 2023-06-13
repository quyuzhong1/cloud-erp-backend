package com.erp.server.scm.controller.api;

import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.scm.dto.SubcontractChangeDTO;
import com.erp.server.scm.service.SubcontractChangeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 委外变更单
 * </p>
 *
 * @author will
 * @since 2023-06-08
 */
@RestController
@RequestMapping("/subcontractChangeOrder")
public class SubcontractChangeController extends BaseController {

    @Autowired
    private SubcontractChangeService subcontractChangeService;

    /**
    * 获取状态统计
    * @return
    */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "scm:subcontractChangeOrder:tabList",
            tableAlias = ""
    )
    public ApiResult<List<SubcontractChangeDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
       return success(subcontractChangeService.tabList(dto));
    }

    /**
    * 列表查询
    * @author will
    * @date: 2023-06-08
    * @param dto
    * @return ApiResult<PagingVO<SubcontractChangeOrderDTO.ListDTO>>
    */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "scm:subcontractChangeOrder:paging",
            tableAlias = ""
    )
    public ApiResult<PagingVO<SubcontractChangeDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<SubcontractChangeDTO.PagingParamDTO> dto) {
        return success(subcontractChangeService.paging(dto));
    }

   /**
   * 新增
   * @author will
   * @date:  2023-06-08
   * @param dto
   * @return ApiResult<Void>
   */
   @PostMapping("/add")
   @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
           tableField = "create_user_id",
           menuCode = "scm:subcontractChangeOrder:add",
           serviceClass = SubcontractChangeService.class,
           keyIdName = "id")
   public ApiResult<Void> add(@RequestBody @Validated SubcontractChangeDTO.AddDTO dto) {
      subcontractChangeService.add(dto);
      return success();
   }

    /**
    * 修改
    * @author will
    * @date:  2023-06-08
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/update")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "scm:subcontractChangeOrder:update",
            serviceClass = SubcontractChangeService.class,
            keyIdName = "id")
    public ApiResult<Void> update(@RequestBody @Validated SubcontractChangeDTO.UpdateDTO dto) {
        subcontractChangeService.update(dto);
        return success();
    }

    /**
    * 新增并提交审核
    * @author will
    * @date:  2023-06-08
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/addAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "scm:subcontractChangeOrder:addAndSubmit",
            serviceClass = SubcontractChangeService.class,
            keyIdName = "id")
    public ApiResult<Void> addAndSubmit(@RequestBody @Validated SubcontractChangeDTO.AddDTO dto) {
        subcontractChangeService.addAndSubmit(dto);
        return success();
    }

    /**
    * 修改并提交审核
    * @author will
    * @date:  2023-06-08
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/updateAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "scm:subcontractChangeOrder:updateAndSubmit",
            serviceClass = SubcontractChangeService.class,
            keyIdName = "id")
    public ApiResult<Void> updateAndSubmit(@RequestBody @Validated SubcontractChangeDTO.UpdateDTO dto) {
        subcontractChangeService.updateAndSubmit(dto);
        return success();
    }

    /**
    * 提交审核
    * @author will
    * @date:  2023-06-08
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "scm:subcontractChangeOrder:submit",
            serviceClass = SubcontractChangeService.class,
            keyIdName = "ids")
    public ApiResult<Void> submit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        subcontractChangeService.submit(dto.getIds());
        return success();
    }

    /**
    * 审核
    * @author will
    * @date:  2023-06-08
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "scm:subcontractChangeOrder:approve",
            serviceClass = SubcontractChangeService.class,
            keyIdName = "ids")
    public ApiResult<Void> approve(@RequestBody @Validated BaseApproveParamDTO dto) {
        subcontractChangeService.approve(dto);
        return success();
    }

    /**
    * 反审核
    * @author will
    * @date:  2023-06-08
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/disApprove")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "scm:subcontractChangeOrder:disApprove",
            serviceClass = SubcontractChangeService.class,
            keyIdName = "ids")
    public ApiResult<Void> disApprove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        subcontractChangeService.disApprove(dto.getIds());
        return success();
    }


    /**
    * 删除
    * @author will
    * @date:  2023-06-08
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "scm:subcontractChangeOrder:delete",
            serviceClass = SubcontractChangeService.class,
            keyIdName = "ids")
    public ApiResult<Void> delete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        subcontractChangeService.delete(dto.getIds());
        return success();
    }

    /**
    * 撤销
    * @author will
    * @date:  2023-06-08
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "scm:subcontractChangeOrder:cancel",
            serviceClass = SubcontractChangeService.class,
            keyIdName = "ids")
    public ApiResult<Void> cancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        subcontractChangeService.cancelProcess(dto.getIds());
        return success();
    }

    /**
    * 详情
    * @author will
    * @date:  2023-06-08
    * @param id
    * @return ApiResult<SubcontractChangeOrderDTO.ViewDTO>>
    */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "scm:subcontractChangeOrder:view",
            serviceClass = SubcontractChangeService.class,
            keyIdName = "id")
    public ApiResult<SubcontractChangeDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(subcontractChangeService.view(id));
    }

    /**
    * 导出Excel数据
    * @author will
    * @date:  2023-06-08
    * @param dto
    * @param response
    * @return
    */
    @PostMapping("/export")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "scm:subcontractChangeOrder:export",
            tableAlias = ""
    )
    public void exportList(@RequestBody @Validated SubcontractChangeDTO.ExportDTO dto, HttpServletResponse response) {
        subcontractChangeService.exportList(dto, response);
    }

}
