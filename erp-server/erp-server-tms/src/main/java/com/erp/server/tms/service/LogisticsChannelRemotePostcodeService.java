package com.erp.server.tms.service;
import com.erp.model.tms.entity.LogisticsChannelRemotePostcodeEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.LogisticsChannelRemotePostcodeDTO;

import java.util.List;

/**
 * <p>
 * 渠道邮编组设置表 服务类
 * </p>
 *
 * @author jack
 * @since 2024-12-05
 */
public interface LogisticsChannelRemotePostcodeService extends SuperService<LogisticsChannelRemotePostcodeEntity> {

    /**
     * 根据渠道id
     * @author jack
     * @date: 2024-12-05
     * @param id
     * @return
     */
    LogisticsChannelRemotePostcodeDTO.ViewDTO getByChannelId(String id);

    BaseResultDTO.AddDTO batchUpdate(String channelId, LogisticsChannelRemotePostcodeDTO.ViewDTO remotePostcodeDTO);

    void removeByChannelIdList(List<String> list);

    void copy(String id, String addChannelId);
}
