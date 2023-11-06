package com.erp.server.tms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.tms.entity.ShippingTemplateRefChannelEntity;
import com.erp.server.tms.mapper.ShippingTemplateRefChannelMapper;
import com.erp.server.tms.service.ShippingTemplateRefChannelService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.tms.service.OperateLogService;
import com.erp.server.tms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.tms.dto.ShippingTemplateRefChannelDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 运费模板渠道关联表 服务实现类
 * </p>
 *
 * @author Will
 * @since 2023-11-03
 */
@Slf4j
@Service
public class ShippingTemplateRefChannelServiceImpl extends SuperServiceImpl<ShippingTemplateRefChannelMapper, ShippingTemplateRefChannelEntity> implements ShippingTemplateRefChannelService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(ShippingTemplateRefChannelDTO.AddDTO addDTO) {
        ShippingTemplateRefChannelEntity shippingTemplateRefChannelEntity = new ShippingTemplateRefChannelEntity();
        BeanMapperUtils.copy(addDTO, shippingTemplateRefChannelEntity);

        // 数据处理
        handleData(shippingTemplateRefChannelEntity);

        log.info("开始新增运费模板渠道关联单");
        boolean save = super.save(shippingTemplateRefChannelEntity);
        if(!save) {
            throw new ServiceException("运费模板渠道关联单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", commonService.getUserInfo().getUserName(), "运费模板渠道关联单" , shippingTemplateRefChannelEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, shippingTemplateRefChannelEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(shippingTemplateRefChannelEntity.getId(), shippingTemplateRefChannelEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(ShippingTemplateRefChannelDTO.UpdateDTO updateDTO) {
        ShippingTemplateRefChannelEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "运费模板渠道关联单"));
        ShippingTemplateRefChannelEntity shippingTemplateRefChannelEntity =  BeanMapperUtils.map(ShippingTemplateRefChannelEntity.class, updateDTO);

        // 数据处理
        handleData(shippingTemplateRefChannelEntity);
        log.info("编辑 开始修改运费模板渠道关联单数据，id：【{}】", old.getId());
        boolean save = super.updateById(shippingTemplateRefChannelEntity);
        if(!save) {
            throw new ServiceException("运费模板渠道关联单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录运费模板渠道关联单日志数据，id：【{}】", shippingTemplateRefChannelEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), shippingTemplateRefChannelEntity.getId(), "运费模板渠道关联单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, shippingTemplateRefChannelEntity, null, shippingTemplateRefChannelEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public List<ShippingTemplateRefChannelDTO.ViewDTO> listByMainIds(List<String> mainIdList) {
        if (CollectionUtils.isEmpty(mainIdList)) {
            return Collections.EMPTY_LIST;
        }
        return baseMapper.listByMainIds(mainIdList);
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(ShippingTemplateRefChannelEntity shippingTemplateRefChannelEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
