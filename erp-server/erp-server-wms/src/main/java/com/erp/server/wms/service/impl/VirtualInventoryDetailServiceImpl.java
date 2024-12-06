package com.erp.server.wms.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.wms.dto.CfgSettingVirtualValueDTO;
import com.erp.model.wms.dto.VirtualInventoryAgeDTO;
import com.erp.model.wms.dto.VirtualInventoryDetailDTO;
import com.erp.model.wms.entity.CfgSettingEntity;
import com.erp.model.wms.entity.VirtualInventoryDetailEntity;
import com.erp.model.wms.entity.VirtualWarehouseEntity;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.model.wms.enums.CfgSettingVirtualEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.wms.mapper.VirtualInventoryDetailMapper;
import com.erp.server.wms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static com.common.business.enums.FileTaskEventEnum.*;

/**
 * <p>
 * 虚拟仓库明细 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-12-03
 */
@Slf4j
@Service
public class VirtualInventoryDetailServiceImpl extends SuperServiceImpl<VirtualInventoryDetailMapper, VirtualInventoryDetailEntity> implements VirtualInventoryDetailService {
    @Autowired
    private OperateLogService operateLogService;

    @Autowired
    private DownloadTaskFeign downloadTaskFeign;

    @Autowired
    private WarehouseService warehouseService;

    @Autowired
    private VirtualWarehouseService virtualWarehouseService;

