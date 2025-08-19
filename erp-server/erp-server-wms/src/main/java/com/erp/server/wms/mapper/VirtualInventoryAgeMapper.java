package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.wms.dto.VirtualInventoryAgeDTO;
import com.erp.model.wms.entity.VirtualInventoryAgeEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;


/**
 * <p>
 * 库龄分析表 Mapper 接口
 * </p>
 *
 * @author will
 * @since 2025-08-19
 */
@Mapper
public interface VirtualInventoryAgeMapper extends BaseMapper<VirtualInventoryAgeEntity> {
    /**
     * 获取生成库龄分析数据
     * @author will
     * @date 2025/8/19 15:15
     * @param date
     * @return List<AddDTO>
     */
    List<VirtualInventoryAgeDTO.AddDTO> listGenerateVirtualInventoryAge(@Param("date") LocalDate date);
    /**
     * 库龄分析分页查询
     * @author will
     * @date 2025/8/19 16:27
     * @param page
     * @param params
     * @return IPage<ListDTO>
     */
    IPage<VirtualInventoryAgeDTO.ListDTO> paging(Page<VirtualInventoryAgeDTO.SearchParamDTO> page,@Param("params") VirtualInventoryAgeDTO.SearchParamDTO params);

    /**
     * 历史库龄
     * @author will
     * @date 2024/12/5 9:45
     * @param page
     * @param params
     * @return IPage<HisInventoryAgeDTO>
     */
    IPage<VirtualInventoryAgeDTO.HisInventoryAgeDTO> hisInventoryAgePaging(Page<VirtualInventoryAgeDTO.HisInventoryAgeParamDTO> page, @Param("params") VirtualInventoryAgeDTO.HisInventoryAgeParamDTO params);
    /**
     * 历史库龄明细
     * @author will
     * @date 2024/12/5 10:18
     * @param page
     * @param params
     * @return IPage<HisInventoryAgeDetailDTO>
     */
    IPage<VirtualInventoryAgeDTO.HisInventoryAgeDetailDTO> hisInventoryAgeDetailPaging(Page page,@Param("params") VirtualInventoryAgeDTO.HisInventoryAgeDetailParamDTO params);
    /**
     * 查询历史库龄图数据
     * @author will
     * @date 2024/12/9 9:51
     * @param params
     * @return List<HisInventoryAgeDTO>
     */
    List<VirtualInventoryAgeDTO.HisInventoryAgeDTO> getHisInventoryAgeChart(@Param("params")VirtualInventoryAgeDTO.HisInventoryAgeParamDTO params);
}
