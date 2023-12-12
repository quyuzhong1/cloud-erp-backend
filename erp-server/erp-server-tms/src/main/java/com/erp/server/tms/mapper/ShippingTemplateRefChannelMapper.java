package com.erp.server.tms.mapper;
import com.erp.model.tms.dto.ShippingTemplateRefChannelDTO;
import com.erp.model.tms.entity.ShippingTemplateRefChannelEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 运费模板渠道关联表 Mapper 接口
 * </p>
 *
 * @author Will
 * @since 2023-11-03
 */
@Mapper
public interface ShippingTemplateRefChannelMapper extends BaseMapper<ShippingTemplateRefChannelEntity> {

    /**
     * @description: 根据主表ids查询
     * @author Will
     * @date: 2023/11/6 17:07
     * @param mainIdList
     * @return List<ViewDTO>
     */
    List<ShippingTemplateRefChannelDTO.ViewDTO> listByMainIds(@Param("mainIdList") List<String> mainIdList);

    /**
     * 根据渠道id 获取对应数据
     *@parms channelIdList
     *@return 
     *@author yl
     *@date 2023-11-14
     */
    List<ShippingTemplateRefChannelEntity> listChannelIdList(@Param("channelIdList") List<String> channelIdList);
}
