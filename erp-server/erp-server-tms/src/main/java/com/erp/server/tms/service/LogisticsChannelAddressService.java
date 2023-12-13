package com.erp.server.tms.service;
import com.erp.model.tms.entity.LogisticsChannelAddressEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.LogisticsChannelAddressDTO;

import java.util.List;

/**
 * <p>
 * 渠道地址表 服务类
 * </p>
 *
 * @author Lambda
 * @since 2023-11-02
 */
public interface LogisticsChannelAddressService extends SuperService<LogisticsChannelAddressEntity> {

    /**
    * 新增
    * @author Lambda
    * @date: 2023-11-02
    * @param list
    * @return
    */
    Boolean add(String channelId, List<LogisticsChannelAddressDTO.AddDTO> list);

    /**
    * 修改
    * @author Lambda
    * @date: 2023-11-02
    * @param list
    * @return
    */
    Boolean update(String channelId,List<LogisticsChannelAddressDTO.UpdateDTO> list);

    /**
     * 根据渠道id查询数据
     *@parms channelId
     *@return 
     *@author yl
     *@date 2023-11-14
     */
    List<LogisticsChannelAddressDTO.ViewDTO> listByChannelId(String channelId);

    /**
     * 根据渠道删除数据
     *@parms channelIdList
     *@return
     *@author yl
     *@date 2023-11-15
     */
    void removeByChannelIdList(List<String> channelIdList);

    /**
     *复制渠道
     *@parms channelId 复制的渠道id
     *@return addChannelId 添加的渠道id
     *@author yl
     *@date 2023-11-15
     */
    void copy(String channelId, String addChannelId);
}
