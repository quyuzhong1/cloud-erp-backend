package com.erp.server.sys.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.sys.dto.DictCityDTO;
import com.erp.model.sys.entity.DictCityEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author Lambda
 * @since 2023-03-21
 */
@Mapper
public interface DictCityMapper extends BaseMapper<DictCityEntity> {

    List<DictCityEntity> listByIdList(@Param("idList") List<String> idList);

    /**
     * 省份分页
     * @param query
     * @param paramDTO
     * @return
     */
    IPage<DictCityDTO.PagingViewDTO> provincePaging(Page query, @Param("params") DictCityDTO.ProvincePagingParamDTO paramDTO);

    /**
     * 城市分页
     * @param query
     * @param paramDTO
     * @return
     */
    IPage<DictCityDTO.PagingViewDTO> cityPaging(Page query,@Param("params") DictCityDTO.CityPagingParamDTO paramDTO);

    /**
     * 省份导出
     * @param dto
     * @return
     */
    List<DictCityDTO.PagingViewDTO> provinceExport(@Param("params")DictCityDTO.ProvincePagingParamDTO dto);
    Page<DictCityDTO.PagingViewDTO> provinceExport(@Param("page") Page<DictCityDTO.PagingViewDTO> page, @Param("params")DictCityDTO.ProvincePagingParamDTO dto);

    List<DictCityDTO.PagingViewDTO> cityExport(@Param("params")DictCityDTO.ProvincePagingParamDTO dto);
    /**
     *
     * @param id
     * @return
     */
    void deleteById(@Param("id") String id);
    Page<DictCityDTO.PagingViewDTO> cityExport(@Param("page") Page<DictCityDTO.PagingViewDTO> page, @Param("params")DictCityDTO.ProvincePagingParamDTO dto);

}
