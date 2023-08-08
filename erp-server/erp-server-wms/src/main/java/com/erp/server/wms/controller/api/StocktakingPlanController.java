package com.erp.server.wms.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.wms.service.StocktakingPlanService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.wms.dto.StocktakingPlanDTO;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 盘点计划表
 *
 * @author Cloud
 * @since 2023-08-08
 */
@RestController
@RequestMapping("/stocktakingPlan")
public class StocktakingPlanController extends BaseController {

    @Autowired
    private StocktakingPlanService stocktakingPlanService;

    /**
    * 获取状态统计
    * @return
    */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:stocktakingPlan:paging",
            tableAlias = ""
    )
    public ApiResult<List<StocktakingPlanDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
       return success(stocktakingPlanService.tabList(dto));
    }

    /**
    * 列表查询
    * @author Cloud
    * @date: 2023-08-08
    * @param dto
    * @return ApiResult<PagingVO<StocktakingPlanDTO.ListDTO>>
    */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:stocktakingPlan:paging",
            tableAlias = ""
    )
    public ApiResult<PagingVO<StocktakingPlanDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<StocktakingPlanDTO.PagingParamDTO> dto) {
        return success(stocktakingPlanService.paging(dto));
    }

   /**
   * 新增
   * @author Cloud
   * @date:  2023-08-08
   * @param dto
   * @return ApiResult<Void>
   */
   @PostMapping("/add")
   @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
           tableField = "create_user_id",
           menuCode = "wms:stocktakingPlan:add",
           serviceClass = StocktakingPlanService.class,
           keyIdName = "id")
   public ApiResult<Void> add(@RequestBody @Validated StocktakingPlanDTO.AddDTO dto) {
      stocktakingPlanService.add(dto);
      return success();
   }

    /**
    * 修改
    * @author Cloud
    * @date:  2023-08-08
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/update")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:stocktakingPlan:update",
            serviceClass = StocktakingPlanService.class,
            keyIdName = "id")
    public ApiResult<Void> update(@RequestBody @Validated StocktakingPlanDTO.UpdateDTO dto) {
        stocktakingPlanService.update(dto);
        return success();
    }

    /**
    * 新增并提交审核
    * @author Cloud
    * @date:  2023-08-08
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/addAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:stocktakingPlan:addAndSubmit",
            serviceClass = StocktakingPlanService.class,
            keyIdName = "id")
    public ApiResult<Void> addAndSubmit(@RequestBody @Validated StocktakingPlanDTO.AddDTO dto) {
        stocktakingPlanService.addAndSubmit(dto);
        return success();
    }

    /**
    * 修改并提交审核
    * @author Cloud
    * @date:  2023-08-08
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/updateAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:stocktakingPlan:updateAndSubmit",
            serviceClass = StocktakingPlanService.class,
            keyIdName = "id")
    public ApiResult<Void> updateAndSubmit(@RequestBody @Validated StocktakingPlanDTO.UpdateDTO dto) {
        stocktakingPlanService.updateAndSubmit(dto);
        return success();
    }

    /**
    * 提交审核
    * @author Cloud
    * @date:  2023-08-08
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:stocktakingPlan:submit",
            serviceClass = StocktakingPlanService.class,
            keyIdName = "ids")
    public ApiResult<Void> submit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        stocktakingPlanService.submit(dto.getIds());
        return success();
    }

    /**
    * 审核
    * @author Cloud
    * @date:  2023-08-08
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:stocktakingPlan:approve",
            serviceClass = StocktakingPlanService.class,
            keyIdName = "ids")
    public ApiResult<Void> approve(@RequestBody @Validated BaseApproveParamDTO dto) {
        stocktakingPlanService.approve(dto);
        return success();
    }

    /**
    * 反审核
    * @author Cloud
    * @date:  2023-08-08
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/disApprove")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:stocktakingPlan:disApprove",
            serviceClass = StocktakingPlanService.class,
            keyIdName = "ids")
    public ApiResult<Void> disApprove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        stocktakingPlanService.disApprove(dto.getIds());
        return success();
    }


    /**
    * 删除
    * @author Cloud
    * @date:  2023-08-08
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:stocktakingPlan:delete",
            serviceClass = StocktakingPlanService.class,
            keyIdName = "ids")
    public ApiResult<Void> delete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        stocktakingPlanService.delete(dto.getIds());
        return success();
    }

    /**
    * 撤销
    * @author Cloud
    * @date:  2023-08-08
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:stocktakingPlan:cancel",
            serviceClass = StocktakingPlanService.class,
            keyIdName = "ids")
    public ApiResult<Void> cancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        stocktakingPlanService.cancelProcess(dto.getIds());
        return success();
    }

    /**
    * 详情
    * @author Cloud
    * @date:  2023-08-08
    * @param id
    * @return ApiResult<StocktakingPlanDTO.ViewDTO>>
    */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:stocktakingPlan:view",
            serviceClass = StocktakingPlanService.class,
            keyIdName = "id")
    public ApiResult<StocktakingPlanDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(stocktakingPlanService.view(id));
    }

    /**
    * 导出Excel数据
    * @author Cloud
    * @date:  2023-08-08
    * @param dto
    * @param response
    * @return
    */
    @PostMapping("/export")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:stocktakingPlan:export",
            tableAlias = ""
    )
    public void exportList(@RequestBody @Validated StocktakingPlanDTO.ExportDTO dto, HttpServletResponse response) {
        stocktakingPlanService.exportList(dto, response);
    }


}
