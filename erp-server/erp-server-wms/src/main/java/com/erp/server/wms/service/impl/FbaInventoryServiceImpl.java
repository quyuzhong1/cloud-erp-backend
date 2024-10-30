package com.erp.server.wms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.oms.dto.ListingInfoParamDTO;
import com.erp.model.oms.dto.ListingInfoWithSkuMappingDTO;
import com.erp.model.oms.enums.RuleTypeEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.wms.dto.FbaInventoryDTO;
import com.erp.model.wms.dto.RequisitionApplicationDTO;
import com.erp.model.wms.dto.RequisitionApplicationDetailDTO;
import com.erp.model.wms.entity.FbaInventoryEntity;
import com.erp.model.wms.entity.OverseasInventoryEntity;
import com.erp.model.wms.enums.DeliveryChannelsEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.oms.feign.OmsListingInfoFeign;
import com.erp.rpc.oms.feign.SkuMappingFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.convert.WmsFbaInventoryConverter;
import com.erp.server.wms.mapper.FbaInventoryMapper;
import com.erp.server.wms.service.FbaInventoryService;
import com.erp.server.wms.service.OperateLogService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_FBA_INVENTORY;

/**
 * <p>
 * FBA库存 服务实现类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-10-30
 */
@Slf4j
@Service
public class FbaInventoryServiceImpl extends SuperServiceImpl<FbaInventoryMapper, FbaInventoryEntity> implements FbaInventoryService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private PlmTaskFeign plmTaskFeign;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Resource
    private OmsListingInfoFeign omsListingInfoFeign;


    @Override
    public PagingVO<FbaInventoryDTO.ListDTO> paging(PagingDTO<FbaInventoryDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<FbaInventoryDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if (CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }


    @Override
    public FbaInventoryDTO.SummaryNumber summaryNumber(PagingDTO<FbaInventoryDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.setPermissionSql(pagingParamDTO.getPermissionSql());
        FbaInventoryDTO.SummaryNumber summaryNumber = this.baseMapper.summaryNumber(pagingParamDTO.getParams());
        return summaryNumber;
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public String add(FbaInventoryDTO.AddDTO addDTO) {
        FbaInventoryEntity fbaInventoryEntity = new FbaInventoryEntity();
        BeanMapperUtils.copy(addDTO, fbaInventoryEntity);

        // 数据处理
        handleData(fbaInventoryEntity);

        log.info("开始新增FBA库存");
        boolean save = super.save(fbaInventoryEntity);
        if (!save) {
            throw new ServiceException("FBA库存保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "FBA库存", fbaInventoryEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, fbaInventoryEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）
        return fbaInventoryEntity.getId();
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(FbaInventoryDTO.UpdateDTO updateDTO) {
        FbaInventoryEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "FBA库存"));
        FbaInventoryEntity fbaInventoryEntity = BeanMapperUtils.map(FbaInventoryEntity.class, updateDTO);

        // 数据处理
        handleData(fbaInventoryEntity);
        log.info("编辑 开始修改FBA库存数据，id：【{}】", old.getId());
        boolean save = super.updateById(fbaInventoryEntity);
        if (!save) {
            throw new ServiceException("FBA库存保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
        log.info("编辑 开始记录FBA库存日志数据，id：【{}】", fbaInventoryEntity.getId());
        String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), fbaInventoryEntity.getId(), "FBA库存");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, fbaInventoryEntity, null, fbaInventoryEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
     * 新增修改处理数据
     */
    private void handleData(FbaInventoryEntity fbaInventoryEntity) {
        // TODO 验证数据 & 数据赋值
    }

    @Override
    public void exportList(FbaInventoryDTO.ExportDTO param) {
        downloadTaskFeign.saveDownloadTask("FBA库存导出", EXPORT_WMS_FBA_INVENTORY.getCode(), param);
    }

    @Override
    public FbaInventoryDTO.InventoryReservedView listInventoryReserved(String id) {
        FbaInventoryEntity entity = lambdaQuery()
                .select(FbaInventoryEntity::getId, FbaInventoryEntity::getReservedTransfersQty, FbaInventoryEntity::getReservedProcessingQty, FbaInventoryEntity::getReservedOrderQty)
                .eq(FbaInventoryEntity::getId, id)
                .last("LIMIT 1")
                .one();
        FbaInventoryDTO.InventoryReservedView view = new FbaInventoryDTO.InventoryReservedView();
        BeanMapper.copy(entity, view);
        return view;
    }


    private void fillList(List<FbaInventoryDTO.ListDTO> records) {
        List<String> skuNoList = records.stream().map(req -> req.getSkuNo()).distinct().collect(Collectors.toList());
        List<SkuVO> skuVOList = plmTaskFeign.listBySkuNoList(skuNoList);
        for (FbaInventoryDTO.ListDTO record : records) {
            //产品信息
            SkuVO skuVO = skuVOList.stream().filter(req -> req.getSkuNo().equals(record.getSkuNo())).findFirst().orElse(new SkuVO());
            record.setProductName(skuVO.getSkuName());
            //销售渠道名称
            record.setDeliveryChannelsName(DeliveryChannelsEnum.getName(record.getDeliveryChannels()));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean allBatchSave(List<FbaInventoryEntity> inventoryEntityList) {
        // 新增的列表
        List<FbaInventoryEntity> newSaveBatch = new LinkedList<>();
        // 更新的列表
        List<FbaInventoryEntity> newUpdateBatch = new LinkedList<>();

        for (FbaInventoryEntity newEntity : inventoryEntityList) {
            FbaInventoryEntity oldEntity = this.getByAttribute(newEntity.getAsin(), newEntity.getMsku(), newEntity.getFnSku(), newEntity.getWarehouseId());
            if (null == oldEntity) {
                newSaveBatch.add(newEntity);
            } else {
                // 时间数据滞后忽略更新
                if (null != newEntity.getDataEndTime() && newEntity.getDataEndTime().isBefore(oldEntity.getDataEndTime())){
                    log.warn("【亚马逊FBA库存数据】 DataEndTime时间滞后忽略更新: entity={}", JSONUtil.toJsonStr(newEntity));
                    continue;
                }
                FbaInventoryEntity updateEntity = WmsFbaInventoryConverter.INSTANCE.newCombineOld(oldEntity, newEntity);
                newUpdateBatch.add(updateEntity);
            }
        }

        // 批量保存
        if (!CollectionUtils.isEmpty(newSaveBatch)){
            boolean result = this.saveBatch(newSaveBatch);
            if (!result) {
                throw new ServiceException("【FbaInventoryEntity】批量保存失败");
            }
        }

        // 批量更新
        if (!CollectionUtils.isEmpty(newUpdateBatch)){
            boolean result = this.updateBatchById(newUpdateBatch);
            if (!result) {
                throw new ServiceException("【FbaInventoryEntity】批量更新失败");
            }
        }

        return true;
    }

    @Override
    public FbaInventoryEntity getByAttribute(String asin, String mSku, String fnSku, String warehouseId) {
        if (StringUtils.isBlank(asin) || StringUtils.isBlank(mSku) || StringUtils.isBlank(fnSku) || StringUtils.isBlank(warehouseId)) {
            String msg = StrUtil.format("数据异常，存在空参数：asin={},skuNo={}, fnSku={}, warehouseId={}", asin, mSku, fnSku, warehouseId);
            throw new ServiceException(msg);
        }
        return lambdaQuery()
                .eq(FbaInventoryEntity::getAsin, asin)
                .eq(FbaInventoryEntity::getMsku, mSku)
                .eq(FbaInventoryEntity::getFnSku, fnSku)
                .eq(FbaInventoryEntity::getWarehouseId, warehouseId)
                .last("LIMIT 1")
                .one();
    }

    @Override
    public List<FbaInventoryEntity> findList(List<String> sellerSkuList) {
        if (CollectionUtils.isEmpty(sellerSkuList)){
            return Collections.emptyList();
        }
        return lambdaQuery()
                .in(FbaInventoryEntity::getMsku, sellerSkuList)
                .list();
    }

    @Override
    public PagingVO<FbaInventoryDTO.ListDTO> exportFbaInventory(PagingDTO<FbaInventoryDTO.ExportDTO> dto) {

        Page<FbaInventoryDTO.ListDTO> page = this.baseMapper.listExport(new Page<>(dto.getCurrPage(), dto.getPageSize()),dto.getParams());
        if (!CollUtil.isEmpty(page.getRecords())) {
            // 数据处理
            fillList(page.getRecords());
        }
        return new PagingVO<>(page);
    }

    @Override
    public List<ListingInfoWithSkuMappingDTO> checkAndSaveFnskuToListing(List<String> platformSkuNoList, String shopId) {
        if (CollectionUtils.isEmpty(platformSkuNoList) || StringUtils.isBlank(shopId)){
            return Collections.emptyList();
        }

        ListingInfoParamDTO paramDTO = new ListingInfoParamDTO();
        paramDTO.setPlatform(PlatformDictEnum.AMAZON.getCode());
        paramDTO.setPlatformSkuNoList(platformSkuNoList);
        paramDTO.setShopIdList(Collections.singletonList(shopId));
        paramDTO.setType(RuleTypeEnum.PLATFORM.getCode());
        paramDTO.setMatchResult(true);

        return omsListingInfoFeign.checkAndUpdateFnsku(paramDTO);
    }

    @Override
    public void checkAndUpdateFnsku(RequisitionApplicationDTO.AddDTO dto) {
        checkAndUpdateFnskuCommon(dto.getDetailList(), dto.getChannelId());
    }

    @Override
    public void checkAndUpdateFnsku(RequisitionApplicationDTO.UpdateDTO dto) {
        checkAndUpdateFnskuCommon(dto.getDetailList(), dto.getChannelId());
    }

    private <T extends RequisitionApplicationDetailDTO.CommonDTO> void checkAndUpdateFnskuCommon(List<T> detailList, String channelId) {
        List<String> platformSkuNoList = detailList.stream()
                .map(RequisitionApplicationDetailDTO.CommonDTO::getPlatformSku)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());

        List<ListingInfoWithSkuMappingDTO> updateFnSkulist = this.checkAndSaveFnskuToListing(platformSkuNoList, channelId);

        if (CollectionUtils.isEmpty(updateFnSkulist)) {
            return;
        }

        for (T addDTO : detailList) {
            if (StringUtils.isNotBlank(addDTO.getPlatformFnSku())) {
                continue;
            }
            updateFnSkulist.stream()
                    .filter(e -> e.getShopId().equalsIgnoreCase(channelId) && e.getPlatformSkuNo().equalsIgnoreCase(addDTO.getPlatformSku()))
                    .findFirst().ifPresent(mappingDTO -> addDTO.setPlatformFnSku(mappingDTO.getPlatformFnSku()));

        }
    }

}
