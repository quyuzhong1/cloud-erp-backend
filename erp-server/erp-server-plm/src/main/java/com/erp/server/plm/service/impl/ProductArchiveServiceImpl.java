package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.server.plm.interceptor.PlmInterceptor;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.LoginUser;
import com.erp.common.vo.PagingVO;
import com.erp.model.plm.dto.ProductArchiveDTO;
import com.erp.model.plm.dto.ProductSearchDTO;
import com.erp.model.plm.dto.CountDTO;
import com.erp.model.plm.entity.ProductArchiveEntity;
import com.erp.server.plm.mapper.ProductArchiveMapper;
import com.erp.server.plm.service.ProductArchiveService;
import com.erp.server.plm.service.ProductInfoService;
import com.erp.server.plm.service.TaskDeliveryService;
import com.erp.server.plm.service.TaskDocsFinishService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @Classname ProductArchiveServiceImpl
 * @Description TODO
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
    public PagingVO paging(PagingDTO<ProductSearchDTO> dto) {
        dto.getParams().setParam(dto.getParam());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        ProductSearchDTO params = dto.getParams();
        IPage pageData = baseMapper.paging(query, params);
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
        return new PagingVO(pageData);

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
        LambdaQueryWrapper<ProductArchiveEntity> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(ProductArchiveEntity::getProductId, productId);
        return this.remove(queryWrapper);
    }


    public ProductArchiveEntity getByProductId(String productId) {
        LambdaQueryWrapper<ProductArchiveEntity> queryWrapper = new LambdaQueryWrapper();
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
        LoginUser user = PlmInterceptor.threadLocal.get();
        String operator = "";
        if (user != null) {
            operator = user.getUid();
        }
        ProductArchiveEntity entity = getByProductId(productId);
        if (!Objects.isNull(entity)) {
            entity.setArchiveTime(new Date());
            entity.setOperator(operator);
            return this.updateById(entity);
        } else {
            ProductArchiveEntity saveEntity = new ProductArchiveEntity();
            saveEntity.setArchiveTime(new Date());
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
        queryWrapper.eq(ProductArchiveEntity::getProductId,productId);
        return this.getOne(queryWrapper);
    }
}
