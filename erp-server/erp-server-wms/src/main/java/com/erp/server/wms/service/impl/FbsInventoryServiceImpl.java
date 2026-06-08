package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.annotation.DataIdempotent;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.exception.ServiceException;
import com.erp.model.wms.dto.FbsInventoryDTO;
import com.erp.model.wms.entity.FbsInventoryEntity;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.wms.mapper.FbsInventoryMapper;
import com.erp.server.wms.service.FbsInventoryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_FBS_INVENTORY;

/**
 * <p>
 * FBS库存 服务实现类
 * </p>
 *
 * @author Cursor
 * @since 2026-05-25
 */
@Slf4j
@Service
public class FbsInventoryServiceImpl extends SuperServiceImpl<FbsInventoryMapper, FbsInventoryEntity> implements FbsInventoryService {

    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @Override
    public PagingVO<FbsInventoryDTO.ListDTO> paging(PagingDTO<FbsInventoryDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page<FbsInventoryDTO.ListDTO> query = new Page<>(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<FbsInventoryDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if (CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO<>(pageData);
        }
        return new PagingVO<>(pageData);
    }

    @Override
    public void exportList(FbsInventoryDTO.ExportDTO param, HttpServletResponse response) {
        downloadTaskFeign.saveDownloadTask("FBS库存导出", EXPORT_WMS_FBS_INVENTORY.getCode(), param);
    }

    @Override
    public FbsInventoryDTO.SummaryNumber summaryNumber(PagingDTO<FbsInventoryDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        return baseMapper.summaryNumber(pagingParamDTO.getParams());
    }

    @DataIdempotent(keyIdName = "addDTO.shopId,addDTO.warehouseId,addDTO.fbsSku", businessType = "fbsInventoryAdd")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(FbsInventoryDTO.AddDTO addDTO) {
        FbsInventoryEntity existEntity = this.lambdaQuery()
                .eq(FbsInventoryEntity::getShopId, addDTO.getShopId())
                .eq(FbsInventoryEntity::getWarehouseId, addDTO.getWarehouseId())
                .eq(FbsInventoryEntity::getFbsSku, addDTO.getFbsSku())
                .one();
        if (Objects.nonNull(existEntity)) {
            boolean update = this.lambdaUpdate()
                    .set(FbsInventoryEntity::getShopName, defaultString(addDTO.getShopName()))
                    .set(FbsInventoryEntity::getWarehouseName, defaultString(addDTO.getWarehouseName()))
                    .set(FbsInventoryEntity::getPlatformSku, defaultString(addDTO.getPlatformSku()))
                    .set(FbsInventoryEntity::getPlatformProductName, defaultString(addDTO.getPlatformProductName()))
                    .set(FbsInventoryEntity::getSpecName, defaultString(addDTO.getSpecName()))
                    .set(FbsInventoryEntity::getSkuId, defaultString(addDTO.getSkuId()))
                    .set(FbsInventoryEntity::getSkuNo, defaultString(addDTO.getSkuNo()))
                    .set(FbsInventoryEntity::getProductName, defaultString(addDTO.getProductName()))
                    .set(FbsInventoryEntity::getPurchaseMode, defaultString(addDTO.getPurchaseMode()))
                    .set(FbsInventoryEntity::getRecommendedReplenishmentQty, defaultInt(addDTO.getRecommendedReplenishmentQty()))
                    .set(FbsInventoryEntity::getTotalStockQty, defaultInt(addDTO.getTotalStockQty()))
                    .set(FbsInventoryEntity::getStockedInboundQty, defaultInt(addDTO.getStockedInboundQty()))
                    .set(FbsInventoryEntity::getTransferAsnInboundQty, defaultInt(addDTO.getTransferAsnInboundQty()))
                    .set(FbsInventoryEntity::getReservedQty, defaultInt(addDTO.getReservedQty()))
                    .set(FbsInventoryEntity::getUnsellableQty, defaultInt(addDTO.getUnsellableQty()))
                    .set(FbsInventoryEntity::getInTransitQty, defaultInt(addDTO.getInTransitQty()))
                    .set(FbsInventoryEntity::getTurnoverDays, defaultInt(addDTO.getTurnoverDays()))
                    .set(FbsInventoryEntity::getWarehouseInventoryCoverageDays, defaultInt(addDTO.getWarehouseInventoryCoverageDays()))
                    .set(FbsInventoryEntity::getDailyAvgSalesQty, addDTO.getDailyAvgSalesQty())
                    .set(FbsInventoryEntity::getLast7DaysSalesQty, defaultInt(addDTO.getLast7DaysSalesQty()))
                    .set(FbsInventoryEntity::getLast15DaysSalesQty, defaultInt(addDTO.getLast15DaysSalesQty()))
                    .set(FbsInventoryEntity::getLast30DaysSalesQty, defaultInt(addDTO.getLast30DaysSalesQty()))
                    .set(FbsInventoryEntity::getLast60DaysSalesQty, defaultInt(addDTO.getLast60DaysSalesQty()))
                    .set(FbsInventoryEntity::getLast90DaysSalesQty, defaultInt(addDTO.getLast90DaysSalesQty()))
                    .set(FbsInventoryEntity::getStockAge030Qty, defaultInt(addDTO.getStockAge030Qty()))
                    .set(FbsInventoryEntity::getStockAge3160Qty, defaultInt(addDTO.getStockAge3160Qty()))
                    .set(FbsInventoryEntity::getStockAge6190Qty, defaultInt(addDTO.getStockAge6190Qty()))
                    .set(FbsInventoryEntity::getStockAge91120Qty, defaultInt(addDTO.getStockAge91120Qty()))
                    .set(FbsInventoryEntity::getStockAge121180Qty, defaultInt(addDTO.getStockAge121180Qty()))
                    .set(FbsInventoryEntity::getStockAgeOver180Qty, defaultInt(addDTO.getStockAgeOver180Qty()))
                    .set(FbsInventoryEntity::getPlatformUpdateTime, addDTO.getPlatformUpdateTime())
                    .eq(FbsInventoryEntity::getId, existEntity.getId())
                    .eq(FbsInventoryEntity::getVersion, existEntity.getVersion())
                    .update();
            if (!update) {
                throw new ServiceException(CharSequenceUtil.format(
                        "FBS库存更新失败，数据可能已被其他操作修改，shopId={}，warehouseId={}，fbsSku={}",
                        addDTO.getShopId(), addDTO.getWarehouseId(), addDTO.getFbsSku()));
            }
            return new BaseResultDTO.AddDTO(existEntity.getId(), existEntity.getId());
        }

        FbsInventoryEntity entity = new FbsInventoryEntity();
        BeanUtils.copyProperties(addDTO, entity);
        if (!super.save(entity)) {
            throw new ServiceException("FBS库存保存失败");
        }
        return new BaseResultDTO.AddDTO(entity.getId(), entity.getId());
    }

    private String defaultString(String value) {
        return StringUtils.defaultString(value);
    }

    private int defaultInt(Integer value) {
        return value == null ? 0 : value;
    }

}
