package com.erp.server.oms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.oms.dto.CustomerDTO;
import com.erp.model.oms.entity.CustomerInfoEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Mapper
public interface CustomerInfoMapper extends BaseMapper<CustomerInfoEntity> {

    
    /**
     * 分页获取数据
     * @author yl
     * @date 2023-05-12 17:25
     * @param query
     * @param params
     * @return com.baomidou.mybatisplus.core.metadata.IPage
     */
    IPage<CustomerDTO.PagingViewDTO> paging(Page query, @Param("params")CustomerDTO.PagingParamDTO params,@Param("approveList") List<String> approveList);

    
    /**
     * 获取到导出的数据
     * @author yl
     * @date 2023-05-15 14:56
     * @param dto
     * @return java.util.List<com.erp.model.oms.dto.CustomerDTO.PagingViewDTO>
     */
    List<CustomerDTO.PagingViewDTO> listExport(@Param("params") CustomerDTO.ExportDTO dto,@Param("approveList") List<String> approveList);
}
