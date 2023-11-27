package com.erp.server.wms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.*;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.enums.DeliveryModeEnum;
import com.erp.model.scm.dto.PurchaseApplicationDTO;
import com.erp.model.scm.dto.excel.PurchaseApplicationExportExcelDTO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.scm.enums.PageListTypeEnum;
import com.erp.model.wms.dto.OverseasWarehouseInboundDetailDTO;
import com.erp.model.wms.dto.WarehouseReceiveDTO;
import com.erp.model.wms.dto.excel.ExportOverseasWarehouseInboundExcelDTO;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.LogisticsMethodEnum;
import com.erp.model.wms.enums.OverseasDeliveryModeEnum;
import com.erp.model.wms.enums.OverseasFinishStatusEnum;
import com.erp.model.wms.enums.OverseasInstockTypeEnum;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.server.wms.convert.OverseasWarehouseInboundConverter;
import com.erp.server.wms.mapper.OverseasWarehouseInboundMapper;
import com.erp.server.wms.service.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.OverseasWarehouseInboundDTO;

import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 海外仓入库单 服务实现类
 * </p>
 *
 * @author Jim
 * @since 2023-11-16
 */
@Slf4j
@Service
public class OverseasWarehouseInboundServiceImpl extends SuperServiceImpl<OverseasWarehouseInboundMapper, OverseasWarehouseInboundEntity> implements OverseasWarehouseInboundService {
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private CommonService commonService;
    @Resource
    private DocNoGenHelper docNoGenHelper;
    @Resource
    private OverseasWarehouseInboundDetailService overseasWarehouseInboundDetailService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(OverseasWarehouseInboundDTO.AddDTO addDTO) {
        OverseasWarehouseInboundEntity overseasWarehouseInboundEntity = new OverseasWarehouseInboundEntity();
        BeanMapperUtils.copy(addDTO, overseasWarehouseInboundEntity);

        // 数据处理
        handleData(overseasWarehouseInboundEntity);

        log.info("开始新增海外仓入库单");
        // 生成单号
        // TODO 此处的null需填写生成单号类型，type查看BusinessNoTypeEnum枚举类 注意需要填写prefix 为单号前缀
        String code = docNoGenHelper.generateCode(null);
        overseasWarehouseInboundEntity.setCode(code);
        boolean save = super.save(overseasWarehouseInboundEntity);
        if (!save) {
            throw new ServiceException("海外仓入库单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", commonService.getUserInfo().getUserName(), "海外仓入库单", overseasWarehouseInboundEntity.getCode());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, overseasWarehouseInboundEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(overseasWarehouseInboundEntity.getId(), code);
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(OverseasWarehouseInboundDTO.UpdateDTO updateDTO) {
        OverseasWarehouseInboundEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "海外仓入库单"));
        OverseasWarehouseInboundEntity overseasWarehouseInboundEntity = BeanMapperUtils.map(OverseasWarehouseInboundEntity.class, updateDTO);

        // 数据处理
        handleData(overseasWarehouseInboundEntity);
        log.info("编辑 开始修改海外仓入库单数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(overseasWarehouseInboundEntity);
        if (!save) {
            throw new ServiceException("海外仓入库单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
        log.info("编辑 开始记录海外仓入库单日志数据，单号：【{}】", overseasWarehouseInboundEntity.getCode());
        String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), overseasWarehouseInboundEntity.getCode(), "海外仓入库单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, overseasWarehouseInboundEntity, null, overseasWarehouseInboundEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public OverseasWarehouseInboundEntity getByCode(String receivingCode) {
        return lambdaQuery().eq(OverseasWarehouseInboundEntity::getCode, receivingCode).one();
    }


    /**
     * 新增修改处理数据
     */
    private void handleData(OverseasWarehouseInboundEntity overseasWarehouseInboundEntity) {
        // TODO 验证数据 & 数据赋值
    }


    @Override
    public PagingVO<OverseasWarehouseInboundDTO.ListDTO> paging(PagingDTO<OverseasWarehouseInboundDTO.PagingParamDTO> dto) {
        OverseasWarehouseInboundDTO.PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page<?> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        IPage<OverseasWarehouseInboundDTO.ListDTO> pageData = baseMapper.paging(query, params);
        if (CollectionUtils.isEmpty(pageData.getRecords())) {
            return new PagingVO<>(pageData);
        }
        fillList(pageData.getRecords());
        return new PagingVO<>(pageData);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO manualFinish(OverseasWarehouseInboundDTO.FinishDTO dto) {

        OverseasWarehouseInboundEntity entity = this.getById(dto.getId());
        Optional.ofNullable(entity).orElseThrow(() -> new ServiceException(ApiError.OVERSEAS_WAREHOUSE_INBOUND_NOT_EXIST));
        // TODO 校验

        entity.setFinishStatus(OverseasFinishStatusEnum.MANUAL.getCode());
        entity.setFinishReason(dto.getFinishReason());
        // 详情更新签收数量
        if (!this.updateById(entity)) {
            throw new ServiceException("海外仓入库单更新失败");
        }
        return BatchResultDTO.success(entity.getId(), entity.getId(), OperationTypeEnum.UPDATE_STATUS);
    }

    @Override
    public OverseasWarehouseInboundDTO.ViewDTO view(String id) {
        OverseasWarehouseInboundEntity entity = this.getById(id);
        Optional.ofNullable(entity).orElseThrow(() -> new ServiceException(ApiError.OVERSEAS_WAREHOUSE_INBOUND_NOT_EXIST));

        OverseasWarehouseInboundDTO.ViewDTO resultDTO = OverseasWarehouseInboundConverter.INSTANCE.entityToViewDTO(entity);

        // 查询详情信息
        List<OverseasWarehouseInboundDetailEntity> detailEntityList = overseasWarehouseInboundDetailService.getByMainId(entity.getId());

        List<OverseasWarehouseInboundDetailDTO.ViewDTO> detailDTOList = detailEntityList.stream()
                .map(OverseasWarehouseInboundConverter.INSTANCE::detailEntityToViewDTO)
                .collect(Collectors.toList());
        resultDTO.setDetailList(detailDTOList);

        return resultDTO;
    }

    @Override
    public List<OverseasWarehouseInboundDetailDTO.ViewListDTO> viewList(OverseasWarehouseInboundDTO.ViewListReqDTO dto) {
        // 详情列表
        List<OverseasWarehouseInboundDetailEntity> detailEntityList = Collections.emptyList();
        // 主表ID
        List<String> mainIds = Collections.emptyList();
        if (RequestIdTypeEnum.MAIN_ID.equals(dto.getRequestIdType())) {
            detailEntityList = overseasWarehouseInboundDetailService.getByMainIds(dto.getRequestIdList());
            mainIds = dto.getRequestIdList();
        } else if (RequestIdTypeEnum.DETAIL_ID.equals(dto.getRequestIdType())) {
            detailEntityList = overseasWarehouseInboundDetailService.getByIds(dto.getRequestIdList());
            // 主键IDS
            mainIds = detailEntityList.stream()
                    .map(OverseasWarehouseInboundDetailEntity::getMainId)
                    .distinct()
                    .collect(Collectors.toList());
        }
        if (CollectionUtils.isEmpty(detailEntityList)) {
            return Collections.emptyList();
        }
        Map<String, OverseasWarehouseInboundEntity> mainEntityMap = this.mapByIds(mainIds);
        // 组合
        return detailEntityList.stream()
                .map(e -> OverseasWarehouseInboundConverter.INSTANCE.detailEntityToViewListDTO(e, mainEntityMap.get(e.getMainId())))
                .collect(Collectors.toList());
    }

    @Override
    public List<OverseasWarehouseInboundDTO.CountDTO> listCount(PermissionsDTO dto) {
        QueryChainWrapper<OverseasWarehouseInboundEntity> queryWrapper = query();

        queryWrapper.select("count(id) as count", "instock_status")
                .groupBy(OverseasWarehouseInboundEntity.INSTOCK_STATUS);
        if (StringUtils.isNotBlank(dto.getPermissionSql())){
            queryWrapper.last(dto.getPermissionSql());
        }
        List<OverseasWarehouseInboundEntity> inStockStatusList = queryWrapper.list();

        Map<String, Integer> countMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(inStockStatusList)){
            countMap = inStockStatusList
                    .stream()
                    .collect(Collectors.toMap(OverseasWarehouseInboundEntity::getInstockStatus, OverseasWarehouseInboundEntity::getCount));
        }
        Map<String, Integer> finalCountMap = countMap;
        return Arrays.stream(OverseasInstockStatusEnum.values())
                .map(e-> new OverseasWarehouseInboundDTO.CountDTO(e.getCode(), finalCountMap.getOrDefault(e.getCode(), 0)))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public BatchResultDTO cancel(String id) {
        OverseasWarehouseInboundEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException(ApiError.OVERSEAS_WAREHOUSE_INBOUND_NOT_EXIST));
        // 只有待提交的单据允许撤销
        if (Objects.equals(entity.getInstockStatus(), OverseasInstockStatusEnum.TO_BE_SHIPPED.getCode())) {
            throw new ServiceException(ApiError.OVERSEAS_WAREHOUSE_INBOUND_NOT_CANCEL);
        }
        // 更新状态
        entity.setInstockStatus(OverseasInstockStatusEnum.CANCELED.getCode());
        if (! this.updateById(entity)){
            throw new ServiceException("【海外入库单】更新状态失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】取消了单据编号为【{}】的海外入库单", commonService.getUserInfo().getUserName(), entity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.OVERSEAS_WAREHOUSE_INBOUND.getCode(), entity.getId(),"取消操作");
        // TODO 调用第三方取消接口

        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.CANCEL_PROCESS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public BatchResultDTO delete(String id) {
        OverseasWarehouseInboundEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException(ApiError.OVERSEAS_WAREHOUSE_INBOUND_NOT_EXIST));
        // 只有取消的单据允许删除
        if (Objects.equals(entity.getInstockStatus(), OverseasInstockStatusEnum.CANCELED.getCode())) {
            throw new ServiceException(ApiError.OVERSEAS_WAREHOUSE_INBOUND_NOT_DELETE);
        }
        // 更新状态
        if (!this.removeById(id)){
            throw new ServiceException("【海外入库单】更新状态失败");
        }
        // 操作日志
        String msg = StrUtil.format("用户【{}】删除了单据编号为【{}】的海外入库单", commonService.getUserInfo().getUserName(), entity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.OVERSEAS_WAREHOUSE_INBOUND.getCode(), entity.getId(),"删除操作");

        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DELETE);
    }

    @Override
    public Boolean exportExcel(OverseasWarehouseInboundDTO.ExportDTO dto, HttpServletResponse response) {
        List<OverseasWarehouseInboundDTO.ListDTO> list = baseMapper.listExportExcel(dto);
        if (CollectionUtils.isEmpty(list)) {
            return Boolean.TRUE;
        }
        //数据处理
        fillList(list);
        List<ExportOverseasWarehouseInboundExcelDTO> resultList = BeanMapperUtils.copyList(ExportOverseasWarehouseInboundExcelDTO.class, list);
        String fileName = "海外入库单数据";
        try {
            ExcelUtil.export(fileName, "海外入库单数据", resultList, ExportOverseasWarehouseInboundExcelDTO.class, response);
        } catch (Exception e) {
            throw new ServiceException(ApiError.ERROR_1015);
        }
        return Boolean.TRUE;
    }

    @Override
    public List<String> getReceiptNumbersForStatus(List<String> statusList) {
        return this.list(Wrappers.<OverseasWarehouseInboundEntity>lambdaQuery()
                        .in(OverseasWarehouseInboundEntity::getInstockStatus, statusList))
                .stream()
                .map(OverseasWarehouseInboundEntity::getCode)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 分页查询、导出 数据处理
     */
    private void fillList(List<OverseasWarehouseInboundDTO.ListDTO> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        // 属性赋值
        for (OverseasWarehouseInboundDTO.ListDTO data : list) {
            // 入库类型名称
            data.setInstockTypeName(OverseasInstockTypeEnum.getNameByCode(data.getInstockType()));
            // 入库状态名称
            data.setInstockStatusName(OverseasInstockStatusEnum.getName(data.getInstockStatus()));
            // 完结状态名称
            data.setFinishStatusName(OverseasFinishStatusEnum.getNameByCode(data.getFinishStatus()));
            // 物流方式
            data.setLogisticsMethodName(LogisticsMethodEnum.getName(data.getLogisticsMethod()));
            // 交货方式
            data.setDeliveryModeName(OverseasDeliveryModeEnum.getNameByCode(data.getDeliveryMode()));
        }
    }

}
