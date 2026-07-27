package com.erp.rpc.wms.feign;

import com.common.business.config.FeignErrorDecoder;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.QcApplicationDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import java.util.List;

/**
 * 质检申请单 Feign 客户端
 * 提供飞书小程序 / OpenApi 端所需的列表、详情、编辑、审核、下推能力
 *
 * @Author zdy
 * @since 2026-07-23
 */
@FeignClient(name = "erp-wms", path = "/feign/qcApplication", contextId = "qcApplicationFeign", configuration = {FeignErrorDecoder.class})
public interface QcApplicationFeign {

    /**
     * 质检申请单 - 状态统计
     */
    @PostMapping("/tabList")
    ApiResult<List<QcApplicationDTO.TabListDTO>> qcApplicationTabList(@RequestBody PermissionsDTO param);

    /**
     * 质检申请单 - 分页查询（支持高级搜索）
     */
    @PostMapping("/paging")
    ApiResult<PagingVO<QcApplicationDTO.ListDTO>> qcApplicationPaging(@RequestBody @Validated PagingDTO<QcApplicationDTO.PagingParamDTO> dto);

    /**
     * 质检申请单 - 详情
     */
    @GetMapping("/view")
    ApiResult<QcApplicationDTO.ViewDTO> qcApplicationView(@RequestParam("id") String id);

    /**
     * 质检申请单 - 修改
     */
    @PostMapping("/update")
    ApiResult<?> qcApplicationUpdate(@RequestBody @Valid QcApplicationDTO.UpdateDTO dto);

    /**
     * 质检申请单 - 提交审核（支持批量）
     */
    @PostMapping("/submit")
    ApiResult<List<BatchResultDTO>> qcApplicationSubmit(@RequestBody @Valid BaseIdsDTO.IdsDTO dto);

    /**
     * 质检申请单 - 审核（支持批量）
     */
    @PostMapping("/approve")
    ApiResult<List<BatchResultDTO>> qcApplicationApprove(@RequestBody @Valid BaseApproveParamDTO dto);

    /**
     * 质检申请单 - 删除（支持批量）
     */
    @PostMapping("/delete")
    ApiResult<List<BatchResultDTO>> qcApplicationDelete(@RequestBody @Valid BaseIdsDTO.IdsDTO dto);

    /**
     * 质检申请单 - 下推质检通知数据回显
     */
    @PostMapping("/listPushQcNotice")
    ApiResult<List<QcApplicationDTO.ListPushQcNoticeDTO>> qcApplicationListPushQcNotice(@RequestBody @Valid BaseIdsDTO.IdsDTO dto);

    /**
     * 质检申请单 - 下推质检通知数据保存
     */
    @PostMapping("/generateQcNotice")
    ApiResult<List<BatchResultDTO>> qcApplicationGenerateQcNotice(@RequestBody @Valid List<QcApplicationDTO.GenerateQcNoticeDTO> list);
}
