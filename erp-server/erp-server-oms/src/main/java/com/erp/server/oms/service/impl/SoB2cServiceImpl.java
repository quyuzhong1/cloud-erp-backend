package com.erp.server.oms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.constant.ApproveType;
import com.common.business.dto.base.*;
import com.common.business.enums.*;
import com.common.business.service.SuperServiceImpl;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.enums.CurrencyEnum;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.common.core.utils.StrUtils;
import com.erp.model.oms.dto.*;
import com.erp.model.oms.entity.*;
import com.erp.model.oms.enums.*;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.model.wms.dto.inventory.InventoryQtyDTO;
import com.erp.model.wms.enums.inventory.InventoryStatusEnum;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.model.workflow.entity.ProcessBusinessEntity;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysDictFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.InventoryFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.oms.mapper.SoB2cMapper;
import com.erp.server.oms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>
 * B2C销售订单表 服务实现类
 * </p>
 *
 * @author Will
 * @since 2023-08-18
 */
@Slf4j
@Service
public class SoB2cServiceImpl extends SuperServiceImpl<SoB2cMapper, SoB2cEntity> implements SoB2cService {

    @Autowired
    private DocNoGenHelper docNoGenHelper;

    @Autowired
    private OperateLogService operateLogService;

    @Autowired
    private WorkflowFeign workflowFeign;

    @Autowired
    private CommonService commonService;

    @Autowired
    private PlmTaskFeign plmTaskFeign;

    @Autowired
    private SysUserFeign sysUserFeign;

    @Autowired
    private InventoryFeign inventoryFeign;

    @Autowired
    private SoB2cDetailService soB2cDetailService;

    @Autowired
    private SoB2cLogisticsService soB2cLogisticsService;

    @Autowired
    private SoB2cReceiverService soB2cReceiverService;

    @Autowired
    private SoB2cRefCategoryService soB2cRefCategoryService;

    @Autowired
    private ShopInfoService shopInfoService;

    @Autowired
    private SysDictFeign sysDictFeign;

    @Autowired
    private DmpTaskFeign dmpTaskFeign;

    @Autowired
    private SoB2cRefService soB2cRefService;

    @Override
    public PagingVO<SoB2cDTO.ListDTO> paging(PagingDTO<SoB2cDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<SoB2cDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public List<SoB2cDTO.TabListDTO> tabList(PermissionsDTO param) {
        SoB2cTabEnum[] values = SoB2cTabEnum.values();
        List<SoB2cDTO.TabListDTO> list = new ArrayList<>();
        for (SoB2cTabEnum item : values) {
            SoB2cDTO.PagingParamDTO searchParamDTO = new SoB2cDTO.PagingParamDTO();
            searchParamDTO.setPermissionSql(param.getPermissionSql());
            SoB2cDTO.TabListDTO resultDTO = new SoB2cDTO.TabListDTO();
            //搜索类型
            searchParamDTO.setTabFlag(item.getCode());
            //列表Tab查询状态处理
            Boolean isFlag = handleTableParam(searchParamDTO);
            Integer count = MathUtil.ZERO;
            if (isFlag) {
                count = this.baseMapper.listCount(searchParamDTO);
            }
            resultDTO.setCount(ObjectUtils.isEmpty(count) ? MathUtil.ZERO : count);
            resultDTO.setTabFlag(item.getCode());
            list.add(resultDTO);
        }
        return list;
    }


    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public String add(SoB2cDTO.AddDTO addDTO) {
        SoB2cEntity soB2cEntity = new SoB2cEntity();
        BeanMapperUtils.copy(addDTO, soB2cEntity);

        // 数据处理
        handleData(soB2cEntity);

        log.info("开始新增B2C销售订单表");
        // 生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_SO_B2C);
        soB2cEntity.setCode(code);
        soB2cEntity.setBillDate(ObjectUtils.isEmpty(soB2cEntity.getBillDate()) ? LocalDate.now() : soB2cEntity.getBillDate());
        boolean save = super.save(soB2cEntity);
        if(!save) {
           throw new ServiceException("B2C销售订单表保存失败");
        }

        //新增物流信息
        soB2cLogisticsService.add(addDTO.getLogisticsDTO(),soB2cEntity.getId());
        //新增买家信息
        soB2cReceiverService.add(addDTO.getReceiverDTO(),soB2cEntity.getId());
        //新增明细
        soB2cDetailService.add(addDTO.getDetailList(),soB2cEntity.getId());
        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", commonService.getUserInfo().getUserName(), "B2C销售订单表" , soB2cEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SO_B2C.getCode(), soB2cEntity.getId(), "新增操作");

        //自动匹配订单规则
        approveRule(soB2cEntity.getId());
        //自动匹配配货规则
        distributionRule(soB2cEntity.getId());

        return soB2cEntity.getId();
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO update(SoB2cDTO.UpdateDTO updateDTO) {
        SoB2cEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "B2C销售订单表"));
        // 待提交和审核不通过允许修改
        if (!ApproveStatusEnum.allowUpdateStatus(old.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_1029);
        }

        SoB2cEntity soB2cEntity =  BeanMapperUtils.map(SoB2cEntity.class, updateDTO);

        // 数据处理
        handleData(soB2cEntity);

        log.info("编辑 开始修改B2C销售订单表数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(soB2cEntity);
        if(!save) {
           throw new ServiceException("B2C销售订单表保存失败");
        }

        //修改物流信息
        soB2cLogisticsService.update(updateDTO.getLogisticsDTO(),soB2cEntity.getId());
        //修改买家信息
        soB2cReceiverService.update(updateDTO.getReceiverDTO(),soB2cEntity.getId());
        //修改明细
        soB2cDetailService.update(updateDTO.getDetailList(),soB2cEntity.getId());

        // 记录主单操作日志
        log.info("编辑 开始记录B2C销售订单表日志数据，单号：【{}】", soB2cEntity.getCode());
        String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), soB2cEntity.getCode(), "B2C销售订单表");
        operateLogService.addModuleOperateLogByObj(old, soB2cEntity, ModuleTypeEnum.SO_B2C.getCode(), soB2cEntity.getId(), msg);
        return BatchResultDTO.success(soB2cEntity.getCode(), OperationTypeEnum.SUBMIT);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO submit(String id,Boolean isProcess) {
        SoB2cEntity entity = getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException("未找到B2C销售订单表数据");
        }
        validateSubmit(entity);
        // 更新单据审核状态
        log.info("提交 开始修改B2C销售订单表状态数据，id：【{}】", id);
        this.updateApproveStatus(id, ApproveStatusEnum.APPROVE_ING.getStatus());

        log.info("提交 开始启动B2C销售订单表流程，id=：【{}】", entity.getId());
        if (isProcess) {
            startProcess(entity);
        }

