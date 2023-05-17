package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.wms.dto.OtherInstockDTO;
import com.erp.model.wms.entity.OtherInstockEntity;
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
public interface OtherInstockMapper extends BaseMapper<OtherInstockEntity> {
    /**
     * @description: 分页查询
     * @author Will
     * @date: 2023/5/17 15:19
     * @param query
     * @param params
     * @return IPage<ListDTO>
     */
    IPage<OtherInstockDTO.ListDTO> paging(Page query, @Param("param") OtherInstockDTO.SearchParamDTO params);
    /**
     * @description: 查询数量
     * @author Will
     * @date: 2023/5/17 15:24
     * @param searchParamDTO
     * @return Integer
     */
    Integer listCount( @Param("param") OtherInstockDTO.SearchParamDTO searchParamDTO);
    /**
     * @description: 导出数据查询
     * @author Will
     * @date: 2023/5/17 16:00
     * @param param 
     * @return List<ListDTO> 
     */
    List<OtherInstockDTO.ListDTO> listExportExcel(@Param("param") OtherInstockDTO.SearchParamDTO param);
}
