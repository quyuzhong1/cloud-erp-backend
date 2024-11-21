package com.erp.server.wms.controller.api;


import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.*;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.wms.dto.RequisitionApplicationChangeDTO;
import com.erp.model.wms.entity.RequisitionApplicationChangeEntity;
import com.erp.server.wms.service.RequisitionApplicationChangeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 要货申请变更单
 *
 * @author lrp
 * @since 2024-11-18
 */
@Slf4j
@RestController
@LogSystemModule("要货申请变更单")
@RequestMapping("/requisitionApplicationChange")
public class RequisitionApplicationChangeController extends BaseController {

    @Resource
    private RequisitionApplicationChangeService requisitionApplicationChangeService;

    /**
     * 添加产品分页查询
     * @author lrp
     * @date:  2024-10-23
     * @param dto
     * @return ApiResult<String>
     */
    @PostMapping("/addProductPaging")
    @WebAdvanceQuery
    public ApiResult<PagingVO<RequisitionApplicationChangeDTO.ProductDTO>> addProductPaging(@RequestBody @Validated PagingDTO<RequisitionApplicationChangeDTO.ProductAddDTO> dto) {
        return success(requisitionApplicationChangeService.addProductPaging(dto));
    }

    /**
    * 新增
    * @author lrp
    * @date:  2024-11-18
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "要货申请变更单新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated RequisitionApplicationChangeDTO.ViewDTO dto) {
        return success(requisitionApplicationChangeService.add(dto));
    }

    /**
    * 修改
    * @author lrp
    * @date:  2024-11-18
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "要货申请变更单修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "wms:requisitionApplicationChange:update",
        serviceClass = RequisitionApplicationChangeService.class,
        keyIdName = "id")
    public ApiResult<Objects> update(@RequestBody @Validated RequisitionApplicationChangeDTO.ViewDTO dto) {
        requisitionApplicationChangeService.update(dto);
        return success();
    }

    /**
    * 获取状态统计
    * @return
    */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:requisitionApplicationChange:paging",
            tableAlias = ""
    )
    public ApiResult<List<RequisitionApplicationChangeDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
       return success(requisitionApplicationChangeService.tabList(dto));
    }

    /**
    * 列表查询
    * @author lrp
    * @date: 2024-11-18
    * @param dto
    * @return ApiResult<PagingVO<RequisitionApplicationChangeDTO.ListDTO>>
    */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:requisitionApplicationChange:paging",
            tableAlias = ""
    )
    public ApiResult<PagingVO<RequisitionApplicationChangeDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<RequisitionApplicationChangeDTO.PagingParamDTO> dto) {
        return success(requisitionApplicationChangeService.paging(dto));
    }

    /**
    * 新增并提交审核
    * @author lrp
    * @date:  2024-11-18
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/addAndSubmit")
    public ApiResult<BaseResultDTO.AddDTO> addAndSubmit(@RequestBody @Validated RequisitionApplicationChangeDTO.ViewDTO dto) {
        BaseResultDTO.AddDTO result = requisitionApplicationChangeService.addAndSubmit(dto);
        return success(result);
    }

    /**
    * 修改并提交审核
    * @author lrp
    * @date:  2024-11-18
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/updateAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:requisitionApplicationChange:updateAndSubmit",
            serviceClass = RequisitionApplicationChangeService.class,
            keyIdName = "id")
    public ApiResult<Void> updateAndSubmit(@RequestBody @Validated RequisitionApplicationChangeDTO.ViewDTO dto) {
        requisitionApplicationChangeService.updateAndSubmit(dto);
        return success();
    }

    /**
    * 提交审核
    * @author lrp
    * @date:  2024-11-18
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:requisitionApplicationChange:submit",
            serviceClass = RequisitionApplicationChangeService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.SUBMIT, desc = "要货申请变更单提交审核")
    public ApiResult<List<BatchResultDTO>> submit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		// TODO 数据查询放入外层，处理结果统一更新或单条更新
		List<RequisitionApplicationChangeEntity> list = requisitionApplicationChangeService.lambdaQuery().in(RequisitionApplicationChangeEntity::getId, ids).list();
		Map<String, RequisitionApplicationChangeEntity> idEntityMap = list.stream().collect(Collectors.toMap(RequisitionApplicationChangeEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO submit;
            try {
                submit = requisitionApplicationChangeService.submit(id);
            }catch (Exception e){
                log.error("要货申请变更单 提交审核失败",e);
                RequisitionApplicationChangeEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    submit = BatchResultDTO.fail(id, id, "要货申请变更单不存在, 提交失败");
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
    * @date:  2024-11-18
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:requisitionApplicationChange:approve",
            serviceClass = RequisitionApplicationChangeService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.APPROVE, desc = "要货申请变更单审核")
    public ApiResult<List<BatchResultDTO>> approve(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		// TODO 数据查询放入外层，处理结果统一更新或单条更新
		List<RequisitionApplicationChangeEntity> list = requisitionApplicationChangeService.lambdaQuery().in(RequisitionApplicationChangeEntity::getId, ids).list();
		Map<String, RequisitionApplicationChangeEntity> idEntityMap = list.stream().collect(Collectors.toMap(RequisitionApplicationChangeEntity::getId, w -> w));
        for (String id : ids) {
            BatchResultDTO approveResult;
            try {
                approveResult = requisitionApplicationChangeService.approve(new ApproveOneDTO(id, dto.getType(),dto.getComment()));
            }catch (Exception e){
                log.error("要货申请变更单审核失败",e);
                RequisitionApplicationChangeEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    approveResult = BatchResultDTO.fail(id, id, "要货申请变更单不存在, 审核失败");
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
    * @date:  2024-11-18
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/disApprove")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:requisitionApplicationChange:disApprove",
            serviceClass = RequisitionApplicationChangeService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DISAPPROVE, desc = "要货申请变更单反审核")
    public ApiResult<List<BatchResultDTO>> disApprove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		// TODO 数据查询放入外层，处理结果统一更新或单条更新
		List<RequisitionApplicationChangeEntity> list = requisitionApplicationChangeService.lambdaQuery().in(RequisitionApplicationChangeEntity::getId, ids).list();
		Map<String, RequisitionApplicationChangeEntity> idEntityMap = list.stream().collect(Collectors.toMap(RequisitionApplicationChangeEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO disApproveResult;
            try {
                disApproveResult = requisitionApplicationChangeService.disApprove(id);
            }catch (Exception e){
                log.error("要货申请变更单反审核失败",e);
                RequisitionApplicationChangeEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    disApproveResult = BatchResultDTO.fail(id, id, "要货申请变更单不存在, 反审核失败");
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
    * @date:  2024-11-18
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:requisitionApplicationChange:delete",
            serviceClass = RequisitionApplicationChangeService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DELETE, desc = "要货申请变更单删除")
    public ApiResult<List<BatchResultDTO>> delete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		// TODO 数据查询放入外层，处理结果统一更新或单条更新
		List<RequisitionApplicationChangeEntity> list = requisitionApplicationChangeService.lambdaQuery().in(RequisitionApplicationChangeEntity::getId, ids).list();
		Map<String, RequisitionApplicationChangeEntity> idEntityMap = list.stream().collect(Collectors.toMap(RequisitionApplicationChangeEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = requisitionApplicationChangeService.delete(id);
            }catch (Exception e){
                log.error("要货申请变更单删除失败",e);
                RequisitionApplicationChangeEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "要货申请变更单不存在, 删除失败");
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
    * @date:  2024-11-18
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:requisitionApplicationChange:cancelProcess",
            serviceClass = RequisitionApplicationChangeService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.CANCEL, desc = "要货申请变更单撤销")
    public ApiResult<List<BatchResultDTO>> cancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        // TODO 数据查询放入外层，处理结果统一更新或单条更新
        List<RequisitionApplicationChangeEntity> list = requisitionApplicationChangeService.lambdaQuery().in(RequisitionApplicationChangeEntity::getId, ids).list();
        Map<String, RequisitionApplicationChangeEntity> idEntityMap = list.stream().collect(Collectors.toMap(RequisitionApplicationChangeEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO cancelResult;
            try {
                cancelResult = requisitionApplicationChangeService.cancelProcess(id);
            }catch (Exception e){
                log.error("要货申请变更单撤回流程失败",e);
                RequisitionApplicationChangeEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    cancelResult = BatchResultDTO.fail(id, id, "要货申请变更单不存在, 撤回流程失败");
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
    * 详情/下推
    * @author lrp
    * @date:  2024-11-18
    * @return ApiResult<RequisitionApplicationChangeDTO.ViewDTO>>
    */
    @PostMapping("/view")
    public ApiResult<RequisitionApplicationChangeDTO.ViewDTO> view(@RequestBody @Validated RequisitionApplicationChangeDTO.ViewIdDTO dto) {
        return success(requisitionApplicationChangeService.view(dto));
    }

    /**
    * 导出Excel数据
    * @author lrp
    * @date:  2024-11-18
    * @param dto
    * @param response
    * @return
    */
    @PostMapping("/export")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:requisitionApplicationChange:export",
            tableAlias = ""
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "要货申请变更单导出Excel数据")
    public void exportList(@RequestBody @Validated RequisitionApplicationChangeDTO.ExportDTO dto, HttpServletResponse response) {
        requisitionApplicationChangeService.exportList(dto, response);
    }


}
