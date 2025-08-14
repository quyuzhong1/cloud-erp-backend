package com.erp.server.wms.controller.api;


import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.validator.ValidList;
import com.common.core.exception.ServiceException;
import com.common.core.utils.ExcelUtil;
import com.erp.model.tms.dto.TmsFirstMileReconciliationDetailDTO;
import com.erp.model.tms.enums.CfgReconciliationTypeEnum;
import com.erp.model.wms.dto.QcNoticeDetailDTO;
import com.erp.server.wms.query.QcNoticeQueryHandler;
import com.erp.server.wms.query.RequisitionApplicationQueryHandler;
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
import com.erp.server.wms.service.QcNoticeService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.wms.dto.QcNoticeDTO;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;
import com.erp.model.wms.entity.QcNoticeEntity;
import org.springframework.web.multipart.MultipartFile;

/**
 * 质检通知单
 *
 * @author jack
 * @since 2025-04-21
 */
@Slf4j
@RestController
@LogSystemModule("质检通知单")
@RequestMapping("/qcNotice")
public class QcNoticeController extends BaseController {

    @Resource
    private QcNoticeService qcNoticeService;

    /**
    * 新增
    * @author jack
    * @date:  2025-04-21
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "质检通知单新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated QcNoticeDTO.AddDTO dto) {
        return success(qcNoticeService.add(dto));
    }

    /**
    * 修改
    * @author jack
    * @date:  2025-04-21
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "质检通知单修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "wms:qcNotice:update",
        serviceClass = QcNoticeService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated QcNoticeDTO.UpdateDTO dto) {
        qcNoticeService.update(dto);
        return success();
    }

    /**
    * 获取状态统计
    * @return
    */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:qcNotice:paging",
            tableAlias = "qn"
    )
    public ApiResult<List<QcNoticeDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
       return success(qcNoticeService.tabList(dto));
    }

    /**
    * 列表查询
    * @author jack
    * @date: 2025-04-21
    * @param dto
    * @return ApiResult<PagingVO<QcNoticeDTO.ListDTO>>
    */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:qcNotice:paging",
            tableAlias = "qn"
    )
    @WebAdvanceQuery(handler = QcNoticeQueryHandler.class)
    public ApiResult<PagingVO<QcNoticeDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<QcNoticeDTO.PagingParamDTO> dto) {
        return success(qcNoticeService.paging(dto));
    }

    /**
    * 新增并提交审核
    * @author jack
    * @date:  2025-04-21
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/addAndSubmit")
    @LogAction(value = LogActionEnum.ADD_AND_SUBMIT, desc = "质检通知单新增并提交审核")
    public ApiResult<BaseResultDTO.AddDTO> addAndSubmit(@RequestBody @Validated QcNoticeDTO.AddDTO dto) {
        BaseResultDTO.AddDTO result = qcNoticeService.addAndSubmit(dto);
        return success(result);
    }

    /**
    * 修改并提交审核
    * @author jack
    * @date:  2025-04-21
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/updateAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:qcNotice:updateAndSubmit",
            serviceClass = QcNoticeService.class,
            keyIdName = "id")
    @LogAction(value = LogActionEnum.UPDATE_AND_SUBMIT, desc = "质检通知单修改并提交审核")
    public ApiResult<Void> updateAndSubmit(@RequestBody @Validated QcNoticeDTO.UpdateDTO dto) {
        qcNoticeService.updateAndSubmit(dto);
        return success();
    }

    /**
    * 提交审核
    * @author jack
    * @date:  2025-04-21
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:qcNotice:submit",
            serviceClass = QcNoticeService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.SUBMIT, desc = "质检通知单提交审核")
    public ApiResult<List<BatchResultDTO>> submit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		List<QcNoticeEntity> list = qcNoticeService.lambdaQuery().in(QcNoticeEntity::getId, ids).list();
		Map<String, QcNoticeEntity> idEntityMap = list.stream().collect(Collectors.toMap(QcNoticeEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO submit;
            try {
                submit = qcNoticeService.submit(id);
            }catch (Exception e){
                log.error("质检通知单 提交审核失败",e);
                QcNoticeEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    submit = BatchResultDTO.fail(id, id, "质检通知单不存在, 提交失败");
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
    * @date:  2025-04-21
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:qcNotice:approve",
            serviceClass = QcNoticeService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.APPROVE, desc = "质检通知单审核")
    public ApiResult<List<BatchResultDTO>> approve(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		List<QcNoticeEntity> list = qcNoticeService.lambdaQuery().in(QcNoticeEntity::getId, ids).list();
		Map<String, QcNoticeEntity> idEntityMap = list.stream().collect(Collectors.toMap(QcNoticeEntity::getId, w -> w));
        for (String id : ids) {
            BatchResultDTO approveResult;
            try {
                approveResult = qcNoticeService.approve(new ApproveOneDTO(id, dto.getType(),dto.getComment()));
            }catch (Exception e){
                log.error("质检通知单审核失败",e);
                QcNoticeEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    approveResult = BatchResultDTO.fail(id, id, "质检通知单不存在, 审核失败");
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
    * @date:  2025-04-21
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/disApprove")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:qcNotice:disApprove",
            serviceClass = QcNoticeService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DISAPPROVE, desc = "质检通知单反审核")
    public ApiResult<List<BatchResultDTO>> disApprove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		List<QcNoticeEntity> list = qcNoticeService.lambdaQuery().in(QcNoticeEntity::getId, ids).list();
		Map<String, QcNoticeEntity> idEntityMap = list.stream().collect(Collectors.toMap(QcNoticeEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO disApproveResult;
            try {
                disApproveResult = qcNoticeService.disApprove(id);
            }catch (Exception e){
                log.error("质检通知单反审核失败",e);
                QcNoticeEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    disApproveResult = BatchResultDTO.fail(id, id, "质检通知单不存在, 反审核失败");
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
    * @date:  2025-04-21
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:qcNotice:delete",
            serviceClass = QcNoticeService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DELETE, desc = "质检通知单删除")
    public ApiResult<List<BatchResultDTO>> delete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		List<QcNoticeEntity> list = qcNoticeService.lambdaQuery().in(QcNoticeEntity::getId, ids).list();
		Map<String, QcNoticeEntity> idEntityMap = list.stream().collect(Collectors.toMap(QcNoticeEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = qcNoticeService.delete(id);
            }catch (Exception e){
                log.error("质检通知单删除失败",e);
                QcNoticeEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "质检通知单不存在, 删除失败");
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
    * @author jack
    * @date:  2025-04-21
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:qcNotice:cancelProcess",
            serviceClass = QcNoticeService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.CANCEL, desc = "质检通知单撤销")
    public ApiResult<List<BatchResultDTO>> cancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<QcNoticeEntity> list = qcNoticeService.lambdaQuery().in(QcNoticeEntity::getId, ids).list();
        Map<String, QcNoticeEntity> idEntityMap = list.stream().collect(Collectors.toMap(QcNoticeEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO cancelResult;
            try {
                cancelResult = qcNoticeService.cancelProcess(id);
            }catch (Exception e){
                log.error("质检通知单撤回流程失败",e);
                QcNoticeEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    cancelResult = BatchResultDTO.fail(id, id, "质检通知单不存在, 撤回流程失败");
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
    * @date:  2025-04-21
    * @param id
    * @return ApiResult<QcNoticeDTO.ViewDTO>>
    */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:qcNotice:view",
            serviceClass = QcNoticeService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<QcNoticeDTO.ViewDTO> view(@RequestParam(value = "id" , required = true) String id) {
        return success(qcNoticeService.view(id));
    }

    /**
     * 下推质检单详情
     * @author jack
     * @date:  2025-04-21
     * @param dto
     * @return ApiResult<QcNoticeDTO.ViewDTO>>
     */
    @PostMapping("/generateQcInfoView")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:qcNotice:generateQcInfoView",
            serviceClass = QcNoticeService.class,
            keyIdName = "ids")
    public ApiResult<List<QcNoticeDTO.QcInfoView>> generateQcInfoView(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        return success(qcNoticeService.generateQcInfoView(dto.getIds()));
    }

    /**
     * 完成质检
     * @author jack
     * @date:  2025-04-21
     * @param dto
     * @return ApiResult<QcNoticeDTO.ViewDTO>>
     */
    @PostMapping("/generateQcInfoFinish")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:qcNotice:generateQcInfoView",
            serviceClass = QcNoticeService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> generateQcInfoFinish(@RequestBody @Valid ValidList<QcNoticeDTO.QcInfoView> dto) {
        qcNoticeService.generateQcInfo(dto.getList());
        return success();
    }


    /**
     * 撤销质检
     * @author jack
     * @date:  2025-04-21
     * @param dto
     * @return ApiResult<QcNoticeDTO.ViewDTO>>
     */
    @PostMapping("/cancelQcInfoFinish")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:qcNotice:cancelQcInfoFinish",
            serviceClass = QcNoticeService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "撤销质检")
    public ApiResult<List<BatchResultDTO>> cancelQcInfoFinish(@RequestBody @Validated BaseIdsDTO.DetailIdListDTO dto) {
        List<BatchResultDTO> resultDTOS = qcNoticeService.cancelQcInfoFinish(dto.getDetailIdList());
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 新增校验库存
     * @author jack
     * @date:  2025-04-21
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/checkInventory")
    public ApiResult<List<BatchResultDTO> > checkInventory(@RequestBody @Validated QcNoticeDTO.AddDTO dto) {
        List<BatchResultDTO>  resultDTOS = qcNoticeService.checkInventory(dto);
        return success(resultDTOS) ;
    }

    /**
    * 导出Excel数据
    * @author jack
    * @date:  2025-04-21
    * @param dto
    * @param response
    * @return
    */
    @PostMapping("/export")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:qcNotice:export",
            tableAlias = "qn"
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "质检通知单导出Excel数据")
    @WebAdvanceQuery(handler = QcNoticeQueryHandler.class)
    public ApiResult<Object> exportList(@RequestBody @Validated QcNoticeDTO.ExportDTO dto, HttpServletResponse response) {
        qcNoticeService.exportList(dto, response);
        return success();
    }

    /**
     * 下载模板
     * @author jack
     * @date:  2025-04-21
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "下载质检通知单明细导入模板")
    @GetMapping("/downloadTemplate")
    public ApiResult<Object> downloadTemplate(HttpServletRequest request, HttpServletResponse response) {
        String standardPath = "classpath:excel/qcNoticeDetailTemplate.xlsx";
        String standardExcelName = "qcNoticeDetailTemplate.xlsx";
        ExcelUtil.downloadTemplate(standardPath, standardExcelName, response);
        return success();
    }

    /**
     * 导入
     * @author jack
     * @date:  2025-04-21
     */
    @PostMapping("/importFile")
    @LogAction(value = LogActionEnum.EXPORT, desc = "质检通知单明细导入")
    public ApiResult<QcNoticeDTO.ImportDTO> importFile(@RequestParam(value = "excelFile") MultipartFile excelFile, HttpServletResponse response) {
        return success(qcNoticeService.importFile(excelFile, response));
    }





}
