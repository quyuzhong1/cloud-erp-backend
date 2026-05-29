package com.erp.server.auth.controller.openapi;

import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.validator.AddGroup;
import com.common.business.validator.UpdateGroup;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogViewService;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.QcInfoDTO;
import com.erp.model.wms.dto.QcNoticeDTO;
import com.erp.rpc.wms.feign.QcInfoFeign;
import com.erp.rpc.wms.feign.QcNoticeFeign;
import com.erp.server.auth.config.OpenApi;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * @author zdy
 * @ClassName QcOpenApi
 * @description: 质检管理
 * @date 2026年04月10日
 * @version: 1.0
 */
@OpenApi
@Component
public class QcOpenApi {
    @Resource
    private QcInfoFeign qcInfoFeign;

    @Resource
    private QcNoticeFeign qcNoticeFeign;

    /**
     * 质检分页查询
     *
     * @param dto
     * @return
     */
    @OpenApi("qcPaging")
    public ApiResult<PagingVO<QcInfoDTO.OpenPagingViewDTO>> qcPaging(@RequestBody @Validated PagingDTO<QcInfoDTO.PagingParamDTO> dto) {
        return qcInfoFeign.qcPaging(dto);
    }

    /**
     * 免检
     *
     * @param dto
     * @return
     */
    @OpenApi("qcExemption")
    public ApiResult<BatchResultDTO> qcExemption(@RequestBody @Validated BaseIdDTO dto){
        return qcInfoFeign.qcExemption(dto);
    }

    /**
     * 详情
     *
     * @param dto
     * @return
     */
    @OpenApi("qcView")
    public ApiResult<QcInfoDTO.ViewDTO> qcView(@RequestBody @Validated BaseIdDTO dto) {
        return qcInfoFeign.qcView(dto);
    }

    /**
     * 暂存
     * @param dto
     * @return
     */
    @OpenApi("qcDraft")
    public ApiResult<?> qcDraft(@RequestBody QcInfoDTO.SaveOrUpdateDTO dto){
        return qcInfoFeign.qcDraft(dto);
    }

    /**
     * 完成质检
     * @param dto
     * @return
     */
    @OpenApi("qcFinish")
    public ApiResult<?> qcFinish(@RequestBody @Validated({AddGroup.class}) QcInfoDTO.SaveOrUpdateDTO dto) {
        return qcInfoFeign.qcFinish(dto);
    }

    /**
     * 获取质检标准
     *
     * @param dto
     * @return
     */
    @OpenApi("getQcStandard")
    public ApiResult<QcNoticeDTO.QcStandardView> getQcStandard(@RequestBody @Validated BaseIdDTO dto) {
        return qcInfoFeign.getQcStandard(dto);
    }

    // ==================== 质检通知单（飞书小程序）相关接口 ====================

    /**
     * 质检通知单 - 状态统计
     */
    @OpenApi("qcNoticeTabList")
    public ApiResult<List<QcNoticeDTO.TabListDTO>> qcNoticeTabList(PermissionsDTO param) {
        return qcNoticeFeign.qcNoticeTabList(param);
    }

    /**
     * 质检通知单 - 分页查询，支持高级查询，条件与 PC 端一致
     */
    @OpenApi("qcNoticePaging")
    public ApiResult<PagingVO<QcNoticeDTO.ListDTO>> qcNoticePaging(@Valid PagingDTO<QcNoticeDTO.PagingParamDTO> dto) {
        return qcNoticeFeign.qcNoticePaging(dto);
    }

    /**
     * 质检通知单 - 详情
     */
    @OpenApi("qcNoticeView")
    public ApiResult<QcNoticeDTO.ViewDTO> qcNoticeView(String id) {
        return qcNoticeFeign.qcNoticeView(id);
    }

    /**
     * 质检通知单 - 新增（暂存）
     */
    @OpenApi("qcNoticeAdd")
    public ApiResult<BaseResultDTO.AddDTO> qcNoticeAdd(@Valid QcNoticeDTO.AddDTO dto) {
        return qcNoticeFeign.qcNoticeAdd(dto);
    }

    /**
     * 质检通知单 - 修改（暂存）：仅待提交状态可操作
     */
    @OpenApi("qcNoticeUpdate")
    public ApiResult<?> qcNoticeUpdate(@Valid QcNoticeDTO.UpdateDTO dto) {
        return qcNoticeFeign.qcNoticeUpdate(dto);
    }

    /**
     * 质检通知单 - 新增并提交审核
     */
    @OpenApi("qcNoticeAddAndSubmit")
    public ApiResult<BaseResultDTO.AddDTO> qcNoticeAddAndSubmit(@Valid QcNoticeDTO.AddDTO dto) {
        return qcNoticeFeign.qcNoticeAddAndSubmit(dto);
    }

    /**
     * 质检通知单 - 修改并提交审核：仅待提交状态可操作
     */
    @OpenApi("qcNoticeUpdateAndSubmit")
    public ApiResult<Void> qcNoticeUpdateAndSubmit(@Valid QcNoticeDTO.UpdateDTO dto) {
        return qcNoticeFeign.qcNoticeUpdateAndSubmit(dto);
    }

    /**
     * 质检通知单 - 提交审核
     */
    @OpenApi("qcNoticeSubmit")
    public ApiResult<List<BatchResultDTO>> qcNoticeSubmit(@Valid BaseIdsDTO.IdsDTO dto) {
        return qcNoticeFeign.qcNoticeSubmit(dto);
    }

    /**
     * 质检通知单 - 审核：仅待审核状态可操作；
     * 当审核通过且质检类型为外验（outsideQc / b2bOutsideQc）时，planQcDate 必填且不能早于今日
     */
    @OpenApi("qcNoticeApprove")
    public ApiResult<List<BatchResultDTO>> qcNoticeApprove(@Valid BaseApproveParamDTO dto) {
        return qcNoticeFeign.qcNoticeApprove(dto);
    }
}
