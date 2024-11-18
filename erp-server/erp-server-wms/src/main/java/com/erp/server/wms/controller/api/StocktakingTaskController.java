package com.erp.server.wms.controller.api;


import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.*;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.ApproveTypeEnum;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.utils.RedisUtil;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.vo.ApiResult;
import com.common.message.constant.RedisKeyConstant;
import com.erp.model.wms.dto.StocktakingTaskDTO;
import com.erp.model.wms.dto.StocktakingTaskDetailDTO;
import com.erp.model.wms.entity.StocktakingProfitLossEntity;
import com.erp.model.wms.entity.StocktakingTaskEntity;
import com.erp.server.wms.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.common.core.anno.LogAction;
import com.common.core.anno.LogViewService;
import com.common.core.enums.LogActionEnum;
import com.common.core.controller.BaseController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

/**
 * 盘点管理-盘点任务
 *
 * @author Lambda
 * @since 2023-07-31
 */
@Slf4j
@RestController
@LogSystemModule("盘点任务")
@RequestMapping("/stocktakingTask")
public class StocktakingTaskController extends BaseController {

    @Resource
    private StocktakingTaskService stocktakingTaskService;
    @Resource
    private RedisUtil redisUtil;
    @Resource
    private StocktakingTaskDetailService stocktakingTaskDetailService;
    @Resource
    private StocktakingProfitLossService stocktakingProfitLossService;


    /**
     * 获取 tab列表
     *
     * @return
     */
    @PostMapping("/tabList")
    public ApiResult<List<StocktakingTaskDTO.TabDTO>> tabList(@RequestBody PermissionsDTO dto) {
        List<StocktakingTaskDTO.TabDTO> tabList = stocktakingTaskService.tabList(dto);
        return success(tabList);
    }

    /**
     * 分页列表
     *
     * @param dto
     * @return
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:stocktakingTask:paging",
            tableAlias = "st"
    )
    public ApiResult<PagingVO<StocktakingTaskDTO.PagingViewDTO>> queryByPage(@RequestBody @Validated PagingDTO<StocktakingTaskDTO.PagingParamDTO> dto) {
        PagingVO<StocktakingTaskDTO.PagingViewDTO> pagingVO = stocktakingTaskService.paging(dto);
        return success(pagingVO);
    }


    /**
     * 提交
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.SUBMIT, desc = "提交盘点任务")
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:stocktakingTask:submit",
            serviceClass = StocktakingTaskService.class,
            keyIdName = "ids"
    )
    public ApiResult submit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<String> ids = dto.getIds();
        for (String id : ids) {
            BatchResultDTO submit;
            try {
                submit = stocktakingTaskService.submit(id);
            } catch (Exception e) {
                log.error("盘点任务 提交审核失败", e);
                StocktakingTaskEntity entity = stocktakingTaskService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    submit = BatchResultDTO.fail(id, id, "盘点任务单不存在, 提交失败");
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
     * 检查 单号是否有SKU 盘点数量为0
     *
     * @param dto
     * @return
     */
    @PostMapping("/checkQty")
    public ApiResult<List<StocktakingTaskDTO.CheckResultDTO>> checkQty(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<StocktakingTaskDTO.CheckResultDTO> resultList = stocktakingTaskService.checkQty(dto.getIds());
        return success(resultList);
    }

