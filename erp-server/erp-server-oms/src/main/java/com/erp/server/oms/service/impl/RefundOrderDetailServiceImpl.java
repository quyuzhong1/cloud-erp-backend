package com.erp.server.oms.service.impl;


import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.oms.entity.RefundOrderDetailEntity;
import com.erp.server.oms.mapper.RefundOrderDetailMapper;
import com.erp.server.oms.service.RefundOrderDetailService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.oms.service.OperateLogService;
import com.erp.server.oms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.oms.dto.RefundOrderDetailDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
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

        // 数据处理
        handleData(refundOrderDetailEntity);

        log.info("开始新增退款订单明细");
        boolean save = super.save(refundOrderDetailEntity);
        if(!save) {
            throw new ServiceException("退款订单明细保存失败");
        }

        // 操作日志
        String msg =  CharSequenceUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "退款订单明细" , refundOrderDetailEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, refundOrderDetailEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(refundOrderDetailEntity.getId(), refundOrderDetailEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(RefundOrderDetailDTO.UpdateDTO updateDTO) {
        RefundOrderDetailEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "退款订单明细"));
        RefundOrderDetailEntity refundOrderDetailEntity =  BeanMapperUtils.map(RefundOrderDetailEntity.class, updateDTO);

        // 数据处理
        handleData(refundOrderDetailEntity);
        log.info("编辑 开始修改退款订单明细数据，id：【{}】", old.getId());
        boolean save = super.updateById(refundOrderDetailEntity);
        if(!save) {
            throw new ServiceException("退款订单明细保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录退款订单明细日志数据，id：【{}】", refundOrderDetailEntity.getId());
            String msg =  CharSequenceUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), refundOrderDetailEntity.getId(), "退款订单明细");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, refundOrderDetailEntity, null, refundOrderDetailEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(RefundOrderDetailEntity refundOrderDetailEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
