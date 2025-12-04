package com.erp.server.oms.controller.api;


import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.*;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.validator.ValidList;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.common.core.utils.ExcelUtil;
import com.erp.model.oms.dto.KolB2bApplicationDTO;
import com.erp.model.oms.entity.KolB2bApplicationEntity;
import com.erp.server.oms.query.KolB2bApplicationQueryHandler;
import com.erp.server.oms.service.KolB2bApplicationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * B2B寄样申请主表
 *
 * @author will
 * @since 2025-12-01
 */
@Slf4j
@RestController
@LogSystemModule("B2B寄样申请主表")
@RequestMapping("/kolB2bApplication")
public class KolB2bApplicationController extends BaseController {

    @Resource
    private KolB2bApplicationService kolB2bApplicationService;

    /**
    * 新增
    * @author will
    * @date:  2025-12-01
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "B2B寄样申请主表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated KolB2bApplicationDTO.AddDTO dto) {
        return success(kolB2bApplicationService.add(dto));
    }

    /**
    * 修改
    * @author will
    * @date:  2025-12-01
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "B2B寄样申请主表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "oms:kolB2bApplication:update",
        serviceClass = KolB2bApplicationService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated KolB2bApplicationDTO.UpdateDTO dto) {
        kolB2bApplicationService.update(dto);
        return success();
    }

    /**
    * 获取状态统计
    * @return
    */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "oms:kolB2bApplication:paging",
            tableAlias = "kba"
    )
    public ApiResult<List<KolB2bApplicationDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
       return success(kolB2bApplicationService.tabList(dto));
    }

    /**
    * 列表查询
    * @author will
    * @date: 2025-12-01
    * @param dto
    * @return ApiResult<PagingVO<KolB2bApplicationDTO.ListDTO>>
    */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "oms:kolB2bApplication:paging",
            tableAlias = "kba"
    )
    @WebAdvanceQuery(handler = KolB2bApplicationQueryHandler.class)
    public ApiResult<PagingVO<KolB2bApplicationDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<KolB2bApplicationDTO.PagingParamDTO> dto) {
        return success(kolB2bApplicationService.paging(dto));
    }

    /**
    * 新增并提交审核
    * @author will
    * @date:  2025-12-01
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/addAndSubmit")
    public ApiResult<BaseResultDTO.AddDTO> addAndSubmit(@RequestBody @Validated KolB2bApplicationDTO.AddDTO dto) {
        BaseResultDTO.AddDTO result = kolB2bApplicationService.addAndSubmit(dto);
        return success(result);
    }

    /**
    * 修改并提交审核
    * @author will
    * @date:  2025-12-01
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/updateAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:kolB2bApplication:updateAndSubmit",
            serviceClass = KolB2bApplicationService.class,
            keyIdName = "id")
    public ApiResult<Void> updateAndSubmit(@RequestBody @Validated KolB2bApplicationDTO.UpdateDTO dto) {
        kolB2bApplicationService.updateAndSubmit(dto);
        return success();
    }

    /**
    * 提交审核
    * @author will
    * @date:  2025-12-01
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:kolB2bApplication:submit",
            serviceClass = KolB2bApplicationService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.SUBMIT, desc = "B2B寄样申请主表提交审核")
    public ApiResult<List<BatchResultDTO>> batchSubmit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		List<KolB2bApplicationEntity> list = kolB2bApplicationService.lambdaQuery().in(KolB2bApplicationEntity::getId, ids).list();
		Map<String, KolB2bApplicationEntity> idEntityMap = list.stream().collect(Collectors.toMap(KolB2bApplicationEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO submit;
            try {
                submit = kolB2bApplicationService.submit(id);
            }catch (Exception e){
                log.error("B2B寄样申请主单 提交审核失败",e);
                KolB2bApplicationEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    submit = BatchResultDTO.fail(id, id, "B2B寄样申请主单不存在, 提交失败");
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
    * @date:  2025-12-01
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:kolB2bApplication:approve",
            serviceClass = KolB2bApplicationService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.APPROVE, desc = "B2B寄样申请主表审核")
    public ApiResult<List<BatchResultDTO>> batchApprove(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		List<KolB2bApplicationEntity> list = kolB2bApplicationService.lambdaQuery().in(KolB2bApplicationEntity::getId, ids).list();
		Map<String, KolB2bApplicationEntity> idEntityMap = list.stream().collect(Collectors.toMap(KolB2bApplicationEntity::getId, w -> w));
        for (String id : ids) {
            BatchResultDTO approveResult;
            try {
                approveResult = kolB2bApplicationService.approve(new ApproveOneDTO(id, dto.getType(),dto.getComment()));
            }catch (Exception e){
                log.error("B2B寄样申请主单审核失败",e);
                KolB2bApplicationEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    approveResult = BatchResultDTO.fail(id, id, "B2B寄样申请主单不存在, 审核失败");
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
    * @date:  2025-12-01
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/disApprove")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:kolB2bApplication:disApprove",
            serviceClass = KolB2bApplicationService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DISAPPROVE, desc = "B2B寄样申请主表反审核")
    public ApiResult<List<BatchResultDTO>> batchDisApprove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		List<KolB2bApplicationEntity> list = kolB2bApplicationService.lambdaQuery().in(KolB2bApplicationEntity::getId, ids).list();
		Map<String, KolB2bApplicationEntity> idEntityMap = list.stream().collect(Collectors.toMap(KolB2bApplicationEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO disApproveResult;
            try {
                disApproveResult = kolB2bApplicationService.disApprove(id);
            }catch (Exception e){
                log.error("B2B寄样申请主单反审核失败",e);
                KolB2bApplicationEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    disApproveResult = BatchResultDTO.fail(id, id, "B2B寄样申请主单不存在, 反审核失败");
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
    * @date:  2025-12-01
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/batchDelete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:kolB2bApplication:batchDelete",
            serviceClass = KolB2bApplicationService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DELETE, desc = "B2B寄样申请主表删除")
    public ApiResult<List<BatchResultDTO>> batchDelete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		List<KolB2bApplicationEntity> list = kolB2bApplicationService.lambdaQuery().in(KolB2bApplicationEntity::getId, ids).list();
		Map<String, KolB2bApplicationEntity> idEntityMap = list.stream().collect(Collectors.toMap(KolB2bApplicationEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = kolB2bApplicationService.delete(id);
            }catch (Exception e){
                log.error("B2B寄样申请主单删除失败",e);
                KolB2bApplicationEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "B2B寄样申请主单不存在, 删除失败");
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
    * @date:  2025-12-01
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/batchInvalid")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:kolB2bApplication:batchInvalid",
            serviceClass = KolB2bApplicationService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.INVALID, desc = "B2B寄样申请主表作废")
    public ApiResult<List<BatchResultDTO>> batchInvalid(@RequestBody @Validated BaseIdsDTO.RemarkDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		List<KolB2bApplicationEntity> list = kolB2bApplicationService.lambdaQuery().in(KolB2bApplicationEntity::getId, ids).list();
		Map<String, KolB2bApplicationEntity> idEntityMap = list.stream().collect(Collectors.toMap(KolB2bApplicationEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO invalidResult;
            try {
                invalidResult = kolB2bApplicationService.invalid(id,dto.getRemark());
            }catch (Exception e){
                log.error("B2B寄样申请主单作废失败",e);
                KolB2bApplicationEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    invalidResult = BatchResultDTO.fail(id, id, "B2B寄样申请主单不存在, 作废失败");
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
    * @date:  2025-12-01
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:kolB2bApplication:cancelProcess",
            serviceClass = KolB2bApplicationService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.CANCEL, desc = "B2B寄样申请主表撤销")
    public ApiResult<List<BatchResultDTO>> batchCancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<KolB2bApplicationEntity> list = kolB2bApplicationService.lambdaQuery().in(KolB2bApplicationEntity::getId, ids).list();
        Map<String, KolB2bApplicationEntity> idEntityMap = list.stream().collect(Collectors.toMap(KolB2bApplicationEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO cancelResult;
            try {
                cancelResult = kolB2bApplicationService.cancelProcess(id);
            }catch (Exception e){
                log.error("B2B寄样申请主单撤回流程失败",e);
                KolB2bApplicationEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    cancelResult = BatchResultDTO.fail(id, id, "B2B寄样申请主单不存在, 撤回流程失败");
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
    * @date:  2025-12-01
    * @param id
    * @return ApiResult<KolB2bApplicationDTO.ViewDTO>>
    */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:kolB2bApplication:view",
            serviceClass = KolB2bApplicationService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<KolB2bApplicationDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(kolB2bApplicationService.view(id));
    }


    /**
     * 查询关联单据
     * @author will
     * @date:  2025-12-01
     * @param id
     * @return ApiResult<KolB2bApplicationDTO.ViewDTO>>
     */
    @GetMapping("/listRefBill")
    public ApiResult<KolB2bApplicationDTO.RefBillDTO> listRefBill(@RequestParam("id") String id) {
        return success(kolB2bApplicationService.listRefBill(id));
    }


    /**
    * 导出Excel数据
    * @author will
    * @date:  2025-12-01
    * @param dto
    * @param response
    * @return
    */
    @PostMapping("/export")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "oms:kolB2bApplication:export",
            tableAlias = "kba"
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "B2B寄样申请主表导出Excel数据")
    public void exportList(@RequestBody @Validated KolB2bApplicationDTO.ExportDTO dto, HttpServletResponse response) {
        kolB2bApplicationService.exportList(dto, response);
    }

    /**
     * 下载模板
     * @author will
     * @date 2025/12/1 16:23
     * @param response
     * @return ApiResult<Object>
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "下载模板")
    @GetMapping("/downloadTemplate")
    public ApiResult<Object> downloadTemplate(HttpServletResponse response) {
        String standardPath = "classpath:excel/kolB2bApplicationTemplate.xlsx";
        String standardExcelName = "kolB2bApplicationTemplate.xlsx";
        ExcelUtil.downloadTemplate(standardPath, standardExcelName, response);
        return success();
    }

    /**
     * 导入Excel数据
     * @author will
     * @date 2025/12/1 16:25
     * @param excelFile
     * @param response
     * @return ApiResult<Object>
     */
    @LogAction(value = LogActionEnum.IMPORT, desc = "导入其他入库单")
    @PostMapping("/import")
    public ApiResult<?> exportWarehouse(@RequestParam(value = "excelFile") MultipartFile excelFile, HttpServletResponse response) {
        Boolean result = kolB2bApplicationService.importFile(excelFile, response);
        return result ? success() : failure();
    }


    /**
     * 下推销售订单保存
     * @author will
     * @date 2025/12/1 18:16
     * @param list
     * @return ApiResult<Boolean>
     */
    @PostMapping("/generateSoInfo")
    public ApiResult<Boolean> generateSoInfo(@RequestBody @Validated ValidList<KolB2bApplicationDTO.GenerateSoInfoDTO> list) {
        Boolean result = kolB2bApplicationService.generateSoInfo(list);
        return result ? success() : failure();
    }


    /**
     * 下推回片登记保存
     * @author will
     * @date 2025/12/1 18:16
     * @param list
     * @return ApiResult<Boolean>
     */
    @PostMapping("/generateFeedback")
    public ApiResult<Boolean> generateFeedback(@RequestBody @Validated ValidList<KolB2bApplicationDTO.GenerateFeedbackDTO> list) {
        Boolean result = kolB2bApplicationService.generateFeedback(list);
        return result ? success() : failure();
    }

}
