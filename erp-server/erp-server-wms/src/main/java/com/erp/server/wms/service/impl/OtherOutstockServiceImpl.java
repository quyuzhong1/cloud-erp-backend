package com.erp.server.wms.service.impl;

import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.OtherOutstockDTO;
import com.erp.model.wms.entity.OtherOutstockEntity;
import com.erp.server.wms.mapper.OtherOutstockMapper;
import com.erp.server.wms.service.OtherOutstockService;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Service
public class OtherOutstockServiceImpl extends SuperServiceImpl<OtherOutstockMapper, OtherOutstockEntity> implements OtherOutstockService {

    @Override
    public PagingVO<OtherOutstockDTO.ListDTO> paging(PagingDTO<OtherOutstockDTO.SearchParamDTO> dto) {
        return null;
    }

    @Override
    public List<OtherOutstockDTO.ListStatusCountDTO> listCount(PermissionsDTO dto) {
        return null;
    }

    @Override
    public String add(OtherOutstockDTO.AddDTO dto) {
        return null;
    }

    @Override
    public String addAndSubmit(OtherOutstockDTO.AddDTO dto) {
        return null;
    }

    @Override
    public Boolean update(OtherOutstockDTO.UpdateDTO dto) {
        return null;
    }

    @Override
    public Boolean updateAndSubmit(OtherOutstockDTO.UpdateDTO dto) {
        return null;
    }

    @Override
    public Boolean submit(List<String> ids) {
        return null;
    }

    @Override
    public OtherOutstockDTO.ViewDTO view(String id) {
        return null;
    }

    @Override
    public Boolean delete(List<String> ids) {
        return null;
    }

    @Override
    public Boolean invalid(List<String> ids, String remark) {
        return null;
    }

    @Override
    public void approve(BaseApproveParamDTO baseApproveParamDTO) {

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
    public Boolean exportExcel(OtherOutstockDTO.SearchParamDTO dto, HttpServletResponse response) {
        return null;
    }
}
