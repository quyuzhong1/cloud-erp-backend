package com.erp.server.sys.service;

import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.sys.dto.DictGlobalAreaDTO;
import com.erp.model.sys.entity.DictGlobalAreaEntity;

import java.util.List;

/**
 * <p>
 * 区域表 服务类
 * </p>
 *
 * @author Lambda
 * @since 2023-03-21
 */
public interface DictGlobalAreaService extends SuperService<DictGlobalAreaEntity> {




    /**
     * 修改
     * @param dto
     * @return
     */
    Boolean update(DictGlobalAreaDTO.UpdateDTO dto);

    
    /**
     * 根据国家id获取地区信息
     * @author yl
     * @date 2023-05-15 11:39
     * @param countryIds
     * @return java.util.List<com.erp.model.sys.dto.DictGlobalAreaDTO.InfoDTO>
     */
    List<DictGlobalAreaDTO.InfoDTO> listGlobalAreaByCountryIds(List<String> countryIds);

    /**
     * 根据区域ids获取地区列表
     * @author yl
     * @date 2023-08-22 15:48
     * @param ids
     * @return java.util.List<com.erp.model.sys.entity.DictGlobalAreaEntity>
     */
    List<DictGlobalAreaEntity> listGlobalAreaByIds(List<String> ids);

    /**
     * 获取地区列表
     * @author Jim
     * @date 2023-09-19 09:48
     * @return java.util.List<com.erp.model.sys.entity.DictGlobalAreaEntity>
     */
    List<DictGlobalAreaEntity> listGlobalArea();

    /**
     * 初始化金蝶数据
     * @description
     * @param
     * @return
     * @date 2024-03-19 10:36
     * @author Lambda
     */
    Boolean init();

    /**
     * 分页
     * @description
     * @param
     * @return
     * @date 2024-03-19 14:00
     * @author Lambda
     */
    PagingVO<DictGlobalAreaDTO.PagingViewDTO> paging(PagingDTO<DictGlobalAreaDTO.PagingParamDTO> dto);

    /**
     * 详情
     * @param id
     * @return
     */
    DictGlobalAreaDTO.ViewDTO view(String id);

    /**
     * 删除
     * @description
     * @param id
     * @return
     * @date 2024-03-19 15:55
     * @author Lambda
     */
    BatchResultDTO delete(String id);

    Boolean addGlobalArea(DictGlobalAreaDTO.AddDTO dto);


    Boolean updateSyncKingdeeId(String businessId, String syncKingdeeId, String syncKingdeeCode);

    /**
     * 导出
     *
     * @param
     * @return
     * @description
     * @date 2024-03-20 16:17
     * @author Lambda
     */
    void exportList(DictGlobalAreaDTO.PagingParamDTO dto);

    PagingVO<DictGlobalAreaDTO.PagingViewDTO> exportGlobalArea(PagingDTO<DictGlobalAreaDTO.PagingParamDTO> dto);
}
