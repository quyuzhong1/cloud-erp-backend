package com.erp.server.wms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.scm.dto.excel.PurchaseChangeExportExcelDTO;
import com.erp.model.wms.dto.excel.ExportOverseasInventoryExcelDTO;
import com.erp.model.wms.entity.OverseasInventoryEntity;
import com.erp.model.wms.entity.OverseasTransferWarehouseEntity;
import com.erp.server.wms.mapper.OverseasInventoryMapper;
import com.erp.server.wms.service.OverseasInventoryService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.OverseasInventoryDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;

import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 海外仓库存 服务实现类
 * </p>
 *
 * @author Jim
 * @since 2023-11-16
 */
@Slf4j
@Service
public class OverseasInventoryServiceImpl extends SuperServiceImpl<OverseasInventoryMapper, OverseasInventoryEntity> implements OverseasInventoryService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(OverseasInventoryDTO.AddDTO addDTO) {
        OverseasInventoryEntity overseasInventoryEntity = new OverseasInventoryEntity();
        BeanMapperUtils.copy(addDTO, overseasInventoryEntity);

        // 数据处理
        handleData(overseasInventoryEntity);

        log.info("开始新增海外仓库存");
        boolean save = super.save(overseasInventoryEntity);
        if(!save) {
            throw new ServiceException("海外仓库存保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", commonService.getUserInfo().getUserName(), "海外仓库存" , overseasInventoryEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, overseasInventoryEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(overseasInventoryEntity.getId(), overseasInventoryEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(OverseasInventoryDTO.UpdateDTO updateDTO) {
        OverseasInventoryEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "海外仓库存"));
        OverseasInventoryEntity overseasInventoryEntity =  BeanMapperUtils.map(OverseasInventoryEntity.class, updateDTO);

        // 数据处理
        handleData(overseasInventoryEntity);
        log.info("编辑 开始修改海外仓库存数据，id：【{}】", old.getId());
        boolean save = super.updateById(overseasInventoryEntity);
        if(!save) {
            throw new ServiceException("海外仓库存保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录海外仓库存日志数据，id：【{}】", overseasInventoryEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), overseasInventoryEntity.getId(), "海外仓库存");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, overseasInventoryEntity, null, overseasInventoryEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(OverseasInventoryEntity overseasInventoryEntity) {
    // TODO 验证数据 & 数据赋值
    }

    @Override
    public PagingVO<OverseasInventoryDTO.ListDTO> paging(PagingDTO<OverseasInventoryDTO.PagingParamDTO> dto) {
        OverseasInventoryDTO.PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page<?> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        IPage<OverseasInventoryDTO.ListDTO> pageData = baseMapper.paging(query, params);
        return new PagingVO<>(pageData);
    }

    @Override
    public OverseasInventoryDTO.ListTotalDTO queryParamsTotal(OverseasInventoryDTO.PagingParamDTO params) {
        return baseMapper.queryParamsTotal(params);
    }

    @Override
    public Boolean exportExcel(OverseasInventoryDTO.PagingParamDTO dto, HttpServletResponse response) {
        //查询所有数据
        List<OverseasInventoryDTO.ListDTO> list = baseMapper.listByParams(dto);
        if (CollectionUtils.isEmpty(list)) {
            return true;
        }
        //  导出
        String fileName = "海外仓库数据";
        try {
            ExcelUtil.export(fileName, "海外仓库数据", list, ExportOverseasInventoryExcelDTO.class, response);
        } catch (Exception e) {
            throw new ServiceException(ApiError.ERROR_1015 + e.getMessage());
        }
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveOrUpdateByPlatform(OverseasInventoryEntity entity) {
        LambdaQueryWrapper<OverseasInventoryEntity> queryWrapper = new LambdaQueryWrapper<OverseasInventoryEntity>()
                .eq(OverseasInventoryEntity::getDictPlatform, entity.getDictPlatform())
                .eq(OverseasInventoryEntity::getWarehouseCode, entity.getWarehouseCode())
                .eq(OverseasInventoryEntity::getPlatformSku, entity.getPlatformSku());
        OverseasInventoryEntity existingEntity = this.getOne(queryWrapper);
        if (existingEntity == null || entity.getDownloadTime().isAfter(existingEntity.getDownloadTime())) {
            return this.saveOrUpdate(entity,queryWrapper);
        }
        return false;
    }

}
