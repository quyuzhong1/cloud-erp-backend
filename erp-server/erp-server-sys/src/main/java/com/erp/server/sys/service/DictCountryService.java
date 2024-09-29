package com.erp.server.sys.service;

import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.sys.dto.DictCountryDTO;
import com.erp.model.sys.entity.DictCountryEntity;

import java.util.List;

/**
 * <p>
 * 国家字典表 服务类
 * </p>
 *
 * @author Lambda
 * @since 2023-03-21
 */
public interface DictCountryService extends SuperService<DictCountryEntity> {

    
    /**
     * 获取国家列表
     * @author yl
     * @date 2023-05-11 16:18
     * @param
     * @return java.util.List<com.erp.model.sys.dto.DictCountryDTO.ListDTO>
     */
    List<DictCountryDTO.ListDTO> listCountry();


    /**
     * 添加
     * @param dto
     * @return
     */
    Boolean add(DictCountryDTO.AddDTO dto);


    Boolean update(DictCountryDTO.UpdateDTO dto);
    /**
     * @description: 根据参数查询国家
     * @author Will
     * @date: 2023/11/9 9:53
     * @return List<ListDTO>
     */
    List<DictCountryDTO.ListDTO> listCountryByParam(DictCountryDTO.ListParamDTO dto);

    /**
     * 初始化国家列表
     */
    void initRegionList(String country);

    /**
     * 根据国家ids 获取信息
     * @author yl
     * @date 2023-08-21 15:31
     * @param ids
     * @return java.util.List<com.erp.model.sys.entity.DictCountryEntity>
     */
    List<DictCountryEntity> listCountryByIds(List<String> ids);

    /**
     * 查询区域国家列表
     * @author yl
     * @date 2023-08-31 10:45
     * @param type
     * @return java.util.List<com.erp.model.sys.dto.DictCountryDTO.CascadeDTO>
     */
    List<DictCountryDTO.CascadeDTO> areaCountryListByType(String type);
    /**
     * @description: 查询国家区域
     * @author Will
     * @date: 2023/11/9 9:37
     * @param dto
     * @return List<ListRegionDTO>
     */
    List<DictCountryDTO.ListRegionDTO> listAreaCountry(DictCountryDTO.ListParamDTO dto);

    /**
     * 根据国家名获取国家
     * @param names
     * @return
     */
    List<DictCountryEntity> listCountryByNames(List<String> names);


    /**
     * 根据区域查
     * @description
     * @param
     * @return
     * @date 2024-03-19 15:59
     * @author Lambda
     */
    List<DictCountryEntity> listByRegionCode(String globalArea);

    /**
     * 初始化金蝶数据
     * @return
     */
    Boolean init();

    /**
     * 分页
     * @param dto
     * @return
     */
    PagingVO<DictCountryDTO.PagingViewDTO> paging(PagingDTO<DictCountryDTO.PagingParamDTO> dto);

    /**
     * 详情
     * @param id
     * @return
     */
    DictCountryDTO.ViewDTO view(String id);

    Boolean updateSyncKingdeeId(String businessId, String syncKingdeeId, String syncKingdeeCode);

    /**
     * 删除
     * @param id
     * @return
     */
    BatchResultDTO delete(String id);
    PagingVO<DictCountryDTO.ListDTO> pagingSelect(PagingDTO<DictCountryDTO.SelectDTO> dto);

    void exportList(DictCountryDTO.PagingParamDTO dto);

    PagingVO<DictCountryDTO.PagingViewDTO> exportCountry(PagingDTO<DictCountryDTO.PagingParamDTO> dto);

    /**
     * 根据国家id或三字码 集合 获取到国家列表
     */
    List<DictCountryEntity> listCountryByIdsOrAlpha3(List<String> codeList);
}
