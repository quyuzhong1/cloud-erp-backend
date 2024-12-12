package com.erp.server.wms.mapper;
import com.erp.model.wms.dto.VirtualInventoryAgeDTO;
import com.erp.model.wms.dto.VirtualInventoryDetailHisDTO;
import com.erp.model.wms.entity.VirtualInventoryDetailHisEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;


/**
 * <p>
 * 虚拟仓库存历史信息 Mapper 接口
 * </p>
 *
 * @author will
 * @since 2024-12-03
 */
@Mapper
public interface VirtualInventoryDetailHisMapper extends BaseMapper<VirtualInventoryDetailHisEntity> {
    /**
     * 根据paramDTO参数查询
     * @author will
     * @date 2024/12/6 11:52
     * @param paramDTO
     * @return List<VirtualInventoryAgeDTO.viewHisInventoryAgeDetailDTO>
     */
    List<VirtualInventoryAgeDTO.viewHisInventoryAgeDetailDTO> listByParam(@Param("params") VirtualInventoryDetailHisDTO.ParamDTO paramDTO);
    /**
     * 查询虚拟仓历史数据
     * @author will
     * @date 2024/12/9 19:55
     * @param date 
     * @return List<VirtualInventoryHisEntity>
     */
    List<VirtualInventoryDetailHisDTO.ViewDTO> listVirtualInventoryHisJobData(@Param("date")LocalDate date);
    /**
     * 查询历史平均库龄数据
     * @author will
     * @date 2024/12/10 17:36
     * @param dto
     * @return viewHisInventoryAgeDetailDTO
     */
    VirtualInventoryAgeDTO.viewHisInventoryAgeDetailDTO getHisInventoryAgeDetail(@Param("params") VirtualInventoryAgeDTO.HisInventoryAgeDetailParamDTO dto);
}
