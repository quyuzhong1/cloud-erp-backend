package com.erp.server.wms.service.impl;

import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.SoReturnNoticeDTO;
import com.erp.model.wms.dto.SoReturnReceiveDTO;
import com.erp.model.wms.entity.SoReturnReceiveEntity;
import com.erp.server.wms.mapper.SoReturnReceiveMapper;
import com.erp.server.wms.service.SoReturnReceiveService;
import com.common.business.service.SuperServiceImpl;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 采购退货签收单
 * @author LUO_WG
 * @since 2023-05-10
 */
@Service
public class SoReturnReceiveServiceImpl extends SuperServiceImpl<SoReturnReceiveMapper, SoReturnReceiveEntity> implements SoReturnReceiveService {

    @Override
    public PagingVO<SoReturnReceiveDTO.PagingView> paging(PagingDTO<SoReturnReceiveDTO.PagingParam> pagingParamDTO) {
        return null;
    }

    @Override
    public List<SoReturnReceiveDTO.StatusCountDTO> listCount(PermissionsDTO dto) {
        return null;
    }

    @Override
    public String add(SoReturnReceiveDTO.Add dto) {
        return null;
    }

    @Override
    public Boolean update(SoReturnReceiveDTO.Update dto) {
        return null;
    }

    @Override
    public SoReturnReceiveDTO.View view(String id) {
        return null;
    }

    @Override
    public Boolean submit(List<String> ids) {
        return null;
    }

    @Override
    public Boolean addAndSubmit(SoReturnReceiveDTO.Add dto) {
        return null;
    }

    @Override
    public Boolean updateAndSubmit(SoReturnReceiveDTO.Update dto) {
        return null;
    }

    @Override
    public Boolean approve(BaseApproveParamDTO baseApproveParamDTO) {
        return null;
    }

    @Override
    public Boolean disApprove(List<String> ids) {
        return null;
    }

    @Override
    public Boolean cancelProcess(List<String> ids) {
        return null;
    }

    @Override
    public Boolean invalid(List<String> ids, String remark) {
        return null;
    }

    @Override
    public Boolean delete(List<String> ids) {
        return null;
    }

    @Override
    public Boolean exportExcel(SoReturnReceiveDTO.PagingParam dto, HttpServletResponse response) {
        return null;
    }

    @Override
    public Boolean generateSoReturnReceiveSave(List<SoReturnNoticeDTO.GenerateSoReturnReceiveView> list) {
        return null;
    }

    @Override
    public List<SoReturnReceiveDTO.GenerateSoReturnInstockView> generateSoReturnInstockView(List<String> ids) {
        return null;
    }

    @Override
    public List<SoReturnReceiveEntity> listBySourceIds(List<String> ids) {
        return lambdaQuery().in(SoReturnReceiveEntity::getSourceId, ids).list();
    }
}
