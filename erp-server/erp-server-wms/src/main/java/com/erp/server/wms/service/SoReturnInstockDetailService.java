package com.erp.server.wms.service;

import com.erp.model.wms.dto.SoReturnInstockDTO;
import com.erp.model.wms.dto.SoReturnInstockDetailDTO;
import com.erp.model.wms.dto.SoReturnNoticeDTO;
import com.erp.model.wms.entity.SoReturnInstockDetailEntity;
import com.common.business.service.SuperService;
import com.erp.model.wms.entity.SoReturnInstockEntity;

import java.util.List;

/**
 * <p>
 * 销售退货入库单明细表 服务类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-05-10
 */
public interface SoReturnInstockDetailService extends SuperService<SoReturnInstockDetailEntity> {
    /**
     * 新增
     * @Author Luo_WG
     * @Date 2023/4/13 14:43
     * @param dto dto
     * @param id id:主表id
     * @return java.lang.Boolean
     **/
    Boolean add(SoReturnInstockDTO.Add dto, String id);

    /**
     * 修改
     * @Author Luo_WG
     * @Date 2023/4/13 15:22
     * @param dto dto
     * @return java.lang.Boolean
     **/
    Boolean update(SoReturnInstockDTO.Update dto);

    /**
     * 根据主表id删除
     * @Author Luo_WG
     * @Date 2023/4/6 19:29
     * @param mainIds mainIds
     * @return java.lang.Boolean
     **/
    Boolean delete(List<String> mainIds);

    /**
     * 根据来源id查询详情信息
     * @Author Luo_WG
     * @Date 2023/5/15 16:55
     * @param sourceIds sourceIds
     * @return java.util.List<com.erp.model.wms.entity.SoReturnInstockDetailEntity>
     **/
    List<SoReturnInstockDetailEntity> listDetailBySourceIds(List<String> sourceIds);

    /**
     * 根据主键id查询详情表信息
     * @Author Luo_WG
     * @Date 2023/5/15 18:56
     * @param id id
     * @return java.util.List<com.erp.model.wms.entity.SoReturnInstockDetailEntity>
     **/
    List<SoReturnInstockDetailEntity> listDetailByMainId(String id);

    /**
     * 根据主键id查询详情表信息
     * @Author Luo_WG
     * @Date 2023/5/15 18:56
     * @param ids ids
     * @return java.util.List<com.erp.model.wms.entity.SoReturnInstockDetailEntity>
     **/
    List<SoReturnInstockDetailEntity> listDetailByMainIds(List<String> ids);

    /**
     * 根据来源详情id查询详情信息
     * @Author Luo_WG
     * @Date 2023/5/22 15:31
     * @param sourceDetailIds sourceDetailIds
     * @return java.util.List<com.erp.model.wms.entity.SoReturnInstockDetailEntity>
     **/
    List<SoReturnInstockDetailEntity> listDetailBySourceDetailIds(List<String> sourceDetailIds);

    List<SoReturnInstockDetailEntity> listDetailBySoReturnDetailIds(List<String> soReturnDetailIds);

    List<SoReturnInstockDetailEntity> getSoReturnInstockByReturnIds(List<String> returnIds);

    void clearSoReturnAndUpdate(SoReturnInstockDetailDTO.ClearSoReturnAndUpdateDTO dto);
}
