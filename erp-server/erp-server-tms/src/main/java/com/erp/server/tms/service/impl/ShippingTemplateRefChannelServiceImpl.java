package com.erp.server.tms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.tms.entity.ShippingRegionCityEntity;
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
import java.util.stream.Collectors;

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

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean add(List<ShippingTemplateRefChannelDTO.AddDTO> list, String mainId) {
        //删除原有城市
        deleteByMainId(mainId);
        if (CollectionUtils.isEmpty(list)) {
            return Boolean.TRUE;
        }
        List<ShippingTemplateRefChannelEntity> refChannelList = BeanMapperUtils.copyList(ShippingTemplateRefChannelEntity.class, list);

        log.info("开始新增渠道关联");
        //新增城市
        boolean save = this.saveBatch(refChannelList);
        if (!save) {
            throw new ServiceException("运渠道关联保存失败");
        }
        return save;
    }


    @Override
    public List<ShippingTemplateRefChannelDTO.ViewDTO> listByMainIds(List<String> mainIdList) {
        if (CollectionUtils.isEmpty(mainIdList)) {
            return Collections.emptyList();
        }
        return baseMapper.listByMainIds(mainIdList);
    }

    @Override
    public List<ShippingTemplateRefChannelEntity> listChannelIdList(List<String> channelIdList) {
        if (CollectionUtils.isEmpty(channelIdList)) {
            return Collections.emptyList();
        }
        return baseMapper.listChannelIdList(channelIdList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addRef(String channelId, String templateId) {
        deleteByChannelId(channelId);
        ShippingTemplateRefChannelEntity entity = new ShippingTemplateRefChannelEntity();
        entity.setLogisticsChannelId(channelId);
        entity.setMainId(templateId);
        this.save(entity);
    }

    private void deleteByChannelId(String channelId) {
        lambdaUpdate().eq(ShippingTemplateRefChannelEntity::getLogisticsChannelId, channelId).remove();
    }

    /**
     * @param mainId
     * @description: 删除渠道
     * @author Will
     * @date: 2023/11/8 14:35
     */
    private void deleteByMainId(String mainId) {
        lambdaUpdate().eq(ShippingTemplateRefChannelEntity::getMainId, mainId).remove();
    }

    /**
     * 新增修改处理数据
     */
    private void handleData(ShippingTemplateRefChannelEntity shippingTemplateRefChannelEntity) {
        // TODO 验证数据 & 数据赋值
    }
}
