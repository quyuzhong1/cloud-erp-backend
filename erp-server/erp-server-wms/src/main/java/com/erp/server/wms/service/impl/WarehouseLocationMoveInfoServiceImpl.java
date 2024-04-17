package com.erp.server.wms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.constant.ApproveType;
import com.common.business.dto.FindUserDTO;
import com.common.business.enums.*;
import com.common.business.vo.LoginUser;

import cn.hutool.core.util.StrUtil;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.utils.date.DateUtil;
import com.erp.model.plm.dto.CleanSkuDto;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.WarehouseLocationMoveDetailDTO;
import com.erp.model.sys.entity.SysAccountingCompanyEntity;
import com.erp.model.wms.dto.excel.WarehouseExcelDTO;
import com.erp.model.wms.dto.inventory.*;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.DictBasicEnum;
import com.erp.model.wms.enums.inventory.*;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.wms.listener.WarehouseExcelListener;
import com.erp.server.wms.mapper.WarehouseLocationMoveInfoMapper;
import com.erp.server.wms.service.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import com.common.core.controller.vo.ApiResult;
import cn.hutool.core.util.ObjectUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.WarehouseLocationMoveInfoDTO;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.rpc.workflow.WorkflowFeign;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.hutool.core.collection.CollUtil;

import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.stream.Collectors;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
import org.springframework.web.multipart.MultipartFile;

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
public class WarehouseLocationMoveInfoServiceImpl extends SuperServiceImpl<WarehouseLocationMoveInfoMapper, WarehouseLocationMoveInfoEntity> implements WarehouseLocationMoveInfoService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;
    @Autowired
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

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public String add(WarehouseLocationMoveInfoDTO.AddDTO addDTO) {
        WarehouseLocationMoveInfoEntity warehouseLocationMoveInfoEntity = new WarehouseLocationMoveInfoEntity();
        BeanMapperUtils.copy(addDTO, warehouseLocationMoveInfoEntity);
        if (addDTO.getPcShow() && StringUtils.isBlank(addDTO.getWarehouseId())) {
            List<WarehouseLocationMoveInfoDTO.ViewDTO> listDTOS = BeanMapperUtils.copyList(WarehouseLocationMoveInfoDTO.ViewDTO.class, addDTO.getDetailList());
            String warehouseId = listDTOS.stream().map(WarehouseLocationMoveInfoDTO.ViewDTO::getWarehouseId).distinct().findFirst().orElse(null);
            if (StringUtils.isBlank(warehouseId)) {
                throw new ServiceException(ApiError.ERROR_99001);
            }
        }
        // 数据处理
        handleData(warehouseLocationMoveInfoEntity);

        log.info("开始新增仓位移动主单");
        // 生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_CWYD);
        warehouseLocationMoveInfoEntity.setCode(code);
        boolean save = super.save(warehouseLocationMoveInfoEntity);
        if(!save) {
            throw new ServiceException("仓位移动主单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", commonService.getUserInfo().getUserName(), "仓位移动主单" , warehouseLocationMoveInfoEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.WAREHOUSE_LOCATION_MOVE_INFO.getCode(), warehouseLocationMoveInfoEntity.getId(), "新增操作");
        // 新增明细
        warehouseLocationMoveDetailService.add(addDTO, warehouseLocationMoveInfoEntity.getId());
        return warehouseLocationMoveInfoEntity.getId();
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(WarehouseLocationMoveInfoDTO.UpdateDTO updateDTO) {
        WarehouseLocationMoveInfoEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "仓位移动主单"));
        // 待提交和审核不通过允许修改
        if (!ApproveStatusEnum.allowUpdateStatus(old.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_1029);
        }
        WarehouseLocationMoveInfoEntity warehouseLocationMoveInfoEntity =  BeanMapperUtils.map(WarehouseLocationMoveInfoEntity.class, updateDTO);
        if (updateDTO.getPcShow() && StringUtils.isBlank(updateDTO.getWarehouseId())) {
            List<WarehouseLocationMoveInfoDTO.ViewDTO> listDTOS = BeanMapperUtils.copyList(WarehouseLocationMoveInfoDTO.ViewDTO.class, updateDTO.getDetailList());
            String warehouseId = listDTOS.stream().map(WarehouseLocationMoveInfoDTO.ViewDTO::getWarehouseId).distinct().findFirst().orElse(null);
            if (StringUtils.isBlank(warehouseId)) {
                throw new ServiceException(ApiError.ERROR_99001);
            }
        }
        // 数据处理
        handleData(warehouseLocationMoveInfoEntity);
        log.info("编辑 开始修改仓位移动主单数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(warehouseLocationMoveInfoEntity);
        if(!save) {
            throw new ServiceException("仓位移动主单保存失败");
        }
        // 修改明细数据（包含增删改）
        warehouseLocationMoveDetailService.update(updateDTO, warehouseLocationMoveInfoEntity.getId());
        // 记录主单操作日志
        log.info("编辑 开始记录仓位移动主单日志数据，单号：【{}】", warehouseLocationMoveInfoEntity.getCode());
        String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), warehouseLocationMoveInfoEntity.getCode(), "仓位移动主单");
        operateLogService.addModuleOperateLogByObj(old, warehouseLocationMoveInfoEntity, ModuleTypeEnum.WAREHOUSE_LOCATION_MOVE_INFO.getCode(), warehouseLocationMoveInfoEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public PagingVO<WarehouseLocationMoveInfoDTO.ListDTO> paging(PagingDTO<WarehouseLocationMoveInfoDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        WarehouseLocationMoveInfoDTO.PagingParamDTO params = pagingParamDTO.getParams();
        List<String> approveStatusList = params.getApproveStatusList();
        if (approveStatusList.contains(ApproveStatusEnum.APPROVE.getCode())) {
            List<LocalDate> dateList = new ArrayList<>();
            LocalDate now = LocalDate.now();
            dateList.add(now.minusDays(30));
            dateList.add(now);
            params.setBillDateList(dateList);
        }

        IPage<WarehouseLocationMoveInfoDTO.PdaListDTO> pageData = this.baseMapper.pdaPaging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public PagingVO<WarehouseLocationMoveInfoDTO.ListDTO> pcPaging(PagingDTO<WarehouseLocationMoveInfoDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        WarehouseLocationMoveInfoDTO.PagingParamDTO params = pagingParamDTO.getParams();

        IPage<WarehouseLocationMoveInfoDTO.PdaPcListDTO> pageData = this.baseMapper.pdaPcPaging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO(pageData);
        }
        // 数据处理
        List<WarehouseLocationMoveInfoDTO.PdaPcListDTO> itemDTOList = pageData.getRecords();
        List<String> skuList = itemDTOList.stream().map(req -> req.getSkuId()).distinct().collect(Collectors.toList());
        //feign获取产品信息
        List<SkuVO> skuVOList = plmTaskFeign.getSkuInfoByIds(skuList);
        List<String> warehouseIds = itemDTOList.stream().map(req -> req.getWarehouseId()).distinct().collect(Collectors.toList());
        List<WarehouseLocationEntity> warehouseLocationEntities = warehouseLocationService.listByWarehouseIds(warehouseIds);
        for (WarehouseLocationMoveInfoDTO.PdaPcListDTO pdaPcListDTO : itemDTOList) {
            pdaPcListDTO.setApproveStatusName(ApproveStatusEnum.getName(pdaPcListDTO.getApproveStatus()));
            SkuVO skuVO = skuVOList.stream().filter(req -> req.getSkuId().equals(pdaPcListDTO.getSkuId())).findFirst().orElse(null);
            pdaPcListDTO.setProductName(skuVO.getSkuName());
            WarehouseLocationEntity warehouseLocationEntity = warehouseLocationEntities.stream().filter(req -> req.getWarehouseId().equals(pdaPcListDTO.getWarehouseId()) && req.getCode().equals(pdaPcListDTO.getInWarehouseLocation())).findFirst().orElse(new WarehouseLocationEntity());
            pdaPcListDTO.setInWarehouseLocationName(warehouseLocationEntity.getName());
            WarehouseLocationEntity outWarehouseLocationEntity = warehouseLocationEntities.stream().filter(req -> req.getWarehouseId().equals(pdaPcListDTO.getWarehouseId()) && req.getCode().equals(pdaPcListDTO.getOutWarehouseLocation())).findFirst().orElse(new WarehouseLocationEntity());
            pdaPcListDTO.setOutWarehouseLocationName(outWarehouseLocationEntity.getName());
        }
        return new PagingVO(pageData);
    }

    @Override
    public List<WarehouseLocationMoveInfoDTO.PdaTabListDTO> tabList(PermissionsDTO dto) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(30);
        PdaTabFlagEnum[] values = PdaTabFlagEnum.values();
        List<WarehouseLocationMoveInfoDTO.PdaTabListDTO> list = new ArrayList<>();
        for (PdaTabFlagEnum item : values) {
            WarehouseLocationMoveInfoDTO.PagingParamDTO pagingParamDTO = new WarehouseLocationMoveInfoDTO.PagingParamDTO();
            pagingParamDTO.setPermissionSql(dto.getPermissionSql());
            pagingParamDTO.setInvalidStatus(Boolean.FALSE);
            WarehouseLocationMoveInfoDTO.PdaTabListDTO resultDTO = new WarehouseLocationMoveInfoDTO.PdaTabListDTO();
            Integer count = MathUtil.ZERO;
            if (PdaTabFlagEnum.WAIT_SUBMIT_AND_REJECT.getCode().equals(item.getCode())) {
                pagingParamDTO.setApproveStatusList(Arrays.asList(ApproveStatusEnum.WAIT_SUBMIT.getStatus(), ApproveStatusEnum.REJECT.getStatus()));
                count = this.baseMapper.listCount(pagingParamDTO);
            }
            if (PdaTabFlagEnum.APPROVE_ING.getCode().equals(item.getCode())) {
                pagingParamDTO.setApproveStatusList(Arrays.asList(ApproveStatusEnum.APPROVE_ING.getStatus()));
                count = this.baseMapper.listCount(pagingParamDTO);
            }
            if (PdaTabFlagEnum.APPROVE.getCode().equals(item.getCode())) {
                pagingParamDTO.setApproveStatusList(Arrays.asList(ApproveStatusEnum.APPROVE.getStatus()));
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
    public List<WarehouseLocationMoveInfoDTO.PdaTabListDTO> pcTabList(PermissionsDTO dto) {
        ApproveStatusEnum[] values = ApproveStatusEnum.values();
        List<WarehouseLocationMoveInfoDTO.PdaTabListDTO> list = new ArrayList<>();
        for (ApproveStatusEnum item : values) {
            WarehouseLocationMoveInfoDTO.PagingParamDTO pagingParamDTO = new WarehouseLocationMoveInfoDTO.PagingParamDTO();
            pagingParamDTO.setPermissionSql(dto.getPermissionSql());
            pagingParamDTO.setInvalidStatus(Boolean.FALSE);
            WarehouseLocationMoveInfoDTO.PdaTabListDTO resultDTO = new WarehouseLocationMoveInfoDTO.PdaTabListDTO();
            Integer count = MathUtil.ZERO;
            if (ApproveStatusEnum.WAIT_SUBMIT.getCode().equals(item.getCode())) {
                pagingParamDTO.setApproveStatusList(Arrays.asList(ApproveStatusEnum.WAIT_SUBMIT.getStatus()));
                count = this.baseMapper.listCount(pagingParamDTO);
            }
            if (ApproveStatusEnum.REJECT.getCode().equals(item.getCode())) {
                pagingParamDTO.setApproveStatusList(Arrays.asList(ApproveStatusEnum.REJECT.getStatus()));
                count = this.baseMapper.listCount(pagingParamDTO);
            }
            if (ApproveStatusEnum.APPROVE_ING.getCode().equals(item.getCode())) {
                pagingParamDTO.setApproveStatusList(Arrays.asList(ApproveStatusEnum.APPROVE_ING.getStatus()));
                count = this.baseMapper.listCount(pagingParamDTO);
            }
            if (ApproveStatusEnum.APPROVE.getCode().equals(item.getCode())) {
                pagingParamDTO.setApproveStatusList(Arrays.asList(ApproveStatusEnum.APPROVE.getStatus()));
                count = this.baseMapper.listCount(pagingParamDTO);
            }
            resultDTO.setCount(ObjectUtils.isEmpty(count) ? MathUtil.ZERO : count);
            resultDTO.setTabFlag(item.getCode());
            list.add(resultDTO);
        }
        //查询所有的数量
        WarehouseLocationMoveInfoDTO.PagingParamDTO pagingParamDTO = new WarehouseLocationMoveInfoDTO.PagingParamDTO();
        Integer totalCount = this.baseMapper.listCount(pagingParamDTO);
        WarehouseLocationMoveInfoDTO.PdaTabListDTO resultDTO = new WarehouseLocationMoveInfoDTO.PdaTabListDTO();
        resultDTO.setTabFlag(PdaTabFlagPcEnum.ALL.getCode());
        resultDTO.setCount(totalCount);
        list.add(resultDTO);
        return list;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO submit(String id) {
        WarehouseLocationMoveInfoEntity entity = getById(id);
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
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据提交审核 ", commonService.getUserInfo().getUserName(), entity.getCode(), "仓位移动主单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.WAREHOUSE_LOCATION_MOVE_INFO.getCode(), entity.getId(), "提交操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.SUBMIT);
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addAndSubmit(WarehouseLocationMoveInfoDTO.AddDTO dto) {
        // 新增
        String id = this.add(dto);
        // 提交
        this.submit(id);
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateAndSubmit(WarehouseLocationMoveInfoDTO.UpdateDTO dto) {
        // 修改
        this.update(dto);
        // 提交
        this.submit(dto.getId());
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO approve(ApproveOneDTO dto) {
        ApproveTypeEnum approveType = ApproveTypeEnum.getByCode(dto.getType());
        if(Objects.equals(approveType, ApproveTypeEnum.REJECT) && StrUtils.isEmpty(dto.getComment())) {
            throw new ServiceException(ApiError.REJECT_COMMENT_NOT_EMPTY);
        }
        WarehouseLocationMoveInfoEntity entity = getById(dto.getId());
        // 审核中的数据允许审核
        if(!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING)) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        // 调用流程审核
        approveProcess(entity, dto);
        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据审核操作  审核结果：【{}】 审核意见 ：【{}】", commonService.getUserInfo().getUserName(), entity.getCode(), "仓位移动主单", approveType.getName(), dto.getComment());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.WAREHOUSE_LOCATION_MOVE_INFO.getCode(), entity.getId(), "审核操作");
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(approveType);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.approveStatus(approveStatus));
    }

    /**
    * 审核流程处理
    * @param entity
    * @param dto
    */
    private void approveProcess(WarehouseLocationMoveInfoEntity entity, ApproveOneDTO dto) {
        LoginUser userInfo = commonService.getUserInfo();
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
        WarehouseLocationMoveInfoEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到仓位移动主单单数据"));
        // 反审核条件判断
        validateDisApprove(entity);

        // 更新审核信息
        updateForDisApprove(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        // 回滚库存
        InventoryBatchUnApproveDTO inventoryBatchUnApproveDTO = new InventoryBatchUnApproveDTO(InventorySourceTypeEnum.WAREHOUSE_LOCATION_MOVE_INFO, Arrays.asList(id));
        inventoryTransCoreService.batchUnApprove(inventoryBatchUnApproveDTO);

        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据反审核操作 ", commonService.getUserInfo().getUserName(), entity.getCode(), "仓位移动主单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.WAREHOUSE_LOCATION_MOVE_INFO.getCode(), entity.getId(), "反审核操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DISAPPROVE);
    }

    private Boolean validateDisApprove(WarehouseLocationMoveInfoEntity entity) {
        // 已审核支持反审核
        if (Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE.getStatus())) {
            throw new ServiceException(ApiError.ERROR_98014);
        }

        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO delete(String id) {
        WarehouseLocationMoveInfoEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到仓位移动主单数据"));
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
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据删除操作 ", commonService.getUserInfo().getUserName(), entity.getCode(), "仓位移动主单");
        operateLogService.addModuleOperateLog(msg, null, entity.getCode(), "删除仓位移动主单数据");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DELETE);
    }

    /**
    * 撤销
    */
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO cancelProcess(String id) {
        WarehouseLocationMoveInfoEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到仓位移动主单数据"));
        // 只有审核中的单据允许撤销
        if (Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING.getStatus())) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        // TODO 撤销流程
        log.info("撤销 开始撤销流程，id：【{}】",id);

        log.info("撤销 开始修改仓位移动主单状态，id：【{}】", id);
        updateApproveStatus(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        //操作日志
        log.info("撤销 开始记录操作日志，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据撤销流程操作 ", commonService.getUserInfo().getUserName(), entity.getCode(), "仓位移动主单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.WAREHOUSE_LOCATION_MOVE_INFO.getCode(), entity.getId(), "取消流程操作");
        ProcessManagementDTO.RevokeDTO revokeDTO = new ProcessManagementDTO.RevokeDTO();
        revokeDTO.setBusinessId(entity.getId());
        revokeDTO.setBusinessKey(ModuleTypeEnum.WAREHOUSE_LOCATION_MOVE_INFO.getCode());
        revokeDTO.setUserId(commonService.getUserInfo().getUid());
        workflowFeign.revokeProcess(revokeDTO);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.CANCEL_PROCESS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approveEnd(ApproveOneDTO dto, WarehouseLocationMoveInfoEntity entity) {
        if (ObjectUtil.isEmpty(entity)) {
            return Boolean.TRUE;
        }

        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(dto.getType());
        updateForApprove(entity.getId(), approveStatus.getStatus());
        InventoryTransferRuleDTO ruleDTO = new InventoryTransferRuleDTO();
        if (ApproveType.PASS.equals(dto.getType())) {
            WarehouseLocationMoveInfoEntity infoEntity = this.getById(dto.getId());
            List<WarehouseLocationMoveDetailEntity> detailEntityList = warehouseLocationMoveDetailService.listByMainIds(Arrays.asList(infoEntity.getId()));
            List<TransferDTO> transferDTOList = new ArrayList<>();
            for (WarehouseLocationMoveDetailEntity detailEntity : detailEntityList) {
                TransferDTO transferDTO = new TransferDTO();
                transferDTO.setSourceType(InventorySourceTypeEnum.WAREHOUSE_LOCATION_MOVE_INFO);
                transferDTO.setSourceId(infoEntity.getId());
                transferDTO.setSourceCode(infoEntity.getCode());
                transferDTO.setBillDate(infoEntity.getBillDate());
                transferDTO.setSourceDetailId(detailEntity.getId());
                transferDTO.setCurWarehouseId(infoEntity.getWarehouseId());
                transferDTO.setCurWarehouseLocation(detailEntity.getOutWarehouseLocation());
                transferDTO.setTargetWarehouseId(infoEntity.getWarehouseId());
                transferDTO.setTargetWarehouseLocation(detailEntity.getInWarehouseLocation());
                transferDTO.setQty(detailEntity.getQty());
                transferDTO.setSkuId(detailEntity.getSkuId());
                transferDTO.setSkuNo(detailEntity.getSkuNo());
                transferDTO.setWarehouseId(infoEntity.getWarehouseId());
//            transferDTO.setWarehouseLocation("");
                transferDTO.setInventoryStatus(InventoryStatusEnum.USABLE);
                transferDTOList.add(transferDTO);
            }

            List<TransactionRuleDTO> transactionRuleDTOList = new ArrayList<>(2);
            transactionRuleDTOList.add(new TransactionRuleDTO(InventoryWarehouseOptionEnum.WAREHOUSE_CURRENT, InventoryStatusEnum.USABLE, InventoryModeEnum.OUT_STOCK));
            transactionRuleDTOList.add(new TransactionRuleDTO(InventoryWarehouseOptionEnum.WAREHOUSE_TARGET, InventoryStatusEnum.USABLE, InventoryModeEnum.IN_STOCK));
            ruleDTO.setParamList(transferDTOList);
            ruleDTO.setBusinessType(InventoryBusinessTypeEnum.WAREHOUSE_LOCATION_MOVE_INFO.getCode());
            ruleDTO.setRules(transactionRuleDTOList);
            inventoryTransCoreService.approveByRule(ruleDTO);
        }

        return Boolean.TRUE;
    }

    @Override
    public WarehouseLocationMoveInfoDTO.ViewDTO view(String id) {
        WarehouseLocationMoveInfoEntity warehouseLocationMoveInfoEntity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到仓位移动主单数据"));
        WarehouseLocationMoveInfoDTO.ViewDTO data = BeanMapperUtils.map(WarehouseLocationMoveInfoDTO.ViewDTO.class, warehouseLocationMoveInfoEntity);
        // 数据填充处理
        fillOne(data);
        List<WarehouseLocationMoveDetailEntity> detailEntityList = warehouseLocationMoveDetailService.listByMainIds(Arrays.asList(data.getId()));
        List<WarehouseLocationMoveDetailDTO.ViewDTO> detailList = BeanMapper.copyList(detailEntityList, WarehouseLocationMoveDetailDTO.ViewDTO.class);
        List<String> skuIds = detailList.stream().map(req -> req.getSkuId()).distinct().collect(Collectors.toList());
        List<SkuVO> skuVOList = plmTaskFeign.getSkuInfoByIds(skuIds);

        List<WarehouseLocationEntity> warehouseLocationEntities = warehouseLocationService.listByWarehouseIds(Arrays.asList(data.getWarehouseId()));

        for (WarehouseLocationMoveDetailDTO.ViewDTO viewDTO : detailList) {
            SkuVO skuVO = skuVOList.stream().filter(req -> req.getSkuId().equals(viewDTO.getSkuId())).findFirst().orElse(null);
            viewDTO.setUnitName(skuVO.getUnitName());
            viewDTO.setSkuImg(skuVO.getSkuImagesUrl());
            viewDTO.setProductName(skuVO.getSkuName());
            WarehouseLocationEntity inWarehouseLocationEntity = warehouseLocationEntities.stream().filter(req -> req.getWarehouseId().equals(data.getWarehouseId()) && req.getCode().equals(viewDTO.getInWarehouseLocation())).findFirst().orElse(new WarehouseLocationEntity());
            viewDTO.setInWarehouseLocationName(inWarehouseLocationEntity.getName());
            WarehouseLocationEntity outWarehouseLocationEntity = warehouseLocationEntities.stream().filter(req -> req.getWarehouseId().equals(data.getWarehouseId()) && req.getCode().equals(viewDTO.getOutWarehouseLocation())).findFirst().orElse(new WarehouseLocationEntity());
            viewDTO.setOutWarehouseLocationName(outWarehouseLocationEntity.getName());

        }

        data.setDetailList(detailList);
        return data;
    }
    @Override
    public WarehouseLocationMoveInfoDTO.PdaPcViewDTO pcView(String id) {
        WarehouseLocationMoveDetailEntity warehouseLocationMoveDetailEntity = warehouseLocationMoveDetailService.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到仓位移动明细数据"));
        WarehouseLocationMoveInfoEntity warehouseLocationMoveInfoEntity = super.getByIdOpt(warehouseLocationMoveDetailEntity.getMainId()).orElseThrow(()->new ServiceException("未找到仓位移动主单数据"));
        WarehouseLocationMoveInfoDTO.PdaPcViewDTO pdaPcViewDTO = baseMapper.getDetail(id);
        InventoryDTO.InventoryBySkuIdAndWarehouseDTO inventoryBySkuIdAndWarehouseDTO = new InventoryDTO.InventoryBySkuIdAndWarehouseDTO();
        BeanMapper.copy(pdaPcViewDTO, inventoryBySkuIdAndWarehouseDTO);
        String skuId = pdaPcViewDTO.getSkuId();
        List<SkuVO> skuVOList = plmTaskFeign.getSkuInfoByIds(Arrays.asList(skuId));
        List<WarehouseLocationEntity> warehouseLocationEntities = warehouseLocationService.listByWarehouseIds(Arrays.asList(pdaPcViewDTO.getWarehouseId()));
        skuVOList.stream().forEach(skuVo->{
            pdaPcViewDTO.setProductName(skuVo.getBrandName());
            WarehouseLocationEntity inWarehouseLocationEntity = warehouseLocationEntities.stream().filter(req -> req.getWarehouseId().equals(pdaPcViewDTO.getWarehouseId()) && req.getCode().equals(pdaPcViewDTO.getInWarehouseLocation())).findFirst().orElse(new WarehouseLocationEntity());
            pdaPcViewDTO.setInWarehouseLocationName(inWarehouseLocationEntity.getName());
            WarehouseLocationEntity outWarehouseLocationEntity = warehouseLocationEntities.stream().filter(req -> req.getWarehouseId().equals(pdaPcViewDTO.getWarehouseId()) && req.getCode().equals(pdaPcViewDTO.getOutWarehouseLocation())).findFirst().orElse(new WarehouseLocationEntity());
            pdaPcViewDTO.setOutWarehouseLocationName(outWarehouseLocationEntity.getName());
            //设置库存
            List<InventoryDTO.InventoryViewQtyDTO> inventoryQtys = inventoryService.getInventoryQty(Arrays.asList(inventoryBySkuIdAndWarehouseDTO));
            inventoryQtys.stream().forEach(inventoryQtyDTO -> {
                pdaPcViewDTO.setUsableQty(inventoryQtyDTO.getUsableQty());
                pdaPcViewDTO.setFrozenQty(inventoryQtyDTO.getFrozenQty());
                pdaPcViewDTO.setRealQty(inventoryQtyDTO.getRealQty());
            });
        });
        return pdaPcViewDTO;
    }
    /**
    * 启动流程
    *
    * @param entity
    * @return void
    * @Date 2023/7/4 10:07
    **/

    public void startProcess(WarehouseLocationMoveInfoEntity entity) {
        ProcessManagementDTO.StartDTO startDTO = new ProcessManagementDTO.StartDTO();
        startDTO.setBusinessId(entity.getId());
        startDTO.setBusinessCode(entity.getCode());
        startDTO.setBusinessKey(ModuleTypeEnum.WAREHOUSE_LOCATION_MOVE_INFO.getCode());
        startDTO.setBusinessName(entity.getCode());
        startDTO.setUserId(commonService.getUserInfo().getUid());
        startDTO.setVariablesMap(BeanUtil.beanToMap(entity));
        ApiResult<ProcessManagementDTO.StartResultDTO> result = workflowFeign.start(startDTO);
        if (!result.isSuccess()) {
            throw new ServiceException(result.getMsg());
        }
    }
    private void fillOne(WarehouseLocationMoveInfoDTO.ViewDTO data) {
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
        LoginUser userInfo = commonService.getUserInfo();
        this.lambdaUpdate().eq(WarehouseLocationMoveInfoEntity::getId, id)
            .set(WarehouseLocationMoveInfoEntity::getApproveUserId, userInfo.getUid())
            .set(WarehouseLocationMoveInfoEntity::getApproveUserName, userInfo.getUserName())
            .set(WarehouseLocationMoveInfoEntity::getApproveStatus, approveStatus)
            .set(WarehouseLocationMoveInfoEntity::getApproveTime, LocalDateTime.now())
            .update(new WarehouseLocationMoveInfoEntity());
     }

    /**
    * 反审核更新审核信息
    * @param id
    * @param approveStatus
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateForDisApprove(String id, String approveStatus) {
        this.lambdaUpdate().eq(WarehouseLocationMoveInfoEntity::getId, id)
            .set(WarehouseLocationMoveInfoEntity::getApproveUserId, "")
            .set(WarehouseLocationMoveInfoEntity::getApproveUserName, "")
            .set(WarehouseLocationMoveInfoEntity::getApproveStatus, approveStatus)
            .set(WarehouseLocationMoveInfoEntity::getApproveTime, null)
            .update(new WarehouseLocationMoveInfoEntity());
        }

    /**
    * 更新审核状态
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateApproveStatus(String id, String approveStatus) {
        lambdaUpdate().eq(WarehouseLocationMoveInfoEntity::getId, id)
        .set(WarehouseLocationMoveInfoEntity::getApproveStatus, approveStatus)
        .update(new WarehouseLocationMoveInfoEntity());
    }

    /**
    * 分页查询、导出 数据处理
    */
    private void fillList(List<WarehouseLocationMoveInfoDTO.PdaListDTO> list) {
        if(CollUtil.isEmpty(list)) {
           return;
        }
       //主键id
        List<String> ids = list.stream().map(req -> req.getId()).collect(Collectors.toList());
        //查询详情
        List<WarehouseLocationMoveDetailEntity> detailEntityList = warehouseLocationMoveDetailService.listByMainIds(ids);
        List<String> warehouseIds = list.stream().map(req -> req.getWarehouseId()).distinct().collect(Collectors.toList());
        List<WarehouseLocationEntity> warehouseLocationEntities = warehouseLocationService.listByWarehouseIds(warehouseIds);
        // 属性赋值
        for(WarehouseLocationMoveInfoDTO.PdaListDTO data : list) {
            data.setApproveStatusName(ApproveStatusEnum.getName(data.getApproveStatus()));
            List<WarehouseLocationMoveDetailEntity> detailEntities = detailEntityList.stream().filter(obj -> obj.getMainId().equals(data.getId())).collect(Collectors.toList());
            List<WarehouseLocationMoveInfoDTO.PdaItemDTO> itemDTOList = BeanMapper.copyList(detailEntities, WarehouseLocationMoveInfoDTO.PdaItemDTO.class);
            List<String> skuList = itemDTOList.stream().map(req -> req.getSkuId()).distinct().collect(Collectors.toList());
            data.setDetailCount(skuList.size());
            for (WarehouseLocationMoveInfoDTO.PdaItemDTO pdaItemDTO : itemDTOList) {
                WarehouseLocationEntity warehouseLocationEntity = warehouseLocationEntities.stream().filter(req -> req.getWarehouseId().equals(data.getWarehouseId()) && req.getCode().equals(pdaItemDTO.getInWarehouseLocation())).findFirst().orElse(new WarehouseLocationEntity());
                pdaItemDTO.setInWarehouseLocationName(warehouseLocationEntity.getName());
            }
            data.setItemList(itemDTOList);
        }
    }

    /**
    * 分页查询、导出 数据处理
    */
    private void validateSubmit(WarehouseLocationMoveInfoEntity entity) {
        // 待提交或审核不通过并且未作废允许提交
        if(!ApproveStatusEnum.allowUpdateStatus(entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98010);
        }
        return;
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(WarehouseLocationMoveInfoEntity warehouseLocationMoveInfoEntity) {
        if (StringUtils.isBlank(warehouseLocationMoveInfoEntity.getId()) && ObjectUtil.isNull(warehouseLocationMoveInfoEntity.getBillDate())) {
            warehouseLocationMoveInfoEntity.setBillDate(LocalDate.now());
        }
        warehouseLocationMoveInfoEntity.getWarehouseId();
        List<WarehouseDTO.UpdateDTO> warehouseList = warehouseService.listWarehouseByIds(Arrays.asList(warehouseLocationMoveInfoEntity.getWarehouseId()));
        WarehouseDTO.UpdateDTO updateDTO = warehouseList.stream().filter(req -> req.getId().equals(warehouseLocationMoveInfoEntity.getWarehouseId())).findFirst().orElse(new WarehouseDTO.UpdateDTO());
        warehouseLocationMoveInfoEntity.setWarehouseName(updateDTO.getName());
        warehouseLocationMoveInfoEntity.setInventoryOrgId(updateDTO.getOrgId());
        //获取核算公司
        SysAccountingCompanyEntity companyEntity = sysUserFeign.getCompanyById(updateDTO.getOrgId());
        if (ObjectUtil.isNotEmpty(companyEntity)) {
            warehouseLocationMoveInfoEntity.setInventoryOrgName(companyEntity.getCompanyName());
        }
    }

    @Override
    public Boolean invalid(List<String> ids, String remark) {
        List<WarehouseLocationMoveInfoEntity> infoEntityList = this.listByIds(ids);
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
        lambdaUpdate().set(WarehouseLocationMoveInfoEntity::getInvalidStatus, Boolean.TRUE)
                .set(WarehouseLocationMoveInfoEntity::getInvalidRemark, remark)
                .in(WarehouseLocationMoveInfoEntity::getId, ids)
                .update();
        //操作日志
        List<Pair<String, String>> pairList = infoEntityList.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("作废了一个收货单【%s】，作废原因：".concat(remark), ModuleTypeEnum.WAREHOUSE_LOCATION_MOVE_INFO.getCode(), pairList, "作废操作");
        return Boolean.TRUE;
    }

    @Override
    public void listExport(WarehouseLocationMoveInfoDTO.ExportDTO dto, HttpServletResponse response) {
        List<WarehouseLocationMoveInfoDTO.PdaPcListDTO> pdaPcListDTOS = baseMapper.listExport(dto);
        List<String> skuList = pdaPcListDTOS.stream().map(req -> req.getSkuId()).distinct().collect(Collectors.toList());
        //feign获取产品信息
        List<SkuVO> skuVOList = plmTaskFeign.getSkuInfoByIds(skuList);
        List<String> warehouseIds = pdaPcListDTOS.stream().map(req -> req.getWarehouseId()).distinct().collect(Collectors.toList());
        List<WarehouseLocationEntity> warehouseLocationEntities = warehouseLocationService.listByWarehouseIds(warehouseIds);
        for (WarehouseLocationMoveInfoDTO.PdaPcListDTO pdaPcListDTO : pdaPcListDTOS) {
            SkuVO skuVO = skuVOList.stream().filter(req -> req.getSkuId().equals(pdaPcListDTO.getSkuId())).findFirst().orElse(null);
            pdaPcListDTO.setProductName(skuVO.getSkuName());
            WarehouseLocationEntity warehouseLocationEntity = warehouseLocationEntities.stream().filter(req -> req.getWarehouseId().equals(pdaPcListDTO.getWarehouseId()) && req.getCode().equals(pdaPcListDTO.getInWarehouseLocation())).findFirst().orElse(new WarehouseLocationEntity());
            pdaPcListDTO.setInWarehouseLocationName(warehouseLocationEntity.getName());
            WarehouseLocationEntity outWarehouseLocationEntity = warehouseLocationEntities.stream().filter(req -> req.getWarehouseId().equals(pdaPcListDTO.getWarehouseId()) && req.getCode().equals(pdaPcListDTO.getOutWarehouseLocation())).findFirst().orElse(new WarehouseLocationEntity());
            pdaPcListDTO.setOutWarehouseLocationName(outWarehouseLocationEntity.getName());
        }
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/pdaMoveInfo.xlsx";
        String name = "仓库移动导出";
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date);
        sb.append(name);
        try {
            new ExcelPrintUtils().patchExport(pdaPcListDTOS, response, sb.toString(), excelPath);
        } catch (IOException e) {
            throw new ServiceException(ApiError.ERROR_1015);
        }
    }
    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public Boolean importFile(MultipartFile excelFile, HttpServletResponse response) {
//
        return true;
    }
}
