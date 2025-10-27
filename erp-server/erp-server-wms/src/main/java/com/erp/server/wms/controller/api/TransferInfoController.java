package com.erp.server.wms.controller.api;


import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.ApproveDTO;
import com.common.business.dto.base.*;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.enums.LogActionEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.wms.dto.TransferInfoDTO;
import com.erp.model.wms.entity.TransferInfoEntity;
import com.erp.server.wms.query.TransferInfoQueryHandler;
import com.erp.server.wms.service.TransferInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 直接调拨单主表
 *
 * @author will
 * @since 2023-05-10
 */
@Slf4j
@RestController
@LogSystemModule("直接调拨单")
@RequestMapping("/transferInfo")
public class TransferInfoController extends BaseController {

    @Resource
    private TransferInfoService transferInfoService;

    /**
     * 列表查询
     * @author Will
     * @date: 2023/5/10 19:56
     * @param dto
     * @return ApiResult<PagingVO<ListDTO>>
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "warehouse_keeper_id,create_user_id",
            warehouseTableField = "tid.in_warehouse_id,tid.out_warehouse_id",
            menuCode = "wms:transferInfo:paging",
            tableAlias = "ti"
    )
    @WebAdvanceQuery(handler = TransferInfoQueryHandler.class)
    public ApiResult<PagingVO<TransferInfoDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<TransferInfoDTO.SearchParamDTO> dto) {
        PagingVO<TransferInfoDTO.ListDTO> pagingVO = transferInfoService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 列表数量
     * @author Will
     * @date: 2023/5/10 20:08
     * @param dto
     * @return ApiResult<List<ListStatusCountDTO>>
     */
    @PostMapping("/listCount")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "warehouse_keeper_id,create_user_id",
            warehouseTableField = "tid.in_warehouse_id,tid.out_warehouse_id",
            menuCode = "wms:transferInfo:paging",
            tableAlias = "ti"
    )
    public ApiResult<List<TransferInfoDTO.ListStatusCountDTO>> listCount(@RequestBody PermissionsDTO dto) {
        List<TransferInfoDTO.ListStatusCountDTO> list = transferInfoService.listCount(dto);
        return success(list);
    }

    /**
     * 新增
     * @author Will
     * @date: 2023/5/10 19:58
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.INSERT, desc = "新增直接调拨单")
    @PostMapping("/add")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "warehouse_keeper_id,create_user_id",
            menuCode = "wms:transferInfo:add",
            serviceClass = TransferInfoService.class,
            keyIdName = "id")
    public ApiResult add(@RequestBody @Validated TransferInfoDTO.AddDTO dto) {
        String id = transferInfoService.add(dto);
        return CharSequenceUtil.isNotBlank(id) ? success() : failure();
    }

    /**
     * 新增并提交
     * @author Will
     * @date: 2023/5/10 19:59
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.ADD_AND_SUBMIT, desc = "新增并提交直接调拨单")
    @PostMapping("/addAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "warehouse_keeper_id,create_user_id",
            menuCode = "wms:transferInfo:add",
            serviceClass = TransferInfoService.class,
            keyIdName = "id")
    public ApiResult addAndSubmit(@RequestBody @Validated TransferInfoDTO.AddDTO dto) {
        // 新增
        TransferInfoEntity entity;
        try {
            String id = transferInfoService.add(dto);
            entity = transferInfoService.getById(id);
            if (ObjUtil.isNull(entity)) {
                throw new ServiceException(ApiError.NOT_EXIST_BILL,"直接调拨单");
            }
        } catch (ServiceException e) {
            log.error("新增失败，dto: {}", dto, e);
            return failure(e.getMessage(),new BaseResultDTO.AddAndSubmmitDTO("","",Boolean.FALSE));
        } catch (Exception e) {
            log.error("新增失败，dto: {}", dto, e);
            return  failure(ApiError.ERROR_1019.msg, new BaseResultDTO.AddAndSubmmitDTO("","",Boolean.FALSE));
        }

        //提审
        try {
            transferInfoService.submit(entity, Boolean.TRUE);
        } catch (ServiceException e) {
            log.error("提交审批失败，ID: {}", entity.getId(), e);
            return failure(e.getMessage(),new BaseResultDTO.AddAndSubmmitDTO(entity.getId(),entity.getCode(),Boolean.TRUE));
        } catch (Exception e) {
            log.error("提交审批失败，ID: {}", entity.getId(), e);
            return failure(ApiError.RETRY_SUBMIT_ERROR.msg,new BaseResultDTO.AddAndSubmmitDTO(entity.getId(),entity.getCode(),Boolean.TRUE));
        }

        return success(new BaseResultDTO.AddAndSubmmitDTO(entity.getId(), entity.getCode(),Boolean.TRUE));
    }

    /**
     * 修改
     * @author Will
     * @date: 2023/5/10 20:02
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改直接调拨单")
    @PostMapping("/update")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "warehouse_keeper_id,create_user_id",
            menuCode = "wms:transferInfo:update",
            serviceClass = TransferInfoService.class,
            keyIdName = "id")
    public ApiResult update(@RequestBody @Validated TransferInfoDTO.UpdateDTO dto) {
        Boolean flag = transferInfoService.update(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 修改并提交
     * @author Will
     * @date: 2023/5/10 20:02
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.UPDATE_AND_SUBMIT, desc = "修改并提交直接调拨单")
    @PostMapping("/updateAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "warehouse_keeper_id,create_user_id",
            menuCode = "wms:transferInfo:update",
            serviceClass = TransferInfoService.class,
            keyIdName = "id")
    public ApiResult<BaseResultDTO.AddAndSubmmitDTO> updateAndSubmit(@RequestBody @Validated TransferInfoDTO.UpdateDTO dto) {
        try {
            transferInfoService.update(dto);
        } catch (ServiceException e) {
            log.error("更新失败，dto: {}", dto, e);
            return failure(e.getMessage(), new BaseResultDTO.AddAndSubmmitDTO(dto.getId(),"",Boolean.FALSE));
        } catch (Exception e) {
            log.error("更新失败，dto: {}", dto, e);
            return failure(ApiError.ERROR_1020.msg,new BaseResultDTO.AddAndSubmmitDTO(dto.getId(),"",Boolean.FALSE));
        }
        //提审
        try {
            TransferInfoEntity entity = transferInfoService.getById(dto.getId());
            if (ObjUtil.isEmpty(entity)) {
                throw new ServiceException(ApiError.ERROR_99047);
            }
            //提交
            BatchResultDTO submit = transferInfoService.submit(entity, Boolean.TRUE);
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
     * 提交
     * @author Will
     * @date: 2023/5/10 20:00
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.SUBMIT, desc = "提交直接调拨单")
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "warehouse_keeper_id,create_user_id",
            menuCode = "wms:transferInfo:submit",
            serviceClass = TransferInfoService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> submit(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        Map<String, TransferInfoEntity> entityMap = transferInfoService.mapByIds(dto.getIds());
        for (String id : dto.getIds()) {
            TransferInfoEntity entity = entityMap.get(id);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"直接调拨单不存在"));
                continue;
            }
            try {
                resultDTOS.add(transferInfoService.submit(entity,Boolean.TRUE));
            }catch (Exception e){
                log.error("直接调拨单提交失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 查看详情
     * @author Will
     * @date: 2023/5/10 20:10
     * @param id
     * @return ApiResult
     */
    @LogViewService
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "warehouse_keeper_id,create_user_id",
            menuCode = "wms:transferInfo:view",
            serviceClass = TransferInfoService.class,
            keyIdName = "id")
    public ApiResult<TransferInfoDTO.ViewDTO> view(@RequestParam("id") String id) {
        TransferInfoDTO.ViewDTO dto = transferInfoService.view(id);
        return success(dto);
    }


    /**
     * 删除
     * @author Will
     * @date: 2023/5/10 20:09
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.DELETE, desc = "批量删除记录")
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "warehouse_keeper_id,create_user_id",
            menuCode = "wms:transferInfo:delete",
            serviceClass = TransferInfoService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> delete(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
            List<BatchResultDTO> resultDTOList = transferInfoService.delete(dto.getIds(), true);
            return resultDTOList.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOList) : failure(resultDTOList);
    }

    /**
     * 作废
     * @author Will
     * @date: 2023/5/10 20:11
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.INVALID, desc = "作废直接调拨单")
    @PostMapping("/invalid")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "warehouse_keeper_id,create_user_id",
            menuCode = "wms:transferInfo:invalid",
            serviceClass = TransferInfoService.class,
            keyIdName = "ids")
    public ApiResult invalid(@RequestBody @Validated BaseIdsDTO.RemarkDTO dto) {
        Boolean flag = transferInfoService.invalid(dto.getIds(),dto.getRemark());
        return flag == true ? success() : failure();
    }

    /**
     * 批量审核
     * @author Will
     * @date: 2023/5/10 20:11
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.APPROVE, desc = "审核直接调拨单")
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "warehouse_keeper_id,create_user_id",
            menuCode = "wms:transferInfo:approve",
            serviceClass = TransferInfoService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> approve(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<TransferInfoEntity> entityList = transferInfoService.listByIds(dto.getIds());
        for (String id : dto.getIds()) {
            TransferInfoEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"直接调拨单记录不存在"));
                continue;
            }
            try {
                resultDTOS.add(transferInfoService.approve(entity,dto.getType(),dto.getComment(),dto.getIsNeedProcess(),Boolean.TRUE,Boolean.TRUE));
            }catch (Exception e){
                log.error("直接调拨单审核失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 批量反审核
     * @author Will
     * @date: 2023/5/10 20:12
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.DISAPPROVE, desc = "反审核直接调拨单")
    @PostMapping("/disApprove")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "warehouse_keeper_id,create_user_id",
            menuCode = "wms:transferInfo:disApprove",
            serviceClass = TransferInfoService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> disApprove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<TransferInfoEntity> entityList = transferInfoService.listByIds(dto.getIds());
        for (String id : dto.getIds()) {
            TransferInfoEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"直接调拨单记录不存在"));
                continue;
            }
            try {
                resultDTOS.add(transferInfoService.disApprove(entity,Boolean.TRUE,Boolean.TRUE));
            }catch (Exception e){
                log.error("直接调拨单反审核失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 取消流程
     * @author Will
     * @date: 2023/5/10 20:24
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.CANCEL, desc = "撤销直接调拨单")
    @PostMapping("/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "warehouse_keeper_id,create_user_id",
            menuCode = "wms:transferInfo:cancelProcess",
            serviceClass = TransferInfoService.class,
            keyIdName = "ids")
    public ApiResult cancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean result = transferInfoService.cancelProcess(new ApproveDTO.BatchCancelProcessDTO(dto.getIds()));
        return result == true ? success() : failure();
    }

    /**
     * 导出
     * @author Will
     * @date: 2023/5/10 20:25
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出直接调拨单")
    @PostMapping(value = "/exportExcel")
    public ApiResult exportExcel(@RequestBody TransferInfoDTO.SearchParamDTO dto) {
        Boolean flag = transferInfoService.exportExcel(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 检测 sku 是否缺货
     * @param dto
     * @return
     */
    @PostMapping("/checkSkuInventory")
    public ApiResult<String> checkSkuInventory(@RequestBody @Validated() TransferInfoDTO.AddDTO dto) {
        String msg = transferInfoService.checkSkuInventory(dto, dto.getDetailList());
        return success( "", msg);
    }

    /**
     * 批量修改调拨日期
     * @author zdy
     * @date: 2024/10/24 20:12
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.CUSTOM_BATCH_UPDATE, desc = "批量修改调拨日期")
    @PostMapping("/updateBillDate")
    public ApiResult<List<BatchResultDTO>> updateBillDate(@RequestBody @Validated BaseIdsDTO.DateDTO dto) {
        List<String> ids = dto.getIds().stream().distinct().collect(Collectors.toList());
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<TransferInfoEntity> entityList = transferInfoService.listByIds(ids);
        for (String id : ids) {
            TransferInfoEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"直接调拨单记录不存在"));
                continue;
            }
            try {
                resultDTOS.add(transferInfoService.updateBillDate(entity,dto.getBillDate()));
            }catch (Exception e){
                log.error("直接调拨单修改调拨日期失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 处理数据
     * @author will
     * @date 2024/11/27 11:02
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/handleErrorData")
    public ApiResult<List<BatchResultDTO>> handleErrorData(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<String> ids = dto.getIds();
        for (String id : ids) {
            BatchResultDTO resultDTO;
            try {
                resultDTO = transferInfoService.handleErrorData(id);
            } catch (Exception e) {
                log.error("直接调拨单 处理数据失败", e);
                TransferInfoEntity entity = transferInfoService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    resultDTO = BatchResultDTO.fail(id, id, "直接调拨单不存在, 处理数据失败");
                    resultDTOS.add(resultDTO);
                    continue;
                }
                resultDTO = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(resultDTO);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }
}
