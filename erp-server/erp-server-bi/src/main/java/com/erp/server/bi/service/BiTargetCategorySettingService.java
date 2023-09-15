package com.erp.server.bi.service;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.bi.dto.BiTargetYearDTO;
import com.erp.model.bi.entity.BiTargetCategorySettingEntity;
import com.common.business.service.SuperService;
import com.erp.model.bi.dto.BiTargetCategorySettingDTO;

/**
 * <p>
 * 分类 目标设置表 服务类
 * </p>
 *
 * @author Lambda
 * @since 2023-09-13
 */
public interface BiTargetCategorySettingService extends SuperService<BiTargetCategorySettingEntity> {

    /**
    * 新增
    * @author Lambda
    * @date: 2023-09-13
    * @param dto
    * @return
    */
    String add(BiTargetCategorySettingDTO.AddDTO dto);

    /**
    * 修改
    * @author Lambda
    * @date: 2023-09-13
    * @param dto
    * @return
    */
    Boolean update(BiTargetCategorySettingDTO.UpdateDTO dto);


    /**
     * 详情
     * @param id
     * @return
     */
    BiTargetCategorySettingDTO.ViewDTO view(String id);

    /**
     * 分页
     * @param dto
     * @return
     */
    PagingVO<BiTargetCategorySettingDTO.PagingViewDTO> paging(PagingDTO<BiTargetYearDTO.PagingParamDTO> dto);
}
