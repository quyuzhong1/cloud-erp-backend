package com.erp.server.plm.service.impl;


import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.plm.dto.MouldInfoDTO;
import com.erp.model.plm.dto.MouldRefundVoucherDTO;
import com.erp.model.plm.entity.MouldRefundVoucherEntity;
import com.erp.server.plm.mapper.MouldRefundVoucherMapper;
import com.erp.server.plm.service.MouldRefundVoucherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.Arrays;

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
        refundVoucher.setFileName(String.join(",", dto.getFileName()));
        refundVoucher.setFileUrl(String.join(",", dto.getFileUrl()));
        refundVoucher.setRemark(dto.getRemark());
        save(refundVoucher);
    }

    @Override
    public MouldRefundVoucherDTO returnConfirmDetail(String detailId) {
        MouldRefundVoucherEntity entity = getOne(Wrappers.<MouldRefundVoucherEntity>lambdaQuery().eq(MouldRefundVoucherEntity::getMouldDetailId, detailId));
        if (ObjectUtils.isEmpty(entity)) {
            return new MouldRefundVoucherDTO();
        }
        MouldRefundVoucherDTO dto = new MouldRefundVoucherDTO();
        dto.setMouldDetailId(entity.getMouldDetailId());
        dto.setFileName(Arrays.asList(entity.getFileName().split(",")));
        dto.setFileUrl(Arrays.asList(entity.getFileUrl().split(",")));
        dto.setCreateUserName(entity.getCreateUserName());
        dto.setCreateTime(entity.getCreateTime());
        dto.setRemark(entity.getRemark());
        return dto;
    }
}
