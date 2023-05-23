package com.erp.server.wms.service;

import com.common.business.service.SuperService;
import com.erp.model.wms.dto.SoDeliveryNoticeDTO;
import com.erp.model.wms.entity.SoDeliveryNoticeDetailEntity;

import java.util.List;

/**
 * <p>
 * 发货通知单主表明细表 服务类
 * </p>
 *
 * @author LUO_WG
 * @since 2023-05-10
 */
public interface SoDeliveryNoticeDetailService extends SuperService<SoDeliveryNoticeDetailEntity> {
    /**
     * 新增
     * @Author Luo_WG
     * @Date 2023/4/13 14:43
     * @param dto dto
     * @param id id:主表id
     * @return java.lang.Boolean
     **/
    Boolean add(SoDeliveryNoticeDTO.Add dto, String id);

    /**
     * 修改
     * @Author Luo_WG
     * @Date 2023/4/13 15:22
     * @param dto dto
     * @return java.lang.Boolean
     **/
    Boolean update(SoDeliveryNoticeDTO.Update dto);

    /**
     * 根据主表id删除
     * @Author Luo_WG
     * @Date 2023/4/6 19:29
     * @param mainIds mainIds
     * @return java.lang.Boolean
     **/
    Boolean delete(List<String> mainIds);

    /**
     * 根据主表id查询详情表信息
     * @Author Luo_WG
     * @Date 2023/4/13 17:44
     * @param mainId mainId
     * @return java.lang.Boolean
     **/
    List<SoDeliveryNoticeDetailEntity> listDetailByMainId(String mainId);

    /**
     * 根据主表id查询详情表信息
     * @Author Luo_WG
     * @Date 2023/4/13 17:44
     * @param mainIds mainIds
     * @return java.lang.Boolean
     **/
    List<SoDeliveryNoticeDetailEntity> listDetailByMainIds(List<String> mainIds);

    /**
     * 根据来源id查询详情信息
     * @Author Luo_WG
     * @Date 2023/5/15 16:55
     * @param sourceIds sourceIds
     * @return java.util.List<com.erp.model.wms.entity.SoDeliveryNoticeDetailEntity>
     **/
    List<SoDeliveryNoticeDetailEntity> listDetailBySourceIds(List<String> sourceIds);

    /**
     * 根据来源明细id查询详情信息
     * @Author Luo_WG
     * @Date 2023/5/15 16:55
     * @param sourceIds sourceIds
     * @return java.util.List<com.erp.model.wms.entity.SoDeliveryNoticeDetailEntity>
     **/
    List<SoDeliveryNoticeDetailEntity> listDetailBySourceDetailIds(List<String> sourceIds);


    /**
     * 处理数据 更改销售订单的发货状态
     * @author yl
     * @date 2023-05-23 10:02
     * @param sourceDetailIdList
     * @return void
     */
    void handleData(List<String> sourceDetailIdList);
}
