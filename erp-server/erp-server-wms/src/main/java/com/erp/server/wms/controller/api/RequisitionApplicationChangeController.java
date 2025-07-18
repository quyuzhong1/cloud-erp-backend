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
import com.common.core.enums.ApiError;
import com.common.core.enums.LogActionEnum;
import com.common.core.exception.ServiceException;
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
            tableAlias = "rac"
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
            tableAlias = "rac"
    )
    @WebAdvanceQuery
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
    public ApiResult<BaseResultDTO.AddAndSubmmitDTO> addAndSubmit(@RequestBody @Validated RequisitionApplicationChangeDTO.ViewDTO dto) {
        // 新增
        BaseResultDTO.AddDTO resultAdd;
        try {
            resultAdd = requisitionApplicationChangeService.add(dto);
        } catch (ServiceException e) {
            log.error("新增失败，dto: {}", dto, e);
            return failure(e.getMessage(),new BaseResultDTO.AddAndSubmmitDTO("","",Boolean.FALSE));
        } catch (Exception e) {
            log.error("新增失败，dto: {}", dto, e);
            return failure(ApiError.ERROR_1019.msg,new BaseResultDTO.AddAndSubmmitDTO("","",Boolean.FALSE));
        }
        //提审
        try {
            requisitionApplicationChangeService.submit(resultAdd.getId());;
        } catch (ServiceException e) {
            log.error("提交审批失败，ID: {}", resultAdd.getId(), e);
            return failure(e.getMessage(),new BaseResultDTO.AddAndSubmmitDTO(resultAdd.getId(),resultAdd.getCode(),Boolean.TRUE));
        } catch (Exception e) {
            log.error("提交审批失败，ID: {}", resultAdd.getId(), e);
            return failure(ApiError.RETRY_SUBMIT_ERROR.msg,new BaseResultDTO.AddAndSubmmitDTO(resultAdd.getId(),resultAdd.getCode(),Boolean.TRUE));
        }

        return success(new BaseResultDTO.AddAndSubmmitDTO(resultAdd.getId(), resultAdd.getCode(),Boolean.TRUE));
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
    public ApiResult<BaseResultDTO.AddAndSubmmitDTO> updateAndSubmit(@RequestBody @Validated RequisitionApplicationChangeDTO.ViewDTO dto) {
        try {
            requisitionApplicationChangeService.update(dto);
        } catch (ServiceException e) {
            log.error("更新失败，dto: {}", dto, e);
            return failure(e.getMessage(), new BaseResultDTO.AddAndSubmmitDTO(dto.getId(),"",Boolean.FALSE));
        } catch (Exception e) {
            log.error("更新失败，dto: {}", dto, e);
            return failure(ApiError.ERROR_1020.msg,new BaseResultDTO.AddAndSubmmitDTO(dto.getId(),"",Boolean.FALSE));
        }
        //提审
        try {
            requisitionApplicationChangeService.submit(dto.getId());
        } catch (ServiceException e) {
            log.error("提交审批失败，ID: {}", dto.getId(), e);
            return failure( e.getMessage(),new BaseResultDTO.AddAndSubmmitDTO(dto.getId(),"",Boolean.TRUE));
        } catch (Exception e) {
            log.error("提交审批失败，ID: {}", dto.getId(), e);
            return failure(ApiError.RETRY_SUBMIT_ERROR.msg,new BaseResultDTO.AddAndSubmmitDTO(dto.getId(),"",Boolean.TRUE));
        }
        return success(new BaseResultDTO.AddAndSubmmitDTO(dto.getId(),"",Boolean.TRUE));
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
    public ApiResult<List<BatchResultDTO>> approve(@RequestBody @Validated RequisitionApplicationChangeDTO.ApproveDTO dto) {
        List<RequisitionApplicationChangeDTO.ApproveView> approveViewList = dto.getApproveViewList();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(approveViewList.size());
        Map<String,List<RequisitionApplicationChangeDTO.ApproveView>> map = approveViewList.stream().collect(Collectors.groupingBy(RequisitionApplicationChangeDTO.ApproveView::getId));
        map.forEach((id,list)->{
            BatchResultDTO approveResult;
            try {
                approveResult = requisitionApplicationChangeService.approve(id,list,dto.getType());
            }catch (Exception e){
                log.error("要货申请变更单审核失败",e);
                approveResult = BatchResultDTO.fail(id, list.get(0).getCode(), e.getMessage());
            }
            resultDTOS.add(approveResult);
        });
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 作废
     * @author lrp
     */
    @PostMapping("/invalid")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:requisitionApplicationChange:invalid",
            serviceClass = RequisitionApplicationChangeService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.INVALID, desc = "发货通知变更单作废")
    public ApiResult<List<BatchResultDTO>> invalid(@RequestBody @Validated  BaseIdsDTO.RemarkDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<RequisitionApplicationChangeEntity> list = requisitionApplicationChangeService.lambdaQuery().in(RequisitionApplicationChangeEntity::getId, ids).list();
        Map<String, RequisitionApplicationChangeEntity> idEntityMap = list.stream().collect(Collectors.toMap(RequisitionApplicationChangeEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = requisitionApplicationChangeService.invalid(id,dto.getRemark());
            }catch (Exception e){
                log.error("要货申请变更单作废失败",e);
                RequisitionApplicationChangeEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "要货申请变更单不存在, 作废失败");
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
    public ApiResult<?> exportList(@RequestBody @Validated RequisitionApplicationChangeDTO.ExportDTO dto, HttpServletResponse response) {
        requisitionApplicationChangeService.exportList(dto, response);
        return success();
    }


    /**
     * 审核弹窗
     * @author lrp
     * @date:  2024-11-18
     * @return ApiResult<RequisitionApplicationChangeDTO.ViewDTO>>
     */
    @PostMapping("/approveView")
    public ApiResult<List<RequisitionApplicationChangeDTO.ApproveView>> approveView(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        return success(requisitionApplicationChangeService.approveView(dto));
    }

}
