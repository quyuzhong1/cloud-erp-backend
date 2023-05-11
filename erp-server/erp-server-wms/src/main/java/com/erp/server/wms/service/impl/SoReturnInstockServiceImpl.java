package com.erp.server.wms.service.impl;

import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.SoReturnInstockDTO;
import com.erp.model.wms.entity.SoReturnInstockEntity;
import com.erp.server.wms.mapper.SoReturnInstockMapper;
import com.erp.server.wms.service.SoReturnInstockService;
import com.common.business.service.SuperServiceImpl;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 退货入库单
 *
 * @author LUO_WG
 * @since 2023-05-10
 */
@Service
public class SoReturnInstockServiceImpl extends SuperServiceImpl<SoReturnInstockMapper, SoReturnInstockEntity> implements SoReturnInstockService {

    @Override
    public PagingVO<SoReturnInstockDTO.PagingView> paging(PagingDTO<SoReturnInstockDTO.PagingParam> pagingParamDTO) {
        return null;
    }

    @Override
    public List<SoReturnInstockDTO.StatusCountDTO> listCount(PermissionsDTO dto) {
        return null;
    }

    @Override
    public String add(SoReturnInstockDTO.Add dto) {
        return null;
    }

    @Override
    public Boolean update(SoReturnInstockDTO.Update dto) {
        return null;
    }

    @Override
    public SoReturnInstockDTO.View view(String id) {
        return null;
    }

    @Override
    public Boolean submit(List<String> ids) {
        return null;
    }

    @Override
    public Boolean addAndSubmit(SoReturnInstockDTO.Add dto) {
        return null;
    }

    @Override
    public Boolean updateAndSubmit(SoReturnInstockDTO.Update dto) {
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
    public Boolean exportExcel(SoReturnInstockDTO.PagingParam dto, HttpServletResponse response) {
        return null;
    }
}
