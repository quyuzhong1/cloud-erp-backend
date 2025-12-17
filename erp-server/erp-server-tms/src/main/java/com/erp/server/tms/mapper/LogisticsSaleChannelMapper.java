package com.erp.server.tms.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.tms.dto.LogisticsSaleChannelDTO;
import com.erp.model.tms.dto.SaleChannelDTO;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 销售平台物流渠道表 Mapper 接口
 * </p>
 *
 * @author zdy
 * @since 2023-11-08
 */
@Mapper
public interface LogisticsSaleChannelMapper extends BaseMapper<LogisticsSaleChannelEntity> {

    /**
     * 根据类型获取原始渠道列表
     *
     * @param platformType
     * @return
     */
    List<SaleChannelDTO> listByType(@Param("platformType") String platformType,@Param("servicePlatform")String servicePlatform);

    /**
     *
     *@parms logisticsPlatform
     *@return 
     *@author yl
     *@date 2023-11-27
     */
    List<LogisticsSaleChannelEntity> listByLogisticsPlatform(@Param("logisticsPlatform") String logisticsPlatform,@Param("servicePlatform")String servicePlatform);

    IPage<SaleChannelDTO> pagingSelect(@Param("query") Page query, @Param("params") LogisticsSaleChannelDTO.QueryDTO params);
}
