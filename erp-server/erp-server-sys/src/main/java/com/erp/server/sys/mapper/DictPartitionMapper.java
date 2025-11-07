package com.erp.server.sys.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.AdvanceQueryContainer;
import com.erp.model.sys.dto.DictPartitionDTO;
import com.erp.model.sys.entity.DictPartitionEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 分区表 Mapper 接口
 * </p>
 *
 * @author lrp
 * @since 2025-01-03
 */
@Mapper
public interface DictPartitionMapper extends BaseMapper<DictPartitionEntity> {
    /**
     * 分页搜索
     * @param query
     * @param params
     * @return
     */
    IPage<DictPartitionDTO.DictDTO> pagingSelect(@Param("query") Page<DictPartitionDTO.ViewDTO> query, @Param("params") DictPartitionDTO.SelectDTO params);

    /**
     * 下拉框
     * @param params
     * @return
     */
    List<DictPartitionDTO.DictDTO> dropDown(@Param("params") DictPartitionDTO.SelectDTO params);

    /**
     * 高级查询军区信息
     * @param params
     * @return
     */
    List<DictPartitionEntity> listByAdvanceQuery(@Param("params") AdvanceQueryContainer params);
}
