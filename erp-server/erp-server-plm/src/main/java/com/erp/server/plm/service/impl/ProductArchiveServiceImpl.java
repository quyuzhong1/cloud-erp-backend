package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.dto.base.PagingDTO;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.dto.CountDTO;
import com.erp.model.plm.dto.ProductArchiveDTO;
import com.erp.model.plm.dto.ProductSearchDTO;
import com.erp.model.plm.entity.ProductArchiveEntity;
import com.erp.server.plm.mapper.ProductArchiveMapper;
import com.erp.server.plm.service.ProductArchiveService;
import com.erp.server.plm.service.ProductInfoService;
import com.erp.server.plm.service.TaskDeliveryService;
import com.erp.server.plm.service.TaskDocsFinishService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @Classname ProductArchiveServiceImpl

 * @Date 2022-10-09 11:40
 * @Created by yl
 */
@Service
public class ProductArchiveServiceImpl extends ServiceImpl<ProductArchiveMapper, ProductArchiveEntity>
        implements ProductArchiveService {

    @Autowired
    private TaskDeliveryService taskDeliveryService;

    @Autowired
    private TaskDocsFinishService finishService;

    @Autowired
    private ProductInfoService productInfoService;

    @Override
    public PagingVO<ProductArchiveDTO> paging(PagingDTO<ProductSearchDTO.PagingParamDTO> dto) {
        dto.getParams().setPermissionSql(dto.getPermissionSql());
        Page<ProductSearchDTO.PagingParamDTO> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        ProductSearchDTO.PagingParamDTO params = dto.getParams();
        IPage<ProductArchiveDTO> pageData = baseMapper.paging(query, params);
        List<ProductArchiveDTO> list = pageData.getRecords();
        if (CollectionUtils.isNotEmpty(list)) {
            //根据产品id 获取到对应的要交付的文档数
            List<CountDTO> productDocs = taskDeliveryService.getTaskDocsCountByProductId();
            //根据产品id 获取到对应完成的文档数
            List<CountDTO> productFinishDocs = finishService.getTaskDocsCountByProductId();
            //   获取到 产品迭代的数量
            List<CountDTO> productRelevance = productInfoService.getProductRelevanceList();

            for (ProductArchiveDTO item : list) {
                String productId = item.getProductId();
                //总的文档数
                CountDTO totalDocsDTO = productDocs.stream().filter(p -> productId.equals(p.getFlagId())).findFirst().orElse(null);
                if (totalDocsDTO != null) {
                    item.setTotalDocsCount(totalDocsDTO.getCount());
                } else {
                    item.setTotalDocsCount(0);
                }
                //完成的
                CountDTO finishDocsDTO = productFinishDocs.stream().filter(p -> productId.equals(p.getFlagId())).findFirst().orElse(null);
                if (finishDocsDTO != null) {
                    item.setFinishDocsCount(finishDocsDTO.getCount());
                } else {
                    item.setFinishDocsCount(0);
                }

                //迭代数
                CountDTO relevanceDTO = productRelevance.stream().filter(p -> productId.equals(p.getFlagId())).findFirst().orElse(null);
                if (relevanceDTO != null) {
                    item.setIterateCount(relevanceDTO.getCount());
                } else {
                    item.setIterateCount(0);
                }

            }

        }
        return new PagingVO<>(pageData);

    }

    /**
     * 提交成功后，可在产品开发列表可见。归档列表不可见
     *
     * @param productId
     * @return void
     * @author yl
     * @date 2022-10-09 14:32
     */
    @Override
    public boolean activate(String productId) {
        LambdaQueryWrapper<ProductArchiveEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductArchiveEntity::getProductId, productId);
        return this.remove(queryWrapper);
    }


    public ProductArchiveEntity getByProductId(String productId) {
        LambdaQueryWrapper<ProductArchiveEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductArchiveEntity::getProductId, productId);
        return baseMapper.selectOne(queryWrapper);
    }


    /**
     * 保存归档信息
     *
     * @param productId
     * @return java.lang.Boolean
     * @author yl
     * @date 2022-10-09 15:09
     */
    @Override
    public Boolean saveArchive(String productId) {
        String operator = UserContext.getDefaultLoginUser().getUid();
        ProductArchiveEntity entity = getByProductId(productId);
        if (!Objects.isNull(entity)) {
            entity.setArchiveTime(LocalDateTime.now());
            entity.setOperator(operator);
            return this.updateById(entity);
        } else {
            ProductArchiveEntity saveEntity = new ProductArchiveEntity();
            saveEntity.setArchiveTime(LocalDateTime.now());
            saveEntity.setProductId(productId);
            saveEntity.setOperator(operator);
            return this.save(saveEntity);
        }


    }


    /**
     * 获取已归档的产品id 集合
     *
     * @param
     * @return java.util.List<java.lang.String>
     * @author yl
     * @date 2022-10-09 16:12
     */
    @Override
    public List<String> getArchiveProductIds() {
        LambdaQueryWrapper<ProductArchiveEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(ProductArchiveEntity::getProductId);
        List<String> list = this.listObjs(queryWrapper, Object::toString);
        return list;
    }

    @Override
    public ProductArchiveEntity getArchiveByProductId(String productId) {
        LambdaQueryWrapper<ProductArchiveEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductArchiveEntity::getProductId, productId);
        return this.getOne(queryWrapper);
    }


    /**
     * 批量添加归档
     *
     * @param productIdList
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-06-14 14:59
     */
    @Override
    public Boolean batchAddArchive(List<String> productIdList) {
        String operator = UserContext.getDefaultLoginUser().getUid();
        List<ProductArchiveEntity> entityList = listByProductIdList(productIdList);
        LocalDateTime now = LocalDateTime.now();
        List<ProductArchiveEntity> saveOrUpdateList = new ArrayList<>(productIdList.size());
        for (String productId : productIdList) {
            ProductArchiveEntity entity = entityList.stream().filter(p -> p.getProductId().equals(productId)).
                    findFirst().orElse(null);
            if (!Objects.isNull(entity)) {
                entity.setArchiveTime(now);
                entity.setOperator(operator);
                saveOrUpdateList.add(entity);
            } else {
                ProductArchiveEntity saveEntity = new ProductArchiveEntity();
                saveEntity.setArchiveTime(now);
                saveEntity.setProductId(productId);
                saveEntity.setOperator(operator);
                saveOrUpdateList.add(saveEntity);
            }
        }
        return this.saveOrUpdateBatch(saveOrUpdateList);

    }

    private List<ProductArchiveEntity> listByProductIdList(List<String> productIdList) {
        if (CollectionUtils.isEmpty(productIdList)) {
            return Collections.emptyList();
        }
        return this.lambdaQuery().in(ProductArchiveEntity::getProductId, productIdList).list();
    }
}
