package com.erp.server.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.service.SuperServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.model.sys.entity.MsgChannelConfig;
import com.erp.model.sys.vo.MsgChannelConfigDTO;
import com.erp.server.sys.mapper.MsgChannelConfigMapper;
import com.erp.server.sys.service.MsgChannelConfigService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Classname: MsgChannelConfigServiceImpl
 * @Description: TODO
 * @CreateTime: 2023-04-21  15:16
 * @Author: zhangchunlin
 */
@Service
public class MsgChannelConfigServiceImpl extends SuperServiceImpl<MsgChannelConfigMapper, MsgChannelConfig> implements MsgChannelConfigService {


    @Override
    public List<MsgChannelConfigDTO> findByMsgConfigId(String msgConfigId) {
        LambdaQueryWrapper<MsgChannelConfig> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MsgChannelConfig::getMsgConfigId, msgConfigId);
        List<MsgChannelConfig> list = this.list(queryWrapper);
        return BeanMapper.copyList(list, MsgChannelConfigDTO.class);
    }
}