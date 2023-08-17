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
import com.erp.model.plm.entity.BomInfoEntity;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.entity.ProductInfoEntity;
import com.erp.model.plm.enums.BomStateEnum;
import com.erp.model.plm.enums.BomTypeEnum;
import com.erp.model.plm.enums.ProductDetailStatusEnum;
import com.erp.server.plm.mapper.BomInfoMapper;
import com.erp.server.plm.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    private BomSkuService bomSkuService;

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
        Boolean addBom = addBomCombination(dto, skuId);
        return addBom;
    }

    @Override
    public Boolean update(BomCombinationDTO.UpdateDTO dto) {
        //新增产品信息
        String skuId = updateProductDetail(dto);
        //新增bom信息
        Boolean addBom = updateBomCombination(dto, skuId);
        return addBom;
    }

    @Override
    public BomCombinationDTO.ViewDTO view(BaseIdDTO dto) {
        BomCombinationDTO.ViewDTO resultDTO = new BomCombinationDTO.ViewDTO();

        List<BomSkuDTO> bomList = bomSkuService.getByBomId(dto.getId());
        if (CollectionUtils.isEmpty(bomList)) {
            throw new ServiceException(ApiError.ERROR_95163);
        }
        BomSkuDTO bomSkuDTO = bomList.get(0);

        List<String> childSkuIds = bomSkuDTO.getChildren().stream().flatMap(obj -> Stream.of(obj.getParentSkuId(),obj.getSkuId())).distinct().collect(Collectors.toList());
        List<ProductDetailEntity> skuList = productDetailService.listByIds(childSkuIds);
        if (CollectionUtils.isEmpty(skuList)) {
            throw new ServiceException(ApiError.ERROR_95084);
        }
        //父级SKU名称
        String parentSkuName = skuList.stream().filter(obj -> obj.getId().equals(bomSkuDTO.getSkuId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
        resultDTO.setId(dto.getId());
        resultDTO.setSkuNo(bomSkuDTO.getSkuNo());
        resultDTO.setName(parentSkuName);
        List<BomCombinationDetailDTO.ViewDTO> detailList = new ArrayList<>();
        for (BomChildrenSkuDTO bomChildrenSkuDTO : bomSkuDTO.getChildren()) {
            BomCombinationDetailDTO.ViewDTO viewDTO = new BomCombinationDetailDTO.ViewDTO();
            viewDTO.setId(bomChildrenSkuDTO.getId());
            viewDTO.setSkuId(bomChildrenSkuDTO.getSkuId());
            viewDTO.setSkuNo(bomChildrenSkuDTO.getSkuNo());
            //子SKU名称
            String childSkuName = skuList.stream().filter(obj -> obj.getId().equals(bomChildrenSkuDTO.getSkuId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            viewDTO.setProductName(childSkuName);
            detailList.add(viewDTO);
        }
        resultDTO.setDetailList(detailList);
        return resultDTO;
    }

    /**
     * @description: 新增
     * @author Will
     * @date: 2023/8/16 18:20
     * @param dto
     * @return String
     */
    private String addProductDetail (BomCombinationDTO.AddDTO dto) {

        Map<String, String> params = new HashMap<>();
        params.put("skuNo",dto.getSkuNo());
        ProductDetailDTO productDetailDTO = productDetailService.getSkuByParam(params);
        //已存在则直接返回sku主键id
        if (ObjectUtils.isNotEmpty(productDetailDTO)) {
            log.info("已存在SKU【{}】",dto.getSkuNo());
            return productDetailDTO.getId();
        }

        List<BomCombinationDetailDTO.AddDTO> detailList = dto.getDetailList();
        //新增产品信息
        String id = commonProductDetail(dto, detailList.get(0).getSkuId());
        //提交并审核
        skuSubmitApprove(id);
        return id;
    }

    /**
     * @description: 修改
     * @author Will
     * @date: 2023/8/17 10:17
     * @param dto
     * @return String
     */
    private String updateProductDetail (BomCombinationDTO.UpdateDTO dto) {
        Map<String, String> params = new HashMap<>();
        params.put("skuNo",dto.getSkuNo());
        ProductDetailDTO productDetailDTO = productDetailService.getSkuByParam(params);
        //已存在则直接返回sku主键id
        if (ObjectUtils.isNotEmpty(productDetailDTO)) {
            log.info("已存在SKU【{}】",dto.getSkuNo());
            //如果已变更sku名称则需要更新sku名称
            if (!dto.getName().equals(productDetailDTO.getName())) {
                updateSkuName(dto.getName(),productDetailDTO);
            }
            return productDetailDTO.getId();
        }
        List<BomCombinationDetailDTO.UpdateDTO> detailList = dto.getDetailList();
        //新增产品信息
        String id = commonProductDetail(dto, detailList.get(0).getSkuId());
        return id;
    }

    /**
     * 更新sku名称
     */
    private void updateSkuName (String name,ProductDetailDTO productDetailDTO) {
        //审核通过后需要反审核
        Boolean isApprove = Boolean.FALSE;
        if (ProductDetailStatusEnum.APPROVAL_PASS.getCode().equals(productDetailDTO.getStatus()) ) {
            productDetailService.deApprove(productDetailDTO.getId());
            isApprove = Boolean.TRUE;
        }
        //更新名称
        productDetailService.updateName(productDetailDTO.getId(),name);
        //审核
        if (isApprove) {
            skuSubmitApprove(productDetailDTO.getId());
        }
    }

    /**
     * @description: 新增产品信息
     * @author Will
     * @date: 2023/8/17 10:16
     * @param dto
     * @param childSkuId
     * @return String
     */
    private String commonProductDetail (BomCombinationDTO.CommonDTO dto,String childSkuId) {

        ProductDetailEntity child = productDetailService.getById(childSkuId);
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

        return productNoSpecDTO.getProductBaseInfoDTO().getProductSkuBaseInfoDTO().getId();
    }

    /**
     * @description: 产品信息提交审核
     * @author Will
     * @date: 2023/8/17 11:29
     * @param id
     */
    private void skuSubmitApprove (String id) {
        //提交
        Boolean submit = productDetailService.submit(Arrays.asList(id));
        if (!submit) {
            throw new ServiceException(ApiError.ERROR_1042);
        }
        //审核
        ProductDetailOperateDTO dto = new ProductDetailOperateDTO();
        dto.setId(id);
        Boolean approve = productDetailService.approvalPass(dto);
        if (!approve) {
            throw new ServiceException(ApiError.ERROR_94006);
        }
    }

    /**
     * @description: 新增bom信息
     * @author Will
     * @date: 2023/8/16 18:21
     * @param dto
     * @param skuId
     * @return Boolean
     */
    private Boolean addBomCombination (BomCombinationDTO.AddDTO dto,String skuId) {
        AddBomDTO addBomDTO = new AddBomDTO();
        //BOM主表信息
        addBomDTO.setVersion(MathUtil.ONE);
        addBomDTO.setSubmitType(BomTypeEnum.COMBINATION.getType());
        addBomDTO.setSubmitType(BomTypeEnum.SUBMIT_AUDIT.getType());
        //BOM父级SKU信息
        List<BomSkuDTO> skuList = new ArrayList<>();
        BomSkuDTO bomSkuDTO = new BomSkuDTO();
        bomSkuDTO.setSkuId(skuId);
        bomSkuDTO.setSkuNo(dto.getSkuNo());

        //BOM子级SKU信息
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
            childrenSkuDTO.setProductId(child.getProductId());
            childrenSkuDTO.setQuantity(addDetail.getQty());
            childrenSkuDTO.setSkuName(child.getName());
            children.add(childrenSkuDTO);
        }
        bomSkuDTO.setChildren(children);
        skuList.add(bomSkuDTO);
        addBomDTO.setSkuList(skuList);
        String bomId = bomInfoService.insert(addBomDTO);
        //审核BOM
        bomSubmitApprove(bomId);
        return Boolean.TRUE;
    }

    /**
     * @description: 修改bom信息
     * @author Will
     * @date: 2023/8/17 11:15
     * @param dto
     * @param skuId
     * @return Boolean
     */
    private Boolean updateBomCombination (BomCombinationDTO.UpdateDTO dto,String skuId) {

        BomInfoEntity bomInfoEntity = bomInfoService.getById(dto.getId());
        if (ObjectUtils.isEmpty(bomInfoEntity)) {
            throw new ServiceException(ApiError.ERROR_95163);
        }
        if (!BomStateEnum.AUDIT_PASS.getState().equals(bomInfoEntity.getState()) && !BomStateEnum.WAIT_AUDIT.getState().equals(bomInfoEntity.getState())) {
            throw new ServiceException(ApiError.ERROR_BOM_COMBINATION_STATE);
        }
        //解除归档
        if (BomStateEnum.AUDIT_PASS.getState().equals(bomInfoEntity.getState())) {
            bomInfoService.removeArchive(dto.getId());
        }
        UpdateBomDTO updateBomDTO = new UpdateBomDTO();
        updateBomDTO.setId(dto.getId());
        //父级SKU
        List<BomSkuDTO> skuList = new ArrayList<>();
        BomSkuDTO bomSkuDTO = new BomSkuDTO();
        bomSkuDTO.setSkuId(skuId);
        bomSkuDTO.setSkuNo(dto.getSkuNo());

        List<String> skuIds = dto.getDetailList().stream().map(BomCombinationDetailDTO.UpdateDTO::getSkuId).collect(Collectors.toList());
        List<ProductDetailEntity> childList = productDetailService.listByIds(skuIds);
        if (CollectionUtils.isEmpty(childList)) {
            throw new ServiceException(ApiError.ERROR_95084);
        }
        //子级SKU
        List<BomChildrenSkuDTO> children = new ArrayList<>();
        for (BomCombinationDetailDTO.UpdateDTO updateDTO : dto.getDetailList()) {
            BomChildrenSkuDTO childrenSkuDTO = new BomChildrenSkuDTO();
            ProductDetailEntity child = childList.stream().filter(obj -> obj.getId().equals(updateDTO.getSkuId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(child)) {
                throw new ServiceException(ApiError.ERROR_95084);
            }
            childrenSkuDTO.setParentSkuId(skuId);
            childrenSkuDTO.setSkuId(updateDTO.getSkuId());
            childrenSkuDTO.setSkuNo(child.getSkuNo());
            childrenSkuDTO.setSkuName(child.getName());
            childrenSkuDTO.setProductId(child.getProductId());
            childrenSkuDTO.setQuantity(updateDTO.getQty());
            children.add(childrenSkuDTO);
        }
        bomSkuDTO.setChildren(children);
        skuList.add(bomSkuDTO);
        updateBomDTO.setSkuList(skuList);
        bomInfoService.edit(updateBomDTO);

        //自动提交并审核
        bomSubmitApprove(dto.getId());
        return Boolean.TRUE;
    }

    /**
     * @description: bom信息提交审核
     * @author Will
     * @date: 2023/8/17 11:40
     * @param bomId
     * @return Boolean
     */
    private Boolean bomSubmitApprove (String bomId) {
        //提交
        Boolean submit = bomInfoService.submitAudit(bomId);
        if (!submit) {
            throw new ServiceException(ApiError.ERROR_1042);
        }
        AuditParamDTO dto = new AuditParamDTO();
        dto.setId(bomId);
        bomInfoService.approvalPass(dto);
        return Boolean.TRUE;
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
