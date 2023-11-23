package com.erp.server.wms.mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.wms.dto.FbaDeliveryDTO;
import com.erp.model.wms.dto.RequisitionApplicationDTO;
import com.erp.model.wms.entity.RequisitionApplicationEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 要货申请单 Mapper 接口
 * </p>
 *
 * @author Luo_WG
 * @since 2023-11-16
 */
@Mapper
public interface RequisitionApplicationMapper extends BaseMapper<RequisitionApplicationEntity> {

    /**
     * 获取状态统计
     * @Author Luo_WG
     * @Date 2023/11/21 18:18
     * @param searchParam
     * @return java.util.List<com.erp.model.wms.dto.RequisitionApplicationDTO.TabListDTO>
     **/
    List<RequisitionApplicationDTO.TabListDTO> tabList(FbaDeliveryDTO.PagingParamDTO searchParam);

    /**
     * 分页查询
     * @Author Luo_WG
     * @Date 2023/11/21 19:04
     * @param query
     * @param params
     * @return com.baomidou.mybatisplus.core.metadata.IPage<com.erp.model.wms.dto.FbaDeliveryDTO.ListDTO>
     **/
    IPage<RequisitionApplicationDTO.ListDTO> paging(Page query, @Param("params") RequisitionApplicationDTO.PagingParamDTO params);

    /**
     * 处理列表查询
     * @Author Luo_WG
     * @Date 2023/11/23 16:24
     * @param ids
     * @return java.util.List<com.erp.model.wms.dto.RequisitionApplicationDTO.handleListDTO>
     **/
    List<RequisitionApplicationDTO.handleListDTO> handleList(@Param("ids") List<String> ids);
}
