package com.erp.server.oms.service.impl;


import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.oms.dto.RefundOrderDetailDTO;
import com.erp.model.oms.entity.SoB2cRefundDetailEntity;
import com.erp.server.oms.mapper.SoB2cRefundDetailMapper;
import com.erp.server.oms.service.OperateLogService;
import com.erp.server.oms.service.SoB2cRefundDetailService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
/**
 * <p>
 * 退款订单明细 服务实现类
 * </p>
 *
 * @author lrp
 * @since 2024-09-27
 */
@Slf4j
@Service
public class SoB2cRefundDetailServiceImpl extends SuperServiceImpl<SoB2cRefundDetailMapper, SoB2cRefundDetailEntity> implements SoB2cRefundDetailService {
    @Resource
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(RefundOrderDetailDTO.AddDTO addDTO) {
        SoB2cRefundDetailEntity soB2cRefundDetailEntity = new SoB2cRefundDetailEntity();
        BeanMapperUtils.copy(addDTO, soB2cRefundDetailEntity);
        log.info("开始新增退款订单明细");
        boolean save = super.save(soB2cRefundDetailEntity);
        if(!save) {
            throw new ServiceException("退款订单明细保存失败");
        }

        // 操作日志
        String msg =  CharSequenceUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), ApiError.ERROR_92161.msg , soB2cRefundDetailEntity.getId());
        operateLogService.addModuleOperateLog(msg, null, soB2cRefundDetailEntity.getId(), "新增操作");
        return new BaseResultDTO.AddDTO(soB2cRefundDetailEntity.getId(), soB2cRefundDetailEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(RefundOrderDetailDTO.UpdateDTO updateDTO) {
        SoB2cRefundDetailEntity old = super.getById(updateDTO.getId());
        if(null == old){
            throw new ServiceException(ApiError.NOT_EXIST_BILL, ApiError.ERROR_92161.msg);
        }
        SoB2cRefundDetailEntity soB2cRefundDetailEntity =  BeanMapperUtils.map(SoB2cRefundDetailEntity.class, updateDTO);
        log.info("编辑 开始修改退款订单明细数据，id：【{}】", old.getId());
        boolean save = super.updateById(soB2cRefundDetailEntity);
        if(!save) {
            throw new ServiceException("退款订单明细保存失败");
        }

        // 记录主单操作日志
            log.info("编辑 开始记录退款订单明细日志数据，id：【{}】", soB2cRefundDetailEntity.getId());
            String msg =  CharSequenceUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), soB2cRefundDetailEntity.getId(), ApiError.ERROR_92161.msg);
        operateLogService.addModuleOperateLogByObj(old, soB2cRefundDetailEntity, null, soB2cRefundDetailEntity.getId(), msg);
        return Boolean.TRUE;
    }
}
