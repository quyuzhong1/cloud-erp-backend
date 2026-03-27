package com.erp.server.wms.controller.api;


import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.ApproveDTO;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.validator.ValidList;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.wms.dto.QcApplicationDTO;
import com.erp.model.wms.dto.QcApplicationSrmDTO;
import com.erp.model.wms.entity.QcApplicationEntity;
import com.erp.server.wms.query.QcApplicationSrmQueryHandler;
import com.erp.server.wms.service.QcApplicationService;
import com.erp.server.wms.service.QcApplicationSrmService;
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
 * 质检申请单SRM主表
 *
 * @author will
 * @since 2026-03-20
 */
@Slf4j
@RestController
@LogSystemModule("质检申请单SRM主表")
@RequestMapping("/qcApplicationSrm")
public class QcApplicationSrmController extends BaseController {

    @Resource
    private QcApplicationService qcApplicationService;

    @Resource
    private QcApplicationSrmService qcApplicationSrmService;
    /**
    * 修改
    * @author will
    * @date:  2026-03-20
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    public ApiResult<?> update(@RequestBody @Validated QcApplicationDTO.UpdateDTO dto) {
        qcApplicationService.update(dto);
        return success();
    }


    /**
    * 获取状态统计
    * @return
    */
    @PostMapping("/tabList")
    public ApiResult<List<QcApplicationDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
       return success(qcApplicationSrmService.srmTabList(dto));
    }

    /**
    * 列表查询
    * @author will
    * @date: 2026-03-20
    * @param dto
    * @return ApiResult<PagingVO<QcApplicationDTO.ListDTO>>
    */
    @PostMapping("/paging")
    @WebAdvanceQuery(handler = QcApplicationSrmQueryHandler.class)
    public ApiResult<PagingVO<QcApplicationSrmDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<QcApplicationDTO.PagingParamDTO> dto) {
        return success(qcApplicationSrmService.srmPaging(dto));
    }

    /**
    * 提交审核
    * @author will
    * @date:  2026-03-20
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/submit")
    @LogAction(value = LogActionEnum.SUBMIT, desc = "质检申请单主表提交审核")
    public ApiResult<List<BatchResultDTO>> batchSubmit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		List<QcApplicationEntity> list = qcApplicationService.lambdaQuery().in(QcApplicationEntity::getId, ids).list();
		Map<String, QcApplicationEntity> idEntityMap = list.stream().collect(Collectors.toMap(QcApplicationEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO submit;
            try {
                submit = qcApplicationService.submit(id);
            }catch (Exception e){
                log.error("质检申请单主单 提交审核失败",e);
                QcApplicationEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    submit = BatchResultDTO.fail(id, id, "质检申请单主单不存在, 提交失败");
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
    * 删除
    * @author will
    * @date:  2026-03-20
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/delete")
    @LogAction(value = LogActionEnum.DELETE, desc = "质检申请单主表删除")
    public ApiResult<List<BatchResultDTO>> batchDelete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		List<QcApplicationEntity> list = qcApplicationService.lambdaQuery().in(QcApplicationEntity::getId, ids).list();
		Map<String, QcApplicationEntity> idEntityMap = list.stream().collect(Collectors.toMap(QcApplicationEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = qcApplicationService.delete(id);
            }catch (Exception e){
                log.error("质检申请单主单删除失败",e);
                QcApplicationEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "质检申请单主单不存在, 删除失败");
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
    * @date:  2026-03-20
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/cancelProcess")
    @LogAction(value = LogActionEnum.CANCEL, desc = "质检申请单主表撤销")
    public ApiResult<List<BatchResultDTO>> batchCancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<QcApplicationEntity> list = qcApplicationService.lambdaQuery().in(QcApplicationEntity::getId, ids).list();
        Map<String, QcApplicationEntity> idEntityMap = list.stream().collect(Collectors.toMap(QcApplicationEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO cancelResult;
            try {
                cancelResult = qcApplicationService.cancelProcess(new ApproveDTO.CancelProcessDTO(id));
            }catch (Exception e){
                log.error("质检申请单主单撤回流程失败",e);
                QcApplicationEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    cancelResult = BatchResultDTO.fail(id, id, "质检申请单主单不存在, 撤回流程失败");
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
    * @date:  2026-03-20
    * @param id
    * @return ApiResult<QcApplicationDTO.ViewDTO>>
    */
    @GetMapping("/view")
    @LogViewService
    public ApiResult<QcApplicationDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(qcApplicationService.view(id));
    }

    /**
    * 导出Excel数据
    * @author will
    * @date:  2026-03-20
    * @param dto
    * @return
    */
    @PostMapping("/export")
    @LogAction(value = LogActionEnum.EXPORT, desc = "质检申请单主表导出Excel数据")
    @WebAdvanceQuery(handler = QcApplicationSrmQueryHandler.class)
    public ApiResult<Object>exportExcel(@RequestBody @Validated QcApplicationDTO.PagingParamDTO dto, HttpServletResponse response) {
        Boolean flag = qcApplicationSrmService.exportList(dto,response);
        return flag ? success() : failure();
    }


    /**
     * 采购订单下推质检申请数据保存
     * @author will
     * @date 2026/3/23 12:24
     * @param list
     * @return QcApplicationDTO.ListPushQcNoticeDTO
     */
    @PostMapping("/generatePoRefQcApplication")
    public ApiResult<Object> generatePoRefQcApplication(@RequestBody @Validated ValidList<QcApplicationDTO.GeneratePoRefQcApplicationDTO> list) {
        return success(qcApplicationService.generatePoRefQcApplication(list));
    }

}
