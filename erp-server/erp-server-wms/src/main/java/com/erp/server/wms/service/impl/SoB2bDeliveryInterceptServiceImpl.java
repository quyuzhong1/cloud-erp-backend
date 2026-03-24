package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.enums.OrderTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.SoB2bDeliveryInterceptDTO;
import com.erp.model.wms.dto.SoB2bDeliveryInterceptDetailDTO;
import com.erp.model.wms.entity.SoB2bDeliveryInterceptDetailEntity;
import com.erp.model.wms.entity.SoB2bDeliveryInterceptEntity;
import com.erp.model.wms.entity.SoOutstockEntity;
import com.erp.model.wms.enums.CancelStatusEnum;
import com.erp.model.wms.enums.HandleResultEnum;
import com.erp.model.wms.enums.InterceptStatusEnum;
import com.erp.model.wms.enums.SoB2bDeliveryInterceptSourceTypeEnum;
import com.erp.model.wms.enums.SoB2cDeliveryInterceptStatusEnum;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.mapper.SoB2bDeliveryInterceptMapper;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.SoB2bDeliveryInterceptDetailService;
import com.erp.server.wms.service.SoB2bDeliveryInterceptService;
import com.erp.server.wms.service.SoOutstockService;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>
 * b2b发货拦截单 服务实现类
 * </p>
 *
 * @author Codex
 */
@Service
public class SoB2bDeliveryInterceptServiceImpl extends SuperServiceImpl<SoB2bDeliveryInterceptMapper, SoB2bDeliveryInterceptEntity> implements SoB2bDeliveryInterceptService {

