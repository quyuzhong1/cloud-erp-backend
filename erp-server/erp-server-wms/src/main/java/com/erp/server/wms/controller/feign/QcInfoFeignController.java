package com.erp.server.wms.controller.feign;

import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.validator.AddGroup;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.wms.dto.QcInfoDTO;
import com.erp.model.wms.dto.QcNoticeDTO;
import com.erp.model.wms.dto.QcResultDTO;
import com.erp.model.wms.entity.QcInfoEntity;
import com.erp.server.wms.query.QcInfoQueryHandler;
import com.erp.server.wms.service.QcInfoService;
import com.erp.server.wms.service.QcResultService;
import com.erp.server.wms.service.QcSamplingPlanRefService;
import lombok.AllArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 质检单feign控制器
 *
 * @CreateTime: 2023-06-19  15:48
 * @Author: zhangchunlin
 */
@RestController
@AllArgsConstructor
@RequestMapping("/feign/qcBill")
public class QcInfoFeignController extends BaseController {

    private final QcInfoService qcInfoService;

    @Resource
    private QcResultService qcResultService;
    @Resource
    private QcSamplingPlanRefService qcSamplingPlanRefService;

    @PostMapping("/getQcInfoByPurchaseOrder")
    public QcInfoDTO.PurchaseQcInfoDTO getQcInfoByPurchaseOrder(@RequestBody QcInfoDTO.PurchaseQcParamDTO dto) {
        return qcInfoService.getQcInfoByPurchaseOrder(dto);
    }

    @PostMapping("/getQcReceiveResult")
    public List<QcInfoDTO.QcReceiveResultDTO> getQcReceiveResult(@RequestBody List<String> purchaseDetailIds) {
        return qcInfoService.getQcReceiveResult(purchaseDetailIds);
    }

    @PostMapping("/getFsQcNoticeTitle")
    public String getFsQcNoticeTitle(@RequestParam("title") String title) {
        return qcInfoService.getFsQcNoticeTitle(title);
    }

    /**
     * 根据质检单id 查询质检结果
     *
     * @param qcInfoIds
     * @return List<QcResultDTO.QcNoticeDTO>
     */
    @PostMapping("/listQcResultMsg")
    public List<QcResultDTO.QcNoticeDTO> listQcResultMsg(@RequestBody List<String> qcInfoIds) {
        return qcResultService.listQcResultMsg(qcInfoIds);
    }

    /**
     * 质检单分页查询(pda端)
     *
     * @param dto
     * @return
     */
    @PostMapping("/qcPaging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "qc_user_id",
            warehouseTableField = "qb.warehouse_id",
            menuCode = "wms:qcBill:paging",
            tableAlias = "qb")
    @WebAdvanceQuery(handler = QcInfoQueryHandler.class)
    public ApiResult<PagingVO<QcInfoDTO.OpenPagingViewDTO>> qcPaging(@RequestBody @Validated PagingDTO<QcInfoDTO.PagingParamDTO> dto) {
        PagingVO<QcInfoDTO.OpenPagingViewDTO> pagingVO = qcInfoService.qcPaging(dto);
        return success(pagingVO);
    }


    /**
     * 免检
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "免检:id={id}")
    @PostMapping("/qcExemption")
    public ApiResult<BatchResultDTO> qcExemption(@RequestBody @Validated BaseIdDTO dto) {
        QcInfoEntity entity = qcInfoService.getById(dto.getId());
        if (null == entity) {
            return failure("质检单不存在");
        }
        try {
            BatchResultDTO resultDTO = qcInfoService.batchExemption(entity);
            return resultDTO.getSuccess() ? success(resultDTO) : failure(resultDTO);
        } catch (Exception e) {
            return failure(e.getMessage());
        }
    }


    /**
     * 详情
     *
     * @param dto
     * @return
     */
    @LogViewService
    @PostMapping("/qcView")
    public ApiResult<QcInfoDTO.ViewDTO> qcView(@RequestBody @Validated BaseIdDTO dto) {
        try {
            QcInfoDTO.ViewDTO view = qcInfoService.qcView(dto.getId());
            return success(view);
        } catch (Exception e) {
            return failure(e.getMessage());
        }
    }

    /**
     * 暂存
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.INSERT, desc = "暂存质检单")
    @PostMapping("/qcDraft")
    public ApiResult<?> qcDraft(@RequestBody QcInfoDTO.SaveOrUpdateDTO dto) {
        try {
            QcInfoEntity entity = qcInfoService.draft(dto);
            return null != entity ? success(new BaseResultDTO.AddDTO(entity.getId(), entity.getCode())) : failure();
        } catch (Exception e) {
            return failure(e.getMessage());
        }
    }

    /**
     * 完成质检
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "完成质检:id={id}")
    @PostMapping("/qcFinish")
    public ApiResult<?> qcFinish(@RequestBody @Validated({AddGroup.class}) QcInfoDTO.SaveOrUpdateDTO dto) {
        try {
            QcInfoEntity entity = qcInfoService.finish(dto);
            return null != entity ? success(new BaseResultDTO.AddDTO(entity.getId(), entity.getCode())) : failure();
        } catch (Exception e) {
            return failure(e.getMessage());
        }
    }

    /**
     * 获取质检标准
     *
     * @param dto
     * @return
     */
    @PostMapping("/getQcStandard")
    public ApiResult<QcNoticeDTO.QcStandardView> getQcStandard(@RequestBody @Validated BaseIdDTO dto) {
        try {
            QcNoticeDTO.QcStandardView qcStandardView = qcSamplingPlanRefService.getByMainId(dto.getId());
            return success(qcStandardView);
        }catch (Exception e){
            return failure(e.getMessage());
        }
    }
}