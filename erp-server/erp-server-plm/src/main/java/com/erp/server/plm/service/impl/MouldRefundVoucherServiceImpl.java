package com.erp.server.plm.service.impl;


import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.plm.dto.MouldInfoDTO;
import com.erp.model.plm.entity.MouldRefundVoucherEntity;
import com.erp.server.plm.mapper.MouldRefundVoucherMapper;
import com.erp.server.plm.service.MouldRefundVoucherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
/**
 * <p>
 * 费用返还详情(凭据) 服务实现类
 * </p>
 *
 * @author liaohui
 * @since 2024-12-03
 */
@Slf4j
@Service
public class MouldRefundVoucherServiceImpl extends SuperServiceImpl<MouldRefundVoucherMapper, MouldRefundVoucherEntity> implements MouldRefundVoucherService {

    @Override
    public void returnConfirm(MouldInfoDTO.ReturnConfirmDTO dto) {
        MouldRefundVoucherEntity refundVoucher = new MouldRefundVoucherEntity();
        refundVoucher.setMouldDetailId(dto.getMouldDetailId());
        refundVoucher.setFileName(dto.getFileName());
        refundVoucher.setFileUrl(String.join(",", dto.getFileUrl()));
        refundVoucher.setRemark(dto.getRemark());
        save(refundVoucher);
    }
}
