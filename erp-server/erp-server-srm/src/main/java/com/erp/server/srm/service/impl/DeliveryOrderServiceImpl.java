package com.erp.server.srm.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.constant.EnumMessage;
import com.erp.model.scm.dto.PurchaseOrderDTO;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.srm.dto.DeliveryOrderDetailDTO;
import com.erp.model.srm.entity.DeliveryOrderDetailEntity;
import com.erp.model.srm.entity.DeliveryOrderEntity;
import com.erp.model.srm.enums.DeliveryOrderConfirmStatusEnum;
import com.erp.model.srm.enums.DeliveryOrderEnum;
import com.erp.model.sys.entity.SysUserWechatEntity;
import com.erp.server.srm.convert.DeliveryOrderConverter;
import com.erp.server.srm.mapper.DeliveryOrderMapper;
import com.erp.server.srm.service.DeliveryOrderDetailService;
import com.erp.server.srm.service.DeliveryOrderService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.srm.service.OperateLogService;
import com.erp.server.srm.service.CommonService;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import com.common.core.controller.vo.ApiResult;
import cn.hutool.core.util.ObjectUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.srm.dto.DeliveryOrderDTO;

import java.util.*;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;

import javax.annotation.Resource;

/**
 * <p>
 * 送货单 服务实现类
 * </p>
 *
 * @author lrp
 * @since 2024-01-12
 */
