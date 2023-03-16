package com.erp.server.scm.service.impl;

import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.scm.dto.ScmPurchaseApplicationDTO;
import com.erp.model.scm.dto.ScmPurchaseApplicationPagingParamDTO;
import com.erp.model.scm.dto.ScmPurchaseApplicationPagingViewDTO;
import com.erp.model.scm.entity.ScmPurchaseApplicationEntity;
import com.erp.server.scm.mapper.ScmPurchaseApplicationMapper;
import com.erp.server.scm.service.ScmPurchaseApplicationService;
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
public class ScmPurchaseApplicationServiceImpl extends SuperServiceImpl<ScmPurchaseApplicationMapper, ScmPurchaseApplicationEntity> implements ScmPurchaseApplicationService {

    @Override
    public PagingVO<List<ScmPurchaseApplicationPagingViewDTO>> paging(PagingDTO<ScmPurchaseApplicationPagingParamDTO> dto) {
        return null;
    }

    @Override
    public Boolean add(ScmPurchaseApplicationDTO scmPurchaseApplicationDTO) {
        return null;
    }

    @Override
    public Boolean update(ScmPurchaseApplicationDTO scmPurchaseApplicationDTO) {
        return null;
    }

    @Override
    public void approve(BaseApproveParamDTO baseApproveParamDTO) {

    }

    @Override
    public Boolean unApprove(List<String> ids) {
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
    public Boolean exportExcel(ScmPurchaseApplicationPagingParamDTO scmPurchaseApplicationPagingParamDTO, HttpServletResponse response) {
        return null;
    }

    @Override
    public String getCode() {
        return null;
    }

    @Override
    public Boolean delete(String id) {
        return null;
    }


}