    /**
     * 详情
     *
     * @param dto
     * @return
     */
    @LogViewService
    @PostMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:stocktakingTask:view",
            serviceClass = StocktakingTaskService.class,
            keyIdName = "id"
    )
    public ApiResult<StocktakingTaskDTO.ViewDTO> view(@RequestBody @Validated BaseIdDTO dto) {
        StocktakingTaskDTO.ViewDTO result = stocktakingTaskService.view(dto.getId());
        return success(result);
    }

    /**
     * 审核
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.APPROVE, desc = "审核盘点任务")
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:stocktakingTask:approve",
            serviceClass = StocktakingTaskService.class,
            keyIdName = "ids"
    )
    public ApiResult audit(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<String> ids = dto.getIds();
        for (String id : ids) {
            StocktakingTaskEntity entity = stocktakingTaskService.getById(id);
            BatchResultDTO submit;
            try {
                submit = stocktakingTaskService.approve(id, new ApproveOneDTO(id, dto.getType(), dto.getComment()));
                StocktakingTaskEntity laterEntity = stocktakingTaskService.getById(id);
                ApproveStatusEnum approveStatus = laterEntity.getApproveStatus();
                if(ApproveStatusEnum.APPROVE.equals(approveStatus)){
                    // 删除缓存
                    List<StocktakingTaskDetailDTO.ViewDTO> detailList = stocktakingTaskDetailService.listByMainId(id);
                    detailList.forEach(detail -> {
                        String key = CharSequenceUtil.format(RedisKeyConstant.INVENTORY_LOCK, entity.getSourceCode(), "*",
                                detail.getWarehouseId(), detail.getWarehouseLocation(), detail.getSkuId(), "*");
                        redisUtil.keys(key).forEach(item -> redisUtil.del(item));
                    });
                }

            } catch (Exception e) {
                log.error("盘点任务 审核失败>>>>{}", e);
                if (ObjectUtil.isEmpty(entity)) {
                    submit = BatchResultDTO.fail(id, id, "盘点任务单不存在, 提交失败");
                    resultDTOS.add(submit);
                    continue;
                }
                String message = e.getMessage();
//                if(CharSequenceUtil.isBlank(message) && ObjectUtil.isNotEmpty(((UndeclaredThrowableException) e).getUndeclaredThrowable())){
//                    message = ((UndeclaredThrowableException) e).getUndeclaredThrowable().getMessage();
//                }
                submit = BatchResultDTO.fail(entity.getId(), entity.getCode(), message);
            }
            resultDTOS.add(submit);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 撤销流程
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.CANCEL, desc = "撤销盘点任务")
    @PostMapping("/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:stocktakingTask:cancelProcess",
            serviceClass = StocktakingTaskService.class,
            keyIdName = "ids"
    )
    public ApiResult cancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<String> ids = dto.getIds();
        for (String id : ids) {
            BatchResultDTO submit;
            try {
                submit = stocktakingTaskService.cancelProcess(id);
            } catch (Exception e) {
                log.error("盘点任务 撤销流程失败>>>>{}", e);
                StocktakingTaskEntity entity = stocktakingTaskService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    submit = BatchResultDTO.fail(id, id, "盘点任务单不存在, 提交失败");
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
     * 分配盘点人
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "盘点任务分配盘点人:盘点任务ids={ids},分配用户ids={userIdList}")
    @PostMapping("/assignUser")
    public ApiResult assignStocktakingUser(@RequestBody @Validated StocktakingTaskDTO.AssignUserDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<String> ids = dto.getIds();
        for (String id : ids) {
            BatchResultDTO submit;
            try {
                submit = stocktakingTaskService.assignUser(id, dto.getUserIdList());
            } catch (Exception e) {
                log.error("盘点任务 撤销流程失败>>>>{}", e);
                StocktakingTaskEntity entity = stocktakingTaskService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    submit = BatchResultDTO.fail(id, id, "盘点任务单不存在, 提交失败");
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
     * 导出
     * 数据
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出盘点任务")
    @PostMapping("/export")
    public ApiResult exportWarehouse(@RequestBody @Valid StocktakingTaskDTO.ExportDTO dto, HttpServletResponse response) {
        Boolean result = stocktakingTaskService.exportExcel(dto, response);
        return result ? success() : failure();
    }

    /**
     * 导入
     * 数据
     */
    @LogAction(value = LogActionEnum.IMPORT, desc = "导入盘点任务")
    @PostMapping("/import")
    public ApiResult exportWarehouse(@RequestParam(value = "excelFile") MultipartFile excelFile, HttpServletResponse response) {
        Boolean result = stocktakingTaskService.importFile(excelFile, response);
        return result ? success() : failure();
    }

    /**
     * 下载模板
     *
     * @return
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "下载模板盘点任务")
    @GetMapping("/downloadTemplate")
    public ApiResult downloadTemplate(HttpServletResponse response) {
        stocktakingTaskService.downloadTemplate(response);
        return success();
    }

}
