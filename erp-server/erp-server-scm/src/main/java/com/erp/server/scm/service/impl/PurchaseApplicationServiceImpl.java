package com.erp.server.scm.service.impl;

import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.scm.dto.PurchaseApplicationDTO;
import com.erp.model.scm.entity.PurchaseApplicationEntity;
import com.erp.server.scm.mapper.PurchaseApplicationMapper;
import com.erp.server.scm.service.PurchaseApplicationService;
import com.common.core.serveice.SuperServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 采购申请表 服务实现类
 * </p>
 *
 * @author will
 * @since 2023-03-15
 */
@Service
public class PurchaseApplicationServiceImpl extends SuperServiceImpl<PurchaseApplicationMapper, PurchaseApplicationEntity> implements PurchaseApplicationService {

    @Override
    public PagingVO<PurchaseApplicationDTO.listDTO> paging(PagingDTO<PurchaseApplicationDTO.searchParamDTO> dto) {
        return null;
    }

    @Override
    public Boolean add(PurchaseApplicationDTO.addDTO dto) {
        return null;
    }

    @Override
    public Boolean update(PurchaseApplicationDTO.updateDTO dto) {
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
    public Boolean generatePurchaseOrder(String id) {
        return null;
    }

    @Override
    public Boolean importFile(MultipartFile excelFile, HttpServletResponse response) {
        return null;
    }

    @Override
    public Boolean exportExcel(PurchaseApplicationDTO.searchParamDTO dto, HttpServletResponse response) {
        return null;
    }

    @Override
    public Boolean delete(List<String> ids) {
        return null;
    }

    @Override
    public Boolean submit(List<String> ids) {
        return null;
    }

    @Override
    public Boolean addAndSubmit(PurchaseApplicationDTO.addDTO dto) {
        return null;
    }


}
