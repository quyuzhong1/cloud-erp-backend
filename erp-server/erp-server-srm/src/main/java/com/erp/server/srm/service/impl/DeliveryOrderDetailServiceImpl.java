package com.erp.server.srm.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.srm.entity.DeliveryOrderDetailEntity;
import com.erp.server.srm.mapper.DeliveryOrderDetailMapper;
import com.erp.server.srm.service.DeliveryOrderDetailService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.srm.service.OperateLogService;
import com.erp.server.srm.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.srm.dto.DeliveryOrderDetailDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 送货单明细 服务实现类
 * </p>
 *
 * @author lrp
 * @since 2024-01-12
 */
@Slf4j
@Service
public class DeliveryOrderDetailServiceImpl extends SuperServiceImpl<DeliveryOrderDetailMapper, DeliveryOrderDetailEntity> implements DeliveryOrderDetailService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(DeliveryOrderDetailDTO.AddDTO addDTO) {
        DeliveryOrderDetailEntity deliveryOrderDetailEntity = new DeliveryOrderDetailEntity();
        BeanMapperUtils.copy(addDTO, deliveryOrderDetailEntity);

        // 数据处理
        handleData(deliveryOrderDetailEntity);

        log.info("开始新增送货单明细");
        boolean save = super.save(deliveryOrderDetailEntity);
        if(!save) {
            throw new ServiceException("送货单明细保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", commonService.getUserInfo().getUserName(), "送货单明细" , deliveryOrderDetailEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, deliveryOrderDetailEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(deliveryOrderDetailEntity.getId(), deliveryOrderDetailEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(DeliveryOrderDetailDTO.UpdateDTO updateDTO) {
        DeliveryOrderDetailEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "送货单明细"));
        DeliveryOrderDetailEntity deliveryOrderDetailEntity =  BeanMapperUtils.map(DeliveryOrderDetailEntity.class, updateDTO);

        // 数据处理
        handleData(deliveryOrderDetailEntity);
        log.info("编辑 开始修改送货单明细数据，id：【{}】", old.getId());
        boolean save = super.updateById(deliveryOrderDetailEntity);
        if(!save) {
            throw new ServiceException("送货单明细保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录送货单明细日志数据，id：【{}】", deliveryOrderDetailEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), deliveryOrderDetailEntity.getId(), "送货单明细");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, deliveryOrderDetailEntity, null, deliveryOrderDetailEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(DeliveryOrderDetailEntity deliveryOrderDetailEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
