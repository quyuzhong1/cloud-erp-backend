package com.erp.server.plm.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.common.business.interceptor.CommonInterceptor;
import com.erp.common.business.vo.LoginUser;
import com.erp.model.plm.entity.UserAddProductEntity;
import com.erp.server.plm.mapper.UserAddProductEntityMapper;
import com.erp.server.plm.service.UserAddProductService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 *
 */
@Service
public class UserAddProductServiceImpl extends ServiceImpl<UserAddProductEntityMapper, UserAddProductEntity>
        implements UserAddProductService {


    /**
     * 收藏产品
     *
     * @param productId
     * @return boolean
     * @author yl
     * @date 2022-10-09 19:05
     */
    @Override
    public boolean userAddProduct(String productId) {
        LoginUser loginUser = CommonInterceptor.threadLocal.get();
        if (loginUser != null) {
            UserAddProductEntity entity = new UserAddProductEntity();
            entity.setProductId(productId);
            entity.setUserId(loginUser.getUid());
            return this.save(entity);
        }
        return false;
    }

    /**
     * 取消收藏产品
     *
     * @param productId
     * @return boolean
     * @author yl
     * @date 2022-10-09 19:09
     */
    @Override
    public boolean userCancelProduct(String productId) {
        LoginUser loginUser = CommonInterceptor.threadLocal.get();
        if (loginUser != null) {
            LambdaQueryWrapper<UserAddProductEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(UserAddProductEntity::getProductId, productId);
            queryWrapper.eq(UserAddProductEntity::getUserId, loginUser.getUid());
            return this.remove(queryWrapper);
        }
        return false;
    }

    @Override
    public List<Map<String, Object>> listByUserId() {
        LoginUser loginUser = CommonInterceptor.threadLocal.get();

        return baseMapper.listByUserId(loginUser.getUid());
    }

    /**
     * 根据用户id 获取到产品id
     * @author yl
     * @date 2022-10-10 17:43
     * @param userId
     * @return java.util.List<java.lang.String>
     */
    @Override
    public List<String> getMyCollectProductIds(String userId) {
        LambdaQueryWrapper<UserAddProductEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(UserAddProductEntity::getProductId);
        queryWrapper.eq(UserAddProductEntity::getUserId,userId);
        return this.listObjs(queryWrapper,Object::toString);
    }
}




