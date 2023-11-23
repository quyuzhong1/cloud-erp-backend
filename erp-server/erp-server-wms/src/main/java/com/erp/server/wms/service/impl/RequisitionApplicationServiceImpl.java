package com.erp.server.wms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.dto.ProductDetailShowDTO;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.FbaDeliveryDTO;
import com.erp.model.wms.dto.RequisitionApplicationDetailDTO;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.RequisitionApplicationStatusEnum;
import com.erp.model.wms.enums.RequisitionApplicationTypeEnum;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.convert.RequisitionApplicationConverter;
import com.erp.server.wms.mapper.RequisitionApplicationMapper;
import com.erp.server.wms.service.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.RequisitionApplicationDTO;
import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;

import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 要货申请单 服务实现类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-11-16
 */
@Slf4j
@Service
public class RequisitionApplicationServiceImpl extends SuperServiceImpl<RequisitionApplicationMapper, RequisitionApplicationEntity> implements RequisitionApplicationService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;
    @Autowired
    private RequisitionApplicationDetailService requisitionApplicationDetailService;
    @Autowired
    private PlmTaskFeign plmTaskFeign;
    @Autowired
    private InventoryService inventoryService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(RequisitionApplicationDTO.AddDTO addDTO) {
        RequisitionApplicationEntity requisitionApplicationEntity = new RequisitionApplicationEntity();
        BeanMapperUtils.copy(addDTO, requisitionApplicationEntity);

        // 数据处理
        handleData(requisitionApplicationEntity);

        log.info("开始新增要货申请单");
        // 生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_FHJH);
        requisitionApplicationEntity.setCode(code);
        boolean save = super.save(requisitionApplicationEntity);
        if(!save) {
            throw new ServiceException("要货申请单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", commonService.getUserInfo().getUserName(), "要货申请单" , requisitionApplicationEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.REQUISITION_APPLICATION.getCode(), requisitionApplicationEntity.getId(), "新增操作");
        // 新增明细
        requisitionApplicationDetailService.add(addDTO, requisitionApplicationEntity.getId());
        return new BaseResultDTO.AddDTO(requisitionApplicationEntity.getId(), code);
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(RequisitionApplicationDTO.UpdateDTO updateDTO) {
        RequisitionApplicationEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "要货申请单"));
        RequisitionApplicationEntity requisitionApplicationEntity =  BeanMapperUtils.map(RequisitionApplicationEntity.class, updateDTO);

        // 数据处理
        handleData(requisitionApplicationEntity);
        log.info("编辑 开始修改要货申请单数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(requisitionApplicationEntity);
        if(!save) {
            throw new ServiceException("要货申请单保存失败");
        }
        // 修改明细数据（包含增删改）
        requisitionApplicationDetailService.update(updateDTO, requisitionApplicationEntity.getId());

        // 记录主单操作日志
        log.info("编辑 开始记录要货申请单日志数据，单号：【{}】", requisitionApplicationEntity.getCode());
        String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), requisitionApplicationEntity.getCode(), "要货申请单");
        operateLogService.addModuleOperateLogByObj(old, requisitionApplicationEntity, ModuleTypeEnum.REQUISITION_APPLICATION.getCode(), requisitionApplicationEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public List<RequisitionApplicationDTO.TabListDTO> tabList(PermissionsDTO param) {
        FbaDeliveryDTO.PagingParamDTO searchParam = new FbaDeliveryDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<RequisitionApplicationDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        // 获取状态列表
        List<String> statusList = RequisitionApplicationStatusEnum.getStatusList();
        // 不存在的状态赋值为0
        List<String> existStatusList = list.stream().map(RequisitionApplicationDTO.TabListDTO::getTabFlag).collect(Collectors.toList());
        statusList.parallelStream().forEach(status -> {
            if(!existStatusList.contains(status)) {
                list.add(new RequisitionApplicationDTO.TabListDTO(status, 0));
            }
        });
        list.add(new RequisitionApplicationDTO.TabListDTO("all", list.stream().mapToInt(RequisitionApplicationDTO.TabListDTO::getCount).sum()));
        // 计算合计数量
        return list;
    }

    @Override
    public PagingVO<RequisitionApplicationDTO.ListDTO> paging(PagingDTO<RequisitionApplicationDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<RequisitionApplicationDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public RequisitionApplicationDTO.ViewDTO view(String id) {
        //发货单主信息
        RequisitionApplicationEntity applicationEntity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到要货申请单数据"));
        RequisitionApplicationDTO.ViewDTO data = BeanMapperUtils.map(RequisitionApplicationDTO.ViewDTO.class, applicationEntity);
        //发货单详情
        List<RequisitionApplicationDetailEntity> requisitionApplicationDetailEntities = requisitionApplicationDetailService.listByMainIds(Arrays.asList(id));
        // 数据填充处理
        fillOne(data, requisitionApplicationDetailEntities);
        return data;
    }

    @Override
    public BatchResultDTO submit(String id) {
        return null;
    }

    @Override
    public List<RequisitionApplicationDTO.handleListDTO> handleList(List<String> ids) {
        return null;
    }

    @Override
    public Boolean handleSave(List<RequisitionApplicationDTO.handleListDTO> list) {
        return null;
    }

    @Override
    public List<RequisitionApplicationDTO.finishListDTO> finishList(List<String> ids) {
        return null;
    }

    @Override
    public Boolean finishSave(List<RequisitionApplicationDTO.finishListDTO> list) {
        return null;
    }

    @Override
    public List<RequisitionApplicationDTO.printPickingViewDTO> printPickingView(List<String> ids) {
        return null;
    }

    @Override
    public BatchResultDTO cancelProcess(String id) {
        return null;
    }

    @Override
    public Boolean exportExcel(RequisitionApplicationDTO.PagingParamDTO dto, HttpServletResponse response) {
        return null;
    }

    @Override
    public BatchResultDTO delete(String id) {
        return null;
    }

    @Override
    public List<RequisitionApplicationEntity> listBySourceIds(List<String> sourceIds) {
        return lambdaQuery().in(RequisitionApplicationEntity::getSourceId, sourceIds).list();
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(RequisitionApplicationEntity requisitionApplicationEntity) {

    }

    /**
     * 详情字段处理
     */
    private void fillOne(RequisitionApplicationDTO.ViewDTO data, List<RequisitionApplicationDetailEntity> detailList) {
        //查询产品信息
        List<String> skuIdList = detailList.stream().map(req -> req.getSkuId()).distinct().collect(Collectors.toList());
        List<SkuVO> skuVOList = plmTaskFeign.getSkuInfoByIds(skuIdList);

        //获取子SKU集合
        List<BomChildrenSkuDTO> bomChildrenSkuDTOS = plmTaskFeign.listBomChildBySkuIds(skuIdList);

        //来源类型中文
        data.setSourceTypeName(SourceTypeEnum.getName(data.getSourceType()));
        //要货类型中文
        data.setTypeName(RequisitionApplicationTypeEnum.getName(data.getType()));
        //单据状态中文
        data.setStatus(RequisitionApplicationStatusEnum.getName(data.getStatus()));

        //详情字段设置
        List<RequisitionApplicationDetailDTO.ViewDTO> viewDetailList = new ArrayList<>();
        for (RequisitionApplicationDetailEntity detailEntity : detailList) {
            RequisitionApplicationDetailDTO.ViewDTO detailView = RequisitionApplicationConverter.INSTANCE.radEntityToRadDto(detailEntity);

            //产品信息
            SkuVO skuVO = skuVOList.stream().filter(req -> req.getSkuId().equals(detailEntity.getSkuId())).findFirst().orElse(new SkuVO());
            detailView.setProductName(skuVO.getSkuName());
            detailView.setImageUrl(skuVO.getSkuImagesUrl());
            detailView.setUsableQty(inventoryService.getUsableInventoryTotal(data.getRequisitionWarehouseId(), skuVO.getSkuId()));

            //查询sku是否存在子SKU
            List<BomChildrenSkuDTO> sonSkuList = bomChildrenSkuDTOS.stream().filter(req -> req.getParentSkuId().equals(detailEntity.getSkuId())).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(sonSkuList)) {
                detailView.setIsCombination(Boolean.TRUE);
            } else {
                detailView.setIsCombination(Boolean.FALSE);
            }

            viewDetailList.add(detailView);
        }

        data.setDetailList(viewDetailList);
    }

    /**
     * 分页查询数据处理
     * @param list
     */
    private void fillList(List<RequisitionApplicationDTO.ListDTO> list) {
        //查询产品信息
        List<String> skuIdList = list.stream().map(req -> req.getSkuId()).distinct().collect(Collectors.toList());
        List<SkuVO> skuVOList = plmTaskFeign.getSkuInfoByIds(skuIdList);
        for (RequisitionApplicationDTO.ListDTO listDTO : list) {
            //产品信息
            SkuVO skuVO = skuVOList.stream().filter(req -> req.getSkuId().equals(listDTO.getSkuId())).findFirst().orElse(new SkuVO());
            listDTO.setProductName(skuVO.getSkuName());
            //状态中文
            listDTO.setStatusName(RequisitionApplicationStatusEnum.getName(listDTO.getStatus()));
            //要货类型中文
            listDTO.setTypeName(RequisitionApplicationTypeEnum.getName(listDTO.getType()));
        }
    }
}
