package com.erp.server.dmp.service.impl;


import cn.hutool.core.collection.CollectionUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.dmp.dto.DmpSkuCostCustomDTO;
import com.erp.model.dmp.entity.DmpSkuCostCustomEntity;
import com.erp.server.dmp.mapper.DmpSkuCostCustomMapper;
import com.erp.server.dmp.service.DmpSkuCostCustomService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
/**
 * <p>
 * sku自定义成本表 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-01-04
 */
@Slf4j
@Service
public class DmpSkuCostCustomServiceImpl extends SuperServiceImpl<DmpSkuCostCustomMapper, DmpSkuCostCustomEntity> implements DmpSkuCostCustomService {

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(DmpSkuCostCustomDTO.AddDTO addDTO) {
        DmpSkuCostCustomEntity dmpSkuCostCustomEntity = new DmpSkuCostCustomEntity();
        BeanMapperUtils.copy(addDTO, dmpSkuCostCustomEntity);

        // 数据处理
        handleData(dmpSkuCostCustomEntity);

        log.info("开始新增sku自定义成本单");
        boolean save = super.save(dmpSkuCostCustomEntity);
        if(!save) {
            throw new ServiceException("sku自定义成本单保存失败");
        }
        return new BaseResultDTO.AddDTO(dmpSkuCostCustomEntity.getId(), dmpSkuCostCustomEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(DmpSkuCostCustomDTO.UpdateDTO updateDTO) {
        DmpSkuCostCustomEntity old = super.getById(updateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "sku自定义成本单"));
        DmpSkuCostCustomEntity dmpSkuCostCustomEntity =  BeanMapperUtils.map(DmpSkuCostCustomEntity.class, updateDTO);

        // 数据处理
        handleData(dmpSkuCostCustomEntity);
        log.info("编辑 开始修改sku自定义成本单数据，id：【{}】", old.getId());
        boolean save = super.updateById(dmpSkuCostCustomEntity);
        if(!save) {
            throw new ServiceException("sku自定义成本单保存失败");
        }
        return Boolean.TRUE;
    }

    @Override
    public DmpSkuCostCustomEntity getBySkuNo(String skuNo) {
        return  lambdaQuery()
                .eq(DmpSkuCostCustomEntity::getSkuNo,skuNo)
                .one();
    }

    @Override
    public List<DmpSkuCostCustomEntity> listDmpSkuCostCustomBySkuNoList(List<String> redisSkuNoList) {
        if (CollectionUtil.isEmpty(redisSkuNoList)) {
            return Collections.EMPTY_LIST;
        }
        return lambdaQuery().in(DmpSkuCostCustomEntity::getSkuNo,redisSkuNoList).list();
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(DmpSkuCostCustomEntity dmpSkuCostCustomEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
