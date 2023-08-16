package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.entity.ProductInfoEntity;
import com.erp.model.plm.enums.BomTypeEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.server.plm.mapper.BomInfoMapper;
import com.erp.server.plm.service.BomCombinationService;
import com.erp.server.plm.service.BomInfoService;
import com.erp.server.plm.service.ProductDetailService;
import com.erp.server.plm.service.ProductInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Will
 * @version 1.0
 * @description: 组合产品业务层
 * @date 2023/8/16 10:01
 */
@Service
@Slf4j
public class BomCombinationServiceImpl implements BomCombinationService {

    @Resource
    private BomInfoMapper bomInfoMapper;

    @Resource
    private BomInfoService bomInfoService;

    @Resource
    private ProductDetailService productDetailService;

    @Resource
    private ProductInfoService productInfoService;


    @Override
    public PagingVO<BomCombinationDTO.ListDTO> paging(PagingDTO<BomCombinationDTO.SearchParamDTO> dto) {
        BomCombinationDTO.SearchParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage<BomCombinationDTO.ListDTO> pageData = bomInfoMapper.combinationPaging(query, dto);
        //处理分页数据
        handlePaging(pageData.getRecords());
        return new PagingVO(pageData);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean add(BomCombinationDTO.AddDTO dto) {
        //新增产品信息
        String skuId = addProductDetail(dto);

        //新增bom信息
        addBomCombination(dto,skuId);

        return null;
    }

    private void addBomCombination (BomCombinationDTO.AddDTO dto,String skuId) {
        AddBomDTO addBomDTO = new AddBomDTO();

        addBomDTO.setVersion(MathUtil.ONE);
        addBomDTO.setSubmitType(BomTypeEnum.COMBINATION.getType());
        addBomDTO.setSubmitType(BomTypeEnum.SUBMIT_AUDIT.getType());
        List<BomSkuDTO> skuList = new ArrayList<>();
        BomSkuDTO bomSkuDTO = new BomSkuDTO();
        bomSkuDTO.setSkuId(skuId);
        bomSkuDTO.setSkuNo(dto.getSkuNo());
        List<BomChildrenSkuDTO> children = new ArrayList<>();

        List<String> skuIds = dto.getDetailList().stream().map(BomCombinationDetailDTO.AddDTO::getSkuId).collect(Collectors.toList());
        List<ProductDetailEntity> childList = productDetailService.listByIds(skuIds);
        if (CollectionUtils.isEmpty(childList)) {
            throw new ServiceException(ApiError.ERROR_95084);
        }

        for (BomCombinationDetailDTO.AddDTO addDetail : dto.getDetailList()) {
            BomChildrenSkuDTO childrenSkuDTO = new BomChildrenSkuDTO();
            ProductDetailEntity child = childList.stream().filter(obj -> obj.getId().equals(addDetail.getSkuId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(child)) {
                throw new ServiceException(ApiError.ERROR_95084);
            }
            childrenSkuDTO.setSkuId(addDetail.getSkuId());
            childrenSkuDTO.setSkuNo(child.getSkuNo());

        }
        bomSkuDTO.setChildren(children);
        skuList.add(bomSkuDTO);
        addBomDTO.setSkuList(skuList);
        bomInfoService.insert(addBomDTO);
    }


    @Override
    public Boolean update(BomCombinationDTO.UpdateDTO dto) {
        return null;
    }

    @Override
    public Boolean view(BaseIdDTO dto) {
        return null;
    }


    private String addProductDetail (BomCombinationDTO.AddDTO dto) {

        List<SkuVO> parentSkuList = productDetailService.getSkuBySkuNos(Arrays.asList(dto.getSkuNo()));
        //已存在则直接返回sku主键id
        if (CollectionUtils.isNotEmpty(parentSkuList)) {
            log.info("已存在SKU【{}】",dto.getSkuNo());
            return parentSkuList.get(0).getSkuId();
        }

        List<BomCombinationDetailDTO.AddDTO> detailList = dto.getDetailList();
        ProductDetailEntity child = productDetailService.getById(detailList.get(0).getSkuId());
        if (ObjectUtils.isEmpty(child)) {
            throw new ServiceException(ApiError.ERROR_95084);
        }

        ProductInfoEntity productInfoEntity = productInfoService.getById(child.getProductId());

        //产品信息新增
        ProductNoSpecDTO productNoSpecDTO = new ProductNoSpecDTO();
        ProductBaseInfoDTO productBaseInfoDTO = new ProductBaseInfoDTO();

        //产品信息
        ProductInfoDTO productInfoDTO = new ProductInfoDTO();
        productInfoDTO.setCategory(productInfoEntity.getCategory());
        productInfoDTO.setCategoryId(productInfoEntity.getCategoryId());
        productBaseInfoDTO.setProductSpuBaseInfoDTO(productInfoDTO);
        //sku信息
        ProductSkuBaseInfoDTO productSkuBaseInfoDTO = new ProductSkuBaseInfoDTO();
        productSkuBaseInfoDTO.setSkuNo(dto.getSkuNo());
        productSkuBaseInfoDTO.setName(dto.getName());
        productSkuBaseInfoDTO.setChargeId(child.getChargeId());
        productSkuBaseInfoDTO.setChargeName(child.getChargeName());
        productBaseInfoDTO.setProductSkuBaseInfoDTO(productSkuBaseInfoDTO);
        productNoSpecDTO.setProductBaseInfoDTO(productBaseInfoDTO);
        //成本信息
        ProductCostDTO productCostDTO = new ProductCostDTO();
        productNoSpecDTO.setProductCostDTO(productCostDTO);
        //采购信息信息
        ProductPurchaseDTO productPurchaseDTO = new ProductPurchaseDTO();
        productNoSpecDTO.setProductPurchaseDTO(productPurchaseDTO);
        //销售信息
        ProductSaleDTO productSaleDTO = new ProductSaleDTO();
        productNoSpecDTO.setProductSaleDTO(productSaleDTO);
        //物流信息
        ProductLogisticsDTO productLogisticsDTO = new ProductLogisticsDTO();
        productNoSpecDTO.setProductLogisticsDTO(productLogisticsDTO);
        //包装信息
        ProductPackDTO productPackDTO = new ProductPackDTO();
        productNoSpecDTO.setProductPackDTO(productPackDTO);
        //包装辅料信息
        List<ProductAccessoriesDTO> productAccessoriesList = new ArrayList<>();
        ProductAccessoriesDTO productAccessoriesDTO = new ProductAccessoriesDTO();
        productAccessoriesDTO.setParentSkuNo(dto.getSkuNo());
        productAccessoriesList.add(productAccessoriesDTO);
        productNoSpecDTO.setProductAccessoriesList(productAccessoriesList);
        
        productDetailService.saveOrUpdateNoSpec(productNoSpecDTO);
        return null;
    }

    /**
     * @description: 处理分页数据
     * @author Will
     * @date: 2023/8/16 14:25
     * @param records
     */
    private void handlePaging (List<BomCombinationDTO.ListDTO> records) {
        if (CollectionUtils.isEmpty(records)) {
            return;
        }
        for (BomCombinationDTO.ListDTO dto : records) {
            if (CollectionUtils.isNotEmpty(dto.getChildList())) {
                //子级sku编号
                String childSkoNos = dto.getChildList().stream().map(BomCombinationDTO.ChildDTO::getChildSkuNo).collect(Collectors.joining(","));
                dto.setChildSkuNos(childSkoNos);
                BigDecimal childSkuCost = dto.getChildList().stream().map(obj -> MathUtil.compareTo(obj.getActualTaxCost(), BigDecimal.ZERO) == MathUtil.ZERO ? obj.getTargetTaxCost() : obj.getActualTaxCost()).reduce(BigDecimal.ZERO, BigDecimal::add);
                dto.setChildSkuCost(childSkuCost);
            }
        }
    }
}
