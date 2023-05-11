package com.erp.server.wms.service.impl;

import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.SoReturnDTO;
import com.erp.model.wms.dto.SoReturnNoticeDTO;
import com.erp.model.wms.entity.SoReturnNoticeEntity;
import com.erp.server.wms.mapper.SoReturnNoticeMapper;
import com.erp.server.wms.service.SoReturnNoticeService;
import com.common.business.service.SuperServiceImpl;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 退货通知单
 * @author LUO_WG
 * @since 2023-05-10
 */
@Service
public class SoReturnNoticeServiceImpl extends SuperServiceImpl<SoReturnNoticeMapper, SoReturnNoticeEntity> implements SoReturnNoticeService {

    @Override
    public PagingVO<SoReturnNoticeDTO.PagingView> paging(PagingDTO<SoReturnNoticeDTO.PagingParam> pagingParamDTO) {
        return null;
    }

    @Override
    public List<SoReturnNoticeDTO.StatusCountDTO> listCount(PermissionsDTO dto) {
        return null;
    }

    @Override
    public String add(SoReturnNoticeDTO.Add dto) {
        return null;
    }

    @Override
    public Boolean update(SoReturnNoticeDTO.Update dto) {
        return null;
    }

    @Override
    public SoReturnNoticeDTO.View view(String id) {
        return null;
    }

    @Override
    public Boolean submit(List<String> ids) {
        return null;
    }

    @Override
    public Boolean addAndSubmit(SoReturnNoticeDTO.Add dto) {
        return null;
    }

    @Override
    public Boolean updateAndSubmit(SoReturnNoticeDTO.Update dto) {
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
    public Boolean exportExcel(SoReturnNoticeDTO.PagingParam dto, HttpServletResponse response) {
        return null;
    }

    @Override
    public Boolean generateSoReturnNoticeSave(List<SoReturnDTO.GenerateSoReturnNoticeView> list) {
        return null;
    }

    @Override
    public List<SoReturnNoticeDTO.GenerateSoReturnReceiveView> generateSoDeliveryView(List<String> ids) {
        return null;
    }
}
