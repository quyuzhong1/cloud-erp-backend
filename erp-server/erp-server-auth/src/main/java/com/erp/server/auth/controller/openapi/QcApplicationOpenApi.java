package com.erp.server.auth.controller.openapi;

import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.validator.ValidList;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.QcApplicationDTO;
import com.erp.rpc.wms.feign.QcApplicationFeign;
import com.erp.server.auth.config.OpenApi;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * 质检申请单 OpenApi（飞书小程序等）
 * <p>
 * 薄封装：仅转调 {@link QcApplicationFeign}，业务逻辑在 WMS FeignController / Service。
 *
 * @Author zdy
 * @since 2026-07-23
 */
@OpenApi
@Component
public class QcApplicationOpenApi {

    @Resource
    private QcApplicationFeign qcApplicationFeign;

    /**
     * 质检申请单 - 状态统计
     */
    @OpenApi("qcApplicationTabList")
    public ApiResult<List<QcApplicationDTO.TabListDTO>> qcApplicationTabList(PermissionsDTO param) {
        return qcApplicationFeign.qcApplicationTabList(param);
    }

    /**
     * 质检申请单 - 分页查询，支持高级查询，条件与 PC 端一致
     */
    @OpenApi("qcApplicationPaging")
    public ApiResult<PagingVO<QcApplicationDTO.ListDTO>> qcApplicationPaging(@Valid PagingDTO<QcApplicationDTO.PagingParamDTO> dto) {
        return qcApplicationFeign.qcApplicationPaging(dto);
    }

    /**
     * 质检申请单 - 详情
     */
    @OpenApi("qcApplicationView")
    public ApiResult<QcApplicationDTO.ViewDTO> qcApplicationView(@RequestBody @Validated BaseIdDTO dto) {
        return qcApplicationFeign.qcApplicationView(dto.getId());
    }

    /**
     * 质检申请单 - 编辑
     */
    @OpenApi("qcApplicationUpdate")
    public ApiResult<?> qcApplicationUpdate(@Valid QcApplicationDTO.UpdateDTO dto) {
        return qcApplicationFeign.qcApplicationUpdate(dto);
    }

    /**
     * 质检申请单 - 提交审核（支持批量）
     */
    @OpenApi("qcApplicationSubmit")
    public ApiResult<List<BatchResultDTO>> qcApplicationSubmit(@Valid BaseIdsDTO.IdsDTO dto) {
        return qcApplicationFeign.qcApplicationSubmit(dto);
    }

    /**
     * 质检申请单 - 审核（支持批量）
     */
    @OpenApi("qcApplicationApprove")
    public ApiResult<List<BatchResultDTO>> qcApplicationApprove(@Valid BaseApproveParamDTO dto) {
        return qcApplicationFeign.qcApplicationApprove(dto);
    }

    /**
     * 质检申请单 - 批量删除
     */
    @OpenApi("qcApplicationDelete")
    public ApiResult<List<BatchResultDTO>> qcApplicationDelete(@Valid BaseIdsDTO.IdsDTO dto) {
        return qcApplicationFeign.qcApplicationDelete(dto);
    }

    /**
     * 质检申请单 - 下推质检通知数据回显
     */
    @OpenApi("qcApplicationListPushQcNotice")
    public ApiResult<List<QcApplicationDTO.ListPushQcNoticeDTO>> qcApplicationListPushQcNotice(@Valid BaseIdsDTO.IdsDTO dto) {
        return qcApplicationFeign.qcApplicationListPushQcNotice(dto);
    }

    /**
     * 质检申请单 - 下推质检通知数据保存
     */
    @OpenApi("qcApplicationGenerateQcNotice")
    public ApiResult<List<BatchResultDTO>> qcApplicationGenerateQcNotice(
            @Valid ValidList<QcApplicationDTO.GenerateQcNoticeDTO> list) {
        return qcApplicationFeign.qcApplicationGenerateQcNotice(list);
    }
}
