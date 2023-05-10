package com.erp.server.oms.service.impl;

import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.SoReturnDTO;
import com.erp.model.oms.entity.SoReturnEntity;
import com.erp.server.oms.mapper.SoReturnMapper;
import com.erp.server.oms.service.SoReturnService;
import com.common.business.service.SuperServiceImpl;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author LUO_WG
 * @since 2023-05-10
 */
@Service
public class SoReturnServiceImpl extends SuperServiceImpl<SoReturnMapper, SoReturnEntity> implements SoReturnService {

    @Override
    public PagingVO<SoReturnDTO.PagingView> paging(PagingDTO<SoReturnDTO.PagingParam> pagingParamDTO) {
        return null;
    }

    @Override
    public List<SoReturnDTO.StatusCountDTO> listCount(PermissionsDTO dto) {
        return null;
    }

    @Override
    public String add(SoReturnDTO.Add dto) {
        return null;
    }

    @Override
    public Boolean update(SoReturnDTO.Update dto) {
        return null;
    }

    @Override
    public SoReturnDTO.View view(String id) {
        return null;
    }

    @Override
    public Boolean submit(List<String> ids) {
        return null;
    }

    @Override
    public Boolean addAndSubmit(SoReturnDTO.Add dto) {
        return null;
    }

    @Override
    public Boolean updateAndSubmit(SoReturnDTO.Update dto) {
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
    public Boolean exportExcel(SoReturnDTO.PagingParam dto, HttpServletResponse response) {
        return null;
    }
}
