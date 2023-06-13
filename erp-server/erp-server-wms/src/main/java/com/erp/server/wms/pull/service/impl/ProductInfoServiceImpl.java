package com.erp.server.wms.pull.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.dto.base.BaseDropDownDTO;
import com.erp.model.plm.entity.ProductInfoEntity;
import com.erp.server.wms.pull.mapper.ProductInfoMapper;
import com.erp.server.wms.pull.service.ProductInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @CreateTime: 2023-05-11  19:19
 * @Author: zhangchunlin
 */
@Slf4j
@Service
public class ProductInfoServiceImpl extends ServiceImpl<ProductInfoMapper, ProductInfoEntity> implements ProductInfoService {

    @Override
    public void saveOrUpdateProductInfo(List<ProductInfoEntity> productInfoEntities) {
        for(ProductInfoEntity productInfoEntity : productInfoEntities) {
            // 此处注意删除的数据
            ProductInfoEntity entity = this.baseMapper.selectById(productInfoEntity.getId());
            //不存在需要新增，同时判断产品是否更新，用最后更新时间
            if (ObjectUtil.isNotEmpty(entity)) {
                if (!Objects.equals(entity.getUpdateTime(), productInfoEntity.getUpdateTime())) { // 逻辑删除字段也会同步
                    try {
                        this.baseMapper.updateAllById(productInfoEntity); // 更新所有字段，包括逻辑删除字段
                    } catch (Exception e) {
                        log.error("产品【{}】更新异常", productInfoEntity.getId(), e);
                    }
                } else {
                    // 数据没有发生变更
                }
            } else {
                try {
                    this.save(productInfoEntity);
                } catch (Exception e) {
                    log.error("产品【{}】新增异常", productInfoEntity.getId(), e);
                }
            }
        }
    }

    @Override
    public List<BaseDropDownDTO.CommonDTO> getNotEmptySpuNos() {
        List<String> spuNos = this.baseMapper.getNotEmptySpuNos();
        if(CollUtil.isNotEmpty(spuNos)) {
            spuNos = spuNos.stream().distinct().collect(Collectors.toList());
        }
        if(CollUtil.isNotEmpty(spuNos)) {
            return spuNos.stream().map(x -> new BaseDropDownDTO.CommonDTO(x, x))
                    .collect(Collectors.toList());
        }
        return null;
    }

}