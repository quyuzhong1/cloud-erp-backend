package com.erp.server.wms.service.impl;

import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.TransferInfoDTO;
import com.erp.model.wms.entity.TransferInfoEntity;
import com.erp.server.wms.mapper.TransferInfoMapper;
import com.erp.server.wms.service.TransferInfoService;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 直接调拨单主表 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Service
public class TransferInfoServiceImpl extends SuperServiceImpl<TransferInfoMapper, TransferInfoEntity> implements TransferInfoService {

    @Override
    public PagingVO<TransferInfoDTO.ListDTO> paging(PagingDTO<TransferInfoDTO.SearchParamDTO> dto) {
        return null;
    }

    @Override
    public List<TransferInfoDTO.ListStatusCountDTO> listCount(PermissionsDTO dto) {
        return null;
    }

    @Override
    public String add(TransferInfoDTO.AddDTO dto) {
        return null;
    }

    @Override
    public String addAndSubmit(TransferInfoDTO.AddDTO dto) {
        return null;
    }

    @Override
    public Boolean update(TransferInfoDTO.UpdateDTO dto) {
        return null;
    }

    @Override
    public Boolean updateAndSubmit(TransferInfoDTO.UpdateDTO dto) {
        return null;
    }

    @Override
    public Boolean submit(List<String> ids) {
        return null;
    }

    @Override
    public TransferInfoDTO.ViewDTO view(String id) {
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
    public Boolean exportExcel(TransferInfoDTO.SearchParamDTO dto, HttpServletResponse response) {
        return null;
    }

    @Override
    public List<TransferInfoEntity> listBySourceIds(List<String> sourceIds) {
        return lambdaQuery().in(TransferInfoEntity::getSourceId,sourceIds).list();
    }
}
