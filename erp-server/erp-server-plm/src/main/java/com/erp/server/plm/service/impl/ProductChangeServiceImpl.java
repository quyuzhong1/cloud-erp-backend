package com.erp.server.plm.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import com.erp.common.modules.workflow.dto.ProcessPassDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.entity.ProductChangeEntity;
import com.erp.model.plm.vo.BomVO;
import com.erp.model.plm.vo.ProductChangePagingVO;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.workflow.vo.MyToDoTaskVO;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.plm.constant.BomConstant;
import com.erp.server.plm.constant.SearchType;
import com.erp.server.plm.controller.AuditParamDTO;
import com.erp.server.plm.enums.BomStateEnum;
import com.erp.server.plm.enums.ProductChangeStateEnum;
import com.erp.server.plm.mapper.ProductChangeMapper;
import com.erp.server.plm.service.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.lang.reflect.Type;
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

    @Resource
    private CommonService commonService;

    @Resource
    private WorkflowFeign workflowFeign;

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
        String searchType = params.getSearchType();
        List<String>  changeIdList = new ArrayList<>();

        //当这个不为空的时候 表示可能要搜索 sku 或者 sku名称 或者bom 编号
        List<String> changeSearch = new ArrayList<>();
        if (StringUtils.isNotBlank(searchKeyword)) {
            changeSearch = baseMapper.getChangeSearchCondition(searchKeyword);
        }
        //待审核
        if (SearchType.WAIT_AUDIT.equals(searchType)) {
            String userId = commonService.getUserInfo().getUid();
            //获取我的待办信息
            List<MyToDoTaskVO> myToDoTasks = workflowFeign.getMyToDoTasks(userId);
            changeIdList = myToDoTasks.stream().map(MyToDoTaskVO::getBusinessTableId).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(changeIdList)) {
                IPage pageData = new Page();
                return new PagingVO(pageData);
            }

        }

        IPage pageData = baseMapper.paging(query, changeSearch,changeIdList);
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
        List<String> skuIdList = list.stream().filter(c -> changeSku.equals(c.getType())).
                map(ProductChangePagingVO::getSourceId).collect(Collectors.toList());

        List<SkuVO> skuList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(skuIdList)) {
            skuList = productDetailService.getSkuBySkuIds(skuIdList);
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
                SkuVO sku = skuList.stream().filter(s -> s.getSkuId().equals(sourceId))
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
    public List<ChangeInfoDTO> getChangeByType(String type, String searchKeyword) {
        String changeBom = BomConstant.CHANGE_BOM;
        String changeSku = BomConstant.CHANGE_SKU;
        if (changeBom.equals(type)) {
            return bomInfoService.getBomInfo(searchKeyword);
        }
        if (changeSku.equals(type)) {
            return productDetailService.getSku(searchKeyword);
        }
        return new ArrayList<>();
    }


    /**
     * 变更详情
     *
     * @param id
     * @return com.erp.model.plm.dto.ProductChangeDTO
     * @author yl
     * @date 2023-01-30 10:50
     */
    @Override
    public ProductChangeDTO details(String id) {
        try {
            ProductChangeDTO result = new ProductChangeDTO();
            //获取到变更信息
            ProductChangeEntity changeEntity = this.getById(id);
            if (Objects.isNull(changeEntity)) {
                throw new ServiceException(ApiError.ERROR_95105);
            }
            //获取到对应的 json
            String detailsJson = changeDetailsService.getDetailsJson(changeEntity.getId());
            result.setSourceId(result.getSourceId());
            String type = result.getType();
            result.setType(type);
            //对应就是bom
            if (BomConstant.CHANGE_BOM.equals(type)) {
                T bom = JSONObject.parseObject(detailsJson, (Type) BomDTO.class);
                result.setInfo(bom);
            }

            //对应就是sku
            if (BomConstant.CHANGE_SKU.equals(type)) {
                T sku = JSONObject.parseObject(detailsJson, (Type) ProductSmallestUnitDTO.class);
                result.setInfo(sku);
            }
            return result;
        } catch (Exception e) {

        }

        return null;

    }


    /**
     * 编辑 变更信息
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-01-30 11:54
     */
    @Override
    public Boolean edit(UpdateChangeDTO dto) {
        String id = dto.getId();
        //获取到变更信息
        ProductChangeEntity changeEntity = this.getById(id);
        if (Objects.isNull(changeEntity)) {
            throw new ServiceException(ApiError.ERROR_95105);
        }
        Integer state = changeEntity.getState();
        Integer waitAudit = ProductChangeStateEnum.WAIT_AUDIT.getState();
        //只有待审核才能编辑
        if (!waitAudit.equals(state)) {
            throw new ServiceException(ApiError.ERROR_95109);
        }
        String type = dto.getType();
        String changeBom = BomConstant.CHANGE_BOM;
        Boolean isBom = changeBom.equals(type);
        //数据库的类型
        String dbType = changeEntity.getType();
        //数据库的bom 表id
        String dbSourceId = changeEntity.getSourceId();
        Boolean dbIsBom = changeBom.equals(dbType);
        //新的
        String sourceId = dto.getSourceId();
        changeEntity.setSourceId(sourceId);
        changeEntity.setType(dto.getType());
        Boolean result = this.updateById(changeEntity);
        if (result) {
            /**
             * 如果变更成功 如果是bom
             * 那么原来老的 bom 状态要改回来
             * bom 要改状态
             *
             */
            changeDetailsService.saveChangeDetails(id, dto.getDetailsJson());

            if (isBom) {
                bomInfoService.updateState(sourceId, BomStateEnum.ARCHIVE_CHANGE_ING.getState());
            }
            //老的bom 状态要改回来
            if (dbIsBom) {
                bomInfoService.updateState(dbSourceId, BomStateEnum.AUDIT_PASS.getState());
            }
        }
        return result;
    }


    /**
     * 变更审核通过
     *
     * @param dto
     * @return void
     * @author yl
     * @date 2023-01-30 14:03
     */
    @Override
    public void approvalPass(AuditParamDTO dto) {
        String id = dto.getId();
        //获取到变更信息
        ProductChangeEntity changeEntity = this.getById(id);
        if (Objects.isNull(changeEntity)) {
            throw new ServiceException(ApiError.ERROR_95105);
        }
        changeEntity.setState(ProductChangeStateEnum.AUDIT_ING.getState());
        if (StringUtils.isNotBlank(dto.getComment())) {
            changeEntity.setRemark(dto.getComment());
        }
        this.updateById(changeEntity);
    }


    /**
     * 变更审核不通过
     * 不通过要停止流程吗
     *
     * @param dto
     * @return void
     * @author yl
     * @date 2023-01-30 14:10
     */
    @Override
    public void approvalNoPass(AuditParamDTO dto) {
        String id = dto.getId();
        //获取到变更信息
        ProductChangeEntity changeEntity = this.getById(id);
        if (Objects.isNull(changeEntity)) {
            throw new ServiceException(ApiError.ERROR_95105);
        }
        changeEntity.setState(ProductChangeStateEnum.AUDIT_NO_PASS.getState());
        if (StringUtils.isNotBlank(dto.getComment())) {
            changeEntity.setRemark(dto.getComment());
        }
        this.updateById(changeEntity);
    }


    /**
     * 流程最终通过后的
     * 操作
     *
     * @param dto
     * @return void
     * @author yl
     * @date 2023-01-30 16:41
     */
    @Override
    @Transactional
    public void processPass(ProcessPassDTO dto) {
        //从流程那边获取到具体业务表id
        String id = dto.getBusinessTableId();
        if (StringUtils.isNotBlank(id)) {
            //获取到变更信息
            ProductChangeEntity change = this.getById(id);
            String type = change.getType();
            if (change != null) {
                //获取到对应的 json
                String detailsJson = changeDetailsService.getDetailsJson(change.getId());
                if (StringUtils.isNotBlank(detailsJson)) {
                    //对应就是bom
                    if (BomConstant.CHANGE_BOM.equals(type)) {
                        BomDTO bom = JSONObject.parseObject(detailsJson, BomDTO.class);
                        //变更bom
                        bomInfoService.changeBom(bom);
                    }

                    //对应就是sku
                    if (BomConstant.CHANGE_SKU.equals(type)) {
                        ProductSmallestUnitDTO sku = JSONObject.parseObject(detailsJson, ProductSmallestUnitDTO.class);
                        productDetailService.changeSku(sku);
                    }
                }
            }

        }
    }
}
