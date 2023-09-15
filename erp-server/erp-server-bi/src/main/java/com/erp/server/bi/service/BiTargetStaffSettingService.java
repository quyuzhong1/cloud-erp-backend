package com.erp.server.bi.service;

import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.bi.dto.BiTargetStaffSettingDTO;
import com.erp.model.bi.dto.BiTargetYearDTO;
import com.erp.model.bi.dto.TargetFinishDTO;
import com.erp.model.bi.entity.BiTargetStaffSettingEntity;

import java.util.List;

/**
 * <p>
 * 人员目标设置表 服务类
 * </p>
 *
 * @author Lambda
 * @since 2023-09-13
 */
public interface BiTargetStaffSettingService extends SuperService<BiTargetStaffSettingEntity> {

    /**
    * 新增
    * @author Lambda
    * @date: 2023-09-13
    * @param dto
    * @return
    */
    String add(BiTargetStaffSettingDTO.AddDTO dto);

    /**
    * 修改
    * @author Lambda
    * @date: 2023-09-13
    * @param dto
    * @return
    */
    Boolean update(BiTargetStaffSettingDTO.UpdateDTO dto);


    /**
     * 获取到详情信息
     * @author yl
     * @date 2023-09-13 15:17
     * @param id
     * @return com.erp.model.bi.dto.BiTargetStaffSettingDTO.ViewDTO
     */
    BiTargetStaffSettingDTO.ViewDTO view(String id);

    
    /**
     * 分页展示数据
     * @author yl
     * @date 2023-09-14 14:11
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.bi.dto.BiTargetStaffSettingDTO.PagingViewDTO>
     */
    PagingVO<BiTargetStaffSettingDTO.PagingViewDTO> paging(PagingDTO<BiTargetYearDTO.PagingParamDTO> dto);
    /**
     * @description: 根据指标查询人员目标值
     * @author Will
     * @date: 2023/9/15 11:06
     * @param dto
     * @return List<BiTargetStaffSettingEntity>
     */
    List<BiTargetStaffSettingEntity> listUserTargetFinish(TargetFinishDTO.ParamDTO dto);
    /**
     * @description: 根据指标查询部门目标值
     * @author Will
     * @date: 2023/9/15 12:06
     * @param dto
     * @return List<BiTargetStaffSettingEntity>
     */
    List<BiTargetStaffSettingEntity> listDeptTargetFinish(TargetFinishDTO.ParamDTO dto);
}
