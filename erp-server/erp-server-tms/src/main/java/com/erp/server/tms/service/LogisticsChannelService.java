package com.erp.server.tms.service;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.tms.entity.LogisticsChannelEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.LogisticsChannelDTO;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;

import java.util.List;

/**
 * <p>
 * 物流渠道表 服务类
 * </p>
 *
 * @author Lambda
 * @since 2023-11-02
 */
public interface LogisticsChannelService extends SuperService<LogisticsChannelEntity> {

    /**
    * 新增
    * @author Lambda
    * @date: 2023-11-02
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(LogisticsChannelDTO.AddDTO dto);

    /**
    * 修改
    * @author Lambda
    * @date: 2023-11-02
    * @param dto
    * @return
    */
    Boolean update(LogisticsChannelDTO.UpdateDTO dto);

    /**
     * @description: 物流渠道列表
     * @author Will
     * @date: 2023/11/10 10:06
     * @return List<ListSelectDTO>
     */
    List<LogisticsChannelDTO.ListSelectDTO> listLogisticsChannel(List<String> logisticsSupplierIds);

    /**
     * 物流商id 获取渠道列表
     * @param mainIdList
     * @param name
     * @return
     */
    List<LogisticsChannelDTO.BaseDTO> listBaseByMainIdList(List<String> mainIdList,String name);

    /**
     * 详情
     *@parms id
     *@return 
     *@author yl
     *@date 2023-11-14
     */
    LogisticsChannelDTO.ViewDTO view(String id);

    /**
     *删除渠道
     *@parms id
     *@return
     *@author yl
     *@date 2023-11-15
     */
    BatchResultDTO delete(String id);

    /**
     *更改启用停用状态
     *@parms id
     *@return disabled 状态
     *@author yl
     *@date 2023-11-15
     */
    BatchResultDTO updateStatus(String id, Boolean disabled);

    /**
     * 删除渠道根据来源id
     *@parms sourceIdList
     *@return 
     *@author yl
     *@date 2023-11-15
     */
    void removeByMainIdList(List<String> sourceIdList);

    /**
     * 复制渠道
     *@parms id
     *@return
     *@author yl
     *@date 2023-11-15
     */
    Boolean copy(String id);

    /**
     *
     *@parms 获取到所有的渠道
     *@return 
     *@author yl
     *@date 2023-11-16
     */
    List<BaseDropDownDTO.DisabledDTO> listAll();


    /**
     * 根据供应商id 获取启用禁用的列表
     *@parms supplierId
     *@return
     *@author yl
     *@date 2023-11-20
     */
    List<BaseIdDTO.CodeDTO> listBySupplierId(String supplierId);

    /**
     * 根据地址id 获取对应渠道
     * @param addressId
     * @return
     */
    List<LogisticsChannelEntity> listByAddressId(String addressId);

    /**
     * 根据同步的来源id 获取数据
     *@parms syncSourceIdList 同步的来源ud
     *@param  mainId 物流商id
     *@return
     *@author yl
     *@date 2023-11-22
     */
    List<LogisticsChannelEntity> listBySyncSourceIds(List<String> syncSourceIdList,String mainId);

    /**
     * 根据渠道id 集合删除
     *@parms deleteSyncSourceIdList
     *@return 
     *@author yl
     *@date 2023-11-27
     */
    void removeByIdList(List<String> deleteChannelIdList);


    /**
     * 根据物流商id 获取渠道
     *@parms logisticsSupplierId
     *@return
     *@author yl
     *@date 2023-11-28
     */
    List<BaseDropDownDTO.DisabledDTO> listByLogisticsSupplierId(String logisticsSupplierId);

    /**
     * 获取信息
     * @author yl
     * @date 2023-12-07 19:22
     * @param channelId 渠道id
     * @return
     */
    LogisticsChannelDTO.BaseDTO getInfoById(String channelId);
}
