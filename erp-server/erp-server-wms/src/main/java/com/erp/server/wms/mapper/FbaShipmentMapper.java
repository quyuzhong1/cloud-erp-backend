package com.erp.server.wms.mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseIdsDTO;
import com.erp.model.wms.dto.FbaShipmentDTO;
import com.erp.model.wms.dto.WmsDataCompareTaskDTO;
import com.erp.model.wms.entity.FbaShipmentEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;


/**
 * <p>
 * FBI货件表 Mapper 接口
 * </p>
 *
 * @author Luo_WG
 * @since 2023-10-30
 */
@Mapper
public interface FbaShipmentMapper extends BaseMapper<FbaShipmentEntity> {

    /**
     * 列表查詢
     * @param query
     * @param params
     * @return com.baomidou.mybatisplus.core.metadata.IPage<com.erp.model.wms.dto.FbaShipmentDTO.ListDTO>
     **/
    IPage<FbaShipmentDTO.ListDTO> paging(Page query, @Param("params") FbaShipmentDTO.PagingParamDTO params);

    /**
     * 下推发货单列表查询
     * @Author Luo_WG
     * @Date 2023/11/1 9:24
     * @param ids
     * @return java.util.List<com.erp.model.wms.dto.FbaShipmentDTO.GenerateDeliverView>
     **/
    List<FbaShipmentDTO.GenerateDeliverView> generateDeliverView(@Param("params") BaseIdsDTO.IdsDTO ids);


    /**
     * 忽略isDelete通过id更新
     * @author  Jim
     * @date 2023/11/20
     */
    boolean updateByIdWithoutIsDelete(FbaShipmentEntity entity);

    /**
     * 下推要货申请
     * @Author Luo_WG
     * @Date 2023/11/27 15:58
     * @param ids
     * @return java.util.List<com.erp.model.wms.dto.FbaShipmentDTO.GenerateRequisitionApplicationViewDTO>
     **/
    List<FbaShipmentDTO.GenerateRequisitionApplicationViewDTO> generateRequisitionApplicationView(@Param("ids") List<String> ids);

    List<FbaShipmentDTO.ListDTO> export(@Param("params") FbaShipmentDTO.PagingParamDTO dto);
    Page<FbaShipmentDTO.ListDTO> export(@Param("page") Page<FbaShipmentDTO.ListDTO> page, @Param("params") FbaShipmentDTO.PagingParamDTO dto);

    /**
     * 根据条件获取数据对比系统数据
     * @param params
     * @return
     */
    List<Map<String, String>> getDataCompareByCondition(@Param("params") WmsDataCompareTaskDTO.FbaShipmentDTO params);
    
    Integer getDataCompareByConditionCount(@Param("params") WmsDataCompareTaskDTO.FbaShipmentDTO params);

    IPage<FbaShipmentDTO.SearchResultDTO> search(Page query, @Param("params") FbaShipmentDTO.SearchDTO params);
}
