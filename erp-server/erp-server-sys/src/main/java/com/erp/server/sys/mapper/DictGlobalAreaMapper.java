package com.erp.server.sys.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.sys.dto.DictGlobalAreaDTO;
import com.erp.model.sys.dto.KingdeeDepartmentDTO;
import com.erp.model.sys.entity.DictGlobalAreaEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 区域表 Mapper 接口
 * </p>
 *
 * @author Lambda
 * @since 2023-03-21
 */
@Mapper
public interface DictGlobalAreaMapper extends BaseMapper<DictGlobalAreaEntity> {

    /**
     * 根据国家id 集合获取到区域信息
     * @author yl
     * @date 2023-08-10 16:32
     * @param countryIds
     * @return java.util.List<com.erp.model.sys.entity.DictGlobalAreaEntity>
     */
    List<DictGlobalAreaDTO.InfoDTO> listByCountryIds(@Param("countryIds") List<String> countryIds);

    IPage<DictGlobalAreaDTO.PagingViewDTO> paging(Page query, @Param("params")DictGlobalAreaDTO.PagingParamDTO paramDTO);

    /**
     * 导出
     * @param dto
     * @return
     */
    List<DictGlobalAreaDTO.PagingViewDTO> listExport(@Param("params")DictGlobalAreaDTO.PagingParamDTO dto);

    /**
     *
     * @param id
     * @return
     */
    void deleteById(@Param("id") String id);
}
