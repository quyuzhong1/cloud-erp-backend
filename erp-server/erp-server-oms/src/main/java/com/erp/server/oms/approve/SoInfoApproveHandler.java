package com.erp.server.oms.approve;

import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.ApproveBusinessKey;
import com.common.business.dto.ApproveDTO;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.handler.AbstractApproveHandler;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.oms.entity.SoChangeEntity;
import com.erp.model.oms.entity.SoInfoEntity;
import com.erp.server.oms.service.SoChangeService;
import com.erp.server.oms.service.SoInfoService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
@ApproveBusinessKey(SourceTypeEnum.SO_INFO)
public class SoInfoApproveHandler extends AbstractApproveHandler {
    @Resource
    private SoInfoService soInfoService;

    @Resource
    private SoChangeService soChangeService;

    @Override
    public Boolean cancelProcess(ApproveDTO.CancelProcessDTO dto) {
        return soInfoService.cancelProcess(Collections.singletonList(dto.getId()));
    }

    @Override
    public Boolean disApprove(ApproveDTO.DisApproveDTO dto) {
        SoInfoEntity entity = soInfoService.getById(dto.getId());
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_92016);
        }
        List<SoChangeEntity> soChangeList = soChangeService.listBySoIds(Collections.singletonList(dto.getId()));
        BatchResultDTO resultDTO = soInfoService.disApprove(entity, soChangeList);
        return resultDTO.getSuccess();
    }

    @Override
    public Boolean approveEnd(ApproveDTO.EndProcessDTO dto) {
        SoInfoEntity entity = soInfoService.getById(dto.getBusinessId());
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_92016);
        }
        BaseApproveParamDTO baseApproveParamDTO = new BaseApproveParamDTO();
        baseApproveParamDTO.setType(dto.getApproveStatus().getStatus());
        baseApproveParamDTO.setIds(Arrays.asList(dto.getBusinessId()));
        return soInfoService.approveEnd(baseApproveParamDTO,entity);
    }
}
