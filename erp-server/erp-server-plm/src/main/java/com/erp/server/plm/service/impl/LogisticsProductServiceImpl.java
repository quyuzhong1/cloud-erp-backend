package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.MathUtil;
import com.erp.model.plm.dto.LogisticsProductDTO;
import com.erp.model.plm.dto.ProductCustomsDTO;
import com.erp.model.plm.entity.BomInfoEntity;
import com.erp.model.plm.entity.ProductCustomsEntity;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.entity.ProductLogisticsEntity;
import com.erp.model.plm.enums.ProductDetailStatusEnum;
import com.erp.model.plm.enums.SaleStateEnum;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.model.sys.dto.DictCountryDTO;
import com.erp.model.sys.dto.DictGlobalAreaDTO;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.plm.mapper.ProductDetailMapper;
import com.erp.server.plm.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description TODO
 * @Author yl
 * @Date 2023-11-06 12:28
 */
@Slf4j
@Service
public class LogisticsProductServiceImpl extends SuperServiceImpl<ProductDetailMapper, ProductDetailEntity> implements LogisticsProductService {

    @Resource
    private SysUserFeign sysUserFeign;
    @Resource
    private BomSkuService bomSkuService;

    @Resource
    private ProductLogisticsService productLogisticsService;

    @Resource
    private ProductCustomsService productCustomsService;

    @Resource
    private ProductDetailService productDetailService;


    @Override
    public PagingVO<LogisticsProductDTO.PagingVO> paging(PagingDTO<LogisticsProductDTO.PagingParamDTO> dto) {
        LogisticsProductDTO.PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        Integer approvalStatus = ProductDetailStatusEnum.APPROVAL_PASS.getCode();
        IPage pageData = baseMapper.logisticsProductPaging(query, params, approvalStatus);
        List<LogisticsProductDTO.PagingVO> list = pageData.getRecords();
        fillPagingDb(list);
        return new PagingVO<>(pageData);
    }


