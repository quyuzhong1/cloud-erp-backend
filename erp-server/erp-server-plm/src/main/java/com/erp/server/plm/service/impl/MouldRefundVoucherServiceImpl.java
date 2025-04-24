package com.erp.server.plm.service.impl;


import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.plm.dto.MouldInfoDTO;
import com.erp.model.plm.dto.MouldRefundVoucherDTO;
import com.erp.model.plm.entity.MouldRefundAgreementEntity;
import com.erp.model.plm.entity.MouldRefundVoucherEntity;
import com.erp.server.plm.mapper.MouldRefundVoucherMapper;
import com.erp.server.plm.service.MouldRefundAgreementService;
import com.erp.server.plm.service.MouldRefundVoucherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
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

    @Resource
    @Lazy
    private MouldRefundAgreementService mouldRefundAgreementService;

    @Override
    public void returnConfirm(MouldInfoDTO.ReturnConfirmDTO dto) {
        MouldRefundVoucherEntity refundVoucher = new MouldRefundVoucherEntity();
        refundVoucher.setMouldDetailId(dto.getMouldDetailId());
        refundVoucher.setFileName(String.join(",", dto.getFileNameList()));
        refundVoucher.setFileUrl(String.join(",", dto.getFileUrlList()));
        refundVoucher.setRemark(dto.getRemark());
        save(refundVoucher);
    }

    @Override
    public MouldRefundVoucherDTO returnConfirmDetail(String detailId) {
        MouldRefundVoucherEntity entity = getOne(Wrappers.<MouldRefundVoucherEntity>lambdaQuery().eq(MouldRefundVoucherEntity::getMouldDetailId, detailId));
        if (ObjectUtils.isEmpty(entity)) {
            return new MouldRefundVoucherDTO();
        }
        MouldRefundAgreementEntity mouldRefundAgreement = mouldRefundAgreementService.getOne(Wrappers.<MouldRefundAgreementEntity>lambdaQuery().eq(MouldRefundAgreementEntity::getMouldDetailId, detailId));
        MouldRefundVoucherDTO dto = new MouldRefundVoucherDTO();
        dto.setMouldDetailId(entity.getMouldDetailId());
        dto.setFileNameList(Arrays.asList(entity.getFileName().split(",")));
        dto.setFileUrlList(Arrays.asList(entity.getFileUrl().split(",")));
        dto.setCreateUserName(entity.getCreateUserName());
        dto.setCreateTime(entity.getCreateTime());
        dto.setRemark(entity.getRemark());
        dto.setRefundAmount(mouldRefundAgreement.getRefundAmount());
        dto.setRealRefundAmount(mouldRefundAgreement.getRealRefundAmount());
        return dto;
    }
}
