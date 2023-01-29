package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.common.dto.base.BaseIdDTO;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import com.erp.common.vo.PagingVO;
import com.erp.model.plm.dto.AddChangeDTO;
import com.erp.model.plm.dto.SearchPagingDTO;
import com.erp.model.plm.entity.ProductChangeEntity;
import com.erp.model.plm.vo.BomVO;
import com.erp.model.plm.vo.ProductChangePagingVO;
import com.erp.model.plm.vo.SkuVO;
import com.erp.server.plm.constant.BomConstant;
import com.erp.server.plm.enums.BomStateEnum;
import com.erp.server.plm.enums.ProductChangeStateEnum;
import com.erp.server.plm.mapper.ProductChangeMapper;
import com.erp.server.plm.service.BomInfoService;
import com.erp.server.plm.service.ProductChangeDetailsService;
import com.erp.server.plm.service.ProductChangeService;
import com.erp.server.plm.service.ProductDetailService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 变更信息表(ProductChange)表服务实现类
 *
 * @author yl
 * @since 2023-01-11 14:05:03
 */
@Service
public class ProductChangeServiceImpl extends ServiceImpl<ProductChangeMapper, ProductChangeEntity> implements ProductChangeService {


    @Resource
    private ProductChangeDetailsService changeDetailsService;

    @Resource
    private BomInfoService bomInfoService;


    @Resource
    private ProductDetailService productDetailService;

    /**
     * 添加变更
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-01-14 15:02
     */
    @Override
    @Transactional
    public Boolean add(AddChangeDTO dto) {
        ProductChangeEntity change = new ProductChangeEntity();
        String type = dto.getType();
        String changeBom = BomConstant.CHANGE_BOM;
        Boolean isBom = changeBom.equals(type);
        String sourceId = dto.getSourceId();
        if (isBom) {
            //检查能否变更 只有归档才可以
            bomInfoService.checkIfChange(sourceId);
        }
        BeanMapper.copy(dto, change);
        String id = IdWorker.getIdStr();
        change.setId(id);
        Boolean saveResult = this.save(change);
        if (saveResult) {
            changeDetailsService.saveChangeDetails(id, dto.getDetailsJson());
            //如果变更成功 如果是bom 要改状态
            if (isBom) {
                bomInfoService.updateState(sourceId, BomStateEnum.ARCHIVE_CHANGE_ING.getState());
            }
        }
        return saveResult;
    }


    /**
     * 分页获取变更信息
     *
     * @param dto
     * @return com.erp.common.vo.PagingVO<java.util.List < com.erp.model.plm.vo.ProductChangePagingVO>>
     * @author yl
     * @date 2023-01-28 11:50
     */
    @Override
    public PagingVO<List<ProductChangePagingVO>> paging(PagingDTO<SearchPagingDTO> dto) {
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        SearchPagingDTO params = dto.getParams();
        String searchKeyword = params.getSearchKeyword();
        //当这个不为空的时候 表示可能要搜索 sku 或者 sku名称 或者bom 编号
        List<String> changeSearch = new ArrayList<>();
        if (StringUtils.isNotBlank(searchKeyword)) {
            changeSearch = baseMapper.getChangeSearchCondition(searchKeyword);
        }

        IPage pageData = baseMapper.paging(query, changeSearch);
        List<ProductChangePagingVO> list = pageData.getRecords();
        String changeBom = BomConstant.CHANGE_BOM;
        String changeSku = BomConstant.CHANGE_SKU;
        //获取到类型是bom 的 源 id
        List<String> bomIdList = list.stream().filter(c -> changeBom.equals(c.getType())).
                map(ProductChangePagingVO::getSourceId).collect(Collectors.toList());
        List<BomVO> bomList = new ArrayList<>();
        //当不为空的时候表示有 bom 的
        if (CollectionUtils.isNotEmpty(bomIdList)) {
            bomList = bomInfoService.getByIds(bomIdList);
        }
        //获取到类型是sku 的 源 id
        List<String> skuNoList = list.stream().filter(c -> changeSku.equals(c.getType())).
                map(ProductChangePagingVO::getSourceId).collect(Collectors.toList());

        List<SkuVO> skuList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(skuNoList)) {
            skuList = productDetailService.getSkuBySkuNos(skuNoList);
        }

        for (ProductChangePagingVO item : list) {
            String type = item.getType();
            String sourceId = item.getSourceId();
            //如果是bom
            if (changeBom.equals(type)) {
                BomVO bom = bomList.stream().filter(b -> b.getBomId().equals(sourceId))
                        .findFirst().orElse(null);
                if (bom != null) {
                    item.setChangeSourceNo(bom.getSerialNumber());
                }
            }
            //如果是sku
            if (changeSku.equals(type)) {
                SkuVO sku = skuList.stream().filter(s -> s.getSkuNo().equals(sourceId))
                        .findFirst().orElse(null);
                if (sku != null) {
                    item.setChangeSourceNo(sku.getSkuNo());
                    item.setChangeSourceName(sku.getSkuName());
                }
            }

        }

        return new PagingVO(pageData);
    }


    /**
     * 作废
     *
     * @param productChangeId
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-01-28 16:41
     */
    @Override
    public Boolean cancellation(String productChangeId) {
        ProductChangeEntity entity = this.getById(productChangeId);
        if (Objects.isNull(entity)) {
            throw new ServiceException(ApiError.ERROR_95105);
        }
        Integer state = entity.getState();
        //待审核
        Integer waitAudit = ProductChangeStateEnum.WAIT_AUDIT.getState();
        //审核不通过
        Integer auditNoPassState = ProductChangeStateEnum.AUDIT_NO_PASS.getState();
        //当不等于他们的时候
        if (!waitAudit.equals(state) && !auditNoPassState.equals(state)) {
            throw new ServiceException(ApiError.ERROR_95106);
        }
        entity.setState(ProductChangeStateEnum.CANCELLATION.getState());
        return this.updateById(entity);
    }


    /**
     * 根据变更的类型 获取到对应的数据
     *
     * @param type
     * @return java.util.List<com.erp.common.dto.base.BaseIdDTO>
     * @author yl
     * @date 2023-01-28 17:02
     */
    @Override
    public List<BaseIdDTO> getChangeByType(String type, String searchKeyword) {
        String changeBom = BomConstant.CHANGE_BOM;
        String changeSku = BomConstant.CHANGE_SKU;
        if (changeBom.equals(type)) {
            return bomInfoService.getBomInfo(searchKeyword);
        }
        if (changeSku.equals(type)) {
            return productDetailService.getSku(searchKeyword);
        }
        return null;
    }
}
