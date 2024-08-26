package com.erp.server.wms.controller.api;


import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.*;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.wms.dto.SubcontractIssueDTO;
import com.erp.model.wms.entity.SubcontractIssueEntity;
import com.erp.server.wms.query.SubcontractIssueQueryHandler;
import com.erp.server.wms.service.SubcontractIssueService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 委外发料单
 *
 * @author will
 * @since 2024-01-08
 */
@Slf4j
@RestController
@LogSystemModule("委外发料单")
@RequestMapping("/subcontractIssue")
public class SubcontractIssueController extends BaseController {

    @Resource
    private SubcontractIssueService subcontractIssueService;

    /**
    * 新增
    * @author will
    * @date:  2024-01-08
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "委外发料单新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated SubcontractIssueDTO.AddDTO dto) {
        return success(subcontractIssueService.add(dto));
    }

    /**
    * 修改
    * @author will
    * @date:  2024-01-08
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "委外发料单修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "wms:subcontractIssue:update",
        serviceClass = SubcontractIssueService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated SubcontractIssueDTO.UpdateDTO dto) {
        subcontractIssueService.update(dto);
        return success();
    }

    /**
    * 获取状态统计
    * @return
    */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:subcontractIssue:paging",
            tableAlias = "si"
    )
    public ApiResult<List<SubcontractIssueDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
       return success(subcontractIssueService.tabList(dto));
    }

    /**
    * 列表查询
    * @author will
    * @date: 2024-01-08
    * @param dto
    * @return ApiResult<PagingVO<SubcontractIssueDTO.ListDTO>>
    */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:subcontractIssue:paging",
            tableAlias = "si"
    )
    @WebAdvanceQuery(handler = SubcontractIssueQueryHandler.class)
    public ApiResult<PagingVO<SubcontractIssueDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<SubcontractIssueDTO.PagingParamDTO> dto) {
        return success(subcontractIssueService.paging(dto));
    }

    /**
    * 提交审核
    * @author will
    * @date:  2024-01-08
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:subcontractIssue:submit",
            serviceClass = SubcontractIssueService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.SUBMIT, desc = "委外发料单提交审核")
    public ApiResult<List<BatchResultDTO>> submit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO submit;
            try {
                submit = subcontractIssueService.submit(id,Boolean.TRUE);
            }catch (Exception e){
                log.error("委外发料单 提交审核失败",e);
                SubcontractIssueEntity entity = subcontractIssueService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    submit = BatchResultDTO.fail(id, id, "委外发料单不存在, 提交失败");
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
    * @date:  2024-01-08
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:subcontractIssue:approve",
            serviceClass = SubcontractIssueService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.APPROVE, desc = "委外发料单审核")
    public ApiResult<List<BatchResultDTO>> approve(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : ids) {
            BatchResultDTO approveResult;
            try {
                approveResult = subcontractIssueService.approve(new ApproveOneDTO(id, dto.getType(),dto.getComment()));
            }catch (Exception e){
                log.error("委外发料单审核失败",e);
                SubcontractIssueEntity entity = subcontractIssueService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    approveResult = BatchResultDTO.fail(id, id, "委外发料单不存在, 审核失败");
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
    * @date:  2024-01-08
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/disApprove")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:subcontractIssue:disApprove",
            serviceClass = SubcontractIssueService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DISAPPROVE, desc = "委外发料单反审核")
    public ApiResult<List<BatchResultDTO>> disApprove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO disApproveResult;
            try {
                disApproveResult = subcontractIssueService.disApprove(id);
            }catch (Exception e){
                log.error("委外发料单反审核失败",e);
                SubcontractIssueEntity entity = subcontractIssueService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    disApproveResult = BatchResultDTO.fail(id, id, "委外发料单不存在, 反审核失败");
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
    * @date:  2024-01-08
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:subcontractIssue:delete",
            serviceClass = SubcontractIssueService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DELETE, desc = "委外发料单删除")
    public ApiResult<List<BatchResultDTO>> delete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = subcontractIssueService.delete(id);
            }catch (Exception e){
                log.error("委外发料单删除失败",e);
                SubcontractIssueEntity entity = subcontractIssueService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "委外发料单不存在, 删除失败");
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
    * @author will
    * @date:  2024-01-08
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/invalid")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:subcontractIssue:invalid",
            serviceClass = SubcontractIssueService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.INVALID, desc = "委外发料单作废")
    public ApiResult<List<BatchResultDTO>> invalid(@RequestBody @Validated BaseIdsDTO.RemarkDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO invalidResult;
            try {
                invalidResult = subcontractIssueService.invalid(id,dto.getRemark());
            }catch (Exception e){
                log.error("委外发料单作废失败",e);
                SubcontractIssueEntity entity = subcontractIssueService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    invalidResult = BatchResultDTO.fail(id, id, "委外发料单不存在, 作废失败");
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
    * @author will
    * @date:  2024-01-08
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:subcontractIssue:cancel",
            serviceClass = SubcontractIssueService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.CANCEL, desc = "委外发料单撤销")
    public ApiResult<List<BatchResultDTO>> cancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO cancelResult;
            try {
                cancelResult = subcontractIssueService.cancelProcess(id);
            }catch (Exception e){
                log.error("委外发料单撤回流程失败",e);
                SubcontractIssueEntity entity = subcontractIssueService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    cancelResult = BatchResultDTO.fail(id, id, "委外发料单不存在, 撤回流程失败");
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
    * @date:  2024-01-08
    * @param id
    * @return ApiResult<SubcontractIssueDTO.ViewDTO>>
    */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:subcontractIssue:view",
            serviceClass = SubcontractIssueService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<SubcontractIssueDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(subcontractIssueService.view(id));
    }

    /**
    * 导出Excel数据
    * @author will
    * @date:  2024-01-08
    * @param dto
    * @return
    */
    @PostMapping("/export")
    @LogAction(value = LogActionEnum.EXPORT, desc = "委外发料单导出Excel数据")
    public ApiResult<Boolean> exportList(@RequestBody @Validated SubcontractIssueDTO.PagingParamDTO dto) {
        subcontractIssueService.exportList(dto);
        return success(true);
    }

    /**
     * 添加产品
     * @author will
     * @date: 2024-01-08
     * @param dto
     * @return ApiResult<PagingVO<SubcontractIssueDTO.SubcontractDetailListDTO>>
     */
    @PostMapping("/listSubcontractDetail")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:subcontractIssue:listSubcontractDetail",
            tableAlias = ""
    )
    public ApiResult<List<SubcontractIssueDTO.SubcontractDetailListDTO>> listSubcontractDetail(@RequestBody @Validated SubcontractIssueDTO.DetailPagingParamDTO dto) {
        return success(subcontractIssueService.listSubcontractDetail(dto));
    }
}
