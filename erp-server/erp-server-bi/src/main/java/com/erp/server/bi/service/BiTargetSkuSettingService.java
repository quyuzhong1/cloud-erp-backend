package com.erp.server.bi.service;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.bi.dto.BiTargetYearDTO;
import com.erp.model.bi.entity.BiTargetSkuSettingEntity;
import com.common.business.service.SuperService;
import com.erp.model.bi.dto.BiTargetSkuSettingDTO;

/**
 * <p>
 * sku 目标设置表 服务类
 * </p>
 *
 * @author Lambda
 * @since 2023-09-13
 */
public interface BiTargetSkuSettingService extends SuperService<BiTargetSkuSettingEntity> {

    /**
    * 新增
    * @author Lambda
    * @date: 2023-09-13
    * @param dto
    * @return
    */
    String add(BiTargetSkuSettingDTO.AddDTO dto);

    /**
    * 修改
    * @author Lambda
    * @date: 2023-09-13
    * @param dto
    * @return
    */
    Boolean update(BiTargetSkuSettingDTO.UpdateDTO dto);


    /**
     * 详情信息
     * @param id
     * @return
     */
    BiTargetSkuSettingDTO.ViewDTO view(String id);

    /**
     * 分页查询
     * @author yl
     * @date 2023-09-14 18:12
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.bi.dto.BiTargetSkuSettingDTO.PagingViewDTO>
     */
    PagingVO<BiTargetSkuSettingDTO.PagingViewDTO> paging(PagingDTO<BiTargetYearDTO.PagingParamDTO> dto);
}
