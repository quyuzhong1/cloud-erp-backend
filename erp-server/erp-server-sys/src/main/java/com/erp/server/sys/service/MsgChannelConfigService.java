package com.erp.server.sys.service;

import com.common.business.service.SuperService;
import com.erp.model.sys.entity.MsgChannelConfig;
import com.erp.model.sys.vo.MsgChannelConfigDTO;

import java.util.List;

/**
 * @Classname: MsgChannelConfigService
 * @Description: TODO
 * @CreateTime: 2023-04-21  15:16
 * @Author: zhangchunlin
 */
public interface MsgChannelConfigService extends SuperService<MsgChannelConfig> {


    /**
     * 根据消息配置id获取消息渠道配置信息
     * @param msgConfigId
     * @return
     */
    List<MsgChannelConfigDTO> findByMsgConfigId(String msgConfigId);

}
