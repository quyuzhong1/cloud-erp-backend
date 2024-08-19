package com.erp.server.sys.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.sys.dto.DictCountryDTO;
import com.erp.model.sys.entity.DictCountryEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 国家字典表 Mapper 接口
 * </p>
 *
 * @author Lambda
 * @since 2023-03-21
 */
@Mapper
public interface DictCountryMapper extends BaseMapper<DictCountryEntity> {

    
    /**
     * 获取国家列表
     * @author yl
     * @date 2023-05-16 19:11
     * @param
     * @return java.util.List<com.erp.model.sys.dto.DictCountryDTO.ListDTO>
     */
    List<DictCountryDTO.ListDTO> listCountry();
    /**
     * @description: 根据参数查询国家
     * @author Will
     * @date: 2023/11/9 9:54
     * @param params
     * @return List<ListDTO>
     */
    List<DictCountryDTO.ListDTO> listCountryByParam(@Param("params") DictCountryDTO.ListParamDTO params);

    /**
     * 分页查询
     * @description
     * @param paramDTO
     * @return
     * @date 2024-03-19 16:57
     * @author Lambda
     */
    IPage<DictCountryDTO.PagingViewDTO> paging(Page query, @Param("params")DictCountryDTO.PagingParamDTO paramDTO);

    List<DictCountryDTO.PagingViewDTO> listExport(@Param("params")DictCountryDTO.PagingParamDTO dto);

    IPage<DictCountryDTO.ListDTO> pagingSelect(Page query, @Param("params") DictCountryDTO.SelectDTO params);
    /**
     *
     * @param id
     * @return
     */
    void deleteById(@Param("id") String id);
}
