package com.erp.server.bi.rocketmq.sync.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.business.constant.ApproveType;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.bi.dto.BiSettlementExchangeRateDTO;
import com.erp.model.bi.entity.BiSettlementExchangeRateEntity;
import com.erp.model.dmp.dto.DmpExchangeRateDTO;
import com.erp.model.dmp.enums.KingdeeDocStatusEnum;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.bi.rocketmq.sync.SyncExchangeRateService;
import com.erp.server.bi.service.BiSettlementExchangeRateService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * @description: 同步直接调拨单业务层
 * @author Will
 * @date: 2023/6/29 11:09
 */
@Service
public class SyncExchangeRateServiceImpl implements SyncExchangeRateService {


    @Resource
    private BiSettlementExchangeRateService biSettlementExchangeRateService;

    @Resource
    private SysUserFeign sysUserFeign;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncKingdeeExchangeRate(DmpExchangeRateDTO dto) {

        BiSettlementExchangeRateEntity oldExchangeRate = biSettlementExchangeRateService.getByKingdeeId(dto.getSourceId());
        //数据格式化
        BiSettlementExchangeRateEntity newExchangeRate = handleDmpExchangeRate(dto,oldExchangeRate);
        if (ObjectUtils.isEmpty(oldExchangeRate)) {
            //非已审核数据无需新增
            if (!ApproveStatusEnum.APPROVE.getStatus().equals(newExchangeRate.getApproveStatus())) {
                return;
            }
            BiSettlementExchangeRateDTO.AddDTO addDTO = BeanMapperUtils.map(BiSettlementExchangeRateDTO.AddDTO.class, newExchangeRate);
            String id = biSettlementExchangeRateService.add(addDTO);
            //提交并审核
            submitAndApprove(id);
        } else {
            /**
             * 判断现有状态
             * 1、现有状态为已审核或审核中时需要反审核后更新数据
             * 2、如果拉取数据非已审核数据则修改数据后无需提交审核
             */

            if (ApproveStatusEnum.APPROVE.getStatus().equals(oldExchangeRate.getApproveStatus())) {
                biSettlementExchangeRateService.disApprove(Arrays.asList(oldExchangeRate.getId()));
            }
            //存在则更新
            BiSettlementExchangeRateDTO.UpdateDTO updateDTO = BeanMapperUtils.map(BiSettlementExchangeRateDTO.UpdateDTO.class, newExchangeRate);
            biSettlementExchangeRateService.update(updateDTO);
            //审核
            if (ApproveStatusEnum.APPROVE.getStatus().equals(newExchangeRate.getApproveStatus())) {
                //提交并审核
                submitAndApprove(oldExchangeRate.getId());
            }
        }
    }

    /**
     * @description: 处理数据
     * @author Will
     * @date: 2023/8/14 18:53
     * @param dto
     * @param oldExchangeRate
     * @return BiSettlementExchangeRateEntity
     */
    private BiSettlementExchangeRateEntity handleDmpExchangeRate (DmpExchangeRateDTO dto,BiSettlementExchangeRateEntity oldExchangeRate) {
        BiSettlementExchangeRateEntity newExchangeRate = new BiSettlementExchangeRateEntity();
        BeanMapperUtils.copy(dto,newExchangeRate);
        //主表id赋值
        if (ObjectUtils.isNotEmpty(oldExchangeRate)) {
            newExchangeRate.setId(oldExchangeRate.getId());
        }
        //审核状态
        if (KingdeeDocStatusEnum.APPROVED.getCode().equals(dto.getApproveStatus())) {
            newExchangeRate.setApproveStatus(ApproveStatusEnum.APPROVE.getStatus());
        } else if (KingdeeDocStatusEnum.REAPPROVE.getCode().equals(dto.getApproveStatus())) {
            newExchangeRate.setApproveStatus(ApproveStatusEnum.WAIT_SUBMIT.getStatus());
        } else if (KingdeeDocStatusEnum.APPROVING.getCode().equals(dto.getApproveStatus())) {
            newExchangeRate.setApproveStatus(ApproveStatusEnum.APPROVE_ING.getStatus());
        }
        //币种
        List<CurrencyDTO.ViewDTO> viewList = sysUserFeign.listCurrencyByKingdeeCodeList(Arrays.asList(dto.getSourceCurrencyCode(), dto.getTargetCurrencyCode()));
        if (CollectionUtils.isEmpty(viewList)) {
            throw new ServiceException(ApiError.ERROR_CURRENCY_NOT_EXIST, JSONUtil.toJsonStr(Arrays.asList(dto.getSourceCurrencyCode(), dto.getTargetCurrencyCode())));
        }
        //原币别
        String sourceCurrency = viewList.stream().filter(obj -> obj.getKingdeeCode().equals(dto.getSourceCurrencyCode())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getId())).orElse("");
        if (StringUtils.isBlank(sourceCurrency)) {
            throw new ServiceException(ApiError.ERROR_CURRENCY_NOT_EXIST,dto.getSourceCurrencyCode());
        }
        newExchangeRate.setSourceCurrencyCode(sourceCurrency);
        //目标币别
        String targetCurrency = viewList.stream().filter(obj -> obj.getKingdeeCode().equals(dto.getTargetCurrencyCode())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getId())).orElse("");
        if (StringUtils.isBlank(targetCurrency)) {
            throw new ServiceException(ApiError.ERROR_CURRENCY_NOT_EXIST,dto.getTargetCurrencyCode());
        }
        newExchangeRate.setTargetCurrencyCode(targetCurrency);
        newExchangeRate.setKingdeeId(dto.getSourceId());
        return newExchangeRate;
    }

    /**
     * @description: 提交并审核
     * @author Will
     * @date: 2023/8/14 18:52
     * @param id
     */
    private void submitAndApprove(String id) {
        //提交
        Boolean submit = biSettlementExchangeRateService.submit(Arrays.asList(id));
        if (Boolean.TRUE.equals(submit)) {//审核
            BaseApproveParamDTO paramDTO = new BaseApproveParamDTO();
            paramDTO.setIds(Arrays.asList(id));
            paramDTO.setType(ApproveType.PASS);
            biSettlementExchangeRateService.approve(paramDTO);
        } else {
            throw new ServiceException(ApiError.ERROR_1042);
        }
    }

}

