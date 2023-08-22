package com.erp.server.oms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
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
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.oms.dto.SoB2cDetailDTO;
import com.erp.model.oms.entity.SoB2cDetailEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.entity.SoB2cLogisticsEntity;
import com.erp.model.oms.entity.SoB2cRefCategoryEntity;
import com.erp.model.oms.enums.B2cSoCategoryTypeEnum;
import com.erp.model.oms.enums.PayStatusEnum;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.model.oms.enums.SoB2cTabEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.inventory.InventoryQtyDTO;
import com.erp.model.wms.enums.inventory.InventoryStatusEnum;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.rpc.plm.feign.PlmTaskFeign;
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
    public BatchResultDTO submit(String id) {
        SoB2cEntity entity = getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException("未找到B2C销售订单表数据");
        }
        validateSubmit(entity);
        // 更新单据审核状态
        log.info("提交 开始修改B2C销售订单表状态数据，id：【{}】", id);
        this.updateApproveStatus(id, ApproveStatusEnum.APPROVE_ING.getStatus());

        log.info("提交 开始启动B2C销售订单表流程，id=：【{}】", entity.getId());
        startProcess(entity);

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
    public BatchResultDTO invalid(String id, String remark) {
        SoB2cEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到B2C销售订单表数据"));
        // 待提交或审核不通过并且未作废允许作废
        if ((!ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(entity.getApproveStatus()) && !ApproveStatusEnum.REJECT.getStatus().equals(entity.getApproveStatus())) || !InvalidStatusEnum.NOT_VOIDED.getStatus().equals(entity.getInvalidStatus())) {
           throw new ServiceException(ApiError.ERROR_98005);
        }
        log.info("作废 开始修改B2C销售订单表状态数据，id：【{}】", id);
        lambdaUpdate().eq(SoB2cEntity::getId, id)
            .set(SoB2cEntity::getInvalidStatus, InvalidStatusEnum.VOIDED.getStatus())
            .set(SoB2cEntity::getInvalidRemark, remark)
            .update();

        log.info("作废 开始记录操作日志，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据作废操作 作废原因：【{}】", commonService.getUserInfo().getUserName(), entity.getCode(), "B2C销售订单表", remark);
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SO_B2C.getCode(), entity.getId(), "作废操作");
        return BatchResultDTO.success(entity.getCode(), OperationTypeEnum.INVALID);
     }

    @Override
    public BatchResultDTO unInvalid(String id) {
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approveEnd(ApproveOneDTO dto, SoB2cEntity entity) {
        if (ObjectUtil.isEmpty(entity)) {
            return Boolean.TRUE;
        }
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(dto.getType());
        updateForApprove(entity.getId(), approveStatus.getStatus());
        // todo 明细数据处理 上下游数据处理

        return Boolean.TRUE;
    }

    @Override
    public BatchResultDTO updateRemark(String id, String remark) {
        //B2C销售订单主表信息
        SoB2cEntity entity = this.getById(id);
        if (ObjectUtils.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_EXIST);
        }
        entity.setRemark(remark);
        //更新
        this.updateById(entity);
        return BatchResultDTO.success(entity.getCode(),"更新订单备注");
    }

    @Override
    public BatchResultDTO updateCategory(String id, B2cSoCategoryTypeEnum typeEnum, List<String> categoryIdList) {
        //B2C销售订单主表信息
        SoB2cEntity entity = this.getById(id);
        if (ObjectUtils.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_EXIST);
        }
        if (B2cSoCategoryTypeEnum.ENUM_ADD.equals(typeEnum)) {
            addCategory(categoryIdList,id);
        }
        if (B2cSoCategoryTypeEnum.ENUM_UPDATE.equals(typeEnum)) {
            updateCategory(categoryIdList,id);
        }
        if (B2cSoCategoryTypeEnum.ENUM_DELETE.equals(typeEnum)) {
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

        //货物物流单号，TODO

        if (Boolean.TRUE.equals(isDelivery)) {
            //提交发货
            submitDelivery(id);
        }

        return BatchResultDTO.success(entity.getCode(),"获取物流单号");
    }

    @Override
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
            throw new ServiceException(ApiError.ERROR_SO_B2C_WAREHOUSE_COMPLEX);
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

    private void fillMergeData (List<SoB2cDTO.MergeListDTO> records) {
        if (CollectionUtils.isEmpty(records)) {
            return;
        }
        //店铺名称


        for (SoB2cDTO.MergeListDTO mergeListDTO: records) {



        }

    }

    @Override
    public Boolean mergeSave(List<String> ids) {
        return null;
    }

    @Override
    public BatchResultDTO cancelMerge(String id) {
        return null;
    }

    @Override
    public List<SoB2cDTO.ViewSplitDTO> viewSplit(String id) {
        return null;
    }

    @Override
    public Boolean splitSave(SoB2cDTO.SplitSaveDTO dto) {
        return null;
    }

    @Override
    public BatchResultDTO cancelSplit(String id) {
        return null;
    }

    @Override
    public SoB2cDTO.ViewDTO view(String id) {
        SoB2cEntity soB2cEntity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到B2C销售订单表数据"));
        SoB2cDTO.ViewDTO data = BeanMapperUtils.map(SoB2cDTO.ViewDTO.class, soB2cEntity);
        // 数据填充处理
        fillOne(data);
        // TODO 查询明细数据（如果有的话）
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
    }

    /**
    * 审核更新审核信息
    * @param id
    * @param approveStatus
    */
    public void updateForApprove(String id, String approveStatus) {
        this.lambdaUpdate().eq(SoB2cEntity::getId, id)
            .set(SoB2cEntity::getApproveStatus, approveStatus)
            .update(new SoB2cEntity());
     }


    /**
    * 更新审核状态
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateApproveStatus(String id, String approveStatus) {
        lambdaUpdate().eq(SoB2cEntity::getId, id)
        .set(SoB2cEntity::getApproveStatus, approveStatus)
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
        soB2cRefCategoryService.deleteByMainId(mainId);
    }

    /**
    * 分页查询、导出 数据处理
    */
    private void fillList(List<SoB2cDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
           return;
        }
        //店铺 TODO

        //国家
        List<String> countryIds = list.stream().map(SoB2cDTO.ListDTO::getCountryId).collect(Collectors.toList());

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
            payStatusList.add(PayStatusEnum.ENUM_PAYMENT.getCode());
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


}
