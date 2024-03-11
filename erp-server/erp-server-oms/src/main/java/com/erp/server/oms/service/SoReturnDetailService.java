package com.erp.server.oms.service;

import com.erp.model.oms.dto.SoDetailDTO;
import com.erp.model.oms.dto.SoReturnDTO;
import com.erp.model.oms.dto.SoReturnDetailDTO;
import com.erp.model.oms.dto.listAddDetailViewDTO;
import com.erp.model.oms.entity.SoReturnDetailEntity;
import com.common.business.service.SuperService;

import java.util.List;

/**
 * <p>
 * 发货通知单主表明细表 服务类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-05-10
 */
public interface SoReturnDetailService extends SuperService<SoReturnDetailEntity> {
    /**
     * 新增
     * @Author Luo_WG
     * @Date 2023/4/13 14:43
     * @param dto dto
     * @param id id:主表id
     * @return java.lang.Boolean
     **/
    Boolean add(SoReturnDTO.Add dto, String id);

    /**
     * 修改
     * @Author Luo_WG
     * @Date 2023/4/13 15:22
     * @param dto dto
     * @return java.lang.Boolean
     **/
    Boolean update(SoReturnDTO.Update dto);

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
    List<SoReturnDetailEntity> listDetailByMainId(String mainId);

    /**
     * 根据主表ids查询详情表信息
     * @Author Luo_WG
     * @Date 2023/4/13 17:44
     * @param mainIds mainIds
     * @return java.lang.Boolean
     **/
    List<SoReturnDetailEntity> listDetailByMainIds(List<String> mainIds);

    /**
     * 根据来源id查询详情信息
     * @Author Luo_WG
     * @Date 2023/5/15 17:18
     * @param sourceIds sourceIds
     * @return java.util.List<com.erp.model.oms.entity.SoReturnDetailEntity>
     **/
    List<SoReturnDetailEntity> listDetailBySourceId(List<String> sourceIds);

    /**
     * 根据id查询详情表信息
     * @Author Luo_WG
     * @Date 2023/5/17 14:52
     * @param ids ids
     * @return java.util.List<com.erp.model.oms.entity.SoReturnDetailEntity>
     **/
    List<SoReturnDetailEntity> listDetailByIds(List<String> ids);

    /**
     * 添加详情按钮-列表查询
     *
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List < com.erp.model.oms.dto.SoDetailDTO.AddDetailView>>
     * @Author Luo_WG
     * @Date 2023/5/16 18:43
     **/
    List<SoDetailDTO.AddDetailView> listAddDetailView(listAddDetailViewDTO dto);

    /**
     * 根据sku获取退货单id
     * @Author Luo_WG
     * @Date 2023/8/15 16:55
     * @param dto
     * @return java.util.List<java.lang.String>
     **/
    List<String> listBySkuNo(SoReturnDTO.PdaSoReturnParam dto);

    List<SoReturnDetailEntity> listDetailByReturnType(List<String> returnType);
}
