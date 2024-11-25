package com.erp.server.wms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelCommonException;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.constant.ApproveType;
import com.common.business.dto.base.ApproveOneDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.*;
import com.erp.model.dmp.dto.DmpPushWdtDTO;
import com.erp.model.dmp.dto.DmpPushWdtDetailDTO;
import com.erp.model.dmp.dto.ThirdMappingDTO;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.entity.SysAccountingCompanyEntity;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.dto.WarehouseLocationMoveDTO;
import com.erp.model.wms.dto.WarehouseLocationMoveDTO.PcAddDTO;
import com.erp.model.wms.dto.WarehouseLocationMoveDetailDTO;
import com.erp.model.wms.dto.excel.MoveInfoExcelDTO;
import com.erp.model.wms.dto.inventory.*;
import com.erp.model.wms.entity.CfgSettingEntity;
import com.erp.model.wms.entity.WarehouseLocationEntity;
import com.erp.model.wms.entity.WarehouseLocationMoveDetailEntity;
import com.erp.model.wms.entity.WarehouseLocationMoveEntity;
import com.erp.model.wms.enums.CfgSettingEnum;
import com.erp.model.wms.enums.inventory.*;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.rpc.dmp.feign.DmpPushWdtFeign;
import com.erp.rpc.dmp.feign.DmpThirdMappingFeign;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.wms.listener.MoveInfoExcelListener;
import com.erp.server.wms.mapper.WarehouseLocationMoveMapper;
import com.erp.server.wms.service.*;
import com.erp.server.wms.wdt.SyncWdtOtherInStockService;
import com.erp.server.wms.wdt.SyncWdtOtherOutStockService;
import com.sdk.wangdian.sdk.api.wms.stockin.dto.CreateOtherStockinRequest;
import com.sdk.wangdian.sdk.api.wms.stockout.dto.CommonCreateBillGoodsReq;
import com.sdk.wangdian.sdk.api.wms.stockout.dto.CreateOtherStockoutRequest;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_PDA_WAREHOUSE_LOCATION_MOVE_INFO;

/**
 * <p>
 * 仓位移动主表 服务实现类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-08-24
 */
@Slf4j
@Service
public class WarehouseLocationMoveServiceImpl extends SuperServiceImpl<WarehouseLocationMoveMapper, WarehouseLocationMoveEntity> implements WarehouseLocationMoveService {
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private DocNoGenHelper docNoGenHelper;
    @Resource
    private WorkflowFeign workflowFeign;
    @Resource
    private WarehouseLocationMoveDetailService warehouseLocationMoveDetailService;
    @Resource
    private InventoryService inventoryService;
    @Resource
    private WarehouseService warehouseService;
    @Resource
    private InventoryTransCoreService inventoryTransCoreService;
    @Resource
    private PlmTaskFeign plmTaskFeign;
    @Resource
    private WarehouseLocationService warehouseLocationService;
    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    @Lazy
    private WarehouseLocationMoveServiceImpl service;

    @Resource
    private DmpMqFeign dmpMqFeign;

    @Resource
    private SyncWdtOtherOutStockService wdtOtherOutStockService;

    @Resource
    private SyncWdtOtherInStockService wdtOtherInStockService;
    @Resource
    private DmpThirdMappingFeign dmpThirdMappingFeign;
    @Resource
    private CfgSettingService cfgSettingService;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @Resource
    private DmpPushWdtFeign dmpPushWdtFeign;
    @Resource
    private AbstractWdtService abstractWdtService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public String add(WarehouseLocationMoveDTO.AddDTO addDTO) {
        WarehouseLocationMoveEntity warehouseLocationMoveEntity = new WarehouseLocationMoveEntity();
        BeanMapperUtils.copy(addDTO, warehouseLocationMoveEntity);
        // 数据处理
        handleData(warehouseLocationMoveEntity);

        log.info("开始新增仓位移动主单");
        // 生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_CWYD);
        warehouseLocationMoveEntity.setCode(code);
        boolean save = super.save(warehouseLocationMoveEntity);
        if(!save) {
            throw new ServiceException("仓位移动主单保存失败");
        }

        // 操作日志
        String msg = CharSequenceUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "仓位移动主单" , warehouseLocationMoveEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.WAREHOUSE_LOCATION_MOVE_INFO.getCode(), warehouseLocationMoveEntity.getId(), "新增操作");
        // 新增明细
        warehouseLocationMoveDetailService.add(addDTO, warehouseLocationMoveEntity);
        return warehouseLocationMoveEntity.getId();
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public String pcAdd(WarehouseLocationMoveDTO.PcAddDTO pcAddDTO) {
        //批量添加
        List<WarehouseLocationMoveDetailDTO.AddDTO> detailList = pcAddDTO.getDetailList();
        if (CollectionUtils.isEmpty(detailList)) {
            throw new ServiceException("产品信息不能为空");
        }
        WarehouseLocationMoveEntity warehouseLocationMoveEntity = new WarehouseLocationMoveEntity();
        BeanMapperUtils.copy(pcAddDTO, warehouseLocationMoveEntity);
        if (CharSequenceUtil.isBlank(warehouseLocationMoveEntity.getId()) && ObjectUtil.isNull(warehouseLocationMoveEntity.getBillDate())) {
            warehouseLocationMoveEntity.setBillDate(LocalDate.now());
        }
        log.info("开始新增仓位移动主单");
        // 生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_CWYD);
        warehouseLocationMoveEntity.setCode(code);
        boolean save = super.save(warehouseLocationMoveEntity);
        if(!save) {
            throw new ServiceException("仓位移动主单保存失败");
        }
        // 操作日志
        String msg = CharSequenceUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "仓位移动主单" , warehouseLocationMoveEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.WAREHOUSE_LOCATION_MOVE_INFO.getCode(), warehouseLocationMoveEntity.getId(), "新增操作");
        //新增明细
        pcAddDTO.getDetailList().forEach(detail->{
            WarehouseLocationMoveDTO.ViewDTO viewDTO = new WarehouseLocationMoveDTO.ViewDTO();
            BeanMapperUtils.copy(detail, viewDTO);
            WarehouseLocationMoveEntity moveInfoEntity = new WarehouseLocationMoveEntity();
            BeanMapperUtils.copy(detail, moveInfoEntity);
            // 数据处理
            moveInfoEntity.setBillDate(pcAddDTO.getBillDate());
            moveInfoEntity.setWarehouseId(viewDTO.getWarehouseId());
            handleData(moveInfoEntity);
            WarehouseLocationMoveDTO.AddDTO addDTO = new WarehouseLocationMoveDTO.AddDTO();
            addDTO.setDetailList(Collections.singletonList(detail));
            addDTO.setWarehouseId(viewDTO.getWarehouseId());
            addDTO.setPcShow(true);
            // 新增明细
            warehouseLocationMoveDetailService.add(addDTO, warehouseLocationMoveEntity);
        });
        return warehouseLocationMoveEntity.getId();
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(WarehouseLocationMoveDTO.UpdateDTO updateDTO) {
        WarehouseLocationMoveEntity old = super.getById(updateDTO.getId());
        if (Objects.isNull(old)){
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "仓位移动主单");
        }
        // 待提交和审核不通过允许修改
        if (!ApproveStatusEnum.allowUpdateStatus(old.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_1029);
        }
        WarehouseLocationMoveEntity warehouseLocationMoveEntity = BeanMapperUtils.map(WarehouseLocationMoveEntity.class, updateDTO);
        // 数据处理
        handleData(warehouseLocationMoveEntity);
        log.info("编辑 开始修改仓位移动主单数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(warehouseLocationMoveEntity);
        if(!save) {
            throw new ServiceException("仓位移动主单保存失败");
        }
        // 修改明细数据（包含增删改）
        warehouseLocationMoveDetailService.update(updateDTO, warehouseLocationMoveEntity);
        // 记录主单操作日志
        log.info("编辑 开始记录仓位移动主单日志数据，单号：【{}】", warehouseLocationMoveEntity.getCode());
        String msg = CharSequenceUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), warehouseLocationMoveEntity.getCode(), "仓位移动主单");
        operateLogService.addModuleOperateLogByObj(old, warehouseLocationMoveEntity, ModuleTypeEnum.WAREHOUSE_LOCATION_MOVE_INFO.getCode(), warehouseLocationMoveEntity.getId(), msg);
        return Boolean.TRUE;
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean pcUpdate(WarehouseLocationMoveDTO.PcUpdateDTO pcUpdateDTO) {
        List<WarehouseLocationMoveDetailDTO.UpdateDTO> detailList = pcUpdateDTO.getDetailList();
        if (CollectionUtils.isEmpty(detailList)) {
            throw new ServiceException("产品信息不能为空");
        }
        WarehouseLocationMoveEntity old = super.getById(pcUpdateDTO.getId());
        if (Objects.isNull(old)){
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "仓位移动主单");
        }
        // 待提交和审核不通过允许修改
        if (!ApproveStatusEnum.allowUpdateStatus(old.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_1029);
        }
        WarehouseLocationMoveEntity warehouseLocationMoveEntity =  BeanMapperUtils.map(WarehouseLocationMoveEntity.class, pcUpdateDTO);
        if (CharSequenceUtil.isBlank(pcUpdateDTO.getWarehouseId())){
            warehouseLocationMoveEntity.setWarehouseId("");
            warehouseLocationMoveEntity.setWarehouseName("");
        }
        // 数据处理
        log.info("编辑 开始修改仓位移动主单数据，单号：【{}】", old.getCode());
        int i = baseMapper.updateById(warehouseLocationMoveEntity);
        if(i<=0) {
            throw new ServiceException("仓位移动主单保存失败");
        }

        // 记录主单操作日志
        log.info("编辑 开始记录仓位移动主单日志数据，单号：【{}】", warehouseLocationMoveEntity.getCode());
        String msg = CharSequenceUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), warehouseLocationMoveEntity.getCode(), "仓位移动主单");
        operateLogService.addModuleOperateLogByObj(old, warehouseLocationMoveEntity, ModuleTypeEnum.WAREHOUSE_LOCATION_MOVE_INFO.getCode(), warehouseLocationMoveEntity.getId(), msg);
        //修改明细
        WarehouseLocationMoveDTO.UpdateDTO updateDto = new WarehouseLocationMoveDTO.UpdateDTO();

        List<Object> detailResultList = new ArrayList<>();
