package com.erp.server.srm.controller.api;


import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.vo.PagingVO;
import com.erp.model.srm.dto.PoReconciliationDetailDTO;
import com.erp.model.srm.entity.PoReconciliationEntity;
import com.erp.model.wms.entity.SubcontractIssueEntity;
import com.erp.server.srm.query.PoReconciliationDetailQueryHandler;
import com.erp.server.srm.query.PoReconciliationQueryHandler;
import com.erp.server.srm.service.PoReconciliationScmService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.enums.LogActionEnum;
import com.common.business.dto.base.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.srm.service.PoReconciliationService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.srm.dto.PoReconciliationDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * 对账单【srm】
 *
 * @author will
 * @since 2024-01-19
 */
@Slf4j
@RestController
@LogSystemModule("采购对账单")
@RequestMapping("/poReconciliation")
public class PoReconciliationController extends BaseController {

    @Resource
    private PoReconciliationService poReconciliationService;

    /**
     * 分页查询
     * @author Will
     * @date: 2024/1/20 12:11
     * @param dto
     * @return ApiResult<PagingVO<ListDTO>>
     */
    @PostMapping("/paging")
    @WebAdvanceQuery(handler = PoReconciliationQueryHandler.class)
    public ApiResult<PagingVO<PoReconciliationDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<PoReconciliationDTO.PagingParamDTO> dto) {
        return success(poReconciliationService.paging(dto));
    }

    /**
     * 获取状态统计
     * @author Will
     * @date: 2024/1/23 15:59
     * @param dto
     * @return ApiResult<List<TabListDTO>>
     */
    @PostMapping("/tabList")
    public ApiResult<List<PoReconciliationDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
        return success(poReconciliationService.tabList(dto));
    }

    /**
     * 修改
     * @author will
     * @date:  2024-01-19
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "采购对账单修改")
    public ApiResult<?> update(@RequestBody @Validated PoReconciliationDTO.UpdateDTO dto) {
        poReconciliationService.update(dto);
        return success();
    }

    /**
     * 查看详情（对账单主表）
     * @author Will
     * @date: 2024/1/23 15:08
     * @param dto
     * @return ApiResult<ViewDTO>
     */
    @PostMapping("/viewMain")
    public ApiResult<PoReconciliationDTO.ViewDTO> viewMain(@RequestBody BaseIdDTO dto) {
        PoReconciliationDTO.ViewDTO viewDTO = poReconciliationService.viewMain(dto.getId());
        return success(viewDTO);
    }

    /**
     * 查看详情（对账单明细）传reconciliationDetail，值为对账单id
     * @author Will
     * @date: 2024/1/23 15:08
     * @param dto
     * @return ApiResult<ViewDTO>
     */
    @PostMapping("/viewDetail")
    @WebAdvanceQuery(handler = PoReconciliationDetailQueryHandler.class)
    public ApiResult<List<PoReconciliationDetailDTO.ViewDTO>> viewDetail(@RequestBody @Validated PoReconciliationDetailDTO.PagingParamDTO dto) {
        List<PoReconciliationDetailDTO.ViewDTO> list = poReconciliationService.viewDetail(dto);
        return success(list);
    }


    /**
     * 导出Excel
     * @author Will
     * @date: 2024/1/20 12:03
     * @param dto
     * @param response
     */
    @PostMapping("/export")
    @LogAction(value = LogActionEnum.EXPORT, desc = "采购对账单导出Excel数据")
    @WebAdvanceQuery(handler = PoReconciliationQueryHandler.class)
    public void exportList(@RequestBody @Validated PoReconciliationDTO.PagingParamDTO dto, HttpServletResponse response) {
        poReconciliationService.exportList(dto, response);
    }


    /**
     * 导出对账单
     * @author Will
     * @date: 2024/1/20 12:03
     * @param dto
     * @param response
     */
    @PostMapping("/exportPoReconciliation")
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出对账单数据")
    @WebAdvanceQuery(handler = PoReconciliationQueryHandler.class)
    public void exportPoReconciliation(@RequestBody @Validated PoReconciliationDTO.PagingParamDTO dto, HttpServletResponse response) {
        poReconciliationService.exportPoReconciliation(dto, response);
    }


    /**
     * 确认对账
     * @author Will
     * @date: 2024/1/23 11:48
     * @param dto
     * @return ApiResult<?>
     */
    @PostMapping("/confirm")
    @LogAction(value = LogActionEnum.CONFIRM, desc = "确认对账")
    public ApiResult<?> confirm(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO resultDTO;
            try {
                resultDTO = poReconciliationService.confirm(id);
            }catch (Exception e){
                log.error("对账单 确认失败",e);
                PoReconciliationEntity entity = poReconciliationService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    resultDTO = BatchResultDTO.fail(id, id, "对账单不存在, 确认失败");
                    resultDTOS.add(resultDTO);
                    continue;
                }
                resultDTO = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(resultDTO);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 取消确认
     * @author Will
     * @date: 2024/1/23 11:48
     * @param dto
     * @return ApiResult<?>
     */
    @PostMapping("/cancelConfirm")
    @LogAction(value = LogActionEnum.CONFIRM, desc = "取消确认")
    public ApiResult<?> cancelConfirm(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO resultDTO;
            try {
                resultDTO = poReconciliationService.cancelConfirm(id);
            }catch (Exception e){
                log.error("对账单 取消确认失败",e);
                PoReconciliationEntity entity = poReconciliationService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    resultDTO = BatchResultDTO.fail(id, id, "对账单不存在, 取消确认失败");
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
