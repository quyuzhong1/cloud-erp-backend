package com.erp.server.tms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseIdDTO;
import com.erp.model.tms.dto.LogisticsBillDetailQueryDTO;
import com.erp.model.tms.dto.LogisticsChannelDTO;
import com.erp.model.tms.dto.LogisticsSupplierDTO;
import com.erp.model.tms.entity.LogisticsChannelEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 物流渠道表 Mapper 接口
 * </p>
 *
 * @author Lambda
 * @since 2023-11-02
 */
@Mapper
public interface LogisticsChannelMapper extends BaseMapper<LogisticsChannelEntity> {
    /**
     * @description: 物流渠道列表
     * @author Will
     * @date: 2023/11/10 10:08
     * @return List<ListSelectDTO>
     */
    List<LogisticsChannelDTO.ListSelectDTO> listLogisticsChannel(@Param("params") LogisticsChannelDTO.ParamDTO params);

    /**
     * 根据供应商id获取到 对应渠道下 启用禁用的列表
     *@parms supplierId
     *@return
     *@author yl
     *@date 2023-11-20
     */
    List<BaseIdDTO.CodeDTO> listBySupplierId(@Param("supplierId") String supplierId);

    /** 根据地址id获取数据
     *
     *@parms addressId
     *@return
     *@author yl
     *@date 2023-11-20
     */
    List<LogisticsChannelEntity> listByAddressId(@Param("addressId") String addressId);

    /**
     * 根据名称获取渠道列表
     * @param channelName
     * @return
     */
    List<LogisticsChannelEntity> getChannelByName(@Param("channelName") String channelName);

    /**
     * 根据渠道ids 获取到平台信息
     * @param channelIdList
     * @return
     */
    List<LogisticsChannelDTO.LogisticsPlatformDTO> listChannelPlatform(@Param("channelIdList")List<String> channelIdList);

    List<LogisticsChannelDTO.ProvideChannelDTO> getProvideChannel(@Param("channelCodeList") List<String> channelCodeList, @Param("provideNameList")List<String> provideNameList);

    /**
     * 根据归属进行模糊匹配
     * @param mainIdList
     * @param params
     * @return
     */
    List<LogisticsChannelEntity> listByMainIdsAndName(@Param("mainIdList") List<String> mainIdList,@Param("params") LogisticsSupplierDTO.PagingParamDTO params);
    IPage<LogisticsChannelDTO.PagingSelectDTO> pagingSelect(Page query, @Param("params") LogisticsChannelDTO.SelectDTO params);

    /**
     * 根据渠道汇总时间段内未更新运单号记录
     * @return
     */
    List<LogisticsChannelDTO.WarnReportDTO> getWarnReportByChannel(@Param("query") LogisticsBillDetailQueryDTO query);

    /**
     * 获取物流类型/仓库类型下 渠道列表
     * @param platform
     * @param authStatus
     * @param warehousePlatformType
     * @param disabled
     * @return
     */
    List<LogisticsChannelDTO.ChannelWarehouseDTO> listChannelWarehouse(@Param("platform") String platform, @Param("authStatus") String authStatus, @Param("warehousePlatformType") String warehousePlatformType, @Param("disabled") Boolean disabled);

    Boolean estimateIsOutOfRangeDelivery(@Param("logisticsChannelId")String logisticsChannelId, @Param("country")String country, @Param("postCode")String postCode);
}
