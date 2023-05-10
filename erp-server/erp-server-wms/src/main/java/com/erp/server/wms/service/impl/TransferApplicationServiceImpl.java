package com.erp.server.wms.service.impl;

import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.TransferApplicationDTO;
import com.erp.model.wms.entity.TransferApplicationEntity;
import com.erp.server.wms.mapper.TransferApplicationMapper;
import com.erp.server.wms.service.TransferApplicationService;
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
public class TransferApplicationServiceImpl extends SuperServiceImpl<TransferApplicationMapper, TransferApplicationEntity> implements TransferApplicationService {

    @Override
    public PagingVO<TransferApplicationDTO.ListDTO> paging(PagingDTO<TransferApplicationDTO.SearchParamDTO> dto) {
        return null;
    }

    @Override
    public List<TransferApplicationDTO.ListStatusCountDTO> listCount(PermissionsDTO dto) {
        return null;
    }

    @Override
    public String add(TransferApplicationDTO.AddDTO dto) {
        return null;
    }

    @Override
    public String addAndSubmit(TransferApplicationDTO.AddDTO dto) {
        return null;
    }

    @Override
    public Boolean update(TransferApplicationDTO.UpdateDTO dto) {
        return null;
    }

    @Override
    public Boolean updateAndSubmit(TransferApplicationDTO.UpdateDTO dto) {
        return null;
    }

    @Override
    public Boolean submit(List<String> ids) {
        return null;
    }

    @Override
    public TransferApplicationDTO.ViewDTO view(String id) {
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
    public Boolean exportExcel(TransferApplicationDTO.SearchParamDTO dto, HttpServletResponse response) {
        return null;
    }
}
