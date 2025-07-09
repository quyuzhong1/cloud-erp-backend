package com.erp.server.plm.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.utils.CollectionUtils;
import com.common.business.vo.PagingVO;
import com.common.core.constant.CommonConstants;
import com.erp.model.plm.dto.ProductCustomsDTO;
import com.erp.model.plm.dto.ProductCustomsSkuDTO;
import com.erp.model.plm.entity.ProductCustomsEntity;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.server.plm.mapper.ProductCustomsMapper;
import com.erp.server.plm.service.ProductCustomsService;
import com.erp.server.plm.service.ProductDetailService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-06-12
 */
@Slf4j
@Service
public class ProductCustomsServiceImpl extends SuperServiceImpl<ProductCustomsMapper, ProductCustomsEntity> implements ProductCustomsService {

    @Resource
    private ProductDetailService productDetailService;

    @Override
    public List<ProductCustomsEntity> listByProductId(String productId) {
/*        if (StringUtils.isBlank(productId)){
            return Collections.emptyList();
        }
        //根据产品id获取sku列表
        List<ProductDetailEntity> productDetailEntityList = productDetailService.listSkuByProductIds(Collections.singletonList(productId));
        if (CollectionUtil.isEmpty(productDetailEntityList)){
            return Collections.emptyList();
        }
        List<String> skuIds = productDetailEntityList.stream().map(ProductDetailEntity::getId).distinct().collect(Collectors.toList());
        return baseMapper.listBySkuIds(skuIds);*/

        return baseMapper.listByProductId(productId);
    }

    @Override
    public List<ProductCustomsEntity> listBySkuId(String skuId) {
        return baseMapper.listBySkuId(skuId);
    }

    @Override
    public Boolean removeBySkuId(List<String> skuIds) {
        return lambdaUpdate().set(ProductCustomsEntity::getIsDeleted, Boolean.TRUE).in(ProductCustomsEntity::getSkuId, skuIds).update();
    }

    @Override
    public Boolean addProductCustoms() {
        List<ProductDetailEntity> list = productDetailService.list();
        List<ProductCustomsEntity> customsEntityList = new ArrayList<>();
        list.forEach(req -> {
            ProductCustomsEntity productCustomsEntity = new ProductCustomsEntity();
            productCustomsEntity.setSkuId(req.getId());
            customsEntityList.add(productCustomsEntity);
        });
        return this.saveBatch(customsEntityList);
    }

    /**
     * 获取sku定义的目的国申报海关编码
     * @param dto
     * @return
     */
    @Override
    public List<ProductCustomsEntity> listProductCustomsBySkuIds(ProductCustomsSkuDTO dto) {
        if (Objects.isNull(dto) || CollectionUtil.isEmpty(dto.getSkuIds())){
            return Collections.emptyList();
        }
        return baseMapper.listProductCustomsBySkuIds(dto);
    }

    @Override
    public void addDefaultCustoms(List<String> skuIds) {
        if (CollectionUtil.isEmpty(skuIds)){
            return;
        }
        //默认记录是否存在 不存在则新增
        List<ProductCustomsEntity> list = lambdaQuery().in(ProductCustomsEntity::getSkuId, skuIds).eq(ProductCustomsEntity::getCountry, CommonConstants.DEFAULT)
                .eq(ProductCustomsEntity::getIsDeleted, Boolean.FALSE).list();
        List<String> existSkuIds = list.stream().map(ProductCustomsEntity::getSkuId).collect(Collectors.toList());
        List<String> noExistSkuIds = skuIds.stream().filter(e -> CollectionUtil.isEmpty(existSkuIds) || !existSkuIds.contains(e)).collect(Collectors.toList());
        if (CollectionUtil.isEmpty(noExistSkuIds)){
            List<ProductCustomsEntity> entityList = new ArrayList<>(noExistSkuIds.size());
            noExistSkuIds.forEach(skuId -> {
                entityList.add(new ProductCustomsEntity().setSkuId(skuId).setCountry(CommonConstants.DEFAULT));
            });
            this.saveBatch(entityList);
        }
    }

    @Override
    public List<ProductCustomsEntity> listBySkuIds(List<String> skuIds, String country) {
        if (CollectionUtil.isEmpty(skuIds)){
            return Collections.emptyList();
        }
        return lambdaQuery().in(ProductCustomsEntity::getSkuId,skuIds)
                .eq(StringUtils.isNotEmpty(country), ProductCustomsEntity::getCountry, country)
                .list();
    }

    @Override
    public ProductCustomsEntity getBySkuIdAndCountry(String skuId, String country) {
        if (StringUtils.isBlank(skuId)){
            return null;
        }
        List<ProductCustomsEntity> list = this.lambdaQuery().eq(ProductCustomsEntity::getSkuId, skuId).eq(ProductCustomsEntity::getCountry, country).list();
        if (CollectionUtil.isNotEmpty(list)){
            return list.get(0);
        }
        return null;
    }

    @Override
    public List<ProductCustomsDTO.TabListDTO> tabList(PermissionsDTO dto) {
        return Collections.emptyList();
    }

    @Override
    public PagingVO<ProductCustomsDTO.ListDTO> paging(PagingDTO<ProductCustomsDTO.PagingParamDTO> dto) {
        return null;
    }

    @Override
    public Boolean add(ProductCustomsDTO.AddDTO dto) {
        return null;
    }

    @Override
    public Boolean update(ProductCustomsDTO.UpdateDTO dto) {
        return null;
    }

    @Override
    public ProductCustomsDTO.ViewDTO view(String id) {
        return null;
    }

    @Override
    public void exportList(ProductCustomsDTO.PagingParamDTO dto, HttpServletResponse response) {

    }

    @Override
    public Boolean importFile(MultipartFile excelFile, HttpServletResponse response) {
        return null;
    }

    @Override
    public void downloadTemplate(HttpServletResponse response) {

    }


}
