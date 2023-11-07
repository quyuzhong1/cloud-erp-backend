package com.erp.server.plm.service.impl;/**
 * @author Lambda
 * @Classname LogisticsProductServiceImpl
 * @Description TODO
 * @Date 2023-11-06 12:28
 * @Created by yl
 */

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.plm.dto.LogisticsProductDTO;
import com.erp.model.plm.dto.ProductCustomsDTO;
import com.erp.model.plm.entity.BomInfoEntity;
import com.erp.model.plm.entity.ProductCustomsEntity;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.entity.ProductLogisticsEntity;
import com.erp.model.plm.enums.ProductDetailStateEnum;
import com.erp.model.plm.enums.ProductDetailStatusEnum;
import com.erp.model.plm.enums.SaleStateEnum;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.plm.mapper.ProductDetailMapper;
import com.erp.server.plm.service.BomSkuService;
import com.erp.server.plm.service.LogisticsProductService;
import com.erp.server.plm.service.ProductCustomsService;
import com.erp.server.plm.service.ProductLogisticsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
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
            BeanMapper.copyList(productCustomsList, ProductCustomsDTO.ViewDTO.class);
        }
        result.setCustomsList(customsList);
        return result;
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
