package com.erp.server.oms.controller.api;


import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.validator.ValidList;
import com.common.core.utils.ExcelUtil;
import com.erp.server.oms.query.KolB2cApplicationQueryHandler;
import lombok.extern.slf4j.Slf4j;
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
import com.erp.server.oms.service.KolB2cApplicationService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.vo.PagingVO;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.oms.dto.KolB2cApplicationDTO;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;
import com.erp.model.oms.entity.KolB2cApplicationEntity;

/**
 * B2C寄样申请单
 *
 * @author jack
 * @since 2025-12-04
 */
@Slf4j
@RestController
@LogSystemModule("B2C寄样申请单")
@RequestMapping("/kolB2cApplication")
public class KolB2cApplicationController extends BaseController {

    @Resource
    private KolB2cApplicationService kolB2cApplicationService;

    /**
    * 新增
    * @author jack
    * @date:  2025-12-04
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "B2C寄样申请单新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated KolB2cApplicationDTO.AddDTO dto) {
        return success(kolB2cApplicationService.add(dto));
    }

    /**
    * 修改
    * @author jack
    * @date:  2025-12-04
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "B2C寄样申请单修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "oms:kolB2cApplication:update",
        serviceClass = KolB2cApplicationService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated KolB2cApplicationDTO.UpdateDTO dto) {
        kolB2cApplicationService.update(dto);
        return success();
    }

    /**
    * 获取状态统计
    * @return
    */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "oms:kolB2cApplication:paging",
            tableAlias = "kba"
    )
    public ApiResult<List<KolB2cApplicationDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
       return success(kolB2cApplicationService.tabList(dto));
    }

    /**
    * 列表查询
    * @author jack
    * @date: 2025-12-04
    * @param dto
    * @return ApiResult<PagingVO<KolB2cApplicationDTO.ListDTO>>
    */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "oms:kolB2cApplication:paging",
            tableAlias = "kba"
    )
    @WebAdvanceQuery(handler = KolB2cApplicationQueryHandler.class)
    public ApiResult<PagingVO<KolB2cApplicationDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<KolB2cApplicationDTO.PagingParamDTO> dto) {
        return success(kolB2cApplicationService.paging(dto));
    }

    /**
    * 新增并提交审核
    * @author jack
    * @date:  2025-12-04
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/addAndSubmit")
    public ApiResult<BaseResultDTO.AddDTO> addAndSubmit(@RequestBody @Validated KolB2cApplicationDTO.AddDTO dto) {
        BaseResultDTO.AddDTO result = kolB2cApplicationService.addAndSubmit(dto);
        return success(result);
    }

    /**
    * 修改并提交审核
    * @author jack
    * @date:  2025-12-04
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/updateAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:kolB2cApplication:updateAndSubmit",
            serviceClass = KolB2cApplicationService.class,
            keyIdName = "id")
    public ApiResult<Void> updateAndSubmit(@RequestBody @Validated KolB2cApplicationDTO.UpdateDTO dto) {
        kolB2cApplicationService.updateAndSubmit(dto);
        return success();
    }

    /**
    * 提交审核
    * @author jack
    * @date:  2025-12-04
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:kolB2cApplication:submit",
            serviceClass = KolB2cApplicationService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.SUBMIT, desc = "B2C寄样申请单提交审核")
    public ApiResult<List<BatchResultDTO>> batchSubmit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		List<KolB2cApplicationEntity> list = kolB2cApplicationService.lambdaQuery().in(KolB2cApplicationEntity::getId, ids).list();
		Map<String, KolB2cApplicationEntity> idEntityMap = list.stream().collect(Collectors.toMap(KolB2cApplicationEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO submit;
            try {
                submit = kolB2cApplicationService.submit(id);
            }catch (Exception e){
                log.error("B2C寄样申请单 提交审核失败",e);
                KolB2cApplicationEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    submit = BatchResultDTO.fail(id, id, "B2C寄样申请单不存在, 提交失败");
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
    * @author jack
    * @date:  2025-12-04
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:kolB2cApplication:approve",
            serviceClass = KolB2cApplicationService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.APPROVE, desc = "B2C寄样申请单审核")
    public ApiResult<List<BatchResultDTO>> batchApprove(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		List<KolB2cApplicationEntity> list = kolB2cApplicationService.lambdaQuery().in(KolB2cApplicationEntity::getId, ids).list();
		Map<String, KolB2cApplicationEntity> idEntityMap = list.stream().collect(Collectors.toMap(KolB2cApplicationEntity::getId, w -> w));
        for (String id : ids) {
            BatchResultDTO approveResult;
            try {
                approveResult = kolB2cApplicationService.approve(new ApproveOneDTO(id, dto.getType(),dto.getComment()));
            }catch (Exception e){
                log.error("B2C寄样申请单审核失败",e);
                KolB2cApplicationEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    approveResult = BatchResultDTO.fail(id, id, "B2C寄样申请单不存在, 审核失败");
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
    * @author jack
    * @date:  2025-12-04
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/disApprove")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:kolB2cApplication:disApprove",
            serviceClass = KolB2cApplicationService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DISAPPROVE, desc = "B2C寄样申请单反审核")
    public ApiResult<List<BatchResultDTO>> batchDisApprove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		List<KolB2cApplicationEntity> list = kolB2cApplicationService.lambdaQuery().in(KolB2cApplicationEntity::getId, ids).list();
		Map<String, KolB2cApplicationEntity> idEntityMap = list.stream().collect(Collectors.toMap(KolB2cApplicationEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO disApproveResult;
            try {
                disApproveResult = kolB2cApplicationService.disApprove(id);
            }catch (Exception e){
                log.error("B2C寄样申请单反审核失败",e);
                KolB2cApplicationEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    disApproveResult = BatchResultDTO.fail(id, id, "B2C寄样申请单不存在, 反审核失败");
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
    * @author jack
    * @date:  2025-12-04
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:kolB2cApplication:delete",
            serviceClass = KolB2cApplicationService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DELETE, desc = "B2C寄样申请单删除")
    public ApiResult<List<BatchResultDTO>> batchDelete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		List<KolB2cApplicationEntity> list = kolB2cApplicationService.lambdaQuery().in(KolB2cApplicationEntity::getId, ids).list();
		Map<String, KolB2cApplicationEntity> idEntityMap = list.stream().collect(Collectors.toMap(KolB2cApplicationEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = kolB2cApplicationService.delete(id);
            }catch (Exception e){
                log.error("B2C寄样申请单删除失败",e);
                KolB2cApplicationEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "B2C寄样申请单不存在, 删除失败");
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
    * @author jack
    * @date:  2025-12-04
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/invalid")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:kolB2cApplication:invalid",
            serviceClass = KolB2cApplicationService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.INVALID, desc = "B2C寄样申请单作废")
    public ApiResult<List<BatchResultDTO>> batchInvalid(@RequestBody @Validated BaseIdsDTO.RemarkDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		List<KolB2cApplicationEntity> list = kolB2cApplicationService.lambdaQuery().in(KolB2cApplicationEntity::getId, ids).list();
		Map<String, KolB2cApplicationEntity> idEntityMap = list.stream().collect(Collectors.toMap(KolB2cApplicationEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO invalidResult;
            try {
                invalidResult = kolB2cApplicationService.invalid(id,dto.getRemark());
            }catch (Exception e){
                log.error("B2C寄样申请单作废失败",e);
                KolB2cApplicationEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    invalidResult = BatchResultDTO.fail(id, id, "B2C寄样申请单不存在, 作废失败");
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
    * @author jack
    * @date:  2025-12-04
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:kolB2cApplication:cancelProcess",
            serviceClass = KolB2cApplicationService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.CANCEL, desc = "B2C寄样申请单撤销")
    public ApiResult<List<BatchResultDTO>> batchCancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<KolB2cApplicationEntity> list = kolB2cApplicationService.lambdaQuery().in(KolB2cApplicationEntity::getId, ids).list();
        Map<String, KolB2cApplicationEntity> idEntityMap = list.stream().collect(Collectors.toMap(KolB2cApplicationEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO cancelResult;
            try {
                cancelResult = kolB2cApplicationService.cancelProcess(id);
            }catch (Exception e){
                log.error("B2C寄样申请单撤回流程失败",e);
                KolB2cApplicationEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    cancelResult = BatchResultDTO.fail(id, id, "B2C寄样申请单不存在, 撤回流程失败");
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
    * @author jack
    * @date:  2025-12-04
    * @param id
    * @return ApiResult<KolB2cApplicationDTO.ViewDTO>>
    */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:kolB2cApplication:view",
            serviceClass = KolB2cApplicationService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<KolB2cApplicationDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(kolB2cApplicationService.view(id));
    }

    /**
    * 导出Excel数据
    * @author jack
    * @date:  2025-12-04
    * @param dto
    * @param response
    * @return
    */
    @PostMapping("/export")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "oms:kolB2cApplication:export",
            tableAlias = "kba"
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "B2C寄样申请单导出Excel数据")
    @WebAdvanceQuery(handler = KolB2cApplicationQueryHandler.class)
    public ApiResult exportList(@RequestBody @Validated KolB2cApplicationDTO.PagingParamDTO dto, HttpServletResponse response) {
        kolB2cApplicationService.exportList(dto, response);
        return success();
    }


    /**
     *  异步导入
     * @author jack
     * @date:  2025-08-20
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.IMPORT, desc = "导入B2C-KOL寄样申请")
    @PostMapping("/import")
    public ApiResult importExcel(@RequestBody BaseDTO.ImportDTO dto) {
        Boolean result = kolB2cApplicationService.importFile(dto);
        return result ? success() : failure();
    }

    /**
     * 下载模板
     * @author jack
     * @date:  2025-08-20
     * @param response
     * @return
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "B2C-KOL寄样申请下载模板")
    @GetMapping("/downloadTemplate")
    public ApiResult downloadTemplate(HttpServletResponse response) {
        String standardPath = "classpath:excel/kolB2cApplicationTemplate.xlsx";
        String standardExcelName = "kolB2cApplicationTemplate.xlsx";
        ExcelUtil.downloadTemplate(standardPath, standardExcelName, response);
        return success();
    }
    /**
     * 回片登记下推查询
     * @author jack
     * @date:  2025-12-04
     * @param dto
     * @return ApiResult<KolB2cApplicationDTO.DetailViewDTO>>
     */
    @PostMapping("/detailView")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:kolB2cApplication:detailView",
            serviceClass = KolB2cApplicationService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<List<KolB2cApplicationDTO.DetailViewDTO>> detailView(@RequestBody @Validated BaseIdsDTO.DetailIdListDTO dto) {
        return success(kolB2cApplicationService.detailView(dto.getDetailIdList()));
    }

    /**
     * 回片登记下推
     * @author jack
     * @date:  2025-12-04
     * @param list
     * @return ApiResult
     */
    @PostMapping("/generateReturnPiece")
    @LogAction(value = LogActionEnum.INSERT, desc = "回片登记下推")
    public ApiResult generateReturnPiece(@RequestBody @Valid ValidList<KolB2cApplicationDTO.DetailViewDTO> list) {
        Boolean flag = kolB2cApplicationService.generateReturnPiece(list.getList());
        return flag == true ? success() : failure();
    }


}
