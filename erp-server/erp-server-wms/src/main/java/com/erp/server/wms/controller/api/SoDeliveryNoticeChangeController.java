package com.erp.server.wms.controller.api;


import com.common.business.annotation.WebAdvanceQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.enums.LogActionEnum;
import com.common.business.dto.base.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.wms.service.SoDeliveryNoticeChangeService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.wms.dto.SoDeliveryNoticeChangeDTO;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;
import com.erp.model.wms.entity.SoDeliveryNoticeChangeEntity;

/**
 * 发货通知变更单
 *
 * @author lrp
 * @since 2024-10-23
 */
@Slf4j
@RestController
@LogSystemModule("发货通知变更单")
@RequestMapping("/soDeliveryNoticeChange")
public class SoDeliveryNoticeChangeController extends BaseController {

    @Resource
    private SoDeliveryNoticeChangeService soDeliveryNoticeChangeService;
    /**
     * 详情/下推
     * @author lrp
     * @date:  2024-10-23
     * @param dto
     * @return ApiResult<String>
     */
    @PostMapping("/view")
    public ApiResult<SoDeliveryNoticeChangeDTO.ViewDTO> view(@RequestBody @Validated SoDeliveryNoticeChangeDTO.ViewIdDTO dto) {
        return success(soDeliveryNoticeChangeService.view(dto));
    }
    /**
     * 添加产品分页查询
     * @author lrp
     * @date:  2024-10-23
     * @param dto
     * @return ApiResult<String>
     */
    @PostMapping("/addProductPaging")
    @WebAdvanceQuery
    public ApiResult<PagingVO<SoDeliveryNoticeChangeDTO.ProductDTO>> addProductPaging(@RequestBody @Validated PagingDTO<SoDeliveryNoticeChangeDTO.ProductAddDTO> dto) {
        return success(soDeliveryNoticeChangeService.addProductPaging(dto));
    }

