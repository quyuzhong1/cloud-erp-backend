package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.oms.dto.SoReturnDTO;
import com.erp.model.wms.dto.SoDeliveryNoticeDTO;
import com.erp.model.wms.entity.SoDeliveryNoticeEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 发货通知单主表明细表 Mapper 接口
 * </p>
 *
 * @author LUO_WG
 * @since 2023-05-10
 */
@Mapper
public interface SoDeliveryNoticeMapper extends BaseMapper<SoDeliveryNoticeEntity> {

    IPage<SoDeliveryNoticeDTO.PagingView> paging(Page query, @Param("params") SoDeliveryNoticeDTO.PagingParam params);

    Integer listCount(@Param("params") SoDeliveryNoticeDTO.PagingParam pagingParam);
}
