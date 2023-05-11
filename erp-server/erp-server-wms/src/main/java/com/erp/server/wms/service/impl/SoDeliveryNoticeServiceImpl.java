package com.erp.server.wms.service.impl;

import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.SoDeliveryNoticeDTO;
import com.erp.model.wms.dto.WarehouseReceiveDTO;
import com.erp.model.wms.entity.SoDeliveryNoticeEntity;
import com.erp.server.wms.mapper.SoDeliveryNoticeMapper;
import com.erp.server.wms.service.SoDeliveryNoticeService;
import com.common.business.service.SuperServiceImpl;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 发货通知单主表明细表 服务实现类
 * </p>
 *
 * @author LUO_WG
 * @since 2023-05-10
 */
@Service
public class SoDeliveryNoticeServiceImpl extends SuperServiceImpl<SoDeliveryNoticeMapper, SoDeliveryNoticeEntity> implements SoDeliveryNoticeService {

    @Override
    public PagingVO<SoDeliveryNoticeDTO.PagingView> paging(PagingDTO<SoDeliveryNoticeDTO.PagingParam> pagingParamDTO) {
        return null;
    }

    @Override
    public List<SoDeliveryNoticeDTO.StatusCountDTO> listCount(PermissionsDTO dto) {
        return null;
    }

    @Override
    public String add(SoDeliveryNoticeDTO.Add dto) {
        return null;
    }

    @Override
    public Boolean update(SoDeliveryNoticeDTO.Update dto) {
        return null;
    }

    @Override
    public SoDeliveryNoticeDTO.View view(String id) {
        return null;
    }

    @Override
    public Boolean submit(List<String> ids) {
        return null;
    }

    @Override
    public Boolean addAndSubmit(SoDeliveryNoticeDTO.Add dto) {
        return null;
    }

    @Override
    public Boolean updateAndSubmit(SoDeliveryNoticeDTO.Update dto) {
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
    public Boolean exportExcel(SoDeliveryNoticeDTO.PagingParam dto, HttpServletResponse response) {
        return null;
    }

    @Override
    public List<SoDeliveryNoticeDTO.GenerateSoDeliveryView> generateStockInView(List<String> ids) {
        return null;
    }
}