    @Autowired
    private CfgSettingService cfgSettingService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(VirtualInventoryDetailDTO.AddDTO addDTO) {
        VirtualInventoryDetailEntity virtualInventoryDetailEntity = new VirtualInventoryDetailEntity();
        BeanMapperUtils.copy(addDTO, virtualInventoryDetailEntity);

        // 数据处理
        handleData(virtualInventoryDetailEntity);

        log.info("开始新增虚拟仓库明细");
        boolean save = super.save(virtualInventoryDetailEntity);
        if(!save) {
            throw new ServiceException("虚拟仓库明细保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "虚拟仓库明细" , virtualInventoryDetailEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, virtualInventoryDetailEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(virtualInventoryDetailEntity.getId(), virtualInventoryDetailEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(VirtualInventoryDetailDTO.UpdateDTO updateDTO) {
        VirtualInventoryDetailEntity old = super.getById(updateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "虚拟仓库明细"));
        VirtualInventoryDetailEntity virtualInventoryDetailEntity =  BeanMapperUtils.map(VirtualInventoryDetailEntity.class, updateDTO);

        // 数据处理
        handleData(virtualInventoryDetailEntity);
        log.info("编辑 开始修改虚拟仓库明细数据，id：【{}】", old.getId());
        boolean save = super.updateById(virtualInventoryDetailEntity);
        if(!save) {
            throw new ServiceException("虚拟仓库明细保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录虚拟仓库明细日志数据，id：【{}】", virtualInventoryDetailEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), virtualInventoryDetailEntity.getId(), "虚拟仓库明细");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, virtualInventoryDetailEntity, null, virtualInventoryDetailEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public PagingVO<VirtualInventoryAgeDTO.ListDTO> paging(PagingDTO<VirtualInventoryAgeDTO.SearchParamDTO> dto) {
        dto.getParams().setPermissionSql(dto.getPermissionSql());
        IPage<VirtualInventoryAgeDTO.ListDTO> pageData = this.baseMapper.paging(dto.page(), dto.getParams());
        // 填充名称
        fillPageData(pageData.getRecords());
        return new PagingVO<>(pageData);
    }

    @Override
    public Boolean exportExcel(VirtualInventoryAgeDTO.SearchParamDTO dto) {
        downloadTaskFeign.saveDownloadTask("库龄分析", EXPORT_WMS_VIRTUAL_INVENTORY_AGE.getCode(), dto);
        return Boolean.TRUE;
    }

    @Override
    public VirtualInventoryAgeDTO.ViewDTO view(VirtualInventoryAgeDTO.HisInventoryAgeParamDTO dto) {
        VirtualInventoryAgeDTO.ViewDTO viewDTO = new VirtualInventoryAgeDTO.ViewDTO();
        BeanMapperUtils.copy(dto,viewDTO);
        //产品信息
        ProductDetailEntity productDetailEntity = FeignQuery.getById(ProductDetailEntity.class, dto.getSkuId());
        if (ObjUtil.isEmpty(productDetailEntity)) {
            throw new ServiceException(ApiError.ERROR_95084);
        }
        viewDTO.setSkuNo(productDetailEntity.getSkuNo());
        viewDTO.setProductName(productDetailEntity.getName());

        //仓库信息
        WarehouseEntity warehouseEntity = warehouseService.getById(dto.getWarehouseId());
        if (ObjUtil.isEmpty(warehouseEntity)) {
            throw new ServiceException(ApiError.ERROR_99002);
        }

        //虚拟仓库
        VirtualWarehouseEntity virtualWarehouseEntity = virtualWarehouseService.getById(dto.getVirtualWarehouseId());
        if (ObjUtil.isEmpty(virtualWarehouseEntity)) {
            throw new ServiceException("未找到虚拟仓库");
        }

        return null;
    }

    @Override
    public Boolean exportHisInventoryAge(VirtualInventoryAgeDTO.HisInventoryAgeParamDTO dto) {
        downloadTaskFeign.saveDownloadTask("历史库龄 ", EXPORT_WMS_VIRTUAL_HIS_INVENTORY_AGE.getCode(), dto);
        return Boolean.TRUE;
    }

    @Override
    public PagingVO<VirtualInventoryAgeDTO.HisInventoryAgeDTO> hisInventoryAgePaging(PagingDTO<VirtualInventoryAgeDTO.HisInventoryAgeParamDTO> dto) {
        IPage<VirtualInventoryAgeDTO.HisInventoryAgeDTO> pageData = this.baseMapper.hisInventoryAgePaging(dto.page(), dto.getParams());
        return new PagingVO<>(pageData);
    }

    @Override
    public PagingVO<VirtualInventoryAgeDTO.HisInventoryAgeDetailDTO> hisInventoryAgeDetailPaging(PagingDTO<VirtualInventoryAgeDTO.HisInventoryAgeParamDTO> dto) {
        IPage<VirtualInventoryAgeDTO.HisInventoryAgeDetailDTO> pageData = this.baseMapper.hisInventoryAgeDetailPaging(dto.page(), dto.getParams());
        return new PagingVO<>(pageData);
    }

    @Override
    public Boolean exportHisInventoryAgeDetail(VirtualInventoryAgeDTO.HisInventoryAgeParamDTO dto) {
        downloadTaskFeign.saveDownloadTask("历史库龄明细", EXPORT_WMS_VIRTUAL_HIS_INVENTORY_AGE_DETAIL.getCode(), dto);
        return Boolean.TRUE;
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(VirtualInventoryDetailEntity virtualInventoryDetailEntity) {
    // TODO 验证数据 & 数据赋值
    }

    /**
     * 分页列表处理数据
     */
    private void fillPageData(List<VirtualInventoryAgeDTO.ListDTO> detailList) {
        if (CollUtil.isEmpty(detailList)) {
            return;
        }
        //查询库龄分析配置
        CfgSettingEntity cfgSettingEntity = cfgSettingService.getByKey(CfgSettingVirtualEnum.INVENTORY_AGE_STATISTICS.getCode());
        if (ObjectUtil.isEmpty(cfgSettingEntity) || ObjectUtil.isEmpty(cfgSettingEntity.getDataJson())) {
            return;
        }
        CfgSettingVirtualValueDTO.InventoryAgeTO inventoryAgeTO = BeanUtil.toBean(cfgSettingEntity.getDataJson(), CfgSettingVirtualValueDTO.InventoryAgeTO.class);
        List<CfgSettingVirtualValueDTO.InventoryAgeDateTO> list = inventoryAgeTO.getList();

        HashMap<String, VirtualInventoryAgeDTO.VirtualIntervalDTO> map = new HashMap<>();
        for (CfgSettingVirtualValueDTO.InventoryAgeDateTO inventoryAgeDateTO :list) {
            String ageDateInterval = "";
            //区间字段
            if (ObjUtil.isNull(inventoryAgeDateTO.getEndDays())) {
                ageDateInterval = CharSequenceUtil.format("{}以上", inventoryAgeDateTO.getEndDays());
            } else {
                ageDateInterval = CharSequenceUtil.format("{}~{}天", inventoryAgeDateTO.getStartDays(), inventoryAgeDateTO.getEndDays());
            }
            map.put(ageDateInterval,new VirtualInventoryAgeDTO.VirtualIntervalDTO());
        }
    }
}

