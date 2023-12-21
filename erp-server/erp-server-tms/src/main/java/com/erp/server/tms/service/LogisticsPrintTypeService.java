package com.erp.server.tms.service;
import com.erp.model.tms.entity.LogisticsPrintTypeEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.LogisticsPrintTypeDTO;

import java.util.List;

/**
 * <p>
 * 面板打印设置表 服务类
 * </p>
 *
 * @author Lambda
 * @since 2023-11-02
 */
public interface LogisticsPrintTypeService extends SuperService<LogisticsPrintTypeEntity> {

    /**
    * 新增
    * @author Lambda
    * @date: 2023-11-02
    * @param list
    * @return
    */
    Boolean add(String channelId, List<LogisticsPrintTypeDTO.AddDTO> list);

    /**
    * 修改
    * @author Lambda
    * @date: 2023-11-02
    * @param list
    * @return
    */
    Boolean update(String channelId,List<LogisticsPrintTypeDTO.UpdateDTO> list);

    /**
     * 根据渠道id 获取打印标签列表
     *@parms channelId
     *@return
     *@author yl
     *@date 2023-11-14
     */
    List<LogisticsPrintTypeDTO.ViewDTO> listByChannelId(String channelId);

    /**
     * 根据渠道删除
     *@parms channelId 渠道id
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

    /**
     * 根据渠道id查询渠道打印类型
     * @Author Luo_WG
     * @Date 2023/12/20 17:08
     * @param channelIdList
     * @return java.util.List<com.erp.model.tms.entity.LogisticsPrintTypeEntity>
     **/
    List<LogisticsPrintTypeEntity> listByChannelIds(List<String> channelIdList);
}
