package com.erp.server.oms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.constant.SqlConstants;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.erp.model.oms.dto.ShopDTO;
import com.erp.model.oms.entity.ShopCostEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.server.oms.mapper.ShopCostMapper;
import com.erp.server.oms.service.ShopCostService;
import com.erp.server.oms.service.ShopInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * <p>
 * 店铺费用表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-08-22
 */
@Service
@Slf4j
public class ShopCostServiceImpl extends SuperServiceImpl<ShopCostMapper, ShopCostEntity> implements ShopCostService {


    @Resource
    private ShopInfoService shopInfoService;

    /**
     * 单个费率设置
     *
     * @param dto
     * @param dto
     * @return com.common.business.dto.base.BatchResultDTO
     * @author yl
     * @date 2023-08-23 15:31
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean setCost(ShopDTO.SetCostDTO dto) {
        ShopCostEntity shopCost = new ShopCostEntity();
        if (StringUtils.isNotBlank(dto.getId())) {
            shopCost = this.getById(dto.getId());
            if (Objects.isNull(shopCost)) {
                throw new ServiceException("店铺费率不存在");
            }
        }
        BeanMapper.copy(dto, shopCost);
        Boolean updateResult = this.saveOrUpdate(shopCost);
        return updateResult;
    }


    /**
     * 批量设置费率
     *
     * @param shop
     * @param dto
     * @return com.common.business.dto.base.BatchResultDTO
     * @author yl
     * @date 2023-08-24 17:38
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO batchSetCost(ShopInfoEntity shop, ShopDTO.BatchSetCostDTO dto) {
        ShopCostEntity shopCost = new ShopCostEntity();
        BeanMapper.copy(dto, shopCost);
        shopCost.setShopId(shop.getId());
        //先删除
        this.removeByShopId(shop.getId());
        Boolean saveResult = this.save(shopCost);
        if (saveResult) {
            return BatchResultDTO.success(shop.getId(),shop.getName(), "设置费率成功");
        }
        return BatchResultDTO.fail(shop.getId(),shop.getName(), "设置费率失败");

    }


    @Override
    public ShopDTO.ViewCostDTO viewCost(String shopId) {
        ShopInfoEntity shop = shopInfoService.getById(shopId);
        if (Objects.isNull(shop)) {
            throw new ServiceException("店铺不存在");
        }
        ShopDTO.ViewCostDTO viewCost = new ShopDTO.ViewCostDTO();
        ShopCostEntity shopCost = this.getByShopId(shopId);
        if (Objects.nonNull(shopCost)) {
            BeanMapper.copy(shopCost, viewCost);
        } else {
            BigDecimal zero = BigDecimal.ZERO;
            viewCost.setPlatformRate(zero);
            viewCost.setVatRate(zero);
            viewCost.setTransferRate(zero);
        }
        viewCost.setDictPlatform(shop.getDictPlatform());
        viewCost.setShopId(shopId);
        viewCost.setShopName(shop.getName());
        return viewCost;
    }

    @Override
    public ShopCostEntity getByShopId(String shopId) {
        return this.lambdaQuery().eq(ShopCostEntity::getShopId, shopId).last( SqlConstants.LIMIT_1).one();
    }

    /**
     * 根据店铺id删除
     *
     * @param shopId
     * @return void
     * @author yl
     * @date 2023-08-24 17:53
     */
    public void removeByShopId(String shopId) {
        LambdaQueryWrapper<ShopCostEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShopCostEntity::getShopId, shopId);
        this.remove(queryWrapper);
    }
}
