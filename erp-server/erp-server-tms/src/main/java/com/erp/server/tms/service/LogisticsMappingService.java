package com.erp.server.tms.service;
import com.common.business.service.SuperService;
import com.erp.model.tms.dto.LogisticsMappingDTO;
import com.erp.model.tms.entity.LogisticsMappingEntity;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;

import java.util.List;

/**
 * <p>
 * 物流渠道映射表 服务类
 * </p>
 *
 * @author Lambda
 * @since 2023-11-02
 */
public interface LogisticsMappingService extends SuperService<LogisticsMappingEntity> {

    /**
    * 新增
    * @author Lambda
    * @date: 2023-11-02
    * @param dtoList
     *@param channelId
    * @return
    */
    Boolean add(String channelId, List<LogisticsMappingDTO.AddDTO> dtoList);

    /**
    * 修改
    * @author Lambda
    * @date: 2023-11-02
    * @param list
    * @return
    */
    Boolean update(String channelId,List<LogisticsMappingDTO.UpdateDTO> list);


    /**
     * 根据渠道id 查询数据
     *@parms channelId
     *@return
     *@author yl
     *@date 2023-11-14
     */
    List<LogisticsMappingDTO.ViewDTO> listByChannelId(String channelId);

    /**
     * 根据渠道id 删除
     *@parms channelId
     *@return 
     *@author yl
     *@date 2023-11-15
     */
    void removeByChannelIdList(List<String> channelIds);

    /**
     *
     *@parms channelId 复制的渠道id
     *@return addChannelId 添加的渠道id
     *@author yl
     *@date 2023-11-15
     */
    void copy(String channelId, String addChannelId);


    /**
     *
     *@parms salesPlatform 销售平台 channelId 渠道id
     *@return
     *@author yl
     *@date 2023-11-27
     */
    LogisticsSaleChannelEntity getBySalesPlatform(String salesPlatform, String channelId);
    /**
     * @description: 根据物流映射参数获取物流映射表
     * @author Will
     * @date: 2024/4/23 16:14
     * @param paramDTO
     * @return LogisticsMappingEntity
     */
    LogisticsMappingEntity getByLogisticsMappingParam(LogisticsMappingDTO.SearchParamDTO paramDTO);
    /**
     * @description: 根据物流渠道id获取物流映射表
     * @author jack
     * @date: 2024/10/09
     * @param id
     * @return List<LogisticsMappingEntity>
     */
    List<LogisticsMappingEntity> listDbByChannelId(String id);
}
