package com.erp.server.auth.controller.openapi;

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
import com.erp.rpc.wms.feign.QcInfoFeign;
import com.erp.server.auth.config.OpenApi;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.Resource;

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

    /**
     * 质检分页查询
     *
     * @param dto
     * @return
     */
    @OpenApi("qcPaging")
    public ApiResult<PagingVO<QcInfoDTO.OpenPagingViewDTO>> qcPaging(@RequestBody @Validated PagingDTO<QcInfoDTO.OpenPagingParamDTO> dto) {
        return qcInfoFeign.qcPaging(dto);
    }

    /**
     * 免检
     *
     * @param dto
     * @return
     */
    @OpenApi("/qcExemption")
    public ApiResult<BatchResultDTO> qcExemption(@RequestBody @Validated BaseIdDTO dto){
        return qcInfoFeign.qcExemption(dto);
    }

    /**
     * 详情
     *
     * @param dto
     * @return
     */
    @OpenApi("/qcView")
    public ApiResult<QcInfoDTO.ViewDTO> qcView(@RequestBody @Validated BaseIdDTO dto) {
        return qcInfoFeign.qcView(dto);
    }

    /**
     * 暂存
     * @param dto
     * @return
     */
    @OpenApi("/qcDraft")
    public ApiResult<?> qcDraft(@RequestBody QcInfoDTO.SaveOrUpdateDTO dto){
        return qcInfoFeign.qcDraft(dto);
    }

    /**
     * 完成质检
     * @param dto
     * @return
     */
    @OpenApi("/qcFinish")
    public ApiResult<?> qcFinish(@RequestBody @Validated({AddGroup.class}) QcInfoDTO.SaveOrUpdateDTO dto) {
        return qcInfoFeign.qcFinish(dto);
    }

    /**
     * 获取质检标准
     *
     * @param dto
     * @return
     */
    @OpenApi("/getQcStandard")
    public ApiResult<QcNoticeDTO.QcStandardView> getQcStandard(@RequestBody @Validated BaseIdDTO dto) {
        return qcInfoFeign.getQcStandard(dto);
    }
}
