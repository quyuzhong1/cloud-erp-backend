package com.erp.server.oms.service.impl;

import com.common.business.service.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.erp.model.dmp.entity.DmpShopInfoEntity;
import com.erp.model.oms.dto.ShopDTO;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.server.oms.mapper.ShopInfoMapper;
import com.erp.server.oms.service.ShopInfoService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * <p>
 * 店铺表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-06-28
 */
@Service
public class ShopInfoServiceImpl extends SuperServiceImpl<ShopInfoMapper, ShopInfoEntity> implements ShopInfoService {


    @Resource
    private DmpTaskFeign dmpTaskFeign;

    /**
     * 添加店铺
     *
     * @param dto
     * @return java.lang.String
     * @author yl
     * @date 2023-06-29 10:39
     */
    @Override
    public String add(ShopDTO.AddDTO dto) {
        ShopInfoEntity shop = new ShopInfoEntity();
        BeanMapper.copy(dto, shop);
        Boolean result = this.save(shop);
        if (result) {
            return shop.getId();
        }
        return "";

    }

    /**
     * 修改店铺
     *
     * @param dto
     * @return java.lang.String
     * @author yl
     * @date 2023-07-03 9:05
     */
    @Override
    public String updateShop(ShopDTO.UpdateDTO dto) {
        ShopInfoEntity shopInfo = this.getById(dto.getId());
        if (Objects.isNull(shopInfo)) {
            throw new ServiceException(ApiError.ERROR_92058);
        }
        shopInfo.setCustomerCode(dto.getCustomerCode());
        shopInfo.setShopCode(dto.getShopCode());
        shopInfo.setPlatformDict(dto.getPlatformDict());
        shopInfo.setName(dto.getName());
        Boolean result = this.updateById(shopInfo);
        if (result) {
            return shopInfo.getId();
        }
        return "";
    }


    /**
     * 初始同步店铺信息
     *
     * @param
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-07-06 12:23
     */
    @Override
    public Boolean initialSync() {
        //获取到dmp 店铺
        List<DmpShopInfoEntity> dmpShopList = dmpTaskFeign.listShop();

        List<ShopInfoEntity> shopInfoList = this.list();
        for(ShopInfoEntity item:shopInfoList){
            String shopCode=item.getShopCode();

        }
        return null;
    }

}