//        detailList.forEach(detail->{
//            WarehouseLocationMoveDTO.ViewDTO viewDTO = new WarehouseLocationMoveDTO.ViewDTO();
//            BeanMapperUtils.copy(detail, viewDTO);
//            WarehouseLocationMoveEntity moveInfoEntity = new WarehouseLocationMoveEntity();
//            BeanMapperUtils.copy(detail, moveInfoEntity);
//            // 数据处理
//            moveInfoEntity.setBillDate(pcUpdateDTO.getBillDate());
//            moveInfoEntity.setWarehouseId(viewDTO.getWarehouseId());
//            handleData(moveInfoEntity);
//            detailResultList.add(detail);
//        });
        // 修改明细数据（包含增删改）
        updateDto.setDetailList(detailList);
//        UpdateDto.setWarehouseId(detail.getWarehouseId());
        updateDto.setPcShow(true);
        warehouseLocationMoveDetailService.update(updateDto, warehouseLocationMoveEntity);

        return Boolean.TRUE;
    }

    @Override
    public PagingVO<WarehouseLocationMoveDTO.ListDTO> paging(PagingDTO<WarehouseLocationMoveDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        WarehouseLocationMoveDTO.PagingParamDTO params = pagingParamDTO.getParams();
        List<String> approveStatusList = params.getApproveStatusList();
        if (approveStatusList.contains(ApproveStatusEnum.APPROVE.getCode())) {
            List<LocalDate> dateList = new ArrayList<>();
            LocalDate now = LocalDate.now();
            dateList.add(now.minusDays(30));
            dateList.add(now);
            params.setBillDateList(dateList);
        }

        IPage<WarehouseLocationMoveDTO.PdaListDTO> pageData = this.baseMapper.pdaPaging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public PagingVO<WarehouseLocationMoveDTO.ListDTO> pcPaging(PagingDTO<WarehouseLocationMoveDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());

        IPage<WarehouseLocationMoveDTO.PdaPcListDTO> pageData = this.baseMapper.pdaPcPaging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO(pageData);
        }
        // 数据处理
        List<WarehouseLocationMoveDTO.PdaPcListDTO> itemDTOList = pageData.getRecords();
        List<String> skuList = itemDTOList.stream().map(req -> req.getSkuId()).distinct().collect(Collectors.toList());
        //feign获取产品信息
        List<SkuVO> skuVOList = plmTaskFeign.listSkuProductByIds(skuList);
        List<String> warehouseIds = itemDTOList.stream().map(req -> req.getWarehouseId()).distinct().collect(Collectors.toList());
        List<String> infoWarehouseIds = itemDTOList.stream().map(req -> req.getInfoWarehouseId()).distinct().collect(Collectors.toList());
        warehouseIds.addAll(infoWarehouseIds);
        dataProcess(itemDTOList, skuVOList, warehouseIds);
        return new PagingVO(pageData);
    }

    @Override
    public List<WarehouseLocationMoveDTO.PdaTabListDTO> tabList(PermissionsDTO dto) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(30);
        PdaTabFlagEnum[] values = PdaTabFlagEnum.values();
        List<WarehouseLocationMoveDTO.PdaTabListDTO> list = new ArrayList<>();
        for (PdaTabFlagEnum item : values) {
            WarehouseLocationMoveDTO.PagingParamDTO pagingParamDTO = new WarehouseLocationMoveDTO.PagingParamDTO();
            pagingParamDTO.setPermissionSql(dto.getPermissionSql());
            pagingParamDTO.setInvalidStatus(Boolean.FALSE);
            WarehouseLocationMoveDTO.PdaTabListDTO resultDTO = new WarehouseLocationMoveDTO.PdaTabListDTO();
            Integer count = MathUtil.ZERO;
            if (PdaTabFlagEnum.WAIT_SUBMIT_AND_REJECT.getCode().equals(item.getCode())) {
                pagingParamDTO.setApproveStatusList(Arrays.asList(ApproveStatusEnum.WAIT_SUBMIT.getStatus(), ApproveStatusEnum.REJECT.getStatus()));
                count = this.baseMapper.listCount(pagingParamDTO);
            }
            if (PdaTabFlagEnum.APPROVE_ING.getCode().equals(item.getCode())) {
                pagingParamDTO.setApproveStatusList(Collections.singletonList(ApproveStatusEnum.APPROVE_ING.getStatus()));
                count = this.baseMapper.listCount(pagingParamDTO);
            }
            if (PdaTabFlagEnum.APPROVE.getCode().equals(item.getCode())) {
                pagingParamDTO.setApproveStatusList(Collections.singletonList(ApproveStatusEnum.APPROVE.getStatus()));
                List<LocalDate> dateList = new ArrayList<>();
                dateList.add(startDate);
                dateList.add(endDate);
                pagingParamDTO.setBillDateList(dateList);
                count = this.baseMapper.listCount(pagingParamDTO);
            }
            resultDTO.setCount(ObjectUtils.isEmpty(count) ? MathUtil.ZERO : count);
            resultDTO.setTabFlag(item.getCode());
            list.add(resultDTO);
        }
        return list;
    }

    @Override
    public List<WarehouseLocationMoveDTO.PdaTabListDTO> pcTabList(PermissionsDTO dto) {
        ApproveStatusEnum[] values = ApproveStatusEnum.values();
        List<WarehouseLocationMoveDTO.PdaTabListDTO> list = new ArrayList<>();
        for (ApproveStatusEnum item : values) {
            WarehouseLocationMoveDTO.PagingParamDTO pagingParamDTO = new WarehouseLocationMoveDTO.PagingParamDTO();
            pagingParamDTO.setPermissionSql(dto.getPermissionSql());
//            pagingParamDTO.setInvalidStatus(Boolean.FALSE);
            WarehouseLocationMoveDTO.PdaTabListDTO resultDTO = new WarehouseLocationMoveDTO.PdaTabListDTO();
            Integer count = MathUtil.ZERO;
            if (ApproveStatusEnum.WAIT_SUBMIT.getCode().equals(item.getCode())) {
                pagingParamDTO.setApproveStatusList(Collections.singletonList(ApproveStatusEnum.WAIT_SUBMIT.getStatus()));
                count = this.baseMapper.listCount(pagingParamDTO);
            }
            if (ApproveStatusEnum.REJECT.getCode().equals(item.getCode())) {
                pagingParamDTO.setApproveStatusList(Collections.singletonList(ApproveStatusEnum.REJECT.getStatus()));
                count = this.baseMapper.listCount(pagingParamDTO);
            }
            if (ApproveStatusEnum.APPROVE_ING.getCode().equals(item.getCode())) {
                pagingParamDTO.setApproveStatusList(Collections.singletonList(ApproveStatusEnum.APPROVE_ING.getStatus()));
                count = this.baseMapper.listCount(pagingParamDTO);
            }
            if (ApproveStatusEnum.APPROVE.getCode().equals(item.getCode())) {
                pagingParamDTO.setApproveStatusList(Collections.singletonList(ApproveStatusEnum.APPROVE.getStatus()));
                count = this.baseMapper.listCount(pagingParamDTO);
            }
            resultDTO.setCount(ObjectUtils.isEmpty(count) ? MathUtil.ZERO : count);
            resultDTO.setTabFlag(item.getCode());
            list.add(resultDTO);
        }
        //查询所有的数量
        WarehouseLocationMoveDTO.PagingParamDTO pagingParamDTO = new WarehouseLocationMoveDTO.PagingParamDTO();
        Integer totalCount = this.baseMapper.listCount(pagingParamDTO);
        WarehouseLocationMoveDTO.PdaTabListDTO resultDTO = new WarehouseLocationMoveDTO.PdaTabListDTO();
        resultDTO.setTabFlag(PdaTabFlagPcEnum.ALL.getCode());
        resultDTO.setCount(totalCount);
        list.add(resultDTO);
        return list;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO submit(String id) {
        WarehouseLocationMoveEntity entity = getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException("未找到仓位移动主单数据");
        }
        validateSubmit(entity);
        // 更新单据审核状态
        log.info("提交 开始修改仓位移动主单状态数据，id：【{}】", id);
        this.updateApproveStatus(id, ApproveStatusEnum.APPROVE_ING.getStatus());

        log.info("提交 开始启动仓位移动主单流程，id=：【{}】", entity.getId());
        startProcess(entity);
        // 记录操作日志
        log.info("提交 开始记录仓位移动主单日志数据，id：【{}】", id);
        String msg = CharSequenceUtil.format("用户【{}】单号为【{}】的【{}】单据提交审核 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "仓位移动主单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.WAREHOUSE_LOCATION_MOVE_INFO.getCode(), entity.getId(), "提交操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.SUBMIT);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO pcSubmit(String id) {
        WarehouseLocationMoveDetailEntity warehouseLocationMoveDetailEntity = warehouseLocationMoveDetailService.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到仓位移动明细数据"));
        String mainId = warehouseLocationMoveDetailEntity.getMainId();
        WarehouseLocationMoveEntity entity = getById(mainId);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException("未找到仓位移动主单数据");
        }
        validateSubmit(entity);
        // 更新单据审核状态
        log.info("提交 开始修改仓位移动主单状态数据，id：【{}】", mainId);
        this.updateApproveStatus(mainId, ApproveStatusEnum.APPROVE_ING.getStatus());

        log.info("提交 开始启动仓位移动主单流程，id=：【{}】", entity.getId());
        startProcess(entity);
        // 记录操作日志
        log.info("提交 开始记录仓位移动主单日志数据，id：【{}】", mainId);
        String msg = CharSequenceUtil.format("用户【{}】单号为【{}】的【{}】单据提交审核 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "仓位移动主单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.WAREHOUSE_LOCATION_MOVE_INFO.getCode(), entity.getId(), "提交操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.SUBMIT);
    }

    @Override
    public String addAndSubmit(WarehouseLocationMoveDTO.AddDTO dto) {
        //新增和提交分开事务
        // 新增
        String id = service.add(dto);
        // 提交
        service.submit(id);
        return id;
    }

    @Override
    public void updateAndSubmit(WarehouseLocationMoveDTO.UpdateDTO dto) {
        //分开事务
        // 修改
        service.update(dto);
        // 提交
        service.submit(dto.getId());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO approve(ApproveOneDTO dto) {
        ApproveTypeEnum approveType = ApproveTypeEnum.getByCode(dto.getType());
        if(Objects.equals(approveType, ApproveTypeEnum.REJECT) && StrUtils.isEmpty(dto.getComment())) {
            throw new ServiceException(ApiError.REJECT_COMMENT_NOT_EMPTY);
        }
        WarehouseLocationMoveEntity entity = getById(dto.getId());
        // 审核中的数据允许审核
        if(!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING)) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        // 调用流程审核
        approveProcess(entity, dto);
        // 操作日志
        String msg = CharSequenceUtil.format("用户【{}】单号为【{}】的【{}】单据审核操作  审核结果：【{}】 审核意见 ：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "仓位移动主单", approveType.getName(), dto.getComment());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.WAREHOUSE_LOCATION_MOVE_INFO.getCode(), entity.getId(), "审核操作");
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(approveType);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.approveStatus(approveStatus));
    }
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO pcApprove(ApproveOneDTO dto) {
        dto.setPcShow(true);
        return approve(dto);
    }

    /**
    * 审核流程处理
    * @param entity
    * @param dto
    */
    private void approveProcess(WarehouseLocationMoveEntity entity, ApproveOneDTO dto) {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ProcessManagementDTO.ApproveDTO approveDTO = new ProcessManagementDTO.ApproveDTO();
        approveDTO.setBusinessId(entity.getId());
        approveDTO.setBusinessKey(ModuleTypeEnum.WAREHOUSE_LOCATION_MOVE_INFO.getCode());
        approveDTO.setApproveType(ApproveTypeEnum.getByCode(dto.getType()));
        approveDTO.setComment(dto.getComment());
        approveDTO.setUserId(userInfo.getUid());
        approveDTO.setVariablesMap(BeanUtil.beanToMap(entity));
        ApiResult<ProcessManagementDTO.ApproveResultDTO> approveResult = workflowFeign.approve(approveDTO);
        Integer code = approveResult.getCode();
        if (200 != code) {
            throw new ServiceException(ApiError.ERROR_94006);
        }
        ProcessManagementDTO.ApproveResultDTO data = approveResult.getData();
        if (ObjectUtil.isEmpty(data.getIsExistProcess()) || !data.getIsExistProcess()) {
            // 无需走流程的数据则直接更新状态
            approveEnd(dto, entity);
        }
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO disApprove(String id) {
        WarehouseLocationMoveEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到仓位移动主单单数据"));
        // 反审核条件判断
        validateDisApprove(entity);

        // 更新审核信息
        updateForDisApprove(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        // 回滚库存
        InventoryBatchUnApproveDTO inventoryBatchUnApproveDTO = new InventoryBatchUnApproveDTO(InventorySourceTypeEnum.WAREHOUSE_LOCATION_MOVE_INFO, Collections.singletonList(id));
        inventoryTransCoreService.batchUnApprove(inventoryBatchUnApproveDTO);


        //发送旺店通
        syncDisApproveInfoToWdt(entity,SyncOperateEnum.OPERATE_DISAPPROVE);
        // 操作日志
        String msg = CharSequenceUtil.format("用户【{}】单号为【{}】的【{}】单据反审核操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "仓位移动主单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.WAREHOUSE_LOCATION_MOVE_INFO.getCode(), entity.getId(), "反审核操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DISAPPROVE);
    }

    private void syncDisApproveInfoToWdt(WarehouseLocationMoveEntity entity,SyncOperateEnum syncOperateEnum) {
        // 如果存在黑名单则跳过推送旺店通
        CfgSettingEntity blackListEntity = cfgSettingService.getByKey(CfgSettingEnum.WAREHOUSE_LOCATION_MOVE_BLACKLIST.getCode());
        List<String> blackList = ListUtil.empty();
        if (ObjectUtil.isNotEmpty(blackListEntity) && ObjectUtil.isNotEmpty(blackListEntity.getDataJson())) {
            JSONObject dataJson = blackListEntity.getDataJson();
            blackList = new ArrayList<>(dataJson.getBeanList("blackList", String.class));
        }
        if (blackList.contains(entity.getCode())) {
            log.warn("单号【{}】的仓位移动单据，被加入黑名单，不推送旺店通", entity.getCode());
            return;
        }

        //发送旺店通
        List<WarehouseLocationMoveDetailEntity> detailEntityList = warehouseLocationMoveDetailService.listByMainIds(Collections.singletonList(entity.getId()));

        HashSet<String> warehouseIdSet = new HashSet<>();
        for (WarehouseLocationMoveDetailEntity detail : detailEntityList) {
            warehouseIdSet.add(detail.getWarehouseId());
        }
        //查询三方仓库映射
        List<ThirdMappingDTO.WarehouseMappingDTO> mappingList = dmpThirdMappingFeign.listMappingBySysIds(new ArrayList<>(warehouseIdSet), "wdt");
        if(mappingList.isEmpty()){
            return;
        }
        //同步旺店通 审核{调入仓位做其他入库单，调出仓位做其他出库单}，反审核{调入仓位做其他出库单，调出仓位做其他入库单}
        List<CreateOtherStockoutRequest.GoodsList> outgoodsList = new ArrayList<>();
        List<CreateOtherStockinRequest.GoodsList> ingoodsList = new ArrayList<>();
        for (WarehouseLocationMoveDetailEntity detailEntity : detailEntityList) {
            CreateOtherStockoutRequest.GoodsList outGoods = new CreateOtherStockoutRequest.GoodsList();
            outGoods.setSpecNo(detailEntity.getSkuNo());
            outGoods.setNum(BigDecimal.valueOf(detailEntity.getQty()));
            outGoods.setPositionNo(syncOperateEnum.equals(SyncOperateEnum.OPERATE_APPROVE) ? detailEntity.getOutWarehouseLocation() : detailEntity.getInWarehouseLocation());
            outGoods.setWarehouseId(detailEntity.getWarehouseId());
            outgoodsList.add(outGoods);

            CreateOtherStockinRequest.GoodsList inGoods = new CreateOtherStockinRequest.GoodsList();
            inGoods.setSpecNo(detailEntity.getSkuNo());
            inGoods.setNum(BigDecimal.valueOf(detailEntity.getQty()));
            inGoods.setPositionNo(syncOperateEnum.equals(SyncOperateEnum.OPERATE_APPROVE) ? detailEntity.getInWarehouseLocation() : detailEntity.getOutWarehouseLocation());
            inGoods.setWarehouseId(detailEntity.getWarehouseId());
            ingoodsList.add(inGoods);
        }
        //转换为同一个其他出库单
        abstractWdtService.transfer(syncOperateEnum, entity.getId(), entity.getCode(), outgoodsList, SourceTypeEnum.OTHER_OUTSTOCK);

        //转换为同一个其他入库单
        abstractWdtService.transfer(syncOperateEnum, entity.getId(), entity.getCode(), ingoodsList, SourceTypeEnum.OTHER_INSTOCK);
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO pcDisApprove(String id) {
        WarehouseLocationMoveDetailEntity warehouseLocationMoveDetailEntity = warehouseLocationMoveDetailService.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到仓位移动明细数据"));
        String mainId = warehouseLocationMoveDetailEntity.getMainId();
        WarehouseLocationMoveEntity entity = super.getByIdOpt(warehouseLocationMoveDetailEntity.getMainId()).orElseThrow(() -> new ServiceException("未找到仓位移动主单单数据"));
        // 反审核条件判断
        validateDisApprove(entity);

        // 更新审核信息
        updateForDisApprove(mainId, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        // 回滚库存
        InventoryBatchUnApproveDTO inventoryBatchUnApproveDTO = new InventoryBatchUnApproveDTO(InventorySourceTypeEnum.WAREHOUSE_LOCATION_MOVE_INFO, Collections.singletonList(mainId));
        inventoryTransCoreService.batchUnApprove(inventoryBatchUnApproveDTO);

        // 操作日志
        String msg = CharSequenceUtil.format("用户【{}】单号为【{}】的【{}】单据反审核操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "仓位移动主单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.WAREHOUSE_LOCATION_MOVE_INFO.getCode(), entity.getId(), "反审核操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DISAPPROVE);
    }

    private Boolean validateDisApprove(WarehouseLocationMoveEntity entity) {
        // 已审核支持反审核
        if (!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE)) {
            throw new ServiceException(ApiError.ERROR_98014);
        }
        if (SourceTypeEnum.PICKING_LISTS_ADD.getCode().equals(entity.getSourceType()) || SourceTypeEnum.PICKING_LISTS_SUBTRACT.getCode().equals(entity.getSourceType())){
            throw new ServiceException(ApiError.ERROR_99135);
        }
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO delete(String id) {
        WarehouseLocationMoveEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到仓位移动主单数据"));
        // 只有待提交数据允许删除
        if (!Objects.equals(ApproveStatusEnum.WAIT_SUBMIT, entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98032);
        }
        // 删除明细数据
        warehouseLocationMoveDetailService.removeByMainId(id);
        // 删除主单数据
        log.info("删除 开始删除仓位移动主单主单数据，id：【{}】", id);
        super.removeById(id);
        // 删除日志数据
        log.info("删除 开始删除仓位移动主单日志数据，id：【{}】", id);
        String msg = CharSequenceUtil.format("用户【{}】单号为【{}】的【{}】单据删除操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "仓位移动主单");
        operateLogService.addModuleOperateLog(msg, null, entity.getCode(), "删除仓位移动主单数据");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DELETE);
    }
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO pcDelete(String id) {
        WarehouseLocationMoveDetailEntity warehouseLocationMoveDetailEntity = warehouseLocationMoveDetailService.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到仓位移动明细数据"));
        WarehouseLocationMoveEntity entity = super.getByIdOpt(warehouseLocationMoveDetailEntity.getMainId()).orElseThrow(()->new ServiceException("未找到仓位移动主单数据"));
        // 只有待提交数据允许删除
        if (!Objects.equals(ApproveStatusEnum.WAIT_SUBMIT, entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98032);
        }
        // 删除明细数据
        warehouseLocationMoveDetailService.removeByMainId(warehouseLocationMoveDetailEntity.getMainId());
        // 删除主单数据
        log.info("删除 开始删除仓位移动主单主单数据，id：【{}】", id);
        super.removeById(id);
        // 删除日志数据
        log.info("删除 开始删除仓位移动主单日志数据，id：【{}】", id);
        String msg = CharSequenceUtil.format("用户【{}】单号为【{}】的【{}】单据删除操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "仓位移动主单");
        operateLogService.addModuleOperateLog(msg, null, entity.getCode(), "删除仓位移动主单数据");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DELETE);
    }

    @Override
    public String addAndApprove(WarehouseLocationMoveDTO.AddDTO dto) {
        String id = service.addAndSubmit(dto);
        service.approve(new ApproveOneDTO(id, ApproveTypeEnum.PASS.getStatus(),"新增自动审核"));
        return id;
    }

    @Override
    public String updateAndApprove(WarehouseLocationMoveDTO.UpdateDTO dto) {
        service.updateAndSubmit(dto);
        service.approve(new ApproveOneDTO(dto.getId(), ApproveTypeEnum.PASS.getStatus(),"更新自动审核"));
        return "";
    }

    @Override
    public PagingVO<WarehouseLocationMoveDTO.PdaPcListDTO> exportWarehouseLocationMoveInfo(PagingDTO<WarehouseLocationMoveDTO.ExportDTO> dto) {
        Page<WarehouseLocationMoveDTO.PdaPcListDTO> page = baseMapper.listExport(new Page<>(dto.getCurrPage(), dto.getPageSize()),dto.getParams());
        List<String> skuList = page.getRecords().stream().map(WarehouseLocationMoveDTO.PdaPcListDTO::getSkuId).distinct().collect(Collectors.toList());
        //feign获取产品信息
        List<SkuVO> skuVOList = plmTaskFeign.listSkuProductByIds(skuList);
        List<String> warehouseIds = page.getRecords().stream().map(WarehouseLocationMoveDTO.PdaPcListDTO::getWarehouseId).distinct().collect(Collectors.toList());
        dataProcess(page.getRecords(), skuVOList, warehouseIds);
        return new PagingVO<>(page);
    }

    /**
    * 撤销
    */
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO cancelProcess(String id) {
        WarehouseLocationMoveEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到仓位移动主单数据"));
        // 只有审核中的单据允许撤销
        if (!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING)) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        if (SourceTypeEnum.pickingLists().contains(entity.getSourceType())) {
            throw new ServiceException(ApiError.ERROR_99141);
        }
        // TODO 撤销流程
        log.info("撤销 开始撤销流程，id：【{}】",id);

        log.info("撤销 开始修改仓位移动主单状态，id：【{}】", id);
        updateApproveStatus(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        //操作日志
        log.info("撤销 开始记录操作日志，id：【{}】", id);
        String msg = CharSequenceUtil.format("用户【{}】单号为【{}】的【{}】单据撤销流程操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "仓位移动主单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.WAREHOUSE_LOCATION_MOVE_INFO.getCode(), entity.getId(), "取消流程操作");
        ProcessManagementDTO.RevokeDTO revokeDTO = new ProcessManagementDTO.RevokeDTO();
        revokeDTO.setBusinessId(entity.getId());
        revokeDTO.setBusinessKey(ModuleTypeEnum.WAREHOUSE_LOCATION_MOVE_INFO.getCode());
        revokeDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        workflowFeign.revokeProcess(revokeDTO);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.CANCEL_PROCESS);
    }
  /**
    * 撤销
    */
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO pcCancelProcess(String id) {
        WarehouseLocationMoveDetailEntity warehouseLocationMoveDetailEntity = warehouseLocationMoveDetailService.getById(id);
        String mainId = warehouseLocationMoveDetailEntity.getMainId();
        WarehouseLocationMoveEntity entity = super.getByIdOpt(mainId).orElseThrow(() -> new ServiceException("未找到仓位移动主单数据"));
        // 只有审核中的单据允许撤销
        if (!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING)) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        // TODO 撤销流程
        log.info("撤销 开始撤销流程，id：【{}】",mainId);

        log.info("撤销 开始修改仓位移动主单状态，id：【{}】", mainId);
        updateApproveStatus(mainId, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        //操作日志
        log.info("撤销 开始记录操作日志，id：【{}】", mainId);
        String msg = CharSequenceUtil.format("用户【{}】单号为【{}】的【{}】单据撤销流程操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "仓位移动主单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.WAREHOUSE_LOCATION_MOVE_INFO.getCode(), mainId, "取消流程操作");
        ProcessManagementDTO.RevokeDTO revokeDTO = new ProcessManagementDTO.RevokeDTO();
        revokeDTO.setBusinessId(entity.getId());
        revokeDTO.setBusinessKey(ModuleTypeEnum.WAREHOUSE_LOCATION_MOVE_INFO.getCode());
        revokeDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        workflowFeign.revokeProcess(revokeDTO);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.CANCEL_PROCESS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean  approveEnd(ApproveOneDTO dto, WarehouseLocationMoveEntity entity) {
        if (ObjectUtil.isEmpty(entity)) {
            return Boolean.TRUE;
        }

        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(dto.getType());
        updateForApprove(entity.getId(), approveStatus.getStatus());

        if (ApproveType.PASS.equals(dto.getType())) {
            WarehouseLocationMoveEntity infoEntity = this.getById(entity.getId());
            List<WarehouseLocationMoveDetailEntity> detailEntityList = warehouseLocationMoveDetailService.listByMainIds(Collections.singletonList(infoEntity.getId()));

            List<TransferDTO> transferDTOList = new ArrayList<>();
            for (WarehouseLocationMoveDetailEntity detailEntity : detailEntityList) {
                TransferDTO transferDTO = new TransferDTO();
                transferDTO.setSourceType(InventorySourceTypeEnum.WAREHOUSE_LOCATION_MOVE_INFO);
                transferDTO.setSourceId(infoEntity.getId());
                transferDTO.setSourceCode(infoEntity.getCode());
                transferDTO.setBillDate(infoEntity.getBillDate());
                transferDTO.setSourceDetailId(detailEntity.getId());
                transferDTO.setCurWarehouseId(dto.getPcShow()?
                        (CharSequenceUtil.isNotBlank(detailEntity.getWarehouseId()) ? detailEntity.getWarehouseId() : infoEntity.getWarehouseId()) : infoEntity.getWarehouseId());
                transferDTO.setCurWarehouseLocation(detailEntity.getOutWarehouseLocation());
                transferDTO.setTargetWarehouseId(dto.getPcShow()?
                        (CharSequenceUtil.isNotBlank(detailEntity.getWarehouseId()) ? detailEntity.getWarehouseId() : infoEntity.getWarehouseId()) : infoEntity.getWarehouseId());
                transferDTO.setTargetWarehouseLocation(detailEntity.getInWarehouseLocation());
                transferDTO.setQty(detailEntity.getQty());
                transferDTO.setSkuId(detailEntity.getSkuId());
                transferDTO.setSkuNo(detailEntity.getSkuNo());
                transferDTO.setWarehouseId(dto.getPcShow() ?
                        (CharSequenceUtil.isNotBlank(detailEntity.getWarehouseId()) ? detailEntity.getWarehouseId() : infoEntity.getWarehouseId()) : infoEntity.getWarehouseId());

                transferDTO.setInventoryStatus(InventoryStatusEnum.USABLE);
                transferDTOList.add(transferDTO);
            }

            if (SourceTypeEnum.PICKING_LISTS_ADD.getCode().equals(entity.getSourceType())) {
                InventoryTransferDTO inventoryTransferDTO = new InventoryTransferDTO();
                inventoryTransferDTO.setParamList(transferDTOList);
                inventoryTransferDTO.setBusinessType(InventoryBusinessTypeEnum.WAREHOUSE_LOCATION_MOVE_INFO_ADD.getCode());
                inventoryTransCoreService.approveByType(inventoryTransferDTO);
            }else if (SourceTypeEnum.PICKING_LISTS_SUBTRACT.getCode().equals(entity.getSourceType())) {
                InventoryTransferDTO inventoryTransferDTO = new InventoryTransferDTO();
                inventoryTransferDTO.setParamList(transferDTOList);
                inventoryTransferDTO.setBusinessType(InventoryBusinessTypeEnum.WAREHOUSE_LOCATION_MOVE_INFO_SUBTRACT.getCode());
                inventoryTransCoreService.approveByType(inventoryTransferDTO);
            }else {
                Map<WarehouseLocationMoveDTO.Group, List<WarehouseLocationMoveDetailEntity>> groupListMap = detailEntityList.stream().collect(Collectors.groupingBy(e -> new WarehouseLocationMoveDTO.Group(e.getOutInventoryStatus(), e.getInInventoryStatus())));
                for (Map.Entry<WarehouseLocationMoveDTO.Group, List<WarehouseLocationMoveDetailEntity>> entry : groupListMap.entrySet()) {
                    List<TransferDTO> transferNewDTOList = new ArrayList<>();
                    for (WarehouseLocationMoveDetailEntity detail : entry.getValue()) {
                        TransferDTO transferDTO = new TransferDTO();
                        transferDTO.setSourceType(InventorySourceTypeEnum.WAREHOUSE_LOCATION_MOVE_INFO);
                        transferDTO.setSourceId(infoEntity.getId());
                        transferDTO.setSourceCode(infoEntity.getCode());
                        transferDTO.setBillDate(infoEntity.getBillDate());
                        transferDTO.setSourceDetailId(detail.getId());
                        transferDTO.setCurWarehouseId(dto.getPcShow()?
                                (CharSequenceUtil.isNotBlank(detail.getWarehouseId()) ? detail.getWarehouseId() : infoEntity.getWarehouseId()) : infoEntity.getWarehouseId());
                        transferDTO.setCurWarehouseLocation(detail.getOutWarehouseLocation());
                        transferDTO.setTargetWarehouseId(dto.getPcShow()?
                                (CharSequenceUtil.isNotBlank(detail.getWarehouseId()) ? detail.getWarehouseId() : infoEntity.getWarehouseId()) : infoEntity.getWarehouseId());
                        transferDTO.setTargetWarehouseLocation(detail.getInWarehouseLocation());
                        transferDTO.setQty(detail.getQty());
                        transferDTO.setSkuId(detail.getSkuId());
                        transferDTO.setSkuNo(detail.getSkuNo());
                        transferDTO.setWarehouseId(dto.getPcShow() ?
                                (CharSequenceUtil.isNotBlank(detail.getWarehouseId()) ? detail.getWarehouseId() : infoEntity.getWarehouseId()) : infoEntity.getWarehouseId());

                        transferDTO.setInventoryStatus(InventoryStatusEnum.USABLE);
                        transferNewDTOList.add(transferDTO);
                    }
                    List<TransactionRuleDTO> transactionRuleDTOList = new ArrayList<>(2);
                    TransactionRuleDTO current = new TransactionRuleDTO(InventoryWarehouseOptionEnum.WAREHOUSE_CURRENT, InventoryStatusEnum.getByCode(entry.getKey().getOutInventoryStatus()), InventoryModeEnum.OUT_STOCK);
                    current.setDictBizType(InventoryBusinessTypeEnum.WAREHOUSE_LOCATION_MOVE_INFO);
                    transactionRuleDTOList.add(current);
                    TransactionRuleDTO target = new TransactionRuleDTO(InventoryWarehouseOptionEnum.WAREHOUSE_TARGET,InventoryStatusEnum.getByCode(entry.getKey().getInInventoryStatus()) , InventoryModeEnum.IN_STOCK);
                    target.setDictBizType(InventoryBusinessTypeEnum.WAREHOUSE_LOCATION_MOVE_INFO);
                    transactionRuleDTOList.add(target);
                    InventoryTransferRuleDTO ruleDTO = new InventoryTransferRuleDTO();
                    ruleDTO.setParamList(transferNewDTOList);
                    ruleDTO.setBusinessType(InventoryBusinessTypeEnum.WAREHOUSE_LOCATION_MOVE_INFO.getCode());
                    ruleDTO.setRules(transactionRuleDTOList);
                    inventoryTransCoreService.approveByRule(ruleDTO);
                }


            }
            //发送旺店通
            syncDisApproveInfoToWdt(entity,SyncOperateEnum.OPERATE_APPROVE);
        }
        return Boolean.TRUE;
    }

    @Override
    public WarehouseLocationMoveDTO.ViewDTO view(String id) {
        WarehouseLocationMoveEntity warehouseLocationMoveEntity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到仓位移动主单数据"));
        WarehouseLocationMoveDTO.ViewDTO data = BeanMapperUtils.map(WarehouseLocationMoveDTO.ViewDTO.class, warehouseLocationMoveEntity);
        // 数据填充处理
        fillOne(data);
        List<WarehouseLocationMoveDetailEntity> detailEntityList = warehouseLocationMoveDetailService.listByMainIds(Collections.singletonList(data.getId()));
        List<WarehouseLocationMoveDetailDTO.ViewDTO> detailList = BeanMapper.copyList(detailEntityList, WarehouseLocationMoveDetailDTO.ViewDTO.class);
        List<String> skuIds = detailList.stream().map(WarehouseLocationMoveDetailDTO.ViewDTO::getSkuId).distinct().collect(Collectors.toList());
        List<SkuVO> skuVOList = plmTaskFeign.listSkuProductByIds(skuIds);

//        List<WarehouseLocationEntity> warehouseLocationEntities = warehouseLocationService.listByWarehouseIds(Collections.singletonList(data.getWarehouseId()));
        List<String> allWarehouseIds=new ArrayList<>();
        List<String> detailWarehouseIds = detailEntityList.stream().map(WarehouseLocationMoveDetailEntity::getWarehouseId)
                .filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
        if (CharSequenceUtil.isNotBlank(warehouseLocationMoveEntity.getWarehouseId())) {
            allWarehouseIds.add(warehouseLocationMoveEntity.getWarehouseId());
        }
        if (CollectionUtils.isNotEmpty(detailWarehouseIds)) {
            allWarehouseIds.addAll(detailWarehouseIds);
        }
        List<WarehouseLocationEntity> warehouseLocationEntities =new ArrayList<>();
        if (CollectionUtils.isNotEmpty(allWarehouseIds)) {
            warehouseLocationEntities = warehouseLocationService.listByWarehouseIds(allWarehouseIds);
        }
        for (WarehouseLocationMoveDetailDTO.ViewDTO viewDTO : detailList) {
            SkuVO skuVO = skuVOList.stream().filter(req -> req.getSkuId().equals(viewDTO.getSkuId())).findFirst().orElse(new SkuVO());
            viewDTO.setUnitName(skuVO.getUnitName());
            viewDTO.setSkuImg(skuVO.getSkuImagesUrl());
            viewDTO.setProductName(skuVO.getSkuName());
            viewDTO.setWarehouseId(CharSequenceUtil.isBlank(warehouseLocationMoveEntity.getWarehouseId()) ? viewDTO.getWarehouseId() : warehouseLocationMoveEntity.getWarehouseId());
            viewDTO.setWarehouseName(CharSequenceUtil.isBlank(warehouseLocationMoveEntity.getWarehouseName()) ? viewDTO.getWarehouseName() : warehouseLocationMoveEntity.getWarehouseName());
            WarehouseLocationEntity inWarehouseLocationEntity = getWarehouseLocationEntity(warehouseLocationEntities,
                    CharSequenceUtil.isBlank(warehouseLocationMoveEntity.getWarehouseId()) ? viewDTO.getWarehouseId() : warehouseLocationMoveEntity.getWarehouseId(), viewDTO.getInWarehouseLocation());
            viewDTO.setInWarehouseLocationName(inWarehouseLocationEntity.getName());
            WarehouseLocationEntity outWarehouseLocationEntity = getWarehouseLocationEntity(warehouseLocationEntities,
                    CharSequenceUtil.isBlank(warehouseLocationMoveEntity.getWarehouseId()) ? viewDTO.getWarehouseId() : warehouseLocationMoveEntity.getWarehouseId(), viewDTO.getOutWarehouseLocation());
            viewDTO.setOutWarehouseLocationName(outWarehouseLocationEntity.getName());
        }

        data.setDetailList(detailList);
        return data;
    }
    @Override
    public WarehouseLocationMoveDTO.PcViewDTO pcView(String id) {
        StopWatch stopWatch = new StopWatch("仓位移动PC预览");
        WarehouseLocationMoveDTO.DetailViewDTO viewDTO = Optional.ofNullable(baseMapper.findOne(id)).orElseThrow(() -> new ServiceException("未找到仓位移动主单数据"));
        WarehouseLocationMoveDTO.PcViewDTO pcViewDTO = new WarehouseLocationMoveDTO.PcViewDTO();
        BeanMapperUtils.copy(viewDTO, pcViewDTO);
        pcViewDTO.setApproveStatusName(ApproveStatusEnum.getName(pcViewDTO.getApproveStatus()));
        stopWatch.start("获取明细");
        List<WarehouseLocationMoveDTO.DetailViewDTO> detailViewDTOs = baseMapper.getDetail(id);
        stopWatch.stop();
        List<String> skuIds = detailViewDTOs.stream().map(WarehouseLocationMoveDTO.DetailViewDTO::getSkuId).collect(Collectors.toList());
        stopWatch.start("获取sku");
        List<SkuVO> skuVOList = plmTaskFeign.listSkuProductByIds(skuIds);
        stopWatch.stop();
        List<WarehouseLocationEntity> warehouseLocationEntities = warehouseLocationService.listByWarehouseIds(detailViewDTOs.stream()
                .map(WarehouseLocationMoveDTO.DetailViewDTO::getWarehouseId).collect(Collectors.toList()));
        stopWatch.start("拼接数据");
        detailViewDTOs.forEach(detailViewDTO -> {
            if (CharSequenceUtil.isBlank(detailViewDTO.getWarehouseId())) {
                detailViewDTO.setWarehouseId(detailViewDTO.getInfoWarehouseId());
                detailViewDTO.setWarehouseName(detailViewDTO.getInfoWarehouseName());
            }
            if (CharSequenceUtil.isBlank(detailViewDTO.getWarehouseName())) {
                detailViewDTO.setWarehouseName(detailViewDTO.getInfoWarehouseName());
            }
            InventoryDTO.InventoryBySkuIdAndWarehouseDTO inventoryBySkuIdAndWarehouseDTO = new InventoryDTO.InventoryBySkuIdAndWarehouseDTO();
            BeanMapper.copy(detailViewDTO, inventoryBySkuIdAndWarehouseDTO);
            inventoryBySkuIdAndWarehouseDTO.setWarehouseLocation(detailViewDTO.getOutWarehouseLocation());
            SkuVO sku = skuVOList.stream().filter(req -> req.getSkuId().equals(detailViewDTO.getSkuId())).findFirst().orElse(null);
            if (Objects.nonNull(sku)) {
                detailViewDTO.setProductName(sku.getSkuName());
            }
            detailViewDTO.setInWarehouseLocationName(getWarehouseLocationEntity(warehouseLocationEntities, detailViewDTO.getWarehouseId(), detailViewDTO.getInWarehouseLocation()).getName());
            detailViewDTO.setOutWarehouseLocationName(getWarehouseLocationEntity(warehouseLocationEntities, detailViewDTO.getWarehouseId(), detailViewDTO.getOutWarehouseLocation()).getName());
            detailViewDTO.setInInventoryStatusName(InventoryStatusEnum.getNameByCode(detailViewDTO.getInInventoryStatus()));
            detailViewDTO.setOutInventoryStatusName(InventoryStatusEnum.getNameByCode(detailViewDTO.getOutInventoryStatus()));
            //设置库存
            List<InventoryDTO.InventoryViewQtyDTO> inventoryQtys = inventoryService.getInventoryQty(Collections.singletonList(inventoryBySkuIdAndWarehouseDTO));
            inventoryQtys.forEach(inventoryQtyDTO -> {
                detailViewDTO.setUsableQty(inventoryQtyDTO.getUsableQty());
                detailViewDTO.setFrozenQty(inventoryQtyDTO.getFrozenQty());
                detailViewDTO.setRealQty(inventoryQtyDTO.getRealQty());
            });
        });
        stopWatch.stop();
        pcViewDTO.setDetailList(detailViewDTOs);
        log.info(stopWatch.prettyPrint());
        return pcViewDTO;
    }

    private static WarehouseLocationEntity getWarehouseLocationEntity(List<WarehouseLocationEntity> warehouseLocationEntities, String detailViewDTO, String detailViewDTO1) {
        return  warehouseLocationEntities.stream().filter(req ->
                        req.getWarehouseId().equals(detailViewDTO) && req.getCode().equals(detailViewDTO1))
                .findFirst().orElse(new WarehouseLocationEntity());
    }

    /**
    * 启动流程
    *
    * @param entity
    * @return void
    * @Date 2023/7/4 10:07
    **/

    public void startProcess(WarehouseLocationMoveEntity entity) {
        ProcessManagementDTO.StartDTO startDTO = new ProcessManagementDTO.StartDTO();
        startDTO.setBusinessId(entity.getId());
        startDTO.setBusinessCode(entity.getCode());
        startDTO.setBusinessKey(ModuleTypeEnum.WAREHOUSE_LOCATION_MOVE_INFO.getCode());
        startDTO.setBusinessName(entity.getCode());
        startDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        startDTO.setVariablesMap(BeanUtil.beanToMap(entity));
        ApiResult<ProcessManagementDTO.StartResultDTO> result = workflowFeign.start(startDTO);
        if (!result.isSuccess()) {
            throw new ServiceException(result.getMsg());
        }
    }
    private void fillOne(WarehouseLocationMoveDTO.ViewDTO data) {
        if (ObjectUtil.isEmpty(data)) {
            return;
        }
    }

    /**
    * 审核更新审核信息
    * @param id
    * @param approveStatus
    */
    public void updateForApprove(String id, String approveStatus) {
        //当前登录人
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        this.lambdaUpdate().eq(WarehouseLocationMoveEntity::getId, id)
            .set(WarehouseLocationMoveEntity::getApproveUserId, userInfo.getUid())
            .set(WarehouseLocationMoveEntity::getApproveUserName, userInfo.getUserName())
            .set(WarehouseLocationMoveEntity::getApproveStatus, approveStatus)
            .set(WarehouseLocationMoveEntity::getApproveTime, LocalDateTime.now())
            .update(new WarehouseLocationMoveEntity());
     }

    /**
    * 反审核更新审核信息
    * @param id
    * @param approveStatus
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateForDisApprove(String id, String approveStatus) {
        this.lambdaUpdate().eq(WarehouseLocationMoveEntity::getId, id)
            .set(WarehouseLocationMoveEntity::getApproveUserId, "")
            .set(WarehouseLocationMoveEntity::getApproveUserName, "")
            .set(WarehouseLocationMoveEntity::getApproveStatus, approveStatus)
            .set(WarehouseLocationMoveEntity::getApproveTime, null)
            .update(new WarehouseLocationMoveEntity());
        }

    /**
    * 更新审核状态
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateApproveStatus(String id, String approveStatus) {
        lambdaUpdate().eq(WarehouseLocationMoveEntity::getId, id)
        .set(WarehouseLocationMoveEntity::getApproveStatus, approveStatus)
        .update(new WarehouseLocationMoveEntity());
    }

    /**
    * 分页查询、导出 数据处理
    */
    private void fillList(List<WarehouseLocationMoveDTO.PdaListDTO> list) {
        if(CollUtil.isEmpty(list)) {
           return;
        }
       //主键id
        List<String> ids = list.stream().map(req -> req.getId()).collect(Collectors.toList());
        //查询详情
        List<WarehouseLocationMoveDetailEntity> detailEntityList = warehouseLocationMoveDetailService.listByMainIds(ids);
        List<String> allWarehouseIds=new ArrayList<>();
        List<String> warehouseIds = list.stream().map(req -> req.getWarehouseId()).filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
        List<String> detailWarehouseIds = detailEntityList.stream().map(req -> req.getWarehouseId()).filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(warehouseIds)) {
            allWarehouseIds.addAll(warehouseIds);
        }
        if (CollectionUtils.isNotEmpty(detailWarehouseIds)) {
            allWarehouseIds.addAll(detailWarehouseIds);
        }
        List<WarehouseLocationEntity> warehouseLocationEntities =new ArrayList<>();
        if (CollectionUtils.isNotEmpty(allWarehouseIds)) {
            warehouseLocationEntities = warehouseLocationService.listByWarehouseIds(allWarehouseIds);
        }
        // 属性赋值
        for(WarehouseLocationMoveDTO.PdaListDTO data : list) {
            data.setApproveStatusName(ApproveStatusEnum.getName(data.getApproveStatus()));
            List<WarehouseLocationMoveDetailEntity> detailEntities = detailEntityList.stream().filter(obj -> obj.getMainId().equals(data.getId())).collect(Collectors.toList());
            List<WarehouseLocationMoveDTO.PdaItemDTO> itemDTOList = BeanMapper.copyList(detailEntities, WarehouseLocationMoveDTO.PdaItemDTO.class);
            List<String> skuList = itemDTOList.stream().map(WarehouseLocationMoveDTO.PdaItemDTO::getSkuId).distinct().collect(Collectors.toList());
            data.setDetailCount(skuList.size());
            for (WarehouseLocationMoveDTO.PdaItemDTO pdaItemDTO : itemDTOList) {
                pdaItemDTO.setInWarehouseLocationName(getWarehouseLocationEntity(warehouseLocationEntities,
                        CharSequenceUtil.isBlank(data.getWarehouseId()) ? pdaItemDTO.getWarehouseId() : data.getWarehouseId(), pdaItemDTO.getInWarehouseLocation()).getName());
                if (CharSequenceUtil.isBlank(pdaItemDTO.getWarehouseId())){
                    pdaItemDTO.setWarehouseId(data.getWarehouseId());
                }

                if (CharSequenceUtil.isBlank(pdaItemDTO.getWarehouseName())){
                    pdaItemDTO.setWarehouseName(data.getWarehouseName());
                }
            }
            data.setItemList(itemDTOList);
        }
    }

    /**
    * 分页查询、导出 数据处理
    */
    private void validateSubmit(WarehouseLocationMoveEntity entity) {
        // 待提交或审核不通过并且未作废允许提交
        if(!ApproveStatusEnum.allowUpdateStatus(entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98010);
        }
        return;
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(WarehouseLocationMoveEntity warehouseLocationMoveEntity) {
        if (CharSequenceUtil.isBlank(warehouseLocationMoveEntity.getId()) && ObjectUtil.isNull(warehouseLocationMoveEntity.getBillDate())) {
            warehouseLocationMoveEntity.setBillDate(LocalDate.now());
        }
        List<WarehouseDTO.UpdateDTO> warehouseList = warehouseService.listWarehouseByIds(Collections.singletonList(warehouseLocationMoveEntity.getWarehouseId()));
        WarehouseDTO.UpdateDTO updateDTO = warehouseList.stream().filter(req -> req.getId().equals(warehouseLocationMoveEntity.getWarehouseId())).findFirst().orElse(new WarehouseDTO.UpdateDTO());
        warehouseLocationMoveEntity.setWarehouseName(updateDTO.getName());
        warehouseLocationMoveEntity.setInventoryOrgId(updateDTO.getOrgId());
        //获取核算公司
        SysAccountingCompanyEntity companyEntity = sysUserFeign.getCompanyById(updateDTO.getOrgId());
        if (ObjectUtil.isNotEmpty(companyEntity)) {
            warehouseLocationMoveEntity.setInventoryOrgName(companyEntity.getCompanyName());
        }
        if (CharSequenceUtil.isBlank(warehouseLocationMoveEntity.getWarehouseId())){
            warehouseLocationMoveEntity.setWarehouseId("");
            warehouseLocationMoveEntity.setWarehouseName("");
        }
    }

    @Override
    public Boolean invalid(List<String> ids, String remark) {
        List<WarehouseLocationMoveEntity> infoEntityList = this.listByIds(ids);
        if (CollectionUtils.isEmpty(ids)) {
            throw new ServiceException(ApiError.ERROR_98004);
        }
        //审核不通过 待提交可以作废
        long count = infoEntityList.stream().filter(entity -> entity.getInvalidStatus() == false
                && (entity.getApproveStatus().equals(ApproveStatusEnum.WAIT_SUBMIT)
                || entity.getApproveStatus().equals(ApproveStatusEnum.REJECT))
        ).count();

        if (count != infoEntityList.size()) {
            throw new ServiceException(ApiError.ERROR_98005);
        }

        //修改状态为待提交
        lambdaUpdate().set(WarehouseLocationMoveEntity::getInvalidStatus, Boolean.TRUE)
                .set(WarehouseLocationMoveEntity::getInvalidRemark, remark)
                .in(WarehouseLocationMoveEntity::getId, ids)
                .update();
        //操作日志
        List<Pair<String, String>> pairList = infoEntityList.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("作废了一个收货单【%s】，作废原因：".concat(remark), ModuleTypeEnum.WAREHOUSE_LOCATION_MOVE_INFO.getCode(), pairList, "作废操作");
        return Boolean.TRUE;
    }

    @Override
    public void listExport(WarehouseLocationMoveDTO.ExportDTO dto) {
        downloadTaskFeign.saveDownloadTask("仓库移动导出", EXPORT_WMS_PDA_WAREHOUSE_LOCATION_MOVE_INFO.getCode(), dto);
    }

    private void dataProcess(List<WarehouseLocationMoveDTO.PdaPcListDTO> pdaPcListDTOS, List<SkuVO> skuVOList, List<String> warehouseIds) {
        List<WarehouseLocationEntity> warehouseLocationEntities = warehouseLocationService.listByWarehouseIds(warehouseIds);
        for (WarehouseLocationMoveDTO.PdaPcListDTO pdaPcListDTO : pdaPcListDTOS) {
            pdaPcListDTO.setApproveStatusName(ApproveStatusEnum.getName(pdaPcListDTO.getApproveStatus()));
            SkuVO skuVO = skuVOList.stream().filter(req -> req.getSkuId().equals(pdaPcListDTO.getSkuId())).findFirst().orElse(null);
            if (Objects.nonNull(skuVO)) {
                pdaPcListDTO.setProductName(skuVO.getSkuName());
            }
            if (CharSequenceUtil.isBlank(pdaPcListDTO.getWarehouseId())){
                pdaPcListDTO.setWarehouseId(pdaPcListDTO.getInfoWarehouseId());
            }
            if (CharSequenceUtil.isBlank(pdaPcListDTO.getWarehouseName())){
                pdaPcListDTO.setWarehouseName(pdaPcListDTO.getInfoWarehouseName());
            }
            pdaPcListDTO.setInWarehouseLocationName(getWarehouseLocationEntity(warehouseLocationEntities, pdaPcListDTO.getWarehouseId(), pdaPcListDTO.getInWarehouseLocation()).getName());
            pdaPcListDTO.setOutWarehouseLocationName(getWarehouseLocationEntity(warehouseLocationEntities, pdaPcListDTO.getWarehouseId(), pdaPcListDTO.getOutWarehouseLocation()).getName());
            pdaPcListDTO.setSourceTypeName(SourceTypeEnum.getName(pdaPcListDTO.getSourceType()));
            pdaPcListDTO.setOutInventoryStatusName(InventoryStatusEnum.getNameByCode(pdaPcListDTO.getOutInventoryStatus()));
            pdaPcListDTO.setInInventoryStatusName(InventoryStatusEnum.getNameByCode(pdaPcListDTO.getInInventoryStatus()));
        }
    }

    @Override
    public WarehouseLocationMoveDTO.ImportDTO importFile(MultipartFile excelFile, HttpServletResponse response) {
        MoveInfoExcelListener excelListenerUtil = new MoveInfoExcelListener(this, warehouseService, warehouseLocationService, plmTaskFeign, inventoryService);
        try {
            EasyExcel.read(excelFile.getInputStream(), MoveInfoExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (IOException e) {
            log.error("导入错误！", e);  throw new ServiceException(ApiError.ERROR_95124);
        } catch (ExcelCommonException e) {
            log.error("导入格式错误！",e);
            throw new ServiceException(ApiError.ERROR_1016);
        }
        //验证导入数据是否为空
        List<MoveInfoExcelDTO> allList = excelListenerUtil.getAllList();
        if (CollectionUtils.isEmpty(allList)) {
            throw new ServiceException(ApiError.ERROR_95123);
        }
        WarehouseLocationMoveDTO.ImportDTO importDTO = new WarehouseLocationMoveDTO.ImportDTO();
        List<WarehouseLocationMoveDTO.DetailViewDTO> successList = excelListenerUtil.getSuccessList();
        String url = "";
        List<MoveInfoExcelDTO> errorList = excelListenerUtil.getErrorList();
        if (errorList.size() > 0) {
            String fileName = "仓位移动导入错误信息.xlsx";
            File file = ExcelUtil.exportFile(fileName, "warehouseMoveInfoError", errorList, MoveInfoExcelDTO.class);
            if (file != null && !file.isDirectory()) {
                url = FastDFSClientUtil.uploadFile(file, fileName);
            }
        }
        importDTO.setSuccessList(successList);
        importDTO.setErrorUrl(url);
        return importDTO;
    }

    /**
     * 生成旺店通中间表数据
     */
    private DmpPushWdtDTO.AddDTO generateWdtInterim(String sourceId, String sourceCode, String operateCode, String warehouseId, String outCode, String thirdWarehouseCode, List<? extends CommonCreateBillGoodsReq> goodsList, SourceTypeEnum sourceTypeEnum) {
        DmpPushWdtDTO.AddDTO pushWdtDTO = new DmpPushWdtDTO.AddDTO();
        pushWdtDTO.setSourceId(sourceId);
        pushWdtDTO.setSourceCode(sourceCode);
        pushWdtDTO.setThirdCode(outCode);
        pushWdtDTO.setWarehouseId(warehouseId);
        pushWdtDTO.setThirdWarehouseCode(thirdWarehouseCode);
        pushWdtDTO.setThirdType(sourceTypeEnum.getCode());
        pushWdtDTO.setOperateType(operateCode);
        List<DmpPushWdtDetailDTO> detailDTOList = BeanMapper.copyList(goodsList, DmpPushWdtDetailDTO.class);
        pushWdtDTO.setDetailDTOList(detailDTOList);
        return pushWdtDTO;
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
	@Override
	public void wdtAutoAdd(PcAddDTO pcAddDTO) {
    	String moveId = this.pcAdd(pcAddDTO);
    	this.submit(moveId);
    	ApproveOneDTO approveOneDTO = new ApproveOneDTO();
		approveOneDTO.setId(moveId);
		approveOneDTO.setType("pass");
		approveOneDTO.setComment("旺店通同步销售出库单库存不足自动仓位移动");
    	this.pcApprove(approveOneDTO);
	}
}
