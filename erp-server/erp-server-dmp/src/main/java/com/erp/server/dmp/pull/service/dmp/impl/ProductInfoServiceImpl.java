package com.erp.server.dmp.pull.service.dmp.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.model.plm.entity.ProductInfoEntity;
import com.erp.model.plm.entity.ProductInfoEntity;
import com.erp.server.dmp.pull.mapper.ProductInfoMapper;
import com.erp.server.dmp.pull.service.dmp.ProductInfoService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductInfoServiceImpl extends ServiceImpl<ProductInfoMapper, ProductInfoEntity> implements ProductInfoService {

    /**
     * 根据主键Id查询产品表信息
     * @Author Luo_WG
     * @Date 2023/4/19 16:25
     * @param ids
     * @return com.erp.model.plm.entity.ProductInfoEntity
     **/
    @Override
    public List<ProductInfoEntity> ListProductInfoByIds(List<String> ids) {
        return baseMapper.ListProductInfoByIds(ids);
    }

    /**
     * 更新PLM同步过来的数据
     * @Author Luo_WG
     * @Date 2023/4/19 16:05
     **/
    public Boolean saveOrUpdateProductInfo(List<ProductInfoEntity> productInfoEntityList) {
        List<String> detailIds = productInfoEntityList.stream().map(ProductInfoEntity::getId).collect(Collectors.toList());
        List<ProductInfoEntity> detailEntityList = ListProductInfoByIds(detailIds);
        List<String> ids = productInfoEntityList.stream().map(ProductInfoEntity::getId).collect(Collectors.toList());
        List<String> dbIds = detailEntityList.stream().map(ProductInfoEntity::getId).collect(Collectors.toList());
        List<String> existIdList = ids.stream().filter(s -> dbIds.contains(s)).collect(Collectors.toList());
        List<String> notExistIdList = ids.stream().filter(s -> !dbIds.contains(s)).collect(Collectors.toList());
        List<ProductInfoEntity> existDetailEntityList = ListProductInfoByIds(existIdList);
        List<ProductInfoEntity> notExistDetailEntityList = ListProductInfoByIds(notExistIdList);
        baseMapper.updateBatchSelective(existDetailEntityList);
        this.saveBatch(notExistDetailEntityList);
        return Boolean.TRUE;
    }
}
