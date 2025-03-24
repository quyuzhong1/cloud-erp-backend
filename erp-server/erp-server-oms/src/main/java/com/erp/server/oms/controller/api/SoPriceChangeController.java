package com.erp.server.oms.controller.api;


import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.*;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.oms.dto.SoPriceChangeDTO;
import com.erp.model.oms.entity.SoPriceChangeEntity;
import com.erp.server.oms.service.SoPriceChangeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 销售价变更表
 *
 * @author will
 * @since 2025-03-24
 */
@Slf4j
@RestController
@LogSystemModule("销售价变更表")
@RequestMapping("/soPriceChange")
public class SoPriceChangeController extends BaseController {

    @Resource
    private SoPriceChangeService soPriceChangeService;

    /**
    * 新增
    * @author will
    * @date:  2025-03-24
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "销售价变更表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated SoPriceChangeDTO.AddDTO dto) {
        return success(soPriceChangeService.add(dto));
    }

    /**
    * 修改
    * @author will
    * @date:  2025-03-24
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "销售价变更表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "oms:soPriceChange:update",
        serviceClass = SoPriceChangeService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated SoPriceChangeDTO.UpdateDTO dto) {
        soPriceChangeService.update(dto);
        return success();
    }

    /**
    * 获取状态统计
    * @return
    */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "oms:soPriceChange:paging",
            tableAlias = ""
    )
    public ApiResult<List<SoPriceChangeDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
       return success(soPriceChangeService.tabList(dto));
    }

    /**
    * 列表查询
    * @author will
    * @date: 2025-03-24
    * @param dto
    * @return ApiResult<PagingVO<SoPriceChangeDTO.ListDTO>>
    */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "oms:soPriceChange:paging",
            tableAlias = ""
    )
    public ApiResult<PagingVO<SoPriceChangeDTO.PagingViewDTO>> paging(@RequestBody @Validated PagingDTO<SoPriceChangeDTO.PagingParamDTO> dto) {
        return success(soPriceChangeService.paging(dto));
    }

    /**
    * 新增并提交审核
    * @author will
    * @date:  2025-03-24
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/addAndSubmit")
    public ApiResult<BaseResultDTO.AddDTO> addAndSubmit(@RequestBody @Validated SoPriceChangeDTO.AddDTO dto) {
        BaseResultDTO.AddDTO result = soPriceChangeService.addAndSubmit(dto);
        return success(result);
    }

    /**
    * 修改并提交审核
    * @author will
    * @date:  2025-03-24
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/updateAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:soPriceChange:updateAndSubmit",
            serviceClass = SoPriceChangeService.class,
            keyIdName = "id")
    public ApiResult<Void> updateAndSubmit(@RequestBody @Validated SoPriceChangeDTO.UpdateDTO dto) {
        soPriceChangeService.updateAndSubmit(dto);
        return success();
    }

    /**
    * 提交审核
    * @author will
    * @date:  2025-03-24
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:soPriceChange:submit",
            serviceClass = SoPriceChangeService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.SUBMIT, desc = "销售价变更表提交审核")
    public ApiResult<List<BatchResultDTO>> submit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		// TODO 数据查询放入外层，处理结果统一更新或单条更新
		List<SoPriceChangeEntity> list = soPriceChangeService.lambdaQuery().in(SoPriceChangeEntity::getId, ids).list();
		Map<String, SoPriceChangeEntity> idEntityMap = list.stream().collect(Collectors.toMap(SoPriceChangeEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO submit;
            try {
                submit = soPriceChangeService.submit(id);
            }catch (Exception e){
                log.error("销售价变更单 提交审核失败",e);
                SoPriceChangeEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    submit = BatchResultDTO.fail(id, id, "销售价变更单不存在, 提交失败");
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
    * @author will
    * @date:  2025-03-24
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:soPriceChange:approve",
            serviceClass = SoPriceChangeService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.APPROVE, desc = "销售价变更表审核")
    public ApiResult<List<BatchResultDTO>> approve(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		// TODO 数据查询放入外层，处理结果统一更新或单条更新
		List<SoPriceChangeEntity> list = soPriceChangeService.lambdaQuery().in(SoPriceChangeEntity::getId, ids).list();
		Map<String, SoPriceChangeEntity> idEntityMap = list.stream().collect(Collectors.toMap(SoPriceChangeEntity::getId, w -> w));
        for (String id : ids) {
            BatchResultDTO approveResult;
            try {
                approveResult = soPriceChangeService.approve(new ApproveOneDTO(id, dto.getType(),dto.getComment()));
            }catch (Exception e){
                log.error("销售价变更单审核失败",e);
                SoPriceChangeEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    approveResult = BatchResultDTO.fail(id, id, "销售价变更单不存在, 审核失败");
                    resultDTOS.add(approveResult);
                    continue;
                }
                approveResult = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(approveResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
    * 反审核
    * @author will
    * @date:  2025-03-24
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/disApprove")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:soPriceChange:disApprove",
            serviceClass = SoPriceChangeService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DISAPPROVE, desc = "销售价变更表反审核")
    public ApiResult<List<BatchResultDTO>> disApprove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		// TODO 数据查询放入外层，处理结果统一更新或单条更新
		List<SoPriceChangeEntity> list = soPriceChangeService.lambdaQuery().in(SoPriceChangeEntity::getId, ids).list();
		Map<String, SoPriceChangeEntity> idEntityMap = list.stream().collect(Collectors.toMap(SoPriceChangeEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO disApproveResult;
            try {
                disApproveResult = soPriceChangeService.disApprove(id);
            }catch (Exception e){
                log.error("销售价变更单反审核失败",e);
                SoPriceChangeEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    disApproveResult = BatchResultDTO.fail(id, id, "销售价变更单不存在, 反审核失败");
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
    * @author will
    * @date:  2025-03-24
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:soPriceChange:delete",
            serviceClass = SoPriceChangeService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DELETE, desc = "销售价变更表删除")
    public ApiResult<List<BatchResultDTO>> delete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		// TODO 数据查询放入外层，处理结果统一更新或单条更新
		List<SoPriceChangeEntity> list = soPriceChangeService.lambdaQuery().in(SoPriceChangeEntity::getId, ids).list();
		Map<String, SoPriceChangeEntity> idEntityMap = list.stream().collect(Collectors.toMap(SoPriceChangeEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = soPriceChangeService.delete(id);
            }catch (Exception e){
                log.error("销售价变更单删除失败",e);
                SoPriceChangeEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "销售价变更单不存在, 删除失败");
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
    * @author will
    * @date:  2025-03-24
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:soPriceChange:cancelProcess",
            serviceClass = SoPriceChangeService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.CANCEL, desc = "销售价变更表撤销")
    public ApiResult<List<BatchResultDTO>> cancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        // TODO 数据查询放入外层，处理结果统一更新或单条更新
        List<SoPriceChangeEntity> list = soPriceChangeService.lambdaQuery().in(SoPriceChangeEntity::getId, ids).list();
        Map<String, SoPriceChangeEntity> idEntityMap = list.stream().collect(Collectors.toMap(SoPriceChangeEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO cancelResult;
            try {
                cancelResult = soPriceChangeService.cancelProcess(id);
            }catch (Exception e){
                log.error("销售价变更单撤回流程失败",e);
                SoPriceChangeEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    cancelResult = BatchResultDTO.fail(id, id, "销售价变更单不存在, 撤回流程失败");
                    resultDTOS.add(cancelResult);
                    continue;
                }
                cancelResult = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(cancelResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
    * 详情
    * @author will
    * @date:  2025-03-24
    * @param id
    * @return ApiResult<SoPriceChangeDTO.ViewDTO>>
    */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:soPriceChange:view",
            serviceClass = SoPriceChangeService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<SoPriceChangeDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(soPriceChangeService.view(id));
    }

    /**
    * 导出Excel数据
    * @author will
    * @date:  2025-03-24
    * @param dto
    * @param response
    * @return
    */
    @PostMapping("/export")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "oms:soPriceChange:export",
            tableAlias = ""
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "销售价变更表导出Excel数据")
    public void exportList(@RequestBody @Validated SoPriceChangeDTO.ExportDTO dto, HttpServletResponse response) {
        soPriceChangeService.exportList(dto, response);
    }


}
