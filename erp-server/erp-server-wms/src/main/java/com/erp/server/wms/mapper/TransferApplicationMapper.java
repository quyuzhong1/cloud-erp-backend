package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.wms.dto.TransferApplicationDTO;
import com.erp.model.wms.entity.TransferApplicationEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Mapper
public interface TransferApplicationMapper extends BaseMapper<TransferApplicationEntity> {
    /**
     * @description: 分页查询
     * @author Will
     * @date: 2023/5/10 19:31
     * @param query
     * @param params
     * @return IPage<ListDTO>
     */
    IPage<TransferApplicationDTO.ListDTO> paging(Page query, @Param("params") TransferApplicationDTO.SearchParamDTO params);
    /**
     * @description: 查询列表数量
     * @author Will
     * @date: 2023/5/10 19:44
     * @param params
     * @return Integer
     */
    Integer listCount(@Param("params") TransferApplicationDTO.SearchParamDTO params);
}
