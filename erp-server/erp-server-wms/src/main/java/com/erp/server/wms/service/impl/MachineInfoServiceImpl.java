package com.erp.server.wms.service.impl;

import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.MachineInfoDTO;
import com.erp.model.wms.entity.MachineInfoEntity;
import com.erp.server.wms.mapper.MachineInfoMapper;
import com.erp.server.wms.service.MachineInfoService;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 加工单 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Service
public class MachineInfoServiceImpl extends SuperServiceImpl<MachineInfoMapper, MachineInfoEntity> implements MachineInfoService {

    @Override
    public PagingVO<MachineInfoDTO.ListDTO> paging(PagingDTO<MachineInfoDTO.SearchParamDTO> dto) {
        return null;
    }

    @Override
    public List<MachineInfoDTO.ListStatusCountDTO> listCount(PermissionsDTO dto) {
        return null;
    }

    @Override
    public String add(MachineInfoDTO.AddDTO dto) {
        return null;
    }

    @Override
    public String addAndSubmit(MachineInfoDTO.AddDTO dto) {
        return null;
    }

    @Override
    public Boolean update(MachineInfoDTO.UpdateDTO dto) {
        return null;
    }

    @Override
    public Boolean updateAndSubmit(MachineInfoDTO.UpdateDTO dto) {
        return null;
    }

    @Override
    public Boolean submit(List<String> ids) {
        return null;
    }

    @Override
    public MachineInfoDTO.ViewDTO view(String id) {
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
    public Boolean exportExcel(MachineInfoDTO.SearchParamDTO dto, HttpServletResponse response) {
        return null;
    }
}
