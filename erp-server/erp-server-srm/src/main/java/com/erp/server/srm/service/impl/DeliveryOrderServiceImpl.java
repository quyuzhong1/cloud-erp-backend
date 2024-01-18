package com.erp.server.srm.service.impl;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.constant.EnumMessage;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.srm.dto.DeliveryOrderDTO;
import com.erp.model.srm.dto.DeliveryOrderDetailDTO;
import com.erp.model.srm.entity.DeliveryOrderDetailEntity;
import com.erp.model.srm.entity.DeliveryOrderEntity;
import com.erp.model.srm.enums.DeliveryOrderEnum;
import com.erp.server.srm.convert.DeliveryOrderConverter;
import com.erp.server.srm.mapper.DeliveryOrderMapper;
import com.erp.server.srm.service.CommonService;
import com.erp.server.srm.service.DeliveryOrderDetailService;
import com.erp.server.srm.service.DeliveryOrderService;
import com.erp.server.srm.service.OperateLogService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

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
        IPage<DeliveryOrderDTO.ListDTO> pageData = this.baseMapper.paging(query, dto.getParams());
        pageData.getRecords().forEach(v-> v.setReceiptStatusName(EnumMessage.getNameByCode(DeliveryOrderEnum.ReceiptStatusEnum.class,v.getReceiptStatus())));
        return new PagingVO<>(pageData);
    }

    @Override
    public List<DeliveryOrderDTO.TabListDTO> tabList(List<String> supplierIdList) {
        List<DeliveryOrderDTO.TabListDTO> result = new ArrayList<>();
        List<DeliveryOrderDTO.StatusListDTO> statusListDTOList =  this.baseMapper.tabList(supplierIdList);
        //ALL
        DeliveryOrderDTO.TabListDTO allDto = DeliveryOrderDTO.TabListDTO.builder()
                .searchType(DeliveryOrderEnum.SearchTypeEnum.ALL.getCode())
                .count(statusListDTOList.stream().mapToInt(DeliveryOrderDTO.StatusListDTO::getCount).sum())
                .build();
        result.add(allDto);
        //待收货-未打印
        DeliveryOrderDTO.TabListDTO dto2 = DeliveryOrderDTO.TabListDTO.builder()
                .searchType(DeliveryOrderEnum.SearchTypeEnum.WAIT_RECEIVE_AND_PRINT.getCode())
                .count(statusListDTOList.stream().filter(v->(v.getReceiptStatus().equals(DeliveryOrderEnum.ReceiptStatusEnum.WAIT_CONFIRMED.getCode())||StringUtils.isBlank(v.getReceiptStatus()))
                && !v.getIsPrint()).mapToInt(DeliveryOrderDTO.StatusListDTO::getCount).sum())
                .build();
        result.add(dto2);
        //待收货-已打印
        DeliveryOrderDTO.TabListDTO dto3 = DeliveryOrderDTO.TabListDTO.builder()
                .searchType(DeliveryOrderEnum.SearchTypeEnum.WAIT_RECEIVE_AND_PRINTED.getCode())
                .count(statusListDTOList.stream().filter(v->(v.getReceiptStatus().equals(DeliveryOrderEnum.ReceiptStatusEnum.WAIT_CONFIRMED.getCode())||StringUtils.isBlank(v.getReceiptStatus()))
                        && v.getIsPrint()).mapToInt(DeliveryOrderDTO.StatusListDTO::getCount).sum())
                .build();
        result.add(dto3);
        //待收货
        DeliveryOrderDTO.TabListDTO dto6 = DeliveryOrderDTO.TabListDTO.builder()
                .searchType(DeliveryOrderEnum.SearchTypeEnum.WAIT_RECEIVE.getCode())
                .count(statusListDTOList.stream().filter(v->(v.getReceiptStatus().equals(DeliveryOrderEnum.ReceiptStatusEnum.WAIT_CONFIRMED.getCode())||StringUtils.isBlank(v.getReceiptStatus()))).mapToInt(DeliveryOrderDTO.StatusListDTO::getCount).sum())
                .build();
        result.add(dto6);
        //已收货
        DeliveryOrderDTO.TabListDTO dto4 = DeliveryOrderDTO.TabListDTO.builder()
                .searchType(DeliveryOrderEnum.SearchTypeEnum.RECEIVED.getCode())
                .count(statusListDTOList.stream().filter(v->v.getReceiptStatus().equals(DeliveryOrderEnum.ReceiptStatusEnum.CONFIRMED.getCode())).mapToInt(DeliveryOrderDTO.StatusListDTO::getCount).sum())
                .build();
        result.add(dto4);
        //收发差异
        DeliveryOrderDTO.TabListDTO dto5 = DeliveryOrderDTO.TabListDTO.builder()
                .searchType(DeliveryOrderEnum.SearchTypeEnum.QTY_DIFFERENCE.getCode())
                .count(statusListDTOList.stream().filter(v->v.getReceiptStatus().equals(DeliveryOrderEnum.ReceiptStatusEnum.CONFIRMED.getCode())).mapToInt(DeliveryOrderDTO.StatusListDTO::getQtyDifferences).sum())
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

    @Override
    public List<DeliveryOrderDTO.PrintDTO> print(List<String> ids) {
        List<DeliveryOrderEntity> entityList = this.lambdaQuery().in(DeliveryOrderEntity::getId, ids).list();
        List<DeliveryOrderDTO.PrintDTO> printDTOList = BeanMapperUtils.copyList(DeliveryOrderDTO.PrintDTO.class, entityList);
        Map<String,List<DeliveryOrderDetailDTO.PrintDTO>> detailEntityMap = detailService.mapPrintByMainIds(ids);
        printDTOList.forEach(v-> Optional.ofNullable(detailEntityMap.get(v.getId()))
                .ifPresent(detailList -> {
                    detailList.forEach(detail -> detail.setCode(v.getSourceCode()));
                    v.setDetailPrintList(detailList);
                }));
        return printDTOList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<BatchResultDTO> cancelPrint(List<String> ids) {
        List<BatchResultDTO> resultDTOList = new ArrayList<>();
        List<DeliveryOrderEntity> deliveryOrderEntityList = this.lambdaQuery().in(DeliveryOrderEntity::getId, ids).list();
        List<DeliveryOrderEntity> updateList = new ArrayList<>();
        for(DeliveryOrderEntity deliveryOrderEntity : deliveryOrderEntityList){
            BatchResultDTO resultDTO = new BatchResultDTO();
            resultDTO.setCode(deliveryOrderEntity.getCode());
            resultDTO.setId(deliveryOrderEntity.getId());
            if(!deliveryOrderEntity.getIsPrint() || StringUtils.isNotBlank(deliveryOrderEntity.getReceiptStatus())){
                resultDTO.setSuccess(false);
                resultDTO.setMsg("存在未打印或者收货状态不为空的送货单，取消打印失败");
            }else{
                resultDTO.setSuccess(true);
                deliveryOrderEntity.setIsPrint(false);
                updateList.add(deliveryOrderEntity);
            }
            resultDTOList.add(resultDTO);
        }
        if(CollectionUtils.isNotEmpty(updateList)){
            if(!this.updateBatchById(updateList)){
                throw new ServiceException("更新送货单打印状态失败");
            }
        }
        return resultDTOList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(List<String> ids) {
        if(CollectionUtils.isEmpty(ids)){
            return true;
        }
        List<DeliveryOrderEntity> deliveryOrderEntityList = this.lambdaQuery().in(DeliveryOrderEntity::getId, ids).list();
        if(deliveryOrderEntityList.stream().anyMatch(v->StringUtils.isNotBlank(v.getReceiveCode()))){
            throw new ServiceException("已有送货单生成收货单，无法删除");
        }
        if(!this.removeByIds(ids)){
            throw new ServiceException("送货单删除失败");
        }
        if(!detailService.deleteByMainIds(ids)){
            throw new ServiceException("送货单明细删除失败");
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean confirmPrint(List<String> ids) {
        List<DeliveryOrderEntity> entityList = this.lambdaQuery().in(DeliveryOrderEntity::getId, ids).list();
        entityList.forEach(v->v.setIsPrint(true));
        if(!this.updateBatchById(entityList)){
            throw new ServiceException("更新打印状态失败");
        }
        return true;
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
        //更新明细
        if(!detailService.update(updateDTO.getDetailList(),deliveryOrderEntity.getId())){
            throw new ServiceException("送货单明细保存失败");
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

    }
}
