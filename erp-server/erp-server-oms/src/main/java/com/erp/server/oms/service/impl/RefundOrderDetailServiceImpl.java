package com.erp.server.oms.service.impl;


import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.oms.dto.RefundOrderDetailDTO;
import com.erp.model.oms.entity.RefundOrderDetailEntity;
import com.erp.server.oms.mapper.RefundOrderDetailMapper;
import com.erp.server.oms.service.OperateLogService;
import com.erp.server.oms.service.RefundOrderDetailService;
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
public class RefundOrderDetailServiceImpl extends SuperServiceImpl<RefundOrderDetailMapper, RefundOrderDetailEntity> implements RefundOrderDetailService {
    @Resource
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(RefundOrderDetailDTO.AddDTO addDTO) {
        RefundOrderDetailEntity refundOrderDetailEntity = new RefundOrderDetailEntity();
        BeanMapperUtils.copy(addDTO, refundOrderDetailEntity);
        log.info("开始新增退款订单明细");
        boolean save = super.save(refundOrderDetailEntity);
        if(!save) {
            throw new ServiceException("退款订单明细保存失败");
        }

        // 操作日志
        String msg =  CharSequenceUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), ApiError.ERROR_92161.msg , refundOrderDetailEntity.getId());
        operateLogService.addModuleOperateLog(msg, null, refundOrderDetailEntity.getId(), "新增操作");
        return new BaseResultDTO.AddDTO(refundOrderDetailEntity.getId(), refundOrderDetailEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(RefundOrderDetailDTO.UpdateDTO updateDTO) {
        RefundOrderDetailEntity old = super.getById(updateDTO.getId());
        if(null == old){
            throw new ServiceException(ApiError.NOT_EXIST_BILL, ApiError.ERROR_92161.msg);
        }
        RefundOrderDetailEntity refundOrderDetailEntity =  BeanMapperUtils.map(RefundOrderDetailEntity.class, updateDTO);
        log.info("编辑 开始修改退款订单明细数据，id：【{}】", old.getId());
        boolean save = super.updateById(refundOrderDetailEntity);
        if(!save) {
            throw new ServiceException("退款订单明细保存失败");
        }

        // 记录主单操作日志
            log.info("编辑 开始记录退款订单明细日志数据，id：【{}】", refundOrderDetailEntity.getId());
            String msg =  CharSequenceUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), refundOrderDetailEntity.getId(), ApiError.ERROR_92161.msg);
        operateLogService.addModuleOperateLogByObj(old, refundOrderDetailEntity, null, refundOrderDetailEntity.getId(), msg);
        return Boolean.TRUE;
    }
}
