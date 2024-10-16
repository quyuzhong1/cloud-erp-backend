package com.erp.server.wms.mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.wms.dto.SoB2cDeliveryInterceptDTO;
import com.erp.model.wms.entity.SoB2cDeliveryInterceptEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * b2c发货拦截单 Mapper 接口
 * </p>
 *
 * @author Luo_WG
 * @since 2023-12-13
 */
@Mapper
public interface SoB2cDeliveryInterceptMapper extends BaseMapper<SoB2cDeliveryInterceptEntity> {

    /**
     * 获取状态统计
     * @Author Luo_WG
     * @Date 2023/12/25 14:38
     * @param searchParam
     * @return java.util.List<com.erp.model.wms.dto.SoB2cDeliveryInterceptDTO.TabListDTO>
     **/
    List<SoB2cDeliveryInterceptDTO.TabListDTO> tabList(SoB2cDeliveryInterceptDTO.PagingParamDTO searchParam);

    /**
     * 分页查询
     * @Author Luo_WG
     * @Date 2023/12/14 11:03
     * @param params
     * @return com.common.business.vo.PagingVO<com.erp.model.wms.dto.SoB2cDeliveryInterceptDTO.ListDTO>
     **/
    IPage<SoB2cDeliveryInterceptDTO.ListDTO> paging(Page query, @Param("params") SoB2cDeliveryInterceptDTO.PagingParamDTO params);

    List<SoB2cDeliveryInterceptDTO.InterceptInventoryDTO> getInterceptInventoryDTOList(@Param("ids") List<String> ids);
}
