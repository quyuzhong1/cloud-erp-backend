package com.erp.server.srm.service.impl;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.srm.entity.DeliveryOrderEntity;
import com.erp.model.sys.entity.SysUserWechatEntity;
import com.erp.server.srm.mapper.DeliveryOrderMapper;
import com.erp.server.srm.service.DeliveryOrderService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.srm.service.OperateLogService;
import com.erp.server.srm.service.CommonService;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import com.common.core.controller.vo.ApiResult;
import cn.hutool.core.util.ObjectUtil;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.srm.dto.DeliveryOrderDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 送货单 服务实现类
 * </p>
 *
 * @author lrp
 * @since 2024-01-12
 */
@Slf4j
@Service
public class DeliveryOrderServiceImpl extends SuperServiceImpl<DeliveryOrderMapper, DeliveryOrderEntity> implements DeliveryOrderService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(DeliveryOrderDTO.AddDTO addDTO) {
        DeliveryOrderEntity deliveryOrderEntity = new DeliveryOrderEntity();
        BeanMapperUtils.copy(addDTO, deliveryOrderEntity);

        // 数据处理
        handleData(deliveryOrderEntity);

        log.info("开始新增送货单");
        // 生成单号
        // TODO 此处的null需填写生成单号类型，type查看BusinessNoTypeEnum枚举类 注意需要填写prefix 为单号前缀
        String code = docNoGenHelper.generateCode(null);
        deliveryOrderEntity.setCode(code);
        boolean save = super.save(deliveryOrderEntity);
        if(!save) {
            throw new ServiceException("送货单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", commonService.getUserInfo().getUserName(), "送货单" , deliveryOrderEntity.getCode());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, deliveryOrderEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(deliveryOrderEntity.getId(), code);
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(DeliveryOrderDTO.UpdateDTO updateDTO) {
        DeliveryOrderEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "送货单"));
        DeliveryOrderEntity deliveryOrderEntity =  BeanMapperUtils.map(DeliveryOrderEntity.class, updateDTO);

        // 数据处理
        handleData(deliveryOrderEntity);
        log.info("编辑 开始修改送货单数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(deliveryOrderEntity);
        if(!save) {
            throw new ServiceException("送货单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录送货单日志数据，单号：【{}】", deliveryOrderEntity.getCode());
            String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), deliveryOrderEntity.getCode(), "送货单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, deliveryOrderEntity, null, deliveryOrderEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public Integer countByPrint(String supplierId, boolean isPrint) {
        LambdaQueryWrapper<DeliveryOrderEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DeliveryOrderEntity::getSupplierId,supplierId);
        queryWrapper.eq(DeliveryOrderEntity::getIsPrint,isPrint);
        return this.count(queryWrapper);
    }

    @Override
    public Integer countByReceiveStatus(String supplierId, String status) {
        LambdaQueryWrapper<DeliveryOrderEntity> queryWrapper = this.getDefaultWrapper(supplierId);
        queryWrapper.eq(DeliveryOrderEntity::getReceiptStatus,status);
        return this.count(queryWrapper);
    }

    private LambdaQueryWrapper<DeliveryOrderEntity> getDefaultWrapper(String supplierId) {
        return new LambdaQueryWrapper<DeliveryOrderEntity>()
                .eq(DeliveryOrderEntity::getSupplierId, supplierId);
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(DeliveryOrderEntity deliveryOrderEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