@Slf4j
@Service
public class DeliveryOrderServiceImpl extends SuperServiceImpl<DeliveryOrderMapper, DeliveryOrderEntity> implements DeliveryOrderService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;

    @Resource
    private DeliveryOrderDetailService detailService;

    @Override
    public PagingVO<DeliveryOrderDTO.ListDTO> paging(PagingDTO<DeliveryOrderDTO.ParamDTO> dto) {
        Page<DeliveryOrderDTO.ListDTO> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        dto.getParams().setSupplierId(commonService.getSupplierEntity().getId());
        IPage<DeliveryOrderDTO.ListDTO> pageData = this.baseMapper.paging(query, dto.getParams());
        pageData.getRecords().forEach(v-> v.setReceiptStatus(EnumMessage.getNameByCode(DeliveryOrderEnum.ReceiptStatusEnum.class,v.getReceiptStatus())));
        return new PagingVO<>(pageData);
    }

    @Override
    public List<DeliveryOrderDTO.TabListDTO> tabList() {
        List<DeliveryOrderDTO.TabListDTO> result = new ArrayList<>();
        List<DeliveryOrderDTO.StatusListDTO> statusListDTOList =  this.baseMapper.tabList(commonService.getSupplierEntity().getId());
        //ALL
        DeliveryOrderDTO.TabListDTO allDto = DeliveryOrderDTO.TabListDTO.builder()
                .searchType(DeliveryOrderEnum.SearchTypeEnum.ALL.getCode())
                .count(statusListDTOList.stream().mapToInt(DeliveryOrderDTO.StatusListDTO::getCount).sum())
                .build();
        result.add(allDto);
        //待收货-未打印
        DeliveryOrderDTO.TabListDTO dto2 = DeliveryOrderDTO.TabListDTO.builder()
                .searchType(DeliveryOrderEnum.SearchTypeEnum.WAIT_RECEIVE_AND_PRINT.getCode())
                .count(statusListDTOList.stream().filter(v->v.getReceiptStatus().equals(DeliveryOrderEnum.ReceiptStatusEnum.WAIT_CONFIRMED.getCode())
                && !v.getIsPrint()).mapToInt(DeliveryOrderDTO.StatusListDTO::getCount).sum())
                .build();
        result.add(dto2);
        //待收货-已打印
        DeliveryOrderDTO.TabListDTO dto3 = DeliveryOrderDTO.TabListDTO.builder()
                .searchType(DeliveryOrderEnum.SearchTypeEnum.WAIT_RECEIVE_AND_PRINTED.getCode())
                .count(statusListDTOList.stream().filter(v->v.getReceiptStatus().equals(DeliveryOrderEnum.ReceiptStatusEnum.WAIT_CONFIRMED.getCode())
                        && v.getIsPrint()).mapToInt(DeliveryOrderDTO.StatusListDTO::getCount).sum())
                .build();
        result.add(dto3);
        //已收货
        DeliveryOrderDTO.TabListDTO dto4 = DeliveryOrderDTO.TabListDTO.builder()
                .searchType(DeliveryOrderEnum.SearchTypeEnum.RECEIVED.getCode())
                .count(statusListDTOList.stream().filter(v->v.getReceiptStatus().equals(DeliveryOrderEnum.ReceiptStatusEnum.CONFIRMED.getCode())).mapToInt(DeliveryOrderDTO.StatusListDTO::getCount).sum())
                .build();
        result.add(dto4);
        //收发差异
        DeliveryOrderDTO.TabListDTO dto5 = DeliveryOrderDTO.TabListDTO.builder()
                .searchType(DeliveryOrderEnum.SearchTypeEnum.QTY_DIFFERENCE.getCode())
                .count(statusListDTOList.stream().filter(v->v.getReceiptStatus().equals(DeliveryOrderEnum.ReceiptStatusEnum.CONFIRMED.getCode())
                        && v.getQtyDifferences()).mapToInt(DeliveryOrderDTO.StatusListDTO::getCount).sum())
                .build();
        result.add(dto5);
        return result;
    }

    @Override
    public DeliveryOrderDTO.ViewDTO view(String id) {

        DeliveryOrderEntity entity = super.getById(id);
        Optional.ofNullable(entity).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "送货单"));
        SupplierEntity supplier = commonService.getSupplierEntity();
        if(!supplier.getId().equals(entity.getSupplierId())){
            throw new ServiceException(ApiError.ERROR_96002);
        }
        List<DeliveryOrderDetailEntity> detailEntityList = detailService.listByMainId(entity.getId());
        return DeliveryOrderConverter.INSTANCE.viewConvert(entity,detailEntityList);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(DeliveryOrderDTO.AddDTO addDTO) {
        DeliveryOrderEntity deliveryOrderEntity = new DeliveryOrderEntity();
        BeanMapperUtils.copy(addDTO, deliveryOrderEntity);

        // 数据处理
        handleData(deliveryOrderEntity,false);

        log.info("开始新增送货单");
        // 生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_SHD);
        deliveryOrderEntity.setCode(code);
        boolean save = super.save(deliveryOrderEntity);
        if (!save) {
            throw new ServiceException("送货单保存失败");
        }
        //保存明细
        detailService.add(addDTO.getDetailList(),deliveryOrderEntity.getId());
        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", commonService.getUserInfo().getUserName(), "送货单", deliveryOrderEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.DELIVERY_ORDER.getCode(), deliveryOrderEntity.getId(), "新增操作");

        return new BaseResultDTO.AddDTO(deliveryOrderEntity.getId(), code);
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(DeliveryOrderDTO.UpdateDTO updateDTO) {
        DeliveryOrderEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "送货单"));
        DeliveryOrderEntity deliveryOrderEntity = BeanMapperUtils.map(DeliveryOrderEntity.class, updateDTO);
        // 数据处理
        handleData(deliveryOrderEntity,true);
        log.info("编辑 开始修改送货单数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(deliveryOrderEntity);
        if (!save) {
            throw new ServiceException("送货单保存失败");
        }
        // 记录主单操作日志
        log.info("编辑 开始记录送货单日志数据，单号：【{}】", deliveryOrderEntity.getCode());
        String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), deliveryOrderEntity.getCode(), "送货单");
        operateLogService.addModuleOperateLogByObj(old, deliveryOrderEntity, ModuleTypeEnum.DELIVERY_ORDER.getCode(), deliveryOrderEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public Integer countByPrint(String supplierId, boolean isPrint) {
        LambdaQueryWrapper<DeliveryOrderEntity> queryWrapper = this.getDefaultWrapper(supplierId);
        queryWrapper.eq(DeliveryOrderEntity::getIsPrint, isPrint);
        return this.count(queryWrapper);
    }

    @Override
    public Integer countByReceiveStatus(String supplierId, String status) {
        LambdaQueryWrapper<DeliveryOrderEntity> queryWrapper = this.getDefaultWrapper(supplierId);
        queryWrapper.eq(DeliveryOrderEntity::getReceiptStatus, status);
        return this.count(queryWrapper);
    }

    private LambdaQueryWrapper<DeliveryOrderEntity> getDefaultWrapper(String supplierId) {
        return new LambdaQueryWrapper<DeliveryOrderEntity>()
                .eq(DeliveryOrderEntity::getSupplierId, supplierId);
    }

    /**
     * 新增修改处理数据
     */
    private void handleData(DeliveryOrderEntity deliveryOrderEntity,Boolean isUpdate) {
        if(StringUtils.isBlank(deliveryOrderEntity.getReceiptStatus()) && !isUpdate){
            deliveryOrderEntity.setReceiptStatus(DeliveryOrderConfirmStatusEnum.WAIT_CONFIRM.getCode());
        }
    }
}