    /**
    * 新增
    * @author lrp
    * @date:  2024-10-23
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "发货通知变更单新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated SoDeliveryNoticeChangeDTO.ViewDTO dto) {
        return success(soDeliveryNoticeChangeService.add(dto));
    }

    /**
    * 修改
    * @author lrp
    * @date:  2024-10-23
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "发货通知变更单修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "wms:soDeliveryNoticeChange:update",
        serviceClass = SoDeliveryNoticeChangeService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated SoDeliveryNoticeChangeDTO.UpdateDTO dto) {
        soDeliveryNoticeChangeService.update(dto);
        return success();
    }

    /**
    * 获取状态统计
    * @return
    */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:soDeliveryNoticeChange:paging",
            tableAlias = ""
    )
    public ApiResult<List<SoDeliveryNoticeChangeDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
       return success(soDeliveryNoticeChangeService.tabList(dto));
    }

    /**
    * 列表查询
    * @author lrp
    * @date: 2024-10-23
    * @param dto
    * @return ApiResult<PagingVO<SoDeliveryNoticeChangeDTO.ListDTO>>
    */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:soDeliveryNoticeChange:paging",
            tableAlias = ""
    )
    public ApiResult<PagingVO<SoDeliveryNoticeChangeDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<SoDeliveryNoticeChangeDTO.PagingParamDTO> dto) {
        return success(soDeliveryNoticeChangeService.paging(dto));
    }

    /**
    * 新增并提交审核
    * @author lrp
    * @date:  2024-10-23
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/addAndSubmit")
    public ApiResult<BaseResultDTO.AddDTO> addAndSubmit(@RequestBody @Validated SoDeliveryNoticeChangeDTO.ViewDTO dto) {
        BaseResultDTO.AddDTO result = soDeliveryNoticeChangeService.addAndSubmit(dto);
        return success(result);
    }

    /**
    * 修改并提交审核
    * @author lrp
    * @date:  2024-10-23
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/updateAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:soDeliveryNoticeChange:updateAndSubmit",
            serviceClass = SoDeliveryNoticeChangeService.class,
            keyIdName = "id")
    public ApiResult<Void> updateAndSubmit(@RequestBody @Validated SoDeliveryNoticeChangeDTO.UpdateDTO dto) {
        soDeliveryNoticeChangeService.updateAndSubmit(dto);
        return success();
    }

    /**
    * 提交审核
    * @author lrp
    * @date:  2024-10-23
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:soDeliveryNoticeChange:submit",
            serviceClass = SoDeliveryNoticeChangeService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.SUBMIT, desc = "发货通知变更单提交审核")
    public ApiResult<List<BatchResultDTO>> submit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		// TODO 数据查询放入外层，处理结果统一更新或单条更新
		List<SoDeliveryNoticeChangeEntity> list = soDeliveryNoticeChangeService.lambdaQuery().in(SoDeliveryNoticeChangeEntity::getId, ids).list();
		Map<String, SoDeliveryNoticeChangeEntity> idEntityMap = list.stream().collect(Collectors.toMap(SoDeliveryNoticeChangeEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO submit;
            try {
                submit = soDeliveryNoticeChangeService.submit(id);
            }catch (Exception e){
                log.error("发货通知变更单 提交审核失败",e);
                SoDeliveryNoticeChangeEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    submit = BatchResultDTO.fail(id, id, "发货通知变更单不存在, 提交失败");
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
    * @author lrp
    * @date:  2024-10-23
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:soDeliveryNoticeChange:approve",
            serviceClass = SoDeliveryNoticeChangeService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.APPROVE, desc = "发货通知变更单审核")
    public ApiResult<List<BatchResultDTO>> approve(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		// TODO 数据查询放入外层，处理结果统一更新或单条更新
		List<SoDeliveryNoticeChangeEntity> list = soDeliveryNoticeChangeService.lambdaQuery().in(SoDeliveryNoticeChangeEntity::getId, ids).list();
		Map<String, SoDeliveryNoticeChangeEntity> idEntityMap = list.stream().collect(Collectors.toMap(SoDeliveryNoticeChangeEntity::getId, w -> w));
        for (String id : ids) {
            BatchResultDTO approveResult;
            try {
                approveResult = soDeliveryNoticeChangeService.approve(new ApproveOneDTO(id, dto.getType(),dto.getComment()));
            }catch (Exception e){
                log.error("发货通知变更单审核失败",e);
                SoDeliveryNoticeChangeEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    approveResult = BatchResultDTO.fail(id, id, "发货通知变更单不存在, 审核失败");
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
    * @author lrp
    * @date:  2024-10-23
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/disApprove")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:soDeliveryNoticeChange:disApprove",
            serviceClass = SoDeliveryNoticeChangeService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DISAPPROVE, desc = "发货通知变更单反审核")
    public ApiResult<List<BatchResultDTO>> disApprove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		// TODO 数据查询放入外层，处理结果统一更新或单条更新
		List<SoDeliveryNoticeChangeEntity> list = soDeliveryNoticeChangeService.lambdaQuery().in(SoDeliveryNoticeChangeEntity::getId, ids).list();
		Map<String, SoDeliveryNoticeChangeEntity> idEntityMap = list.stream().collect(Collectors.toMap(SoDeliveryNoticeChangeEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO disApproveResult;
            try {
                disApproveResult = soDeliveryNoticeChangeService.disApprove(id);
            }catch (Exception e){
                log.error("发货通知变更单反审核失败",e);
                SoDeliveryNoticeChangeEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    disApproveResult = BatchResultDTO.fail(id, id, "发货通知变更单不存在, 反审核失败");
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
    * @author lrp
    * @date:  2024-10-23
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:soDeliveryNoticeChange:delete",
            serviceClass = SoDeliveryNoticeChangeService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DELETE, desc = "发货通知变更单删除")
    public ApiResult<List<BatchResultDTO>> delete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		// TODO 数据查询放入外层，处理结果统一更新或单条更新
		List<SoDeliveryNoticeChangeEntity> list = soDeliveryNoticeChangeService.lambdaQuery().in(SoDeliveryNoticeChangeEntity::getId, ids).list();
		Map<String, SoDeliveryNoticeChangeEntity> idEntityMap = list.stream().collect(Collectors.toMap(SoDeliveryNoticeChangeEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = soDeliveryNoticeChangeService.delete(id);
            }catch (Exception e){
                log.error("发货通知变更单删除失败",e);
                SoDeliveryNoticeChangeEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "发货通知变更单不存在, 删除失败");
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
    * @author lrp
    * @date:  2024-10-23
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:soDeliveryNoticeChange:cancelProcess",
            serviceClass = SoDeliveryNoticeChangeService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.CANCEL, desc = "发货通知变更单撤销")
    public ApiResult<List<BatchResultDTO>> cancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        // TODO 数据查询放入外层，处理结果统一更新或单条更新
        List<SoDeliveryNoticeChangeEntity> list = soDeliveryNoticeChangeService.lambdaQuery().in(SoDeliveryNoticeChangeEntity::getId, ids).list();
        Map<String, SoDeliveryNoticeChangeEntity> idEntityMap = list.stream().collect(Collectors.toMap(SoDeliveryNoticeChangeEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO cancelResult;
            try {
                cancelResult = soDeliveryNoticeChangeService.cancelProcess(id);
            }catch (Exception e){
                log.error("发货通知变更单撤回流程失败",e);
                SoDeliveryNoticeChangeEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    cancelResult = BatchResultDTO.fail(id, id, "发货通知变更单不存在, 撤回流程失败");
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
    * 导出Excel数据
    * @author lrp
    * @date:  2024-10-23
    * @param dto
    * @param response
    * @return
    */
    @PostMapping("/export")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:soDeliveryNoticeChange:export",
            tableAlias = ""
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "发货通知变更单导出Excel数据")
    public void exportList(@RequestBody @Validated SoDeliveryNoticeChangeDTO.ExportDTO dto, HttpServletResponse response) {
        soDeliveryNoticeChangeService.exportList(dto, response);
    }


}