    @Resource
    private DocNoGenHelper docNoGenHelper;
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private SoB2bDeliveryInterceptDetailService soB2bDeliveryInterceptDetailService;
    @Resource
    private SoOutstockService soOutstockService;
    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Override
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    public BaseResultDTO.AddDTO add(SoB2bDeliveryInterceptDTO.AddDTO addDTO) {
        SoB2bDeliveryInterceptEntity entity = new SoB2bDeliveryInterceptEntity();
        BeanMapperUtils.copy(addDTO, entity);
        handleData(entity);
        entity.setCode(docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_BFLJ));
        if (!super.save(entity)) {
            throw new ServiceException("b2b发货拦截单保存失败");
        }
        soB2bDeliveryInterceptDetailService.add(addDTO, entity.getId());
        String msg = CharSequenceUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "B2B发货拦截单", entity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SO_B2B_DELIVERY_INTERCEPT.getCode(), entity.getId(), "新增操作");
        return new BaseResultDTO.AddDTO(entity.getId(), entity.getCode());
    }

    @Override
    public List<SoB2bDeliveryInterceptDTO.TabListDTO> tabList(PermissionsDTO dto) {
        SoB2bDeliveryInterceptDTO.PagingParamDTO params = new SoB2bDeliveryInterceptDTO.PagingParamDTO();
        params.setPermissionSql(dto.getPermissionSql());
        List<SoB2bDeliveryInterceptDTO.TabListDTO> list = baseMapper.tabList(params);
        list.forEach(item -> item.setTabFlagName(SoB2cDeliveryInterceptStatusEnum.getName(item.getTabFlag())));
        List<String> statusList = SoB2cDeliveryInterceptStatusEnum.getStatusList();
        List<String> existStatusList = list.stream().map(SoB2bDeliveryInterceptDTO.TabListDTO::getTabFlag).collect(Collectors.toList());
        statusList.forEach(status -> {
            if (!existStatusList.contains(status)) {
                list.add(new SoB2bDeliveryInterceptDTO.TabListDTO(status, SoB2cDeliveryInterceptStatusEnum.getName(status), 0));
            }
        });
        list.add(new SoB2bDeliveryInterceptDTO.TabListDTO("all", "全部", list.stream().mapToInt(SoB2bDeliveryInterceptDTO.TabListDTO::getCount).sum()));
        return list;
    }

    @Override
    public PagingVO<SoB2bDeliveryInterceptDTO.ListDTO> paging(PagingDTO<SoB2bDeliveryInterceptDTO.PagingParamDTO> dto) {
        dto.getParams().setPermissionSql(dto.getPermissionSql());
        Page<SoB2bDeliveryInterceptDTO.ListDTO> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        IPage<SoB2bDeliveryInterceptDTO.ListDTO> pageData = baseMapper.paging(query, dto.getParams());
        if (CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO<>(pageData);
        }
        fillList(pageData.getRecords());
        return new PagingVO<>(pageData);
    }

    @Override
    public SoB2bDeliveryInterceptDTO.ViewDTO view(String id) {
        SoB2bDeliveryInterceptEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "B2B发货拦截单"));
        SoB2bDeliveryInterceptDTO.ViewDTO data = BeanMapperUtils.map(SoB2bDeliveryInterceptDTO.ViewDTO.class, entity);
        data.setHandleStatusName(SoB2cDeliveryInterceptStatusEnum.getName(data.getHandleStatus()));
        data.setHandleResultName(HandleResultEnum.getName(data.getHandleResult()));
        data.setSourceTypeName(SoB2bDeliveryInterceptSourceTypeEnum.getName(data.getSourceType()));
        List<SoB2bDeliveryInterceptDetailEntity> detailList = soB2bDeliveryInterceptDetailService.listByMainIds(Collections.singletonList(id));
        fillOne(data, detailList);
        return data;
    }

    @Override
    public SoB2bDeliveryInterceptEntity getLatestBySourceId(String sourceId) {
        if (CharSequenceUtil.isBlank(sourceId)) {
            return null;
        }
        return lambdaQuery().eq(SoB2bDeliveryInterceptEntity::getSourceId, sourceId)
                .orderByDesc(SoB2bDeliveryInterceptEntity::getCreateTime)
                .last("limit 1")
                .one();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleResultBySourceId(String sourceId, String handleResult, String handleRemark, String transportNo, String soOutstockCode) {
        SoB2bDeliveryInterceptEntity entity = getLatestBySourceId(sourceId);
        if (Objects.isNull(entity)) {
            return;
        }
        if (SoB2cDeliveryInterceptStatusEnum.HANDLE.getStatus().equals(entity.getHandleStatus())
                && Objects.equals(entity.getHandleResult(), handleResult)
                && Objects.equals(entity.getTransportNo(), transportNo)
                && Objects.equals(entity.getSoOutstockCode(), soOutstockCode)) {
            return;
        }
        entity.setHandleStatus(SoB2cDeliveryInterceptStatusEnum.HANDLE.getStatus());
        entity.setHandleResult(handleResult);
        entity.setHandleRemark(handleRemark);
        entity.setHandleTime(LocalDateTime.now());
        entity.setHandleUserId("system");
        entity.setHandleUserName("system");
        if (CharSequenceUtil.isNotBlank(transportNo)) {
            entity.setTransportNo(transportNo);
        }
        if (CharSequenceUtil.isNotBlank(soOutstockCode)) {
            entity.setSoOutstockCode(soOutstockCode);
        }
        if (HandleResultEnum.SUCCESS.getCode().equals(handleResult)) {
            entity.setCancelStatus(CancelStatusEnum.SUCCESS.getCode());
            entity.setInterceptStatus(InterceptStatusEnum.SUCCESS.getCode());
        } else if (HandleResultEnum.FAILURE.getCode().equals(handleResult)) {
            entity.setCancelStatus(CancelStatusEnum.FAILURE.getCode());
            entity.setInterceptStatus(InterceptStatusEnum.FAILURE.getCode());
        }
        super.updateById(entity);
        String opName = HandleResultEnum.SUCCESS.getCode().equals(handleResult) ? "拦截成功" : "拦截失败";
        operateLogService.addModuleOperateLog("系统回写拦截结果，备注：" + CharSequenceUtil.blankToDefault(handleRemark, ""), ModuleTypeEnum.SO_B2B_DELIVERY_INTERCEPT.getCode(), entity.getId(), opName);
    }

    private void fillList(List<SoB2bDeliveryInterceptDTO.ListDTO> records) {
        List<String> skuIdList = records.stream().map(SoB2bDeliveryInterceptDTO.ListDTO::getSkuId).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<ProductDetailEntity> detailEntityList = CollUtil.isEmpty(skuIdList) ? Collections.emptyList() : plmTaskFeign.getByIdList(skuIdList);
        for (SoB2bDeliveryInterceptDTO.ListDTO record : records) {
            record.setCancelStatusName(CancelStatusEnum.getName(record.getCancelStatus()));
            record.setHandleResultName(HandleResultEnum.getName(record.getHandleResult()));
            record.setHandleStatusName(SoB2cDeliveryInterceptStatusEnum.getName(record.getHandleStatus()));
            record.setInterceptStatusName(InterceptStatusEnum.getName(record.getInterceptStatus()));
            record.setBillTypeName(OrderTypeEnum.getName(record.getBillType()));
            record.setSourceTypeName(SoB2bDeliveryInterceptSourceTypeEnum.getName(record.getSourceType()));
            ProductDetailEntity productDetailEntity = detailEntityList.stream().filter(item -> item.getId().equals(record.getSkuId())).findFirst().orElse(null);
            if (Objects.nonNull(productDetailEntity)) {
                record.setProductName(productDetailEntity.getName());
            }
        }
    }

    private void fillOne(SoB2bDeliveryInterceptDTO.ViewDTO data, List<SoB2bDeliveryInterceptDetailEntity> detailList) {
        List<String> skuIdList = detailList.stream().map(SoB2bDeliveryInterceptDetailEntity::getSkuId).distinct().collect(Collectors.toList());
        List<ProductDetailEntity> productDetailEntityList = CollectionUtils.isEmpty(skuIdList) ? Collections.emptyList() : plmTaskFeign.getByIdList(skuIdList);
        List<SoB2bDeliveryInterceptDetailDTO.ViewDTO> viewDetailList = BeanMapper.copyList(detailList, SoB2bDeliveryInterceptDetailDTO.ViewDTO.class);
        for (SoB2bDeliveryInterceptDetailDTO.ViewDTO viewDTO : viewDetailList) {
            ProductDetailEntity productDetailEntity = productDetailEntityList.stream().filter(item -> item.getId().equals(viewDTO.getSkuId())).findFirst().orElse(null);
            if (Objects.nonNull(productDetailEntity)) {
                viewDTO.setProductName(productDetailEntity.getName());
            }
        }
        data.setDetailList(viewDetailList);
    }

    private void handleData(SoB2bDeliveryInterceptEntity entity) {
        if (CharSequenceUtil.isBlank(entity.getSoOutstockCode())) {
            SoOutstockEntity soOutstockEntity = soOutstockService.getBySourceCode(entity.getSourceCode());
            if (Objects.nonNull(soOutstockEntity)) {
                entity.setSoOutstockCode(soOutstockEntity.getCode());
            }
        }
    }
}
