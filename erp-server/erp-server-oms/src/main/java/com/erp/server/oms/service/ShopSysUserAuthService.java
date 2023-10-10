package com.erp.server.oms.service;

import com.common.business.service.SuperService;
import com.erp.model.oms.dto.ShopSysUserAuthDTO;
import com.erp.model.oms.entity.ShopSysUserAuthEntity;

import java.util.List;

/**
 * <p>
 * 店铺权限设置表 服务类
 * </p>
 *
 * @author Will
 * @since 2023-09-01
 */
public interface ShopSysUserAuthService extends SuperService<ShopSysUserAuthEntity> {

    /**
     * @description: 批量授权
     * @author Will
     * @date: 2023/9/1 10:59
     * @param dto
     * @return Boolean
     */
    Boolean batchAuth(ShopSysUserAuthDTO.BatchAuthDTO dto);
    /**
     * @description: 查看详情
     * @author Will
     * @date: 2023/9/1 14:48
     * @param dto
     * @return ViewDTO
     */
    ShopSysUserAuthDTO.ViewDTO view(ShopSysUserAuthDTO.ViewParamDTO dto);
    /**
     * @description: 根据用户id集合查询店铺权限设置
     * @author Will
     * @date: 2023/9/4 15:05
     * @param userIdList
     * @return List<ViewDTO>
     */
    List<ShopSysUserAuthDTO.ViewDTO> listShopSysUserAuthByUserIdList(List<String> userIdList);
    /**
     * @description:根据店铺id查询已关联用户id
     * @author Will
     * @date: 2023/9/7 9:36
     * @param shopIdList
     * @return List<String>
     */
    List<String> listUserIdByShopIdList(List<String> shopIdList);
}
