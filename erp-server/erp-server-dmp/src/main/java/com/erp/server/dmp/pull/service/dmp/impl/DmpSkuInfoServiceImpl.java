package com.erp.server.dmp.pull.service.dmp.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.model.dmp.entity.DmpSkuInfoEntity;
import com.erp.server.dmp.pull.mapper.DmpSkuInfoMapper;
import com.erp.server.dmp.pull.service.dmp.DmpSkuInfoService;
import org.springframework.stereotype.Service;

/**
 *
 */
@Service
public class DmpSkuInfoServiceImpl extends ServiceImpl<DmpSkuInfoMapper, DmpSkuInfoEntity>
    implements DmpSkuInfoService {

    /**
     * 添加订单信息
     * @Author Luo_WG
     * @Date 2022/11/14 21:10
     * @param dmpSkuInfoEntity 订单信息
     * @return java.lang.Boolean
     **/
    @Override
    public Boolean add(DmpSkuInfoEntity dmpSkuInfoEntity){
        return this.save(dmpSkuInfoEntity);
    }

    /**
     * 根据sku查询商品信息
     * @Author Luo_WG
     * @Date 2022/11/14 21:28
     * @param skuNo 商品sku
     * @return com.erp.model.dmp.entity.DmpOrderInfoEntity
     **/
    @Override
    public DmpSkuInfoEntity getSkuBySkuNo(String skuNo){
        LambdaQueryWrapper<DmpSkuInfoEntity> lambdaQueryWrapper = new LambdaQueryWrapper();
        lambdaQueryWrapper.eq(DmpSkuInfoEntity::getSkuNo, skuNo);
        return this.getOne(lambdaQueryWrapper);
    }

    /**
     * 根据平台订单id修改订单信息
     * @Author Luo_WG
     * @Date 2022/11/14 21:39
     * @param dmpSkuInfoEntity 商品信息
     * @return java.lang.Boolean
     **/
    @Override
    public Boolean updateSkuBySkuNo(DmpSkuInfoEntity dmpSkuInfoEntity){
        LambdaQueryWrapper<DmpSkuInfoEntity> lambdaQueryWrapper = new LambdaQueryWrapper();
        lambdaQueryWrapper.eq(DmpSkuInfoEntity::getSkuNo, dmpSkuInfoEntity.getSkuNo());
        return this.update(dmpSkuInfoEntity, lambdaQueryWrapper);
    }

    /**
     * 校验商品在中台是否存在，存在就修改不存在则新增
     * @Author Luo_WG
     * @Date 2022/11/14 21:25
     * @return void
     **/
    @Override
    public void checkOrder(DmpSkuInfoEntity dmpSkuInfoEntity) {
        DmpSkuInfoEntity dmpOrderInfoEntity = this.getSkuBySkuNo(dmpSkuInfoEntity.getSkuNo());
        if (dmpOrderInfoEntity != null) {
            //如果数据有变动需要更新数据库订单信息
            if (!dmpOrderInfoEntity.toString().equals(dmpSkuInfoEntity.toString())) {
                this.updateSkuBySkuNo(dmpOrderInfoEntity);
            }
        } else {
            this.add(dmpSkuInfoEntity);
        }
    }
}




