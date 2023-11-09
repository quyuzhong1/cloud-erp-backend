package com.erp.server.wms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.utils.date.DateUtil;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.wms.dto.FbaDeliveryDTO;
import com.erp.model.wms.dto.inventory.InitStockDTO;
import com.erp.model.wms.entity.FbaInventoryEntity;
import com.erp.model.wms.entity.FbaInventoryReservedEntity;
import com.erp.model.wms.enums.DeliveryChannelsEnum;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.mapper.FbaInventoryMapper;
import com.erp.server.wms.service.FbaInventoryReservedService;
import com.erp.server.wms.service.FbaInventoryService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.FbaInventoryDTO;
import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;

import javax.servlet.http.HttpServletResponse;

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
    private CommonService commonService;
    @Autowired
    private PlmTaskFeign plmTaskFeign;

    @Override
    public PagingVO<FbaInventoryDTO.ListDTO> paging(PagingDTO<FbaInventoryDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<FbaInventoryDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }


    @Override
    public FbaInventoryDTO.SummaryNumber summaryNumber(FbaInventoryDTO.PagingParamDTO pagingParamDTO) {
        pagingParamDTO.setPermissionSql(pagingParamDTO.getPermissionSql());
        FbaInventoryDTO.SummaryNumber summaryNumber = this.baseMapper.summaryNumber(pagingParamDTO);
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
        if(!save) {
            throw new ServiceException("FBA库存保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", commonService.getUserInfo().getUserName(), "FBA库存" , fbaInventoryEntity.getId());
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
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "FBA库存"));
        FbaInventoryEntity fbaInventoryEntity =  BeanMapperUtils.map(FbaInventoryEntity.class, updateDTO);

        // 数据处理
        handleData(fbaInventoryEntity);
        log.info("编辑 开始修改FBA库存数据，id：【{}】", old.getId());
        boolean save = super.updateById(fbaInventoryEntity);
        if(!save) {
            throw new ServiceException("FBA库存保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录FBA库存日志数据，id：【{}】", fbaInventoryEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), fbaInventoryEntity.getId(), "FBA库存");
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
    public void exportList(FbaInventoryDTO.ExportDTO param, HttpServletResponse response) {
        List<FbaInventoryDTO.ListDTO> list = this.baseMapper.listExport(param);
        if(CollUtil.isEmpty(list)) {
            return;
        }
        // 数据处理
        fillList(list);

        // 导出数据
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/fbaInventory.xlsx";
        String name = "FBA库存导出";
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date).append(name);
        try {
            new ExcelPrintUtils().patchExport(list, response, sb.toString(), excelPath);
        } catch (Exception e) {
            throw new ServiceException(ApiError.ERROR_1015);
        }
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

}
