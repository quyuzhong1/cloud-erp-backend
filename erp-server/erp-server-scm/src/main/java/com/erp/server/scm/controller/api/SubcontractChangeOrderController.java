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
import com.erp.model.scm.dto.SubcontractChangeOrderDTO;
import com.erp.server.scm.service.SubcontractChangeOrderService;
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
public class SubcontractChangeOrderController extends BaseController {

    @Autowired
    private SubcontractChangeOrderService subcontractChangeOrderService;

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
    public ApiResult<List<SubcontractChangeOrderDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
       return success(subcontractChangeOrderService.tabList(dto));
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
    public ApiResult<PagingVO<SubcontractChangeOrderDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<SubcontractChangeOrderDTO.PagingParamDTO> dto) {
        return success(subcontractChangeOrderService.paging(dto));
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
           serviceClass = SubcontractChangeOrderService.class,
           keyIdName = "id")
   public ApiResult<Void> add(@RequestBody @Validated SubcontractChangeOrderDTO.AddDTO dto) {
      subcontractChangeOrderService.add(dto);
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
            serviceClass = SubcontractChangeOrderService.class,
            keyIdName = "id")
    public ApiResult<Void> update(@RequestBody @Validated SubcontractChangeOrderDTO.UpdateDTO dto) {
        subcontractChangeOrderService.update(dto);
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
            serviceClass = SubcontractChangeOrderService.class,
            keyIdName = "id")
    public ApiResult<Void> addAndSubmit(@RequestBody @Validated SubcontractChangeOrderDTO.AddDTO dto) {
        subcontractChangeOrderService.addAndSubmit(dto);
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
            serviceClass = SubcontractChangeOrderService.class,
            keyIdName = "id")
    public ApiResult<Void> updateAndSubmit(@RequestBody @Validated SubcontractChangeOrderDTO.UpdateDTO dto) {
        subcontractChangeOrderService.updateAndSubmit(dto);
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
            serviceClass = SubcontractChangeOrderService.class,
            keyIdName = "ids")
    public ApiResult<Void> submit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        subcontractChangeOrderService.submit(dto.getIds());
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
            serviceClass = SubcontractChangeOrderService.class,
            keyIdName = "ids")
    public ApiResult<Void> approve(@RequestBody @Validated BaseApproveParamDTO dto) {
        subcontractChangeOrderService.approve(dto);
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
            serviceClass = SubcontractChangeOrderService.class,
            keyIdName = "ids")
    public ApiResult<Void> disApprove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        subcontractChangeOrderService.disApprove(dto.getIds());
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
            serviceClass = SubcontractChangeOrderService.class,
            keyIdName = "ids")
    public ApiResult<Void> delete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        subcontractChangeOrderService.delete(dto.getIds());
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
            serviceClass = SubcontractChangeOrderService.class,
            keyIdName = "ids")
    public ApiResult<Void> cancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        subcontractChangeOrderService.cancelProcess(dto.getIds());
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
            serviceClass = SubcontractChangeOrderService.class,
            keyIdName = "id")
    public ApiResult<SubcontractChangeOrderDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(subcontractChangeOrderService.view(id));
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
    public void exportList(@RequestBody @Validated SubcontractChangeOrderDTO.ExportDTO dto, HttpServletResponse response) {
        subcontractChangeOrderService.exportList(dto, response);
    }

}