    @Override
    public LogisticsProductDTO.ViewDTO view(String skuId) {
        LogisticsProductDTO.ViewDTO result = new LogisticsProductDTO.ViewDTO();
        LogisticsProductDTO.ProductBaseInfoDTO productBaseInfo = baseMapper.getProductBaseInfo(skuId);
        Integer salesStatus = productBaseInfo.getSalesStatus();
        String salesStatusName = SaleStateEnum.getNameByCode(salesStatus);
        productBaseInfo.setSalesStatusName(salesStatusName);
        result.setProductBaseInfo(productBaseInfo);
        LogisticsProductDTO.DeclareInfoDTO declareInfo = new LogisticsProductDTO.DeclareInfoDTO();
        ProductLogisticsEntity productLogistics = productLogisticsService.getBySkuId(skuId);
        if (Objects.nonNull(productLogistics)) {
            BeanMapper.copy(productLogistics, declareInfo);
        }
        result.setDeclareInfo(declareInfo);

        List<ProductCustomsEntity> productCustomsList = productCustomsService.listBySkuId(skuId);
        List<ProductCustomsDTO.ViewDTO> customsList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(productCustomsList)) {
            customsList = BeanMapper.copyList(productCustomsList, ProductCustomsDTO.ViewDTO.class);
            BigDecimal flag = MathUtil.BigDecimal_100;
            for (ProductCustomsDTO.ViewDTO item : customsList) {
                BigDecimal taxRate = item.getTaxRate();
                taxRate = MathUtil.multiply(taxRate, flag);
                item.setTaxRate(taxRate);
            }
        }
        result.setCustomsList(customsList);
        return result;
    }

    /**
     * 编辑信息
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-11-08 8:37
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(LogisticsProductDTO.UpdateDTO dto) {
        //报关信息
        LogisticsProductDTO.DeclareInfoDTO declareInfo = dto.getDeclareInfo();
        ProductLogisticsEntity productLogistics = new ProductLogisticsEntity();
        BeanMapper.copy(declareInfo, productLogistics);
        handleProductLogistics(productLogistics);

        List<ProductCustomsDTO.ViewDTO> customsList = dto.getCustomsList();
        List<ProductCustomsEntity> productCustomsList = BeanMapper.copyList(customsList, ProductCustomsEntity.class);
        List<String> deleteIdList = handleCustoms(productCustomsList);
        Boolean logisticsResult = productLogisticsService.saveOrUpdate(productLogistics);
        if (CollectionUtils.isNotEmpty(deleteIdList)) {
            productCustomsService.removeByIds(deleteIdList);
        }
        Boolean customsResult = productCustomsService.saveOrUpdateBatch(productCustomsList);
        return logisticsResult && customsResult;
    }

    private List<String> handleCustoms(List<ProductCustomsEntity> productCustomsList) {
        if (CollectionUtils.isEmpty(productCustomsList)) {
            return Collections.emptyList();
        }
        //国家
        List<String> countryIdList = productCustomsList.stream().map(ProductCustomsEntity::getCountry).distinct().collect(Collectors.toList());
        //sku
        List<String> skuIdList = productCustomsList.stream().map(ProductCustomsEntity::getSkuId).distinct().collect(Collectors.toList());
        List<ProductDetailEntity> skuList = productDetailService.listByIds(skuIdList);
        List<DictCountryDTO.ListDTO> countryList = sysUserFeign.countryList();
        for (ProductCustomsEntity item : productCustomsList) {
            String country = item.getCountry();
            String countryName = countryList.stream().filter(c -> c.getId().equals(country)).map(DictCountryDTO.ListDTO::getNameCn).
                    findFirst().orElse("");
            item.setCountryName(countryName);
            String skuNo = skuList.stream().filter(s -> s.getId().equals(item.getSkuId())).
                    map(ProductDetailEntity::getSkuNo).findFirst().orElse("");
            item.setSkuNo(skuNo);
            BigDecimal taxRate = item.getTaxRate();
            taxRate = MathUtil.divide(taxRate, MathUtil.BigDecimal_100);
            item.setTaxRate(taxRate);
        }
        List<String> updateIdList = productCustomsList.stream().filter(c -> StringUtils.isNotEmpty(c.getId())).
                map(ProductCustomsEntity::getId).collect(Collectors.toList());
        List<ProductCustomsEntity> dbList = productCustomsService.listBySkuId(productCustomsList.get(0).getSkuId());
        return dbList.stream().filter(c -> !updateIdList.contains(c.getId())).map(ProductCustomsEntity::getId).collect(Collectors.toList());

    }

    /**
     * 处理物流产品数据
     *
     * @param productLogistics
     */
    private void handleProductLogistics(ProductLogisticsEntity productLogistics) {
        if (Objects.isNull(productLogistics)) {
            return;
        }
        //报关币种
        String declareCurrency = productLogistics.getDeclareCurrency();
        //目的国币种
        String destCurrency = productLogistics.getDestCurrency();

        List<String> currencyCodeList = Arrays.asList(declareCurrency, destCurrency);
        List<CurrencyDTO.ViewDTO> currencyList = sysUserFeign.listByCurrency(currencyCodeList);
        String declareCurrencySymbol = currencyList.stream().filter(c -> c.getId().equals(declareCurrency)).findFirst().
                map(CurrencyDTO.ViewDTO::getSymbol).orElse("");
        productLogistics.setDeclareCurrencySymbol(declareCurrencySymbol);

        String destCurrencySymbol = currencyList.stream().filter(c -> c.getId().equals(destCurrency)).findFirst().
                map(CurrencyDTO.ViewDTO::getSymbol).orElse("");
        productLogistics.setDestCurrencySymbol(destCurrencySymbol);
    }

    /**
     * 填充分页数据
     *
     * @param list
     * @return void
     * @author yl
     * @date 2023-11-06 17:33
     */
    private void fillPagingDb(List<LogisticsProductDTO.PagingVO> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        List<FindUserDTO> userList = sysUserFeign.getUserList();
        List<String> skuNoList = list.stream().map(LogisticsProductDTO.PagingVO::getSkuNo).collect(Collectors.toList());
        List<BomInfoEntity> bomSkuList = bomSkuService.listAllBomByParentSkuNos(skuNoList);
        for (LogisticsProductDTO.PagingVO item : list) {
            String skuNo = item.getSkuNo();
            Integer salesStatus = item.getSalesStatus();
            String salesStatusName = SaleStateEnum.getNameByCode(salesStatus);
            item.setSalesStatusName(salesStatusName);
            Integer approveStatus = item.getApproveStatus();
            String approveStatusName = ProductDetailStatusEnum.getName(approveStatus);
            item.setApproveStatusName(approveStatusName);
            //产品经理
            String chargeId = item.getChargeId();
            List<String> chargeIdList = Arrays.asList(chargeId.split(","));
            String chargeName = userList.stream().filter(u -> chargeIdList.contains(u.getUserId())).
                    map(FindUserDTO::getUserName).collect(Collectors.joining(","));
            item.setCategoryName(chargeName);
            BomInfoEntity bomInfo = bomSkuList.stream().filter(b -> b.getParentSkuNo().equals(skuNo)).
                    findFirst().orElse(null);
            item.setIsCombination(Objects.nonNull(bomInfo));
        }
    }
}
