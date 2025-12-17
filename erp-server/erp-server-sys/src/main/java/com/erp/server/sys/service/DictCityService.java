package com.erp.server.sys.service;

import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.sys.dto.DictCityDTO;
import com.erp.model.sys.entity.DictCityEntity;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Lambda
 * @since 2023-03-21
 */
public interface DictCityService extends SuperService<DictCityEntity> {


    
    /**
     * 获取省城市
     * @author yl
     * @date 2023-05-11 16:30
     * @param countryCode
     * @return java.util.List<com.erp.model.sys.dto.DictCityDTO.ListDTO>
     */
    List<DictCityDTO.ListDTO> listCity(String countryCode);

    /**
     * 获取省城市
     * @param idList
     * @return
     */
    List<DictCityEntity> listByIdList(List<String> idList);


    /**
     * 获取指定区名称
     * @return
     */
    DictCityEntity getReginByName(String reginName,Integer level);


    /**
     * 省份分页
     * @param dto
     * @return
     */
    PagingVO<DictCityDTO.PagingViewDTO> provincePaging(PagingDTO<DictCityDTO.ProvincePagingParamDTO> dto);

    /**
     * 添加省
     * @param dto
     * @return
     */
    Boolean addProvince(DictCityDTO.AddProvinceDTO dto);

    Boolean updateSyncKingdeeId(String businessId, String syncKingdeeId, String syncKingdeeCode);

    /**
     * 修改省信息
     * @param dto
     * @return
     */
    Boolean updateProvince(DictCityDTO.UpdateProvinceDTO dto);

    /**
     * 删除
     * @description
     * @param id
     * @return
     * @date 2024-03-20 12:27
     * @author Lambda
     */
    BatchResultDTO delete(String id);

    /**
     * 省详情
     * @param id
     * @return
     */
    DictCityDTO.ViewDTO provinceView(String id);

    /**
     * 城市分页
     * @param dto
     * @return
     */
    PagingVO<DictCityDTO.PagingViewDTO> cityPaging(PagingDTO<DictCityDTO.CityPagingParamDTO> dto);

    /**
     * 添加城市
     * @param dto
     * @return
     */
    Boolean addCity(DictCityDTO.AddCityDTO dto);

    /**
     * 城市详情
     * @description
     * @param id
     * @return
     * @date 2024-03-20 14:44
     * @author Lambda
     */
    DictCityDTO.ViewDTO cityView(String id);

    /**
     * 修改城市
     * @param dto
     * @return
     */
    Boolean updateCity(DictCityDTO.UpdateCityDTO dto);

    /**
     * 省导出
     *
     * @param dto
     */
    void provinceExport(DictCityDTO.ProvincePagingParamDTO dto);

    /**
     * 城市导出
     *
     * @param dto
     */
    void cityExport(DictCityDTO.ProvincePagingParamDTO dto);

    List<DictCityEntity> listProvince();
    /**
     * 查询所有城市
     * @author will
     * @date 2025/9/2 16:58
     * @return List<DictCityEntity>
     */
    List<DictCityEntity> listCity();

    PagingVO<DictCityDTO.PagingViewDTO> exportCity(PagingDTO<DictCityDTO.ProvincePagingParamDTO> dto);

    PagingVO<DictCityDTO.PagingViewDTO> exportCityProvince(PagingDTO<DictCityDTO.ProvincePagingParamDTO> dto);

    List<DictCityEntity> listCityByNames(List<String> names);
    /**
     * 全部国家级联
     * @author will
     * @date 2025/7/16 14:57
     * @return List<ListDTO>
     */
    List<DictCityDTO.ListDTO> countryTreeList();
}
