package com.erp.rpc.wms.feign;

import com.common.business.config.FeignErrorDecoder;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.QcNoticeDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;
import java.util.List;

/**
 * 质检通知单 Feign 客户端
 * 提供质检通知单飞书小程序端所需的列表、详情、编辑、审核能力
 *
 * @author qcNoticeApp
 * @since 2026-05-29
 */
@FeignClient(name = "erp-wms", path = "/feign/qcNotice", contextId = "qcNoticeFeign", configuration = {FeignErrorDecoder.class})
public interface QcNoticeFeign {

    /**
     * 质检通知单 - 状态统计
     */
    @PostMapping("/tabList")
    ApiResult<List<QcNoticeDTO.TabListDTO>> qcNoticeTabList(@RequestBody PermissionsDTO param);

    /**
     * 质检通知单 - 分页查询
     */
    @PostMapping("/paging")
    ApiResult<PagingVO<QcNoticeDTO.ListDTO>> qcNoticePaging(@RequestBody @Validated PagingDTO<QcNoticeDTO.PagingParamDTO> dto);

    /**
     * 质检通知单 - 详情
     */
    @PostMapping("/view")
    ApiResult<QcNoticeDTO.ViewDTO> qcNoticeView(@RequestBody @Validated BaseIdDTO dto);

    /**
     * 质检通知单 - 新增（暂存）
     */
    @PostMapping("/add")
    ApiResult<BaseResultDTO.AddDTO> qcNoticeAdd(@RequestBody @Valid QcNoticeDTO.AddDTO dto);

    /**
     * 质检通知单 - 修改（暂存）
     */
    @PostMapping("/update")
    ApiResult<?> qcNoticeUpdate(@RequestBody @Valid QcNoticeDTO.UpdateDTO dto);

    /**
     * 质检通知单 - 新增并提交审核
     */
    @PostMapping("/addAndSubmit")
    ApiResult<BaseResultDTO.AddDTO> qcNoticeAddAndSubmit(@RequestBody @Valid QcNoticeDTO.AddDTO dto);

    /**
     * 质检通知单 - 修改并提交审核
     */
    @PostMapping("/updateAndSubmit")
    ApiResult<Void> qcNoticeUpdateAndSubmit(@RequestBody @Valid QcNoticeDTO.UpdateDTO dto);

    /**
     * 质检通知单 - 提交审核
     */
    @PostMapping("/submit")
    ApiResult<List<BatchResultDTO>> qcNoticeSubmit(@RequestBody @Valid BaseIdsDTO.IdsDTO dto);

    /**
     * 质检通知单 - 审核（外验质检类型审核通过时 planQcDate 必填）
     */
    @PostMapping("/approve")
    ApiResult<List<BatchResultDTO>> qcNoticeApprove(@RequestBody @Valid BaseApproveParamDTO dto);
}
