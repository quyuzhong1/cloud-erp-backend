package com.erp.server.dmp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.model.dmp.entity.BiSkuInfoEntity;
import com.erp.server.dmp.pull.mapper.BiSkuInfoMapper;
import com.erp.server.dmp.service.BiSkuInfoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 */
@Service
public class BiSkuInfoServiceImpl extends ServiceImpl<BiSkuInfoMapper, BiSkuInfoEntity>
    implements BiSkuInfoService {

    /**
     * 添加订单信息
     * @Author Luo_WG
     * @Date 2022/11/14 21:10
     * @param biSkuInfoEntity 订单信息
     * @return java.lang.Boolean
     **/
    @Override
    public Boolean add(BiSkuInfoEntity biSkuInfoEntity){
        return this.save(biSkuInfoEntity);
    }

    /**
     * 根据sku查询商品信息
     *
     * @param skuNo     商品sku
     * @param companyId
     * @return com.erp.model.dmp.entity.DmpOrderInfoEntity
     * @Author Luo_WG
     * @Date 2022/11/14 21:28
     **/
    @Override
    public BiSkuInfoEntity getSkuBySkuNo(String skuNo, String companyId){
        LambdaQueryWrapper<BiSkuInfoEntity> lambdaQueryWrapper = new LambdaQueryWrapper();
        lambdaQueryWrapper.eq(BiSkuInfoEntity::getSkuNo, skuNo);
        lambdaQueryWrapper.eq(BiSkuInfoEntity::getCompanyId, companyId);
        return this.getOne(lambdaQueryWrapper);
    }

    /**
     * 根据平台订单id修改订单信息
     * @Author Luo_WG
     * @Date 2022/11/14 21:39
     * @param biSkuInfoEntity 商品信息
     * @return java.lang.Boolean
     **/
    @Override
    public Boolean updateSkuBySkuNo(BiSkuInfoEntity biSkuInfoEntity){
        LambdaQueryWrapper<BiSkuInfoEntity> lambdaQueryWrapper = new LambdaQueryWrapper();
        lambdaQueryWrapper.eq(BiSkuInfoEntity::getSkuNo, biSkuInfoEntity.getSkuNo());
        return this.update(biSkuInfoEntity, lambdaQueryWrapper);
    }

    /**
     * 校验商品在中台是否存在，存在就修改不存在则新增
     * @Author Luo_WG
     * @Date 2022/11/14 21:25
     * @return void
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkOrder(BiSkuInfoEntity biSkuInfoEntity) {
        BiSkuInfoEntity dmpOrderInfoEntity = this.getSkuBySkuNo(biSkuInfoEntity.getSkuNo(), biSkuInfoEntity.getCompanyId());
        if (dmpOrderInfoEntity != null) {
            //如果数据有变动需要更新数据库订单信息
            biSkuInfoEntity.setId(dmpOrderInfoEntity.getId());
            if (!dmpOrderInfoEntity.toString().equals(biSkuInfoEntity.toString())) {
                this.updateById(biSkuInfoEntity);
            }
        } else {
            this.add(biSkuInfoEntity);
        }
    }
}




