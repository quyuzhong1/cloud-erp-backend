package com.erp.server.wms.mapper;

import com.erp.model.wms.entity.SoDeliveryNoticeDetailEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 发货通知单主表明细表 Mapper 接口
 * </p>
 *
 * @author LUO_WG
 * @since 2023-05-10
 */
@Mapper
public interface SoDeliveryNoticeDetailMapper extends BaseMapper<SoDeliveryNoticeDetailEntity> {
    /**
     * 根据来源id查询详情信息
     * @Author Luo_WG
     * @Date 2023/5/17 16:44
     * @param ids ids
     * @return java.util.List<com.erp.model.wms.entity.SoDeliveryNoticeDetailEntity>
     **/
    List<SoDeliveryNoticeDetailEntity> listDetailBySourceIds(@Param("ids") List<String> ids);

    /**
     * 根据来源明细id查询详情信息
     * @Author Luo_WG
     * @Date 2023/5/17 16:44
     * @param sourceDetailIds sourceDetailIds
     * @return java.util.List<com.erp.model.wms.entity.SoDeliveryNoticeDetailEntity>
     **/
    List<SoDeliveryNoticeDetailEntity> listDetailBySourceDetailIds(@Param("ids") List<String> sourceDetailIds);
}
