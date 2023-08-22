package com.erp.server.oms.service;

import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.UpdateStateDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.ShopDTO;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.common.business.service.SuperService;

/**
 * <p>
 * 店铺表 服务类
 * </p>
 *
 * @author Lambda
 * @since 2023-06-28
 */
public interface ShopInfoService extends SuperService<ShopInfoEntity> {

    
    /**
     * 添加店铺
     * @author yl
     * @date 2023-06-29 10:39
     * @param dto
     * @return java.lang.String
     */
    Boolean add(ShopDTO.AddDTO dto);

    /**
     * 修改店铺
     * @author yl
     * @date 2023-07-03 9:05
     * @param dto
     * @return java.lang.String
     */
    String updateShop(ShopDTO.UpdateDTO dto);

    


    /**
     * 店铺分页
     * @param dto
     * @return
     */
    PagingVO<ShopDTO.PagingViewDTO> paging(PagingDTO<ShopDTO.PagingParamDTO> dto);

    /**
     * 批量启用或者禁用店铺
     * @param shop 店铺信息
     * @param disabled 禁用状态
     * @return
     */
    BatchResultDTO updateStatus(ShopInfoEntity shop, Boolean disabled);

    /**
     * 获取详情
     * @author yl
     * @date 2023-08-22 16:13
     * @param id
     * @return com.erp.model.oms.dto.ShopDTO.ViewDTO
     */
    ShopDTO.ViewDTO view(String id);
}
