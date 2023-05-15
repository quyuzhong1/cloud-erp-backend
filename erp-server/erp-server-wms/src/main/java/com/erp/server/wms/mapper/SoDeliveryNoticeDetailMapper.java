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
    List<SoDeliveryNoticeDetailEntity> listDetailBySourceIds(@Param("ids") List<String> ids);
}
