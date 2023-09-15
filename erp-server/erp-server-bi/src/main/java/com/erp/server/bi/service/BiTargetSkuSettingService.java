package com.erp.server.bi.service;

import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.bi.dto.BiTargetSkuSettingDTO;
import com.erp.model.bi.dto.BiTargetYearDTO;
import com.erp.model.bi.dto.TargetFinishDTO;
import com.erp.model.bi.entity.BiTargetSkuSettingEntity;

import java.util.List;

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
    /**
     * @description: 根据指标查询SKU目标值
     * @author Will
     * @date: 2023/9/15 11:42
     * @param dto
     * @return List<BiTargetSkuSettingEntity>
     */
    List<BiTargetSkuSettingEntity> listTargetFinish(TargetFinishDTO.ParamDTO dto);
}
