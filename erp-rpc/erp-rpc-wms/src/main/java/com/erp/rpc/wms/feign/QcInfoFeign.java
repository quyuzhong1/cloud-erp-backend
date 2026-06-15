package com.erp.rpc.wms.feign;


import com.common.business.config.FeignErrorDecoder;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.validator.AddGroup;
import com.common.business.validator.UpdateGroup;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogViewService;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.QcInfoDTO;
import com.erp.model.wms.dto.QcNoticeDTO;
import com.erp.model.wms.dto.WmsAttachmentDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "erp-wms", contextId = "qcInfo", configuration = {FeignErrorDecoder.class})
public interface QcInfoFeign {

    /**
     * 仓库设备新增附件
     *
     * @param dto
     */
    @PostMapping("/feign/qcBill/qcPaging")
    ApiResult<PagingVO<QcInfoDTO.OpenPagingViewDTO>> qcPaging(@RequestBody @Validated PagingDTO<QcInfoDTO.PagingParamDTO> dto);

    /**
     * 免检
     *
     * @param dto
     * @return
     */
    @PostMapping("/feign/qcBill/qcExemption")
    ApiResult<BatchResultDTO> qcExemption(@RequestBody @Validated BaseIdDTO dto);

    /**
     * 详情
     *
     * @param dto
     * @return
     */
    @PostMapping("/feign/qcBill/qcView")
    ApiResult<QcInfoDTO.ViewDTO> qcView(@RequestBody @Validated BaseIdDTO dto);

    /**
     * 暂存
     * @param dto
     * @return
     */
    @PostMapping("/feign/qcBill/qcDraft")
    ApiResult<?> qcDraft(@RequestBody QcInfoDTO.SaveOrUpdateDTO dto);

    /**
     * 完成质检
     * @param dto
     * @return
     */
    @PostMapping("/feign/qcBill/qcFinish")
    ApiResult<?> qcFinish(@RequestBody @Validated({AddGroup.class}) QcInfoDTO.SaveOrUpdateDTO dto);

    /**
     * 获取质检标准
     * @param dto
     * @return
     */
    @PostMapping("/feign/qcBill/getQcStandard")
    ApiResult<QcNoticeDTO.QcStandardView> getQcStandard(@RequestBody @Validated BaseIdDTO dto);
}
