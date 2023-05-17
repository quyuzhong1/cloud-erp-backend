package com.erp.server.wms.service;

import com.erp.model.wms.dto.SoReturnNoticeDTO;
import com.erp.model.wms.dto.SoReturnReceiveDTO;
import com.erp.model.wms.entity.SoReturnNoticeDetailEntity;
import com.erp.model.wms.entity.SoReturnReceiveDetailEntity;
import com.common.business.service.SuperService;

import java.util.List;

/**
 * <p>
 * 销售退货签收单明细表 服务类
 * </p>
 *
 * @author LUO_WG
 * @since 2023-05-10
 */
public interface SoReturnReceiveDetailService extends SuperService<SoReturnReceiveDetailEntity> {
    /**
     * 新增
     * @Author Luo_WG
     * @Date 2023/4/13 14:43
     * @param dto dto
     * @param id id:主表id
     * @return java.lang.Boolean
     **/
    Boolean add(SoReturnReceiveDTO.Add dto, String id);

    /**
     * 修改
     * @Author Luo_WG
     * @Date 2023/4/13 15:22
     * @param dto dto
     * @return java.lang.Boolean
     **/
    Boolean update(SoReturnReceiveDTO.Update dto);

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
     * @return java.util.List<com.erp.model.wms.entity.SoReturnNoticeDetailEntity>
     **/
    List<SoReturnReceiveDetailEntity> listDetailBySourceIds(List<String> sourceIds);

    /**
     * 根据主键id查询详情表信息
     * @Author Luo_WG
     * @Date 2023/5/15 18:56
     * @param id id
     * @return java.util.List<com.erp.model.oms.entity.SoReturnDetailEntity>
     **/
    List<SoReturnReceiveDetailEntity> listDetailByMainId(String id);
}
