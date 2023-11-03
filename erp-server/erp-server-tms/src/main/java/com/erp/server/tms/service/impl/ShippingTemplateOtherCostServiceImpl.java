package com.erp.server.tms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.erp.model.tms.entity.ShippingTemplateOtherCostEntity;
import com.erp.server.tms.mapper.ShippingTemplateOtherCostMapper;
import com.erp.server.tms.service.ShippingTemplateOtherCostService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.tms.service.OperateLogService;
import com.erp.server.tms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.tms.dto.ShippingTemplateOtherCostDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Will
 * @since 2023-11-03
 */
@Slf4j
@Service
public class ShippingTemplateOtherCostServiceImpl extends SuperServiceImpl<ShippingTemplateOtherCostMapper, ShippingTemplateOtherCostEntity> implements ShippingTemplateOtherCostService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(ShippingTemplateOtherCostDTO.AddDTO addDTO) {
        ShippingTemplateOtherCostEntity shippingTemplateOtherCostEntity = new ShippingTemplateOtherCostEntity();
        BeanMapperUtils.copy(addDTO, shippingTemplateOtherCostEntity);

        // 数据处理
        handleData(shippingTemplateOtherCostEntity);

        log.info("开始新增");
        boolean save = super.save(shippingTemplateOtherCostEntity);
        if(!save) {
            throw new ServiceException("保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", commonService.getUserInfo().getUserName(), "" , shippingTemplateOtherCostEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, shippingTemplateOtherCostEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(fbaDeliveryEntity.getId(), fbaDeliveryEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(ShippingTemplateOtherCostDTO.UpdateDTO updateDTO) {
        ShippingTemplateOtherCostEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, ""));
        ShippingTemplateOtherCostEntity shippingTemplateOtherCostEntity =  BeanMapperUtils.map(ShippingTemplateOtherCostEntity.class, updateDTO);

        // 数据处理
        handleData(shippingTemplateOtherCostEntity);
        log.info("编辑 开始修改数据，id：【{}】", old.getId());
        boolean save = super.updateById(shippingTemplateOtherCostEntity);
        if(!save) {
            throw new ServiceException("保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录日志数据，id：【{}】", shippingTemplateOtherCostEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), shippingTemplateOtherCostEntity.getId(), "");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, shippingTemplateOtherCostEntity, null, shippingTemplateOtherCostEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(ShippingTemplateOtherCostEntity shippingTemplateOtherCostEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