        // 记录操作日志
        log.info("提交 开始记录B2C销售订单表日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据提交审核 ", commonService.getUserInfo().getUserName(), entity.getCode(), "B2C销售订单表");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SO_B2C.getCode(), entity.getId(), "提交操作");
        return BatchResultDTO.success(entity.getCode(), OperationTypeEnum.SUBMIT);
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO approve(ApproveOneDTO dto) {
        ApproveTypeEnum approveType = ApproveTypeEnum.getByCode(dto.getType());
        if(Objects.equals(approveType, ApproveTypeEnum.REJECT) && StrUtils.isEmpty(dto.getComment())) {
            throw new ServiceException(ApiError.REJECT_COMMENT_NOT_EMPTY);
        }
        SoB2cEntity entity = getById(dto.getId());
        // 审核中的数据允许审核
        if(!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING)) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        // 调用流程审核
        approveProcess(entity, dto);

        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据审核操作  审核结果：【{}】 审核意见 ：【{}】", commonService.getUserInfo().getUserName(), entity.getCode(), "B2C销售订单表", approveType.getName(), dto.getComment());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SO_B2C.getCode(), entity.getId(), "审核操作");
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(approveType);
        return BatchResultDTO.success(entity.getCode(), OperationTypeEnum.approveStatus(approveStatus));
    }

    /**
    * 审核流程处理
    * @param entity
    * @param dto
    */
    private void approveProcess(SoB2cEntity entity, ApproveOneDTO dto) {
        LoginUser userInfo = commonService.getUserInfo();
        ProcessManagementDTO.ApproveDTO approveDTO = new ProcessManagementDTO.ApproveDTO();
        approveDTO.setBusinessId(entity.getId());
        approveDTO.setBusinessKey(SourceTypeEnum.SO_B2C.getCode());
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


    /**
    * 作废
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO invalid(String id, String remark, SoB2cInvalidTypeEnum soB2cInvalidTypeEnum) {
        SoB2cEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到B2C销售订单表数据"));
        // 待提交或审核不通过并且未作废允许作废
        if ((!ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(entity.getApproveStatus()) && !ApproveStatusEnum.REJECT.getStatus().equals(entity.getApproveStatus())) || !InvalidStatusEnum.NOT_VOIDED.getStatus().equals(entity.getInvalidStatus())) {
           throw new ServiceException(ApiError.ERROR_98005);
        }
        log.info("作废 开始修改B2C销售订单表状态数据，id：【{}】", id);
        lambdaUpdate().eq(SoB2cEntity::getId, id)
            .set(SoB2cEntity::getInvalidStatus, InvalidStatusEnum.VOIDED.getStatus())
            .set(SoB2cEntity::getInvalidRemark, remark)
            .set(SoB2cEntity::getInvalidType, soB2cInvalidTypeEnum.getCode())
            .update();

        log.info("作废 开始记录操作日志，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据作废操作 作废原因：【{}】", commonService.getUserInfo().getUserName(), entity.getCode(), "B2C销售订单表", remark);
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SO_B2C.getCode(), entity.getId(), "作废操作");
        return BatchResultDTO.success(entity.getCode(), OperationTypeEnum.INVALID);
     }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO unInvalid(String id, SoB2cInvalidTypeEnum soB2cInvalidTypeEnum) {
        SoB2cEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到B2C销售订单表数据"));
        if (InvalidStatusEnum.NOT_VOIDED.getStatus().equals(entity.getInvalidStatus())) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_INVALID);
        }
        if (!soB2cInvalidTypeEnum.getCode().equals(entity.getInvalidType()) ) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_INVALID,entity.getCode(), soB2cInvalidTypeEnum.getName());
        }
        log.info("反作废 开始修改B2C销售订单表状态数据，id：【{}】", id);
        lambdaUpdate().eq(SoB2cEntity::getId, id)
                .set(SoB2cEntity::getInvalidStatus, InvalidStatusEnum.NOT_VOIDED.getStatus())
                .update();

        log.info("反作废 开始记录操作日志，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据反作废操作 ", commonService.getUserInfo().getUserName(), entity.getCode(), "B2C销售订单表");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SO_B2C.getCode(), entity.getId(), "反作废操作");
        return BatchResultDTO.success(entity.getCode(), OperationTypeEnum.UN_INVALID);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approveEnd(ApproveOneDTO dto, SoB2cEntity entity) {
        if (ObjectUtil.isEmpty(entity)) {
            return Boolean.TRUE;
        }
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(dto.getType());
        updateForApprove(entity.getId(), approveStatus.getStatus());
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO updateRemark(String id, String remark) {
        //B2C销售订单主表信息
        SoB2cEntity entity = this.getById(id);
        if (ObjectUtils.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_EXIST);
        }
       this.lambdaUpdate().eq(SoB2cEntity::getId,id).set(SoB2cEntity::getRemark,remark).update(new SoB2cEntity());
        String msg = StrUtil.format("订单备注由{}变更为{}",entity.getRemark(),remark);
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SO_B2C.getCode(), entity.getId(), "修改订单备注");
        return BatchResultDTO.success(entity.getCode(),"更新订单备注");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO updateCategory(String id, SoB2cCategoryTypeEnum typeEnum, List<String> categoryIdList) {
        //B2C销售订单主表信息
        SoB2cEntity entity = this.getById(id);
        if (ObjectUtils.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_EXIST);
        }
        if (SoB2cCategoryTypeEnum.ENUM_ADD.equals(typeEnum)) {
            addCategory(categoryIdList,id);
        }
        if (SoB2cCategoryTypeEnum.ENUM_UPDATE.equals(typeEnum)) {
            updateCategory(categoryIdList,id);
        }
        if (SoB2cCategoryTypeEnum.ENUM_DELETE.equals(typeEnum)) {
            deleteCategory(id);
        }
        return BatchResultDTO.success(entity.getCode(),"更新订单分类");
    }

    @Override
    public List<SoB2cDTO.ViewSoB2cDistributionDTO> viewSoB2cDistribution(BaseIdsDTO.IdsDTO dto) {
        //B2C销售订单主表信息
        List<SoB2cEntity> list = this.listByIds(dto.getIds());
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_EXIST);
        }
        //物流信息
        List<SoB2cLogisticsEntity> soB2cLogisticsList = soB2cLogisticsService.listByMainIds(dto.getIds());
        if (CollectionUtils.isEmpty(soB2cLogisticsList)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_LOGISTICS_NOT_EXIST);
        }
        List<SoB2cDTO.ViewSoB2cDistributionDTO> resultList = new ArrayList<>();
        for (SoB2cEntity soB2cEntity : list) {
            SoB2cDTO.ViewSoB2cDistributionDTO viewDTO = new SoB2cDTO.ViewSoB2cDistributionDTO();
            viewDTO.setId(soB2cEntity.getId());
            viewDTO.setCode(soB2cEntity.getCode());
            viewDTO.setSourceAmount(soB2cEntity.getAmount());
            viewDTO.setSourceCurrency(soB2cEntity.getCurrency());
            viewDTO.setAmount(MathUtil.multiply(soB2cEntity.getAmount(),soB2cEntity.getExchangeRate()));
            viewDTO.setCurrency(CurrencyEnum.CNY.getCurrencyCode());
            //物流信息
            SoB2cLogisticsEntity soB2cLogisticsEntity = soB2cLogisticsList.stream().filter(obj -> obj.getMainId().equals(soB2cEntity.getId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(soB2cLogisticsEntity)) {
                throw new ServiceException(ApiError.ERROR_SO_B2C_LOGISTICS_NOT_EXIST);
            }
            viewDTO.setWeight(soB2cLogisticsEntity.getWeight());
            viewDTO.setLogisticsCode(soB2cLogisticsEntity.getCode());
            viewDTO.setLogisticsMethod(soB2cLogisticsEntity.getDictLogisticsMethod());
            resultList.add(viewDTO);
        }
        return resultList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO saveSoB2cDistribution(String id, SoB2cDTO.SaveSoB2cDistributionDTO dto) {
        //B2C销售订单主表信息
        SoB2cEntity entity = this.getById(id);
        if (ObjectUtils.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_EXIST);
        }
        //待配货和配货中订单允许配货
        if (!SoB2cBillStatusEnum.ENUM_WAIT_DISTRIBUTION.getCode().equals(entity.getBillStatus())
                && !SoB2cBillStatusEnum.ENUM_IN_DISTRIBUTION.getCode().equals(entity.getBillStatus())) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_EXIST);
        }
        //物流信息
        SoB2cLogisticsEntity soB2cLogisticsEntity = soB2cLogisticsService.getByMainId(id);
        if (ObjectUtils.isEmpty(soB2cLogisticsEntity)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_LOGISTICS_NOT_EXIST);
        }
        /**
         * 是否覆盖
         * 是：按照新选择的物流渠道和仓库下推配货中；如果物流方式跟订单已有的物流不一致，清空物流单号信息，且更新明细仓库
         * 否：新选择的物流渠道和仓库只添加到物流方式和仓库为空的订单，已存在物流方式和仓库的订单不做更改
         */
        Boolean isCover = dto.getIsCover();
        if (Boolean.TRUE.equals(isCover)) {
            soB2cLogisticsEntity.setDictLogisticsMethod(dto.getDictLogisticsMethod());
        } else {
            if (StringUtils.isBlank(soB2cLogisticsEntity.getDictLogisticsMethod())) {
                soB2cLogisticsEntity.setDictLogisticsMethod(dto.getDictLogisticsMethod());
            }
        }
        //货物物流单号，TODO
        String logisticsCode = IdWorker.getIdStr();
        soB2cLogisticsEntity.setCode(logisticsCode);
        //物流信息更新
        soB2cLogisticsService.updateById(soB2cLogisticsEntity);
        //明细仓库更新
        if (Boolean.TRUE.equals(isCover)) {
            soB2cDetailService.updateWarehouseIdByMainId(id,dto.getWarehouseId());
        }
        //销售订单更新
        entity.setBillStatus(SoB2cBillStatusEnum.ENUM_IN_DISTRIBUTION.getCode());
        this.updateById(entity);
        return BatchResultDTO.success(entity.getCode(),"手动配货");
    }

    @Override
    public BatchResultDTO getLogisticsCode(String id, Boolean isDelivery) {
        //B2C销售订单主表信息
        SoB2cEntity entity = this.getById(id);
        if (ObjectUtils.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_EXIST);
        }
        //物流信息
        SoB2cLogisticsEntity soB2cLogisticsEntity = soB2cLogisticsService.getByMainId(id);
        if (ObjectUtils.isEmpty(soB2cLogisticsEntity)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_LOGISTICS_NOT_EXIST);
        }
        if (StringUtils.isBlank(soB2cLogisticsEntity.getDictLogisticsMethod())
                || StringUtils.isNotBlank(soB2cLogisticsEntity.getCode()) ) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_LOGISTICS_CODE);
        }

        //货取物流单号，TODO
        String logisticsCode = IdWorker.getIdStr();
        soB2cLogisticsService.updateLogisticsCode(id,logisticsCode);


        if (Boolean.TRUE.equals(isDelivery)) {
            //提交发货
            submitDelivery(id);
        }

        return BatchResultDTO.success(entity.getCode(),"获取物流单号");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO submitDelivery(String id) {
        //B2C销售订单主表信息
        SoB2cEntity entity = this.getById(id);
        if (ObjectUtils.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_EXIST);
        }
        /**
         * 未获取物流单号或获取物流单号失败的订单不允许提交发货
         * 缺货订单不允许提交发货
         * 存在多发货仓库的，不允许提交发货
         */
        //B2C销售订单明细信息
        List<SoB2cDetailEntity> list = soB2cDetailService.listByMainId(id);
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_DETAIL_NOT_EXIST);
        }
        //发货仓库
        long count = list.stream().map(SoB2cDetailEntity::getWarehouseId).distinct().count();
        if (count > MathUtil.ONE) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_DELIVERY_WAREHOUSE_COMPLEX);
        }
        //skuId集合
        List<String> skuIdList = list.stream().map(SoB2cDetailEntity::getSkuId).distinct().collect(Collectors.toList());
        //发货仓库Id集合
        List<String> warehouseIdList = list.stream().map(SoB2cDetailEntity::getWarehouseId).distinct().collect(Collectors.toList());

        //查询可用库存
        InventoryQtyDTO.SkuInventoryParamDTO skuInventoryDTO = new InventoryQtyDTO.SkuInventoryParamDTO();
        skuInventoryDTO.setWarehouseIdList(warehouseIdList);
        skuInventoryDTO.setSkuIdList(skuIdList);
        skuInventoryDTO.setInventoryStatus(InventoryStatusEnum.USABLE.getCode());
        List<InventoryQtyDTO.SkuInventoryTotalDTO> inventoryList = inventoryFeign.listSkuInventoryByParam(skuInventoryDTO);
        if (CollectionUtils.isEmpty(inventoryList)) {
            log.error("B2C销售订单【{}】未找到可用库存，skuIdList = {}，warehouseIdList = {}",entity.getCode(),skuIdList,warehouseIdList);
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_INVENTORY,entity.getCode());
        }
        for (SoB2cDetailEntity detailEntity : list) {
            //验证是否存在可用库存
            Integer useableQty = inventoryList.stream().filter(obj -> obj.getSkuId().equals(detailEntity.getSkuId()) && obj.getWarehouseId().equals(detailEntity.getWarehouseId()))
                    .findFirst().flatMap(obj -> Optional.ofNullable(obj.getInventoryTotal())).orElse(MathUtil.ZERO);
            if (MathUtil.compareTo(detailEntity.getQty(),useableQty) > MathUtil.ZERO) {
                throw new ServiceException(ApiError.ERROR_SO_B2C_SKU_NOT_INVENTORY,entity.getCode(),detailEntity.getSkuNo(),detailEntity.getWarehouseName());
            }
        }
        return  BatchResultDTO.success(entity.getCode(),"提交发货");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO deliveryIntercept(String id, String remark) {
        //B2C销售订单主表信息
        SoB2cEntity entity = this.getById(id);
        if (ObjectUtils.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_EXIST);
        }
        //TODO
        return BatchResultDTO.success(entity.getCode(),"发货拦截");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO cancelDeliveryIntercept(String id) {
        //B2C销售订单主表信息
        SoB2cEntity entity = this.getById(id);
        if (ObjectUtils.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_EXIST);
        }
        //TODO
        return BatchResultDTO.success(entity.getCode(),"取消发货拦截");
    }

    @Override
    public PagingVO<SoB2cDTO.MergeListDTO> mergePaging(PagingDTO<SoB2cDTO.MergePagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<SoB2cDTO.MergeListDTO> pageData = this.baseMapper.mergePaging(query, pagingParamDTO.getParams());
        List<SoB2cDTO.MergeListDTO> records = pageData.getRecords();
        fillMergeData(records);
        return new PagingVO(pageData);
    }

    @Override
    public Integer mergePagingCount(SoB2cDTO.MergePagingParamDTO pagingParamDTO) {
        pagingParamDTO.setPermissionSql(pagingParamDTO.getPermissionSql());
        return this.baseMapper.mergePagingCount(pagingParamDTO);
    }



    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean mergeSave(List<String> ids) {
        if (MathUtil.TWO.intValue() > ids.size()) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_MERGE_SIZE);
        }

        List<SoB2cEntity> list = this.listByIds(ids);
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_EXIST);
        }
        //物流信息
        List<SoB2cLogisticsEntity> soB2cLogisticsList = soB2cLogisticsService.listByMainIds(ids);
        if (CollectionUtils.isEmpty(soB2cLogisticsList)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_LOGISTICS_NOT_EXIST);
        }
        //买家信息
        List<SoB2cReceiverEntity> soB2cReceiverList = soB2cReceiverService.listByMainIds(ids);
        if (CollectionUtils.isEmpty(soB2cReceiverList)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_RECEIVER_NOT_EXIST);
        }
        // 销售明细
        List<SoB2cDetailEntity> soB2cDetailList = soB2cDetailService.listByMainIds(ids);
        if (CollectionUtils.isEmpty(soB2cDetailList)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_DETAIL_NOT_EXIST);
        }
        //检验合并数据
        checkMergeData (list,soB2cLogisticsList,soB2cReceiverList,soB2cDetailList);

        //新增合并后数据
        SoB2cDTO.AddDTO addDTO = new SoB2cDTO.AddDTO();
        BeanMapperUtils.copy(list.get(0),addDTO);
        BigDecimal totalAmount = list.stream().map(SoB2cEntity::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        addDTO.setAmount(totalAmount);
        //合并后取最小单据日期
        LocalDate billDate = list.stream().map(SoB2cEntity::getBillDate).min((x, y) -> x.compareTo(y)).orElse(null);
        addDTO.setBillDate(billDate);
        addDTO.setSourceType(SourceTypeEnum.SO_B2C.getCode());
        addDTO.setSourceId(StrUtil.join(",",ids));
        List<String> codes = list.stream().map(SoB2cEntity::getCode).collect(Collectors.toList());
        addDTO.setSourceCode(StrUtil.join(",",codes));

        //物流信息
        SoB2cLogisticsDTO.AddDTO logisticsAddDTO = new SoB2cLogisticsDTO.AddDTO();
        BeanMapperUtils.copy(soB2cLogisticsList.get(0),logisticsAddDTO);

        //查询汇率
        BigDecimal rate = dmpTaskFeign.getRate(billDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), list.get(0).getCurrency());
        if (ObjectUtils.isEmpty(rate) || MathUtil.compareTo(BigDecimal.ZERO,rate) == MathUtil.ZERO) {
            throw new ServiceException(ApiError.ERROR_EXCHANGE_RATE_NOT_EXIST,billDate,list.get(0).getCurrency());
        }

        //预估运费(合并后默认转本位币)
        BigDecimal estimatedShippingCost = soB2cLogisticsList.stream().map(obj -> CurrencyEnum.CNY.getCurrencyCode().equals(obj.getEstimatedShippingCurrency()) ?
                obj.getEstimatedShippingCost() : MathUtil.multiply(obj.getEstimatedShippingCost(),rate)).reduce(BigDecimal.ZERO, BigDecimal::add);
        logisticsAddDTO.setEstimatedShippingCost(estimatedShippingCost);
        //实际运费(合并后默认转本位币)
        BigDecimal actualShippingCost = soB2cLogisticsList.stream().map(obj -> CurrencyEnum.CNY.getCurrencyCode().equals(obj.getActualShippingCurrency()) ?
                obj.getActualShippingCost() : MathUtil.multiply(obj.getActualShippingCost(),rate)).reduce(BigDecimal.ZERO, BigDecimal::add);
        logisticsAddDTO.setActualShippingCost(actualShippingCost);
        //包装辅料费(合并后默认转本位币)
        BigDecimal accessoriesCost = soB2cLogisticsList.stream().map(obj -> CurrencyEnum.CNY.getCurrencyCode().equals(obj.getAccessoriesCostCurrency()) ?
                obj.getAccessoriesCost() : MathUtil.multiply(obj.getAccessoriesCost(),rate)).reduce(BigDecimal.ZERO, BigDecimal::add);
        logisticsAddDTO.setAccessoriesCost(accessoriesCost);
        //包装辅料数量
        Integer accessoriesQty = soB2cLogisticsList.stream().map(SoB2cLogisticsEntity::getAccessoriesQty).reduce(MathUtil.ZERO, Integer::sum);
        logisticsAddDTO.setAccessoriesQty(accessoriesQty);
        //包装辅料净重
        BigDecimal accessoriesNw = soB2cLogisticsList.stream().map(SoB2cLogisticsEntity::getAccessoriesNw).reduce(BigDecimal.ZERO, BigDecimal::add);
        logisticsAddDTO.setAccessoriesNw(accessoriesNw);
        //长
        BigDecimal height = soB2cLogisticsList.stream().map(SoB2cLogisticsEntity::getHeight).reduce(BigDecimal.ZERO, BigDecimal::add);
        logisticsAddDTO.setHeight(height);
        //宽
        BigDecimal width = soB2cLogisticsList.stream().map(SoB2cLogisticsEntity::getWidth).reduce(BigDecimal.ZERO, BigDecimal::add);
        logisticsAddDTO.setWidth(width);
        //高
        BigDecimal weight = soB2cLogisticsList.stream().map(SoB2cLogisticsEntity::getWeight).reduce(BigDecimal.ZERO, BigDecimal::add);
        logisticsAddDTO.setWeight(weight);


        addDTO.setLogisticsDTO(logisticsAddDTO);
        //买家信息
        SoB2cReceiverDTO.AddDTO receiverAddDTO = new SoB2cReceiverDTO.AddDTO();
        BeanMapperUtils.copy(soB2cReceiverList.get(0),receiverAddDTO);
        addDTO.setReceiverDTO(receiverAddDTO);

        //订单分类
        List<SoB2cRefCategoryEntity> soB2cRefCategoryList = soB2cRefCategoryService.listByMainIds(ids);
        if (CollectionUtils.isNotEmpty(soB2cRefCategoryList)) {
            List<String> categoryIdList = soB2cRefCategoryList.stream().map(SoB2cRefCategoryEntity::getCategoryId).distinct().collect(Collectors.toList());
            addDTO.setCategoryIdList(categoryIdList);
        }

        //明细信息
        List<SoB2cDetailDTO.AddDTO> detailList = new ArrayList<>();
        for (SoB2cDetailEntity detailEntity : soB2cDetailList) {
            SoB2cDetailDTO.AddDTO detailAddDTO = new SoB2cDetailDTO.AddDTO();
            BeanMapperUtils.copy(detailEntity,detailAddDTO);
            detailList.add(detailAddDTO);
        }
        addDTO.setDetailList(detailList);
        log.info("新增合并后的B2C销售订单，addDTO = {}",addDTO);
        //新增数据
        String soId = this.add(addDTO);
        //新增关联信息
        List<SoB2cRefDTO.AddDTO> refList = new ArrayList<>();
        for (String id : ids) {
            SoB2cRefDTO.AddDTO refAddDTO = new SoB2cRefDTO.AddDTO();
            refAddDTO.setType(SoB2cOptionTypeEnum.ENUM_MERGE.getCode());
            refAddDTO.setSourceId(id);
            refAddDTO.setTargetId(soId);
            refList.add(refAddDTO);
            log.info("作废原销售订单数据，id = {}",id);
            //作废
            this.invalid(id, StrUtil.format("B2C销售订单【{}】合并作废"), SoB2cInvalidTypeEnum.ENUM_AUTOMATIC);
        }
        log.info("新增合并后的订单关联关系，refList = {}",refList);
        //新增关联关系
        soB2cRefService.add(refList);
        return Boolean.TRUE;
    }



    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO cancelMerge(String id) {
        //B2C销售订单主表信息
        SoB2cEntity entity = this.getById(id);
        if (ObjectUtils.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_EXIST);
        }
        //关联数据
        List<SoB2cRefEntity> soB2cRefList = soB2cRefService.listByTargetId(id, SoB2cOptionTypeEnum.ENUM_MERGE);
        if (CollectionUtils.isEmpty(soB2cRefList)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_CANCEL_MERGE_NOT_EXIST,entity.getCode());
        }
        log.info("删除销售订单数据，id = {}",id);
        //删除合并后的数据
        deleteById(Arrays.asList(id));
        //反作废合并前的数据
        for (SoB2cRefEntity refEntity : soB2cRefList) {
            log.info("作废原销售订单数据，id = {}",refEntity.getId());
            unInvalid(refEntity.getId(), SoB2cInvalidTypeEnum.ENUM_AUTOMATIC);
        }
        return BatchResultDTO.success(entity.getCode(),"取消合并");
    }


    @Override
    public SoB2cDTO.ViewSplitDTO viewSplit(String id) {
        //B2C销售订单主表信息
        SoB2cEntity entity = this.getById(id);
        if (ObjectUtils.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_EXIST);
        }
        //验证拆分数据
        checkSplitData(id,entity);

        SoB2cDTO.ViewSplitDTO viewSplitDTO = new SoB2cDTO.ViewSplitDTO();
        viewSplitDTO.setId(id);
        //查询明细
        List<SoB2cDetailEntity> soB2cDetailList = soB2cDetailService.listByMainId(id);
        if (CollectionUtils.isEmpty(soB2cDetailList)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_DETAIL_NOT_EXIST);
        }
        //产品信息
        List<String> skuIdList = soB2cDetailList.stream().map(SoB2cDetailEntity::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.getSkuInfoByIds(skuIdList);
        if (CollectionUtils.isEmpty(skuList)) {
            throw new ServiceException(ApiError.ERROR_95084);
        }
        List<SoB2cDTO.ViewSplitDetailDTO> viewSplitDetailList = new ArrayList<>();
        for (SoB2cDetailEntity detailEntity : soB2cDetailList) {
            SoB2cDTO.ViewSplitDetailDTO viewSplitDetailDTO = new SoB2cDTO.ViewSplitDetailDTO();
            BeanMapperUtils.copy(detailEntity,viewSplitDetailDTO);
            SkuVO skuVO = skuList.stream().filter(obj -> obj.getSkuId().equals(detailEntity.getSkuId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(skuVO)) {
                throw new ServiceException(ApiError.ERROR_95084);
            }
            viewSplitDetailDTO.setProductName(skuVO.getSkuName());
            viewSplitDetailDTO.setSourceAmount(detailEntity.getAmount());
            viewSplitDetailDTO.setSourceCurrency(detailEntity.getCurrency());
            viewSplitDetailDTO.setAmount(MathUtil.multiply(detailEntity.getAmount(),detailEntity.getExchangeRate()));
            viewSplitDetailDTO.setCurrency(CurrencyEnum.CNY.getCurrencyCode());
            //产品包装重量 = SKU毛重 * 数量
            viewSplitDetailDTO.setWeight(MathUtil.multiply(skuVO.getGrossWeight(),detailEntity.getQty()));
            viewSplitDetailList.add(viewSplitDetailDTO);
        }
        viewSplitDTO.setDetailList(viewSplitDetailList);
        return viewSplitDTO;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean splitSave(SoB2cDTO.SplitSaveDTO dto) {
        //B2C销售订单主表信息
        SoB2cEntity entity = this.getById(dto.getId());
        if (ObjectUtils.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_EXIST);
        }
        /**
         * 拆分后金额、费用根据金额比例进行分摊
         */

        //验证拆分数据
        checkSplitData(dto.getId(),entity);
        //原单据明细
        List<SoB2cDetailEntity> oldDetailList = soB2cDetailService.listByMainId(dto.getId());
        if (CollectionUtils.isEmpty(oldDetailList)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_DETAIL_NOT_EXIST);
        }

        //物流信息
        SoB2cLogisticsEntity soB2cLogisticsEntity = soB2cLogisticsService.getByMainId(dto.getId());
        if (ObjectUtils.isEmpty(soB2cLogisticsEntity)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_LOGISTICS_NOT_EXIST);
        }
        SoB2cLogisticsDTO.AddDTO logisticsAddDTO = new SoB2cLogisticsDTO.AddDTO();
        BeanMapperUtils.copy(soB2cLogisticsEntity,logisticsAddDTO);

        //买家信息
        SoB2cReceiverEntity soB2cReceiverEntity = soB2cReceiverService.getByMainId(dto.getId());
        if (ObjectUtils.isEmpty(soB2cReceiverEntity)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_RECEIVER_NOT_EXIST);
        }
        SoB2cReceiverDTO.AddDTO receiverAddDTO = new SoB2cReceiverDTO.AddDTO();
        BeanMapperUtils.copy(soB2cReceiverEntity,receiverAddDTO);

        //订单分类
        List<SoB2cRefCategoryEntity> soB2cRefCategoryList = soB2cRefCategoryService.listByMainIds(Arrays.asList(dto.getId()));

        //原明细金额合计
        BigDecimal totalAmount = oldDetailList.stream().map(SoB2cDetailEntity::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        //拆分后数据
        List<SoB2cDTO.GroupSplitSaveDTO> splitList = dto.getGroupList();
        for (SoB2cDTO.GroupSplitSaveDTO groupSplitSaveDTO :  splitList) {
            //新建拆分后数据
            SoB2cDTO.AddDTO addDTO = new SoB2cDTO.AddDTO();
            BeanMapperUtils.copy(entity,addDTO);
            addDTO.setSourceId(entity.getSourceId());
            addDTO.setSourceCode(entity.getCode());
            addDTO.setSourceType(SourceTypeEnum.SO_B2C.getCode());
            if (CollectionUtils.isNotEmpty(soB2cRefCategoryList)) {
                List<String> categoryIdList = soB2cRefCategoryList.stream().map(SoB2cRefCategoryEntity::getCategoryId).collect(Collectors.toList());
                addDTO.setCategoryIdList(categoryIdList);
            }
            addDTO.setReceiverDTO(receiverAddDTO);

            //拆分后金额合计
            BigDecimal splitTotalAmount = BigDecimal.ZERO;

            List<SoB2cDetailDTO.AddDTO> detailList = new ArrayList<>();
            for (SoB2cDTO.SplitDetailSaveDTO splitDetailSaveDTO : groupSplitSaveDTO.getDetailList()) {
                SoB2cDetailEntity detailEntity = oldDetailList.stream().filter(obj -> obj.getId().equals(splitDetailSaveDTO.getId())).findFirst().orElse(null);
                if (ObjectUtils.isEmpty(detailEntity)) {
                    throw new ServiceException(ApiError.ERROR_SO_B2C_DETAIL_NOT_EXIST);
                }
                if (MathUtil.compareTo(splitDetailSaveDTO.getQty(),detailEntity.getQty()) > MathUtil.ZERO) {
                    log.error("订单【{}】SKU【{}】拆分数量【{}】不能大于原数量【{}】",entity.getCode(),detailEntity.getSkuNo(),splitDetailSaveDTO.getQty(),detailEntity.getQty());
                    throw new ServiceException(ApiError.ERROR_SO_B2C_SPLIT_QTY,entity.getCode(),detailEntity.getSkuNo(),splitDetailSaveDTO.getQty(),detailEntity.getQty());
                }
                SoB2cDetailDTO.AddDTO addDetailDTO = new SoB2cDetailDTO.AddDTO();
                BeanMapperUtils.copy(detailEntity,addDetailDTO);
                addDetailDTO.setQty(splitDetailSaveDTO.getQty());
                addDetailDTO.setSourceDetailId(detailEntity.getId());
                detailList.add(addDetailDTO);
                //累加拆分金额
                splitTotalAmount = MathUtil.add(splitTotalAmount, MathUtil.multiply(detailEntity.getPrice(), splitDetailSaveDTO.getQty()));

            }
            addDTO.setDetailList(detailList);
            //拆分金额所占比例
            BigDecimal rate = MathUtil.divide(splitTotalAmount, totalAmount);
            //基本信息金额
            addDTO.setAmount(MathUtil.multiply(rate,entity.getAmount()));

            //预估费用
            logisticsAddDTO.setEstimatedShippingCost(MathUtil.multiply(rate,soB2cLogisticsEntity.getEstimatedShippingCost()));
            //实际费用
            logisticsAddDTO.setActualShippingCost(MathUtil.multiply(rate,soB2cLogisticsEntity.getActualShippingCost()));
            //包装辅料费
            logisticsAddDTO.setAccessoriesCost(MathUtil.multiply(rate,soB2cLogisticsEntity.getActualShippingCost()));
            //包装净重
            logisticsAddDTO.setAccessoriesNw(MathUtil.multiply(rate,soB2cLogisticsEntity.getAccessoriesNw()));
            //包装重量
            logisticsAddDTO.setWeight(MathUtil.multiply(rate,soB2cLogisticsEntity.getWeight()));

            addDTO.setLogisticsDTO(logisticsAddDTO);

            //新增拆分后订单
            String soB2cId = this.add(addDTO);
            //新增拆分订单关联关系
            soB2cRefService.add(SoB2cOptionTypeEnum.ENUM_SPLIT.getCode(),entity.getId(),soB2cId);
        }
        this.invalid(entity.getId(),StrUtil.format("B2C销售订单【{}】拆分作废"), SoB2cInvalidTypeEnum.ENUM_AUTOMATIC);

        return Boolean.TRUE;
    }

    @Override
    public List<SoB2cDTO.CheckCancelSplitDTO> checkCancelSplit(List<String> ids) {
        //B2C销售订单主表信息
        List<SoB2cEntity> list = this.listByIds(ids);
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_EXIST);
        }
        List<String> codes = list.stream().map(SoB2cEntity::getCode).collect(Collectors.toList());
        List<String> sourceIdList = list.stream().map(SoB2cEntity::getSourceId).collect(Collectors.toList());
        List<SoB2cRefEntity> parentSoB2cRefList = soB2cRefService.listBySourceIds(sourceIdList, SoB2cOptionTypeEnum.ENUM_SPLIT);
        if (CollectionUtils.isEmpty(parentSoB2cRefList)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_SPLIT,codes);
        }
        List<String> targetIdList = parentSoB2cRefList.stream().map(SoB2cRefEntity::getTargetId).collect(Collectors.toList());
        List<SoB2cEntity> targetList = this.listByIds(targetIdList);
        if (CollectionUtils.isEmpty(targetList)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_EXIST);
        }

        List<SoB2cDTO.CheckCancelSplitDTO> resultList = new ArrayList<>();
        for (SoB2cEntity soB2cEntity : list) {
            SoB2cDTO.CheckCancelSplitDTO checkCancelSplitDTO = new SoB2cDTO.CheckCancelSplitDTO();
            checkCancelSplitDTO.setParentB2cSoCode(soB2cEntity.getSourceCode());
            //拆分后订单
            List<SoB2cEntity> splitList = targetList.stream().filter(obj -> obj.getSourceId().equals(soB2cEntity.getSourceId())).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(splitList)) {
                throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_SPLIT,soB2cEntity.getCode());
            }
            List<SoB2cDTO.CheckCancelSplitDetailDTO> detailList = new ArrayList<>();
            for (SoB2cEntity splitEntity : splitList) {
                SoB2cDTO.CheckCancelSplitDetailDTO checkCancelSplitDetailDTO = new SoB2cDTO.CheckCancelSplitDetailDTO();
                checkCancelSplitDetailDTO.setChildB2cSoCode(splitEntity.getCode());
                checkCancelSplitDetailDTO.setInvalidStatus(splitEntity.getInvalidStatus());
                detailList.add(checkCancelSplitDetailDTO);
            }
            checkCancelSplitDTO.setDetailList(detailList);
            resultList.add(checkCancelSplitDTO);
        }
        return resultList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO cancelSplit(String id) {
        //B2C销售订单主表信息
        SoB2cEntity entity = this.getById(id);
        if (ObjectUtils.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_EXIST);
        }
        //关联关系
        List<SoB2cRefEntity> soB2cRefList = soB2cRefService.listBySourceIds(Arrays.asList(id), SoB2cOptionTypeEnum.ENUM_SPLIT);
        if (CollectionUtils.isEmpty(soB2cRefList)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_PARENT_NOT_SPLIT);
        }
        List<String> targetIdList = soB2cRefList.stream().map(SoB2cRefEntity::getTargetId).collect(Collectors.toList());
        List<SoB2cEntity> targetList = this.listByIds(targetIdList);
        if (CollectionUtils.isEmpty(targetList)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_CHILD_NOT_EXIST,entity.getCode());
        }
        //验证拆分后单据是否作废
        String invalidCodes = targetList.stream().filter(obj -> InvalidStatusEnum.VOIDED.getStatus().equals(obj.getInvalidStatus())).map(SoB2cEntity::getCode).collect(Collectors.joining(","));
        if (StringUtils.isNotBlank(invalidCodes)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_CHILD_HAS_INVALID,invalidCodes);
        }
        //验证拆分后单据是否审核
        String approveCodes = targetList.stream().filter(obj -> ApproveStatusEnum.APPROVE.equals(obj.getApproveStatus())).map(SoB2cEntity::getCode).collect(Collectors.joining(","));
        if (StringUtils.isNotBlank(approveCodes)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_CHILD_HAS_APPROVE,approveCodes);
        }
        log.info("删除B2C销售订单数据，ids = {}",targetIdList);
        //删除拆分后的数据
        deleteById(targetIdList);
        //反作废合并前的数据
        log.info("反作废原B2C销售订单数据，id = {}",entity.getId());
        unInvalid(entity.getId(), SoB2cInvalidTypeEnum.ENUM_AUTOMATIC);
        return BatchResultDTO.success(entity.getCode(),"取消拆分");
    }

    /**
     * @description: 根据主表id删除
     * @author Will
     * @date: 2023/8/23 14:09
     * @param ids
     */
    private void deleteById (List<String> ids) {
        //删除物流信息
        soB2cLogisticsService.deleteByMainIds(ids);
        //删除买家信息
        soB2cReceiverService.deleteByMainIds(ids);
        //删除明细信息
        soB2cDetailService.deleteByMainIds(ids);
        //删除关联关系
        soB2cRefService.deleteByTargetIds(ids);
        //删除分类信息
        soB2cRefCategoryService.deleteByMainIds(ids);
        //删除操作日志
        operateLogService.removeByBusinessIds(ids);
    }


    @Override
    public SoB2cDTO.ViewDTO view(String id) {
        SoB2cEntity soB2cEntity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到B2C销售订单表数据"));
        SoB2cDTO.ViewDTO data = BeanMapperUtils.map(SoB2cDTO.ViewDTO.class, soB2cEntity);
        // 数据填充处理
        fillOne(data);
        //物流
        SoB2cLogisticsEntity soB2cLogisticsEntity = soB2cLogisticsService.getByMainId(id);
        if (ObjectUtils.isEmpty(soB2cLogisticsEntity)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_LOGISTICS_NOT_EXIST);
        }
        SoB2cLogisticsDTO.ViewDTO logisticsDTO = new SoB2cLogisticsDTO.ViewDTO();
        BeanMapperUtils.copy(soB2cLogisticsEntity,logisticsDTO);
        data.setLogisticsDTO(logisticsDTO);
        //买家
        SoB2cReceiverEntity soB2cReceiverEntity = soB2cReceiverService.getByMainId(id);
        if (ObjectUtils.isEmpty(soB2cReceiverEntity)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_RECEIVER_NOT_EXIST);
        }
        SoB2cReceiverDTO.ViewDTO receiverDTO = new SoB2cReceiverDTO.ViewDTO();
        BeanMapperUtils.copy(soB2cReceiverEntity,receiverDTO);
        data.setReceiverDTO(receiverDTO);
        //订单分类
        List<SoB2cRefCategoryEntity> soB2cRefCategoryList = soB2cRefCategoryService.listByMainIds(Arrays.asList(id));
        if (CollectionUtils.isNotEmpty(soB2cRefCategoryList)) {
            List<String> categoryIdList = soB2cRefCategoryList.stream().map(SoB2cRefCategoryEntity::getCategoryId).distinct().collect(Collectors.toList());
            data.setCategoryIdList(categoryIdList);
        }
        //明细
        List<SoB2cDetailEntity> soB2cDetailList = soB2cDetailService.listByMainId(id);
        if (CollectionUtils.isEmpty(soB2cDetailList)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_DETAIL_NOT_EXIST);
        }
        List<SoB2cDetailDTO.ViewDTO> detailList = BeanMapperUtils.copyList(SoB2cDetailDTO.ViewDTO.class, soB2cDetailList);
        data.setDetailList(detailList);
        return data;
    }
    /**
    * 启动流程
    *
    * @param entity
    * @return void
    * @Date 2023/7/4 10:07
    **/

    public void startProcess(SoB2cEntity entity) {
        ProcessManagementDTO.StartDTO startDTO = new ProcessManagementDTO.StartDTO();
        startDTO.setBusinessId(entity.getId());
        startDTO.setBusinessCode(entity.getCode());
        startDTO.setBusinessKey(SourceTypeEnum.SO_B2C.getCode());
        startDTO.setBusinessName(entity.getCode());
        startDTO.setUserId(commonService.getUserInfo().getUid());
        startDTO.setVariablesMap(BeanUtil.beanToMap(entity));
        ApiResult<ProcessManagementDTO.StartResultDTO> result = workflowFeign.start(startDTO);
        if (!result.isSuccess()) {
            throw new ServiceException(result.getMsg());
        }
    }
    private void fillOne(SoB2cDTO.ViewDTO data) {
        if (ObjectUtil.isEmpty(data)) {
            return;
        }
        data.setApproveStatusName(data.getApproveStatus().getName());
        data.setBillStatusName(SoB2cBillStatusEnum.getName(data.getBillStatus()));
    }

    /**
    * 审核更新审核信息
    * @param id
    * @param approveStatus
    */
    public void updateForApprove(String id, String approveStatus) {
        this.lambdaUpdate().eq(SoB2cEntity::getId, id)
            .set(SoB2cEntity::getApproveStatus, approveStatus)
            .set(ApproveStatusEnum.REJECT.getStatus().equals(approveStatus),SoB2cEntity::getAbnormalType,SoB2cAbnormalTypeEnum.ENUM_MANUAL_REJECT.getCode())
            .update(new SoB2cEntity());
     }


    /**
    * 更新审核状态
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateApproveStatus(String id, String approveStatus) {
        lambdaUpdate().eq(SoB2cEntity::getId, id)
        .set(SoB2cEntity::getApproveStatus, approveStatus)
        .set(SoB2cEntity::getAbnormalType,"")
        .update(new SoB2cEntity());
    }

    /**
     * @description: 新增分类
     * @author Will
     * @date: 2023/8/22 14:22
     * @param categoryIdList
     * @param mainId
     */
    private void  addCategory (List<String> categoryIdList,String mainId) {
        List<SoB2cRefCategoryEntity> list =   soB2cRefCategoryService.listByMainIds(Arrays.asList(mainId));
        List<SoB2cRefCategoryEntity> addList = new ArrayList<>();
        for (String categoryId : categoryIdList) {
            //如果已存在分类则无需新增
            if (CollectionUtils.isNotEmpty(list)) {
                long count = list.stream().filter(obj -> obj.getCategoryId().equals(categoryId)).count();
                if (count > 0) {
                    log.info("已存在分类，categoryId = {}",categoryId);
                    continue;
                }
            }
            SoB2cRefCategoryEntity entry = new SoB2cRefCategoryEntity();
            entry.setCategoryId(categoryId);
            entry.setSoB2cId(mainId);
            addList.add(entry);
        }
        soB2cRefCategoryService.saveBatch(addList);
    }
    /**
     * @description: 修改分类
     * @author Will
     * @date: 2023/8/22 14:36
     * @param categoryIdList
     * @param mainId
     */
    private void  updateCategory (List<String> categoryIdList,String mainId) {
        //删除原有分类
        deleteCategory(mainId);
        //新增分类
        addCategory(categoryIdList,mainId);
    }
    /**
     * @description: 删除已有分类
     * @author Will
     * @date: 2023/8/22 14:30
     * @param mainId

     */
    private void deleteCategory (String mainId) {
        soB2cRefCategoryService.deleteByMainIds(Arrays.asList(mainId));
    }

    /**
    * 分页查询、导出 数据处理
    */
    private void fillList(List<SoB2cDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
           return;
        }
        //店铺 TODO
        List<String> shopIdList = list.stream().map(SoB2cDTO.ListDTO::getShopId).collect(Collectors.toList());
        List<ShopInfoEntity> shopInfoList = shopInfoService.listByIds(shopIdList);
        if (CollectionUtils.isEmpty(shopIdList)) {
            throw new ServiceException(ApiError.ERROR_92058);
        }

        //国家
        List<String> countryIds = list.stream().map(SoB2cDTO.ListDTO::getCountryId).collect(Collectors.toList());
        List<DictCountryEntity> dictCountryList = sysDictFeign.listCountryByIds(countryIds);

        //产品信息
        List<String> skuIdList = list.stream().flatMap(obj -> Stream.of(obj.getDetailList().stream().map(SoB2cDetailDTO.ListDTO::getSkuId).toArray(String[]::new))).distinct().collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.getSkuInfoByIds(skuIdList);
        if (CollectionUtils.isEmpty(skuList)) {
            throw new ServiceException(ApiError.ERROR_95084);
        }
        //可用库存
        List<String> warehouseIdList = list.stream().flatMap(obj -> Stream.of(obj.getDetailList().stream().map(SoB2cDetailDTO.ListDTO::getWarehouseId).toArray(String[]::new))).distinct().collect(Collectors.toList());


        InventoryQtyDTO.SkuInventoryStatusParamDTO skuInventoryDTO = new InventoryQtyDTO.SkuInventoryStatusParamDTO();
        skuInventoryDTO.setInventoryStatusList(Arrays.asList(InventoryStatusEnum.USABLE.getCode(),InventoryStatusEnum.FROZEN.getCode()));
        skuInventoryDTO.setWarehouseIdList(warehouseIdList);
        skuInventoryDTO.setSkuIdList(skuIdList);
        List<InventoryQtyDTO.SkuInventoryStatusTotalDTO> inventoryList = inventoryFeign.listSkuInventoryStatusByParam(skuInventoryDTO);

        // 属性赋值
        for(SoB2cDTO.ListDTO data : list) {
            //物流方式 TODO

            //国家
            if (CollectionUtils.isNotEmpty(dictCountryList)) {
                String countryName = dictCountryList.stream().filter(obj -> obj.getId().equals(data.getCountryId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getNameCn())).orElse("");
                data.setCountryName(countryName);
            }
            //店铺
            String shopName = shopInfoList.stream().filter(obj -> obj.getId().equals(data.getShopId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            data.setShopName(shopName);

            //明细信息
            List<SoB2cDetailDTO.ListDTO> detailList = data.getDetailList();
            for (SoB2cDetailDTO.ListDTO detailDTO : detailList) {
                //SKU信息
                SkuVO skuVO = skuList.stream().filter(obj -> obj.getSkuId().equals(detailDTO.getSkuId())).findFirst().orElse(null);
                if (ObjectUtils.isEmpty(skuVO)) {
                    throw new ServiceException(ApiError.ERROR_95084);
                }
                detailDTO.setVariantProperty(skuVO.getVariantProperty());
                detailDTO.setProductName(skuVO.getSkuName());
                //订单本位币金额
                BigDecimal amount = MathUtil.multiply(detailDTO.getSourceAmount(), detailDTO.getExchangeRate());
                detailDTO.setAmount(amount);
                detailDTO.setCurrency(CurrencyEnum.CNY.getCurrencyCode());


                if (CollectionUtils.isNotEmpty(inventoryList)) {
                    //可用库存
                    Integer useableQty = inventoryList.stream().filter(obj -> obj.getSkuId().equals(detailDTO.getSkuId())
                                    && obj.getWarehouseId().equals(detailDTO.getWarehouseId())
                                    && obj.getWarehouseLocationId().equals(detailDTO.getWarehouseLocation())
                                    && InventoryStatusEnum.USABLE.getCode().equals(obj.getInventoryStatus()))
                            .findFirst().flatMap(obj -> Optional.ofNullable(obj.getInventoryTotal()))
                            .orElse(MathUtil.ZERO);
                    detailDTO.setUseableQty(useableQty);
                    //冻结库存
                    Integer freezeQty = inventoryList.stream().filter(obj -> obj.getSkuId().equals(detailDTO.getSkuId())
                                    && obj.getWarehouseId().equals(detailDTO.getWarehouseId())
                                    && obj.getWarehouseLocationId().equals(detailDTO.getWarehouseLocation())
                                    && InventoryStatusEnum.FROZEN.getCode().equals(obj.getInventoryStatus()))
                            .findFirst().flatMap(obj -> Optional.ofNullable(obj.getInventoryTotal()))
                            .orElse(MathUtil.ZERO);
                    detailDTO.setFreezeQty(freezeQty);
                }
            }
        }
    }
    /**
    * 分页查询、导出 数据处理
    */
    private void validateSubmit(SoB2cEntity entity) {
        // 待提交或审核不通过并且未作废允许提交
        if(!ApproveStatusEnum.allowUpdateStatus(entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98010);
        }
        return;
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(SoB2cEntity soB2cEntity) {
       if (ObjectUtils.isEmpty(soB2cEntity)) {
           return;
       }
       soB2cEntity.setBillDate(LocalDate.now());
       BigDecimal  exchangeRate = dmpTaskFeign.getRate(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), soB2cEntity.getCurrency());
       soB2cEntity.setExchangeRate(exchangeRate);

       //店铺
        ShopInfoEntity shopInfoEntity = shopInfoService.getById(soB2cEntity.getShopId());
        if (ObjectUtils.isEmpty(shopInfoEntity)) {
            throw new ServiceException(ApiError.ERROR_92058);
        }
        soB2cEntity.setOrgId(shopInfoEntity.getSalesOrgId());
        soB2cEntity.setOrgName(shopInfoEntity.getSalesOrgName());
    }

    /**
     * @description: 列表查询数量状态处理
     * @author Will
     * @date: 2023/8/21 12:16
     * @param params
     * @return Boolean
     */
    private Boolean handleTableParam (SoB2cDTO.PagingParamDTO params) {
        //审核状态
        List<String> approveStatusList = new ArrayList<>(1);
        //付款状态
        List<String> payStatusList = new ArrayList<>(1);
        //单据状态
        List<String> billStatusList = new ArrayList<>(1);

        // 待付款
        if (SoB2cTabEnum.ENUM_PAYMENT.getCode().equals(params.getTabFlag())) {
            payStatusList.add(SoB2cPayStatusEnum.ENUM_PAYMENT.getCode());
        }
        //待处理
        if (SoB2cTabEnum.ENUM_PENDING.getCode().equals(params.getTabFlag())) {
            approveStatusList.add(ApproveStatusEnum.WAIT_SUBMIT.getStatus());
            approveStatusList.add(ApproveStatusEnum.REJECT.getStatus());
        }
        //审核中
        if (SoB2cTabEnum.ENUM_APPROVE_ING.getCode().equals(params.getTabFlag())) {
            approveStatusList.add(ApproveStatusEnum.APPROVE_ING.getStatus());
        }
        //待配货
        if (SoB2cTabEnum.ENUM_IN_DISTRIBUTION.getCode().equals(params.getTabFlag())) {
            approveStatusList.add(ApproveStatusEnum.APPROVE.getStatus());
            billStatusList.add(SoB2cBillStatusEnum.ENUM_IN_DISTRIBUTION.getCode());
        }
        //配货中
        if (SoB2cTabEnum.ENUM_IN_DISTRIBUTION.getCode().equals(params.getTabFlag())) {
            approveStatusList.add(ApproveStatusEnum.APPROVE.getStatus());
            billStatusList.add(SoB2cBillStatusEnum.ENUM_IN_DISTRIBUTION.getCode());
        }
        //待发货
        if (SoB2cTabEnum.ENUM_WAIT_SHIPPED.getCode().equals(params.getTabFlag())) {
            approveStatusList.add(ApproveStatusEnum.APPROVE.getStatus());
            billStatusList.add(SoB2cBillStatusEnum.ENUM_WAIT_SHIPPED.getCode());
        }
        //已发货
        if (SoB2cTabEnum.ENUM_SHIPPED.getCode().equals(params.getTabFlag())) {
            approveStatusList.add(ApproveStatusEnum.APPROVE.getStatus());
            billStatusList.add(SoB2cBillStatusEnum.ENUM_SHIPPED.getCode());
        }
        //冻结中
        if (SoB2cTabEnum.ENUM_FROZEN.getCode().equals(params.getTabFlag())) {
            billStatusList.add(SoB2cBillStatusEnum.ENUM_FROZEN.getCode());
        }
        //已作废
        if (SoB2cTabEnum.ENUM_INVALID.getCode().equals(params.getTabFlag())) {
            params.setInvalidStatus(Boolean.TRUE);
        }
        if (CollectionUtils.isNotEmpty(approveStatusList)) {
            params.setApproveStatusList(approveStatusList);
        }
        if (CollectionUtils.isNotEmpty(billStatusList)) {
            params.setBillStatusList(billStatusList);
        }
        if (CollectionUtils.isNotEmpty(payStatusList)) {
            params.setPayStatusList(payStatusList);
        }
        return Boolean.TRUE;
    }

    /**
     * @description: 合并列表数据显示处理
     * @author Will
     * @date: 2023/8/23 9:32
     * @param records
     */
    private void fillMergeData (List<SoB2cDTO.MergeListDTO> records) {
        if (CollectionUtils.isEmpty(records)) {
            return;
        }
        //店铺信息
        List<String> shopIdList = records.stream().map(SoB2cDTO.MergeListDTO::getShopId).collect(Collectors.toList());
        List<ShopInfoEntity> shopList = shopInfoService.listByIds(shopIdList);
        if (CollectionUtils.isEmpty(shopList)) {
            throw new ServiceException(ApiError.ERROR_92058);
        }
        //国家信息
        List<String> countryIdList = shopList.stream().map(ShopInfoEntity::getDictCountryId).collect(Collectors.toList());
        List<DictCountryEntity> dictCountryList = sysDictFeign.listCountryByIds(countryIdList);

        //平台
        List<String> platformList = records.stream().map(SoB2cDTO.MergeListDTO::getDictPlatform)
                .distinct().collect(Collectors.toList());
        //币别
        List<String> currencyList = records.stream().map(SoB2cDTO.MergeListDTO::getCurrency)
                .distinct().collect(Collectors.toList());
        //买家名称
        List<String> buyerNameList = records.stream().map(SoB2cDTO.MergeListDTO::getBuyerName)
                .distinct().collect(Collectors.toList());
        //平台
        List<String> receiverNameList = records.stream().map(SoB2cDTO.MergeListDTO::getReceiverName)
                .distinct().collect(Collectors.toList());
        //地址1
        List<String> firstAddressList = records.stream().flatMap(obj -> Stream.of(obj.getMainList().stream().map(SoB2cDTO.MergeMainDTO::getFirstAddress).toArray(String[]::new)))
                .distinct().collect(Collectors.toList());
        //地址2
        List<String> secondAddressList = records.stream().flatMap(obj -> Stream.of(obj.getMainList().stream().map(SoB2cDTO.MergeMainDTO::getSecondAddress).toArray(String[]::new)))
                .distinct().collect(Collectors.toList());
        //详细地址
        List<String> fullAddressList = records.stream().flatMap(obj -> Stream.of(obj.getMainList().stream()
                .map(SoB2cDTO.MergeMainDTO::getFullAddress).toArray(String[]::new))).distinct().collect(Collectors.toList());
        //仓库
        List<String> warehouseIdList = records.stream().flatMap(obj -> Stream.of(obj.getMainList().stream().flatMap(e ->Stream.of(e.getDetailList().stream().map(SoB2cDTO.MergeDetailDTO::getWarehouseId).toArray(String[]::new))).toArray(String[]::new)))
                .distinct().collect(Collectors.toList());
        //物流方式
        List<String> dictLogisticsMethodList = records.stream().map(SoB2cDTO.MergeListDTO::getDictLogisticsMethod).collect(Collectors.toList());

        SoB2cDTO.MergeParamDTO mergeParamDTO = new SoB2cDTO.MergeParamDTO();
        mergeParamDTO.setPlatformList(platformList);
        mergeParamDTO.setShopIdList(shopIdList);
        mergeParamDTO.setCurrencyList(currencyList);
        mergeParamDTO.setBuyerNameList(buyerNameList);
        mergeParamDTO.setReceiverNameList(receiverNameList);
        mergeParamDTO.setFirstAddressList(firstAddressList);

        mergeParamDTO.setSecondAddressList(secondAddressList);
        mergeParamDTO.setFullAddressList(fullAddressList);
        mergeParamDTO.setWarehouseIdList(warehouseIdList);
        mergeParamDTO.setDictLogisticsMethodList(dictLogisticsMethodList);
        List<SoB2cDTO.MergeMainDTO> mergeMainList = baseMapper.listMerge(mergeParamDTO);
        if (CollectionUtils.isEmpty(mergeMainList)) {
            return;
        }
        //产品详细
        List<String> skuIdList = mergeMainList.stream().flatMap(obj -> Stream.of(obj.getDetailList().stream().map(SoB2cDTO.MergeDetailDTO::getSkuId).toArray(String[]::new)))
                .distinct().collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.getSkuInfoByIds(skuIdList);
        if (CollectionUtils.isEmpty(skuList)) {
            log.error("未发现产品详细，skuIdList = {}",skuIdList);
            throw new ServiceException(ApiError.ERROR_95084);
        }

        for (SoB2cDTO.MergeListDTO mergeListDTO: records) {

            //店铺信息
            ShopInfoEntity shopInfoEntity = shopList.stream().filter(obj -> obj.getId().equals(mergeListDTO.getShopId())).findFirst().orElse(null);
            if (CollectionUtils.isEmpty(shopList)) {
                throw new ServiceException(ApiError.ERROR_92058);
            }
            mergeListDTO.setShopName(shopInfoEntity.getName());
            //国家信息
            if (CollectionUtils.isNotEmpty(dictCountryList)) {
                String countryName = dictCountryList.stream().filter(obj -> obj.getId().equals(shopInfoEntity.getDictCountryId()))
                        .findFirst().flatMap(obj -> Optional.ofNullable(obj.getNameCn())).orElse("");
                mergeListDTO.setCountyName(countryName);
            }
            //主表数据
            if (CollectionUtils.isNotEmpty(mergeMainList)) {
                for (SoB2cDTO.MergeMainDTO mergeMainDTO : mergeMainList) {
                    List<SoB2cDTO.MergeDetailDTO> detailList = mergeMainDTO.getDetailList();
                    if (CollectionUtils.isEmpty(detailList)) {
                        continue;
                    }
                    //明细数据
                    for (SoB2cDTO.MergeDetailDTO mergeDetailDTO : detailList) {
                        //产品名称
                        String productName = skuList.stream().filter(obj -> obj.getSkuId().equals(mergeDetailDTO.getSkuId())).findFirst()
                                .flatMap(obj -> Optional.ofNullable(obj.getSkuName())).orElse("");
                        mergeDetailDTO.setProductName(productName);
                        //本位币金额
                        mergeDetailDTO.setAmount(MathUtil.multiply(mergeDetailDTO.getSourceAmount(),mergeDetailDTO.getExchangeRate()));
                        mergeDetailDTO.setCurrency(CurrencyEnum.CNY.getCurrencyCode());
                    }
                }
                mergeListDTO.setMainList(mergeMainList);
                //总原币金额
                BigDecimal totalSourceAmount = mergeMainList.stream().filter(obj -> CollectionUtils.isNotEmpty(obj.getDetailList()))
                        .flatMap(obj -> Stream.of(obj.getDetailList().stream().map(SoB2cDTO.MergeDetailDTO::getSourceAmount).reduce(BigDecimal.ZERO, BigDecimal::add)))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                mergeListDTO.setSourceAmount(totalSourceAmount);
                //总本位币金额
                BigDecimal totalAmount = mergeMainList.stream().filter(obj -> CollectionUtils.isNotEmpty(obj.getDetailList()))
                        .flatMap(obj -> Stream.of(obj.getDetailList().stream().map(SoB2cDTO.MergeDetailDTO::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add)))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                mergeListDTO.setAmount(totalAmount);
                mergeListDTO.setCurrency(CurrencyEnum.CNY.getCurrencyCode());
                //总重量
                BigDecimal totalWeight = mergeMainList.stream().filter(obj -> CollectionUtils.isNotEmpty(obj.getDetailList()))
                        .flatMap(obj -> Stream.of(obj.getDetailList().stream().map(SoB2cDTO.MergeDetailDTO::getWeight).reduce(BigDecimal.ZERO, BigDecimal::add)))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                mergeListDTO.setWeight(totalWeight);
            }
            mergeListDTO.setMainList(mergeMainList);
        }
    }

    /**
     * @description: 检验合并数据
     * @author Will
     * @date: 2023/8/23 10:23
     * @param list
     * @param soB2cLogisticsList
     * @param soB2cReceiverList
     * @param soB2cDetailList
     */
    private void checkMergeData (List<SoB2cEntity> list,List<SoB2cLogisticsEntity> soB2cLogisticsList,
                                 List<SoB2cReceiverEntity> soB2cReceiverList,List<SoB2cDetailEntity> soB2cDetailList) {
        //销售平台
        long platformCount = list.stream().map(SoB2cEntity::getDictPlatform).distinct().count();
        if (platformCount > MathUtil.ONE) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_PLATFORM_CODE_COMPLEX);
        }
        //店铺
        long shopCount = list.stream().map(SoB2cEntity::getShopId).distinct().count();
        if (shopCount > MathUtil.ONE) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_SHOP_COMPLEX);
        }
        //币别
        long currencyCount = list.stream().map(SoB2cEntity::getCurrency).distinct().count();
        if (currencyCount > MathUtil.ONE) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_CURRENCY_COMPLEX);
        }
        //物流方式
        long logisticsMethodCount = soB2cLogisticsList.stream().map(SoB2cLogisticsEntity::getDictLogisticsMethod).distinct().count();
        if (logisticsMethodCount > MathUtil.ONE) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_LOGISTICS_METHOD_COMPLEX);
        }

        //买家
        long nameCount = soB2cReceiverList.stream().map(SoB2cReceiverEntity::getName).distinct().count();
        if (nameCount > MathUtil.ONE) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_BUYER_NAME_COMPLEX);
        }
        //收货人
        long receiverNameCount = soB2cReceiverList.stream().map(SoB2cReceiverEntity::getReceiverName).distinct().count();
        if (receiverNameCount > MathUtil.ONE) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_RECEIVER_NAME_COMPLEX);
        }
        //收货地址
        long addressCount = soB2cReceiverList.stream().map(obj -> StrUtil.join(",",obj.getFirstAddress(),obj.getSecondAddress(),obj.getFullAddress())).distinct().count();
        if (addressCount > MathUtil.ONE) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_ADDRESS_COMPLEX);
        }

        //仓库
        long warehouseCount = soB2cDetailList.stream().map(SoB2cDetailEntity::getWarehouseId).distinct().count();
        if (warehouseCount > MathUtil.ONE) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_WAREHOUSE_COMPLEX);
        }
    }

    /**
     * @description: 验证拆分数据
     * @author Will
     * @date: 2023/8/23 15:12
     * @param id
     * @param entity
     */
    private void checkSplitData (String id,SoB2cEntity entity) {
        //查询订单是否是合并订单或拆分子订单
        List<SoB2cRefEntity> soB2cRefList = soB2cRefService.listByTargetId(id, null);
        if (CollectionUtils.isEmpty(soB2cRefList)) {
            return;
        }
        long mergeCount = soB2cRefList.stream().filter(obj -> SoB2cOptionTypeEnum.ENUM_MERGE.getCode().equals(obj.getType())).count();
        if (mergeCount > 0) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_MERGE_NOT_SPLIT,entity.getCode());
        }
        long splitCount = soB2cRefList.stream().filter(obj -> SoB2cOptionTypeEnum.ENUM_SPLIT.getCode().equals(obj.getType())).count();
        if (splitCount > 0) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_CHILD_SPLIT_NOT_SPLIT,entity.getCode());
        }
    }

    /**
     * @description: 匹配审核规则
     * @author Will
     * @date: 2023/8/24 15:18
     * @param id
     * @return Boolean
     */
    private Boolean approveRule (String id) {
        SoB2cEntity entity = super.getById(id);
        Optional.ofNullable(entity).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "B2C销售订单表"));
        //匹配审核规则 TODO
        Boolean approveSuccess = StringUtils.isBlank(entity.getInterceptRemark()) ? Boolean.FALSE : Boolean.TRUE;

        //匹配审核规则通过,自动提交并审核
        if (approveSuccess) {
            //自动提交
            BatchResultDTO submit = this.submit(id,Boolean.FALSE);
            if (!submit.getSuccess()) {
                throw new ServiceException(ApiError.ERROR_1042);
            }
            //自动审核通过
            BatchResultDTO approve = this.approve(new ApproveOneDTO(id, ApproveType.PASS, "自动审核通过"));
            if (!approve.getSuccess()) {
                throw new ServiceException(ApiError.ERROR_94006);
            }
            return Boolean.TRUE;
        }
        //匹配审核规则未通过
        /**
         * 存在流程，则直接提交进入流程审核
         * 不存在流程，则判断标记异常，进入审核不通过
         */
        ProcessBusinessEntity processBusinessEntity = workflowFeign.getProcessBusiness(SourceTypeEnum.SO_B2C.getCode());
        if (ObjectUtils.isEmpty(processBusinessEntity)) {
            //自动提交
            BatchResultDTO submit = submit(id,Boolean.TRUE);
            if (!submit.getSuccess()) {
                throw new ServiceException(ApiError.ERROR_1042);
            }
            return Boolean.TRUE;
        }
        //标识异常并且审核不通过
        updateAbnormalTypeApprove(id,ApproveStatusEnum.REJECT,SoB2cAbnormalTypeEnum.ENUM_APPROVE_REJECT);
        return Boolean.TRUE;
    }
    /**
     * @description: 匹配配货规则
     * @author Will
     * @date: 2023/8/24 15:19
     * @param id
     * @return Boolean
     */
    private Boolean distributionRule (String id) {
        SoB2cEntity entity = super.getById(id);
        Optional.ofNullable(entity).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "B2C销售订单表"));
        //进行配货规则匹配 TODO
        Boolean distributionSuccess = "1".equals(entity.getInterceptRemark()) ? Boolean.FALSE : Boolean.TRUE;
        if (distributionSuccess) {
            //状态更新为配货中
            updateBillStatus(id,SoB2cBillStatusEnum.ENUM_IN_DISTRIBUTION);
            //自动发货(物流规则有设置则自动发货) TODO
            submitDelivery(id);
            return Boolean.TRUE;
        }
        //配货规则不匹配,标识异常
        updateAbnormalTypeApprove(id,null,SoB2cAbnormalTypeEnum.ENUM_DISTRIBUTION_REJECT);
        return Boolean.TRUE;
    }

    /**
     * @description: 异常审核不通过
     * @author Will
     * @date: 2023/8/24 15:14
     * @param id
     * @param approveStatusEnum
     * @param soB2cAbnormalTypeEnum
     * @return Boolean
     */
    private Boolean updateAbnormalTypeApprove (String id,ApproveStatusEnum approveStatusEnum,SoB2cAbnormalTypeEnum soB2cAbnormalTypeEnum) {
        return lambdaUpdate().eq(SoB2cEntity::getId,id)
                .set(ObjectUtils.isNotEmpty(approveStatusEnum),SoB2cEntity::getApproveStatus,approveStatusEnum.getCode())
                .set(SoB2cEntity::getAbnormalType,soB2cAbnormalTypeEnum.getCode())
                .update(new SoB2cEntity());
    }

    /**
     * @description: 更新订单状态
     * @author Will
     * @date: 2023/8/24 14:55
     * @param id
     * @param soB2cBillStatusEnum
     */
    private Boolean updateBillStatus(String id,SoB2cBillStatusEnum soB2cBillStatusEnum ) {
        return lambdaUpdate().eq(SoB2cEntity::getId,id)
                .set(SoB2cEntity::getBillStatus,soB2cBillStatusEnum.getCode())
                .update(new SoB2cEntity());
    }
}
