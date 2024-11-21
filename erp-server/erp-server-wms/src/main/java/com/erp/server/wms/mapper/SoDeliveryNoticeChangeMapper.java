package com.erp.server.wms.mapper;
import com.erp.model.wms.entity.SoDeliveryNoticeChangeEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.wms.dto.SoDeliveryNoticeChangeDTO;
import com.common.business.dto.base.ApproveStatusQtyDTO;

import java.util.List;

/**
 * <p>
 * 发货通知变更单 Mapper 接口
 * </p>
 *
 * @author lrp
 * @since 2024-10-23
 */
@Mapper
public interface SoDeliveryNoticeChangeMapper extends BaseMapper<SoDeliveryNoticeChangeEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<SoDeliveryNoticeChangeDTO.ListDTO> paging(Page query, @Param("params") SoDeliveryNoticeChangeDTO.PagingParamDTO params);


    /**
    * 获取状态统计
    * @param searchParam
    * @return
    */
    List<SoDeliveryNoticeChangeDTO.TabListDTO> tabList(@Param("params") SoDeliveryNoticeChangeDTO.PagingParamDTO searchParam);

    List<SoDeliveryNoticeChangeDTO.ViewDetail> listViewDetailList(@Param("id") String id);

    IPage<SoDeliveryNoticeChangeDTO.ProductDTO> productPaging(Page query, @Param("params") SoDeliveryNoticeChangeDTO.ProductAddDTO params);
}
