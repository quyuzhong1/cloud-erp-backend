package com.erp.server.wms.controller.api;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.*;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.utils.RedisUtil;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogViewService;
import com.common.core.enums.LogActionEnum;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.common.message.constant.RedisKeyConstant;
import com.erp.model.wms.dto.StocktakingPlanDTO;
import com.erp.model.wms.entity.StocktakingPlanEntity;
import com.erp.server.wms.service.StocktakingPlanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 盘点计划
 *
 * @author Cloud
 * @since 2023-08-08
 */
@Slf4j
@RestController
@LogSystemModule("盘点计划")
@RequestMapping("/stocktakingPlan")
public class StocktakingPlanController extends BaseController {

    @Resource
    private StocktakingPlanService stocktakingPlanService;
    @Resource
    private RedisUtil redisUtil;

    /**
    * 获取状态统计
    * @return
    */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:stocktakingPlan:paging",
            tableAlias = "stocktaking_plan"
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
            tableAlias = "stocktaking_plan"
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
   @LogAction(value = LogActionEnum.INSERT, desc = "新增盘点计划")
   @PostMapping("/add")
//   @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
//           tableField = "create_user_id",
//           menuCode = "wms:stocktakingPlan:add",
//           serviceClass = StocktakingPlanService.class)
   public ApiResult<String> add(@RequestBody @Validated StocktakingPlanDTO.AddDTO dto) {
      return success(stocktakingPlanService.add(dto));
   }

    /**
    * 修改
    * @author Cloud
    * @date:  2023-08-08
    * @param dto
    * @return ApiResult<Void>
    */
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改盘点计划")
    @PostMapping("/update")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:stocktakingPlan:update",
            serviceClass = StocktakingPlanService.class,
            keyIdName = "id")
    public ApiResult update(@RequestBody @Validated StocktakingPlanDTO.UpdateDTO dto) {
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
    @LogAction(value = LogActionEnum.ADD_AND_SUBMIT, desc = "新增并提交盘点计划")
    @PostMapping("/addAndSubmit")
//    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
//            tableField = "create_user_id",
//            menuCode = "wms:stocktakingPlan:addAndSubmit",
//            serviceClass = StocktakingPlanService.class,
//            keyIdName = "id")
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
    @LogAction(value = LogActionEnum.UPDATE_AND_SUBMIT, desc = "修改并提交盘点计划")
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
    @LogAction(value = LogActionEnum.SUBMIT, desc = "盘点计划提交审核盘点计划")
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:stocktakingPlan:submit",
            serviceClass = StocktakingPlanService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> submit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO submit;
            try {
                submit = stocktakingPlanService.submit(id);
            }catch (Exception e){
                log.error("盘点计划 提交审核失败",e);
                StocktakingPlanEntity entity = stocktakingPlanService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    submit = BatchResultDTO.fail(id, id, "盘点计划不存在, 提交失败");
                    resultDTOS.add(submit);
                    continue;
                }
                submit = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(submit);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
    * 审核
    * @author Cloud
    * @date:  2023-08-08
    * @param dto
    * @return ApiResult<Void>
    */
    @LogAction(value = LogActionEnum.APPROVE, desc = "审核盘点计划")
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:stocktakingPlan:approve",
            serviceClass = StocktakingPlanService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> approve(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : ids) {
            StocktakingPlanEntity entity = stocktakingPlanService.getById(id);
            BatchResultDTO approveResult;
            try {
                approveResult = stocktakingPlanService.approve(new ApproveOneDTO(id, dto.getType(),dto.getComment()));
            }catch (Exception e){
                log.error("盘点计划审核失败",e);
                if (ObjectUtil.isEmpty(entity)) {
                    approveResult = BatchResultDTO.fail(id, id, "盘点计划不存在, 审核失败");
                    resultDTOS.add(approveResult);
                    continue;
                }
                approveResult = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
                // 删除盘点锁定的库存
                redisUtil.keys(CharSequenceUtil.format(RedisKeyConstant.INVENTORY_LOCK_CODE, entity.getCode()))
                        .forEach(key -> redisUtil.del(key));
            }
            resultDTOS.add(approveResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
    * 反审核
    * @author Cloud
    * @date:  2023-08-08
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @LogAction(value = LogActionEnum.DISAPPROVE, desc = "反审核盘点计划")
    @PostMapping("/disApprove")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:stocktakingPlan:disApprove",
            serviceClass = StocktakingPlanService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> disApprove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            StocktakingPlanEntity entity = stocktakingPlanService.getById(id);
            BatchResultDTO disApproveResult;
            try {
                disApproveResult = stocktakingPlanService.disApprove(id);
                // 删除盘点锁定的库存
                redisUtil.keys(CharSequenceUtil.format(RedisKeyConstant.INVENTORY_LOCK_CODE, entity.getCode()))
                        .forEach(key -> redisUtil.del(key));
            }catch (Exception e){
                log.error("盘点计划反审核失败",e);
                if (ObjectUtil.isEmpty(entity)) {
                    disApproveResult = BatchResultDTO.fail(id, id, "盘点计划不存在, 反审核失败");
                    resultDTOS.add(disApproveResult);
                    continue;
                }
                disApproveResult = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(disApproveResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }


    /**
    * 删除
    * @author Cloud
    * @date:  2023-08-08
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @LogAction(value = LogActionEnum.DELETE, desc = "删除盘点计划")
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:stocktakingPlan:delete",
            serviceClass = StocktakingPlanService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> delete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = stocktakingPlanService.delete(id);
            }catch (Exception e){
                log.error("盘点计划删除失败",e);
                StocktakingPlanEntity entity = stocktakingPlanService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "盘点计划不存在, 删除失败");
                    resultDTOS.add(deleteResult);
                    continue;
                }
                deleteResult = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(deleteResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
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
            menuCode = "wms:stocktakingPlan:cancelProcess",
            serviceClass = StocktakingPlanService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.CANCEL, desc = "盘点计划撤销")
    public ApiResult<List<BatchResultDTO>> cancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = stocktakingPlanService.cancelProcess(id);
            }catch (Exception e){
                log.error("盘点计划撤回流程失败",e);
                StocktakingPlanEntity entity = stocktakingPlanService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "盘点计划不存在, 撤回流程失败");
                    resultDTOS.add(deleteResult);
                    continue;
                }
                deleteResult = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(deleteResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
    * 详情
    * @author Cloud
    * @date:  2023-08-08
    * @param id
    * @return ApiResult<StocktakingPlanDTO.ViewDTO>>
    */
    @LogViewService
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:stocktakingPlan:view",
            serviceClass = StocktakingPlanService.class,
            keyIdName = "id")
    public ApiResult<StocktakingPlanDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(stocktakingPlanService.view(id));
    }


//    /**
//    * 导出Excel数据
//    * @author Cloud
//    * @date:  2023-08-08
//    * @param dto
//    * @param response
//    * @return
//    */
//    @PostMapping("/export")
//    @DataPermission(operationType = DataAttributeEnum.LIST,
//            tableField = "create_user_id",
//            menuCode = "wms:stocktakingPlan:export",
//            tableAlias = ""
//    )
//    public void exportList(@RequestBody @Validated StocktakingPlanDTO.ExportDTO dto, HttpServletResponse response) {
//        stocktakingPlanService.exportList(dto, response);
//    }


}
