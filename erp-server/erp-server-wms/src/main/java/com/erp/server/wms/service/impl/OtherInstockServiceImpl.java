package com.erp.server.wms.service.impl;

import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.OtherInstockDTO;
import com.erp.model.wms.entity.OtherInstockEntity;
import com.erp.server.wms.mapper.OtherInstockMapper;
import com.erp.server.wms.service.OtherInstockService;
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
public class OtherInstockServiceImpl extends SuperServiceImpl<OtherInstockMapper, OtherInstockEntity> implements OtherInstockService {

    @Override
    public PagingVO<OtherInstockDTO.ListDTO> paging(PagingDTO<OtherInstockDTO.SearchParamDTO> dto) {
        return null;
    }

    @Override
    public List<OtherInstockDTO.ListStatusCountDTO> listCount(PermissionsDTO dto) {
        return null;
    }

    @Override
    public String add(OtherInstockDTO.AddDTO dto) {
        return null;
    }

    @Override
    public String addAndSubmit(OtherInstockDTO.AddDTO dto) {
        return null;
    }

    @Override
    public Boolean update(OtherInstockDTO.UpdateDTO dto) {
        return null;
    }

    @Override
    public Boolean updateAndSubmit(OtherInstockDTO.UpdateDTO dto) {
        return null;
    }

    @Override
    public Boolean submit(List<String> ids) {
        return null;
    }

    @Override
    public OtherInstockDTO.ViewDTO view(String id) {
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
    public Boolean exportExcel(OtherInstockDTO.SearchParamDTO dto, HttpServletResponse response) {
        return null;
    }
}
