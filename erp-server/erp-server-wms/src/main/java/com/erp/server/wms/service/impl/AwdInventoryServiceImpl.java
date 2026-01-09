package com.erp.server.wms.service.impl;

import cn.hutool.core.util.StrUtil;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.FbaInventoryDTO;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.wms.entity.AwdInventoryEntity;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.mapper.AwdInventoryMapper;
import com.erp.server.wms.service.AwdInventoryService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.wms.service.OperateLogService;
import com.common.core.exception.ServiceException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.AwdInventoryDTO;
import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.hutool.core.collection.CollUtil;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import javax.servlet.http.HttpServletResponse;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_AWD_INVENTORY;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author wtr
 * @since 2025-12-26
 */
@Slf4j
@Service
public class AwdInventoryServiceImpl extends SuperServiceImpl<AwdInventoryMapper, AwdInventoryEntity> implements AwdInventoryService {

    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @Resource
    private OperateLogService operateLogService;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(AwdInventoryDTO.AddDTO addDTO) {
        AwdInventoryEntity awdInventoryEntity = this.lambdaQuery()
                .eq(AwdInventoryEntity::getMsku, addDTO.getMsku())
                .one();
        AwdInventoryEntity entity = new AwdInventoryEntity();
        if (Objects.nonNull(awdInventoryEntity)) {
            log.info("awd库存开始更新");
            boolean update = this.lambdaUpdate()
                    .set(AwdInventoryEntity::getAsin,StringUtils.isNotBlank(addDTO.getAsin()) ? addDTO.getAsin() : "")
                    .set(AwdInventoryEntity::getSkuId, StringUtils.isNotBlank(addDTO.getSkuId()) ? addDTO.getSkuId() : "")
                    .set(AwdInventoryEntity::getSkuNo, StringUtils.isNotBlank(addDTO.getSkuNo()) ? addDTO.getSkuNo() : "")
                    .set(AwdInventoryEntity::getProductName, StringUtils.isNotBlank(addDTO.getProductName()) ? addDTO.getProductName() : "")
                    .set(AwdInventoryEntity::getAvailableDistributableQty, addDTO.getAvailableDistributableQty() != null ? addDTO.getAvailableDistributableQty() : 0)
                    .set(AwdInventoryEntity::getReplenishmentQty, addDTO.getReplenishmentQty() != null ? addDTO.getReplenishmentQty() : 0)
                    .set(AwdInventoryEntity::getReservedDistributableQty, addDTO.getReservedDistributableQty() != null ? addDTO.getReservedDistributableQty() : 0)
                    .set(AwdInventoryEntity::getTotalInboundQty, addDTO.getTotalInboundQty() != null ? addDTO.getTotalInboundQty() : 0)
                    .set(AwdInventoryEntity::getTotalOnhandQty, addDTO.getTotalOnhandQty() != null ? addDTO.getTotalOnhandQty() : 0)
                    .set(AwdInventoryEntity::getWarehouseId,StringUtils.isNotBlank(addDTO.getWarehouseId()) ? addDTO.getWarehouseId() : "")
                    .set(AwdInventoryEntity::getWarehouseName,StringUtils.isNotBlank(addDTO.getWarehouseName()) ? addDTO.getWarehouseName() : "")
                    .eq(AwdInventoryEntity::getMsku, addDTO.getMsku())
                    .update();
            if(!update) {
                throw new ServiceException("更新失败");
            }
        } else {
            BeanUtils.copyProperties(addDTO, entity);
            log.info("awd库存开始新增");
            boolean save = super.save(entity);
            if(!save) {
                throw new ServiceException("保存失败");
            }
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】",
                UserContext.getDefaultLoginUser().getUserName(),
                "awd库存" ,
                Objects.nonNull(awdInventoryEntity) ? awdInventoryEntity.getId() : entity.getId());
        operateLogService.addModuleOperateLog(msg,
                ModuleTypeEnum.AWD_INVENTORY.getCode(),
                Objects.nonNull(awdInventoryEntity) ? awdInventoryEntity.getId() : entity.getId(),
                "新增操作");
        return new BaseResultDTO.AddDTO(Objects.nonNull(awdInventoryEntity) ? awdInventoryEntity.getId() : entity.getId(),
                Objects.nonNull(awdInventoryEntity) ? awdInventoryEntity.getId() : entity.getId());
    }


    @Override
    public PagingVO<AwdInventoryDTO.ListDTO> paging(PagingDTO<AwdInventoryDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<AwdInventoryDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public void exportList(AwdInventoryDTO.ExportDTO param, HttpServletResponse response) {
        downloadTaskFeign.saveDownloadTask("AWD库存导出", EXPORT_WMS_AWD_INVENTORY.getCode(), param);
    }

    @Override
    public AwdInventoryDTO.SummaryNumber summaryNumber(PagingDTO<AwdInventoryDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        AwdInventoryDTO.SummaryNumber summaryNumber = baseMapper.summaryNumber(pagingParamDTO.getParams());
        return summaryNumber;
    }

    /**
    * 分页查询、导出 数据处理
   */
   private void fillList(List<AwdInventoryDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
            return;
        }
   }
}
