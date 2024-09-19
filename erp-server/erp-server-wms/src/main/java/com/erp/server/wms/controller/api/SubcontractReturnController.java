package com.erp.server.wms.controller.api;


import com.common.business.annotation.WebAdvanceQuery;
import com.erp.model.wms.dto.SubcontractIssueDTO;
import com.erp.server.wms.query.SubcontractIssueQueryHandler;
import com.erp.server.wms.query.SubcontractReturnQueryHandler;
import com.erp.server.wms.service.SubcontractReturnDetailService;
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
import com.erp.server.wms.service.SubcontractReturnService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.wms.dto.SubcontractReturnDTO;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;
import com.erp.model.wms.entity.SubcontractReturnEntity;

/**
 * 委外退料单
 *
 * @author zdy
 * @since 2024-09-15
 */
@Slf4j
@RestController
@LogSystemModule("委外退料单")
@RequestMapping("/subcontractReturn")
public class SubcontractReturnController extends BaseController {

    @Resource
    private SubcontractReturnService subcontractReturnService;
    @Resource
    private SubcontractReturnDetailService subcontractReturnDetailService;

    /**
    * 新增
    * @author zdy
    * @date:  2024-09-15
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "委外退料单新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated SubcontractReturnDTO.AddDTO dto) {
        return success(subcontractReturnService.add(dto));
    }

    /**
    * 修改
    * @author zdy
    * @date:  2024-09-15
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "委外退料单修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "wms:subcontractReturn:update",
        serviceClass = SubcontractReturnService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated SubcontractReturnDTO.UpdateDTO dto) {
        subcontractReturnService.update(dto);
        return success();
    }

    /**
    * 获取状态统计
    * @return
    */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:subcontractReturn:paging",
            tableAlias = "sr"
    )
    public ApiResult<List<SubcontractReturnDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
       return success(subcontractReturnService.tabList(dto));
    }

    /**
    * 列表查询
    * @author zdy
    * @date: 2024-09-15
    * @param dto
    * @return ApiResult<PagingVO<SubcontractReturnDTO.ListDTO>>
    */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:subcontractReturn:paging",
            tableAlias = "sr"
    )
    @WebAdvanceQuery(handler = SubcontractReturnQueryHandler.class)
    public ApiResult<PagingVO<SubcontractReturnDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<SubcontractReturnDTO.PagingParamDTO> dto) {
        return success(subcontractReturnService.paging(dto));
    }

    /**
    * 新增并提交审核
    * @author zdy
    * @date:  2024-09-15
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/addAndSubmit")
    public ApiResult<BaseResultDTO.AddDTO> addAndSubmit(@RequestBody @Validated SubcontractReturnDTO.AddDTO dto) {
        BaseResultDTO.AddDTO result = subcontractReturnService.addAndSubmit(dto);
        return success(result);
    }

    /**
    * 修改并提交审核
    * @author zdy
    * @date:  2024-09-15
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/updateAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:subcontractReturn:updateAndSubmit",
            serviceClass = SubcontractReturnService.class,
            keyIdName = "id")
    public ApiResult<Void> updateAndSubmit(@RequestBody @Validated SubcontractReturnDTO.UpdateDTO dto) {
        subcontractReturnService.updateAndSubmit(dto);
        return success();
    }

    /**
    * 提交审核
    * @author zdy
    * @date:  2024-09-15
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:subcontractReturn:submit",
            serviceClass = SubcontractReturnService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.SUBMIT, desc = "委外退料单提交审核")
    public ApiResult<List<BatchResultDTO>> submit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds().stream().distinct().collect(Collectors.toList());
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		//数据查询放入外层，处理结果统一更新或单条更新
		List<SubcontractReturnEntity> list = subcontractReturnService.lambdaQuery().in(SubcontractReturnEntity::getId, ids).list();
		Map<String, SubcontractReturnEntity> idEntityMap = list.stream().collect(Collectors.toMap(SubcontractReturnEntity::getId, w -> w));
        for (String id : ids) {
            BatchResultDTO submit;
            try {
                submit = subcontractReturnService.submit(id);
            }catch (Exception e){
                log.error("委外退料单 提交审核失败",e);
                SubcontractReturnEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    submit = BatchResultDTO.fail(id, id, "委外退料单不存在, 提交失败");
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
    * @author zdy
    * @date:  2024-09-15
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:subcontractReturn:approve",
            serviceClass = SubcontractReturnService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.APPROVE, desc = "委外退料单审核")
    public ApiResult<List<BatchResultDTO>> approve(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<String> ids = dto.getIds().stream().distinct().collect(Collectors.toList());
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		//数据查询放入外层，处理结果统一更新或单条更新
		List<SubcontractReturnEntity> list = subcontractReturnService.lambdaQuery().in(SubcontractReturnEntity::getId, ids).list();
		Map<String, SubcontractReturnEntity> idEntityMap = list.stream().collect(Collectors.toMap(SubcontractReturnEntity::getId, w -> w));
        for (String id : ids) {
            BatchResultDTO approveResult;
            try {
                approveResult = subcontractReturnService.approve(new ApproveOneDTO(id, dto.getType(),dto.getComment()));
            }catch (Exception e){
                log.error("委外退料单审核失败",e);
                SubcontractReturnEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    approveResult = BatchResultDTO.fail(id, id, "委外退料单不存在, 审核失败");
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
    * @author zdy
    * @date:  2024-09-15
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/disApprove")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:subcontractReturn:disApprove",
            serviceClass = SubcontractReturnService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DISAPPROVE, desc = "委外退料单反审核")
    public ApiResult<List<BatchResultDTO>> disApprove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds().stream().distinct().collect(Collectors.toList());
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		List<SubcontractReturnEntity> list = subcontractReturnService.lambdaQuery().in(SubcontractReturnEntity::getId, ids).list();
		Map<String, SubcontractReturnEntity> idEntityMap = list.stream().collect(Collectors.toMap(SubcontractReturnEntity::getId, w -> w));
        for (String id : ids) {
            BatchResultDTO disApproveResult;
            try {
                disApproveResult = subcontractReturnService.disApprove(id);
            }catch (Exception e){
                log.error("委外退料单反审核失败",e);
                SubcontractReturnEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    disApproveResult = BatchResultDTO.fail(id, id, "委外退料单不存在, 反审核失败");
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
    * @author zdy
    * @date:  2024-09-15
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:subcontractReturn:delete",
            serviceClass = SubcontractReturnService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DELETE, desc = "委外退料单删除")
    public ApiResult<List<BatchResultDTO>> delete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds().stream().distinct().collect(Collectors.toList());
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		List<SubcontractReturnEntity> list = subcontractReturnService.lambdaQuery().in(SubcontractReturnEntity::getId, ids).list();
		Map<String, SubcontractReturnEntity> idEntityMap = list.stream().collect(Collectors.toMap(SubcontractReturnEntity::getId, w -> w));
        for (String id : ids) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = subcontractReturnService.delete(id);
            }catch (Exception e){
                log.error("委外退料单删除失败",e);
                SubcontractReturnEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "委外退料单不存在, 删除失败");
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
    * 作废
    * @author zdy
    * @date:  2024-09-15
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/invalid")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:subcontractReturn:invalid",
            serviceClass = SubcontractReturnService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.INVALID, desc = "委外退料单作废")
    public ApiResult<List<BatchResultDTO>> invalid(@RequestBody @Validated BaseIdsDTO.RemarkDTO dto) {
        List<String> ids = dto.getIds().stream().distinct().collect(Collectors.toList());
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		List<SubcontractReturnEntity> list = subcontractReturnService.lambdaQuery().in(SubcontractReturnEntity::getId, ids).list();
		Map<String, SubcontractReturnEntity> idEntityMap = list.stream().collect(Collectors.toMap(SubcontractReturnEntity::getId, w -> w));
        for (String id : ids) {
            BatchResultDTO invalidResult;
            try {
                invalidResult = subcontractReturnService.invalid(id,dto.getRemark());
            }catch (Exception e){
                log.error("委外退料单作废失败",e);
                SubcontractReturnEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    invalidResult = BatchResultDTO.fail(id, id, "委外退料单不存在, 作废失败");
                    resultDTOS.add(invalidResult);
                    continue;
                }
                invalidResult = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(invalidResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
    * 撤销
    * @author zdy
    * @date:  2024-09-15
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:subcontractReturn:cancelProcess",
            serviceClass = SubcontractReturnService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.CANCEL, desc = "委外退料单撤销")
    public ApiResult<List<BatchResultDTO>> cancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds().stream().distinct().collect(Collectors.toList());
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<SubcontractReturnEntity> list = subcontractReturnService.lambdaQuery().in(SubcontractReturnEntity::getId, ids).list();
        Map<String, SubcontractReturnEntity> idEntityMap = list.stream().collect(Collectors.toMap(SubcontractReturnEntity::getId, w -> w));
        for (String id : ids) {
            BatchResultDTO cancelResult;
            try {
                cancelResult = subcontractReturnService.cancelProcess(id);
            }catch (Exception e){
                log.error("委外退料单撤回流程失败",e);
                SubcontractReturnEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    cancelResult = BatchResultDTO.fail(id, id, "委外退料单不存在, 撤回流程失败");
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
    * @author zdy
    * @date:  2024-09-15
    * @param id
    * @return ApiResult<SubcontractReturnDTO.ViewDTO>>
    */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:subcontractReturn:view",
            serviceClass = SubcontractReturnService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<SubcontractReturnDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(subcontractReturnService.view(id));
    }

    /**
    * 导出Excel数据
    * @author zdy
    * @date:  2024-09-15
    * @param dto
    * @param response
    * @return
    */
    @PostMapping("/export")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:subcontractReturn:export",
            tableAlias = "sr"
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "委外退料单导出Excel数据")
    public void exportList(@RequestBody @Validated SubcontractReturnDTO.PagingParamDTO dto, HttpServletResponse response) {
        subcontractReturnService.exportList(dto, response);
    }

    /**
     * 添加产品
     * @author zdy
     * @date: 2024-09-18
     * @param dto
     * @return ApiResult<PagingVO<SubcontractIssueDTO.SubcontractDetailListDTO>>
     */
    @PostMapping("/listSubcontractDetail")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:subcontractReturn:listSubcontractDetail",
            tableAlias = "sr"
    )
    public ApiResult<List<SubcontractReturnDTO.SubcontractDetailListDTO>> listSubcontractDetail(@RequestBody @Validated SubcontractReturnDTO.DetailPagingParamDTO dto) {
        return success(subcontractReturnService.listSubcontractDetail(dto));
    }
}
