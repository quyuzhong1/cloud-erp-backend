package com.erp.server.tms.service;

import com.common.business.dto.base.*;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.dto.LogisticsBillDetailQueryDTO;
import com.erp.model.tms.dto.LogisticsChannelDTO;
import com.erp.model.tms.dto.LogisticsSupplierDTO;
import com.erp.model.tms.entity.LogisticsChannelEntity;
import com.erp.model.wms.dto.DictBasicDTO;

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
    List<LogisticsChannelDTO.ListSelectDTO> listLogisticsChannel(LogisticsChannelDTO.ParamDTO dto);

    /**
     * 物流商id 获取渠道列表
     * @param mainIdList
     * @param params
     * @return
     */
    List<LogisticsChannelDTO.BaseDTO> listBaseByMainIdList(List<String> mainIdList, LogisticsSupplierDTO.PagingParamDTO params);

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

    /**
     * 根据渠道id查询物流商信息
     * @Author Luo_WG
     * @Date 2023/12/15 15:45
     * @param channelIds
     * @return java.util.List<com.erp.model.tms.dto.LogisticsChannelDTO.BaseDTO>
     **/
    List<LogisticsChannelDTO.BaseDTO> listChannelInfoById(List<String> channelIds);


    /**
     * 根据名称匹配现在的渠道
     * @param channelName
     * @return
     */
    List<LogisticsChannelEntity> getChannelByName(String channelName);

    
    /**
     * 获取渠道 平台信息
     * @description
     * @param channelIdList
     * @author Lambda
     * @return 
     * @create 2024-01-05 14:24
     */
    List<LogisticsChannelDTO.LogisticsPlatformDTO> listChannelPlatform(List<String> channelIdList);


    /**
     * 获取供应商渠道信息
     * @return
     */
    List<LogisticsChannelDTO.ProvideChannelDTO> getProvideChannel(List<String> channelCodeList,List<String> provideNameList);

    LogisticsChannelDTO.LogisticsChannelConstraintDTO getLogisticsChannelConstraint(String channelId, String country);

    List<LogisticsChannelEntity> listByName(List<String> channelNameList);

    List<BaseDropDownDTO.Tree> tree(Boolean filterDisabled, String type);

    LogisticsChannelDTO.SignShipDTO getScaleChannelByChannelById(String logisticsChannelId, String dictPlatform);
    /**
     * 所有渠道下拉远程搜索
     * @return PagingVO<BaseDropDownDTO.DisabledDTO>
     */
    PagingVO<LogisticsChannelDTO.PagingSelectDTO> pagingSelect(PagingDTO<LogisticsChannelDTO.SelectDTO> dto);

    /**
     * 根据主表id，更新启用状态
     * @param channelIds
     * @param status
     */
    void updateStatusByIds(List<String> channelIds, Boolean status);

    /**
     * 发货配置
     * @param dto
     */
    void deliverySetting(LogisticsChannelDTO.DeliveryDTO dto);

    /**
     * 根据主键id获取渠道列表
     * @param id
     * @return
     */
    List<LogisticsChannelEntity> listByMainId(String id);

    /**
     * 根据渠道汇总时间段内未更新运单号记录
     * @return
     */
    List<LogisticsChannelDTO.WarnReportDTO> getWarnReportByChannel(LogisticsBillDetailQueryDTO query);

    /**
     * 获取物流类型/仓库类型下 渠道列表
     * @param platform 物流平台
     * @param authStatus 授权类型
     * @param warehousePlatformType 海外仓类型
     * @param disabled 是否启用
     * @return
     */
    List<LogisticsChannelDTO.ChannelWarehouseDTO> listChannelWarehouse(String platform, String authStatus, String warehousePlatformType, Boolean disabled);
    /**
     * 根据渠道id ， 国家二字码，邮编判断是否属于偏远邮编组
     * @param
     * @return
     */
    Boolean estimateIsOutOfRangeDelivery(String logisticsChannelId, String country, String postCode);

    PagingVO<LogisticsChannelDTO.PagingViewDTO> paging(PagingDTO<LogisticsChannelDTO.PagingParamDTO> dto);

    void platformSignSetting(LogisticsChannelDTO.PlatformSignSettingDTO dto);

    List<LogisticsChannelDTO.WarehouseChannelDTO> listWarehouseChannel();

    List<LogisticsChannelDTO.PlatformChannelDTO> listByPlatformCode(List<String> platformCodeList);

    List<DictBasicDTO.DropDownDTO> getByPlatformWarehouseAndType(LogisticsChannelDTO.PlatformWarehouseDTO dto);
}
