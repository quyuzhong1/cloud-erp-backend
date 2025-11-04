package com.erp.server.oms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.ApproveDTO;
import com.common.business.constant.ThirdConstants;
import com.common.business.dto.DmpPushTaskFeignDTO;
import com.common.business.dto.base.*;
import com.common.business.enums.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.utils.RedisUtil;
import com.common.business.validator.ValidList;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.enums.CurrencyEnum;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.common.message.constant.RedisKeyConstant;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.bi.entity.BiSettlementExchangeRateEntity;
import com.erp.model.dmp.entity.BiReturnOrderInfoEntity;
import com.erp.model.dmp.entity.BiReturnOrderItemEntity;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.oms.dto.*;
import com.erp.model.oms.entity.*;
import com.erp.model.oms.enums.BillTypeEnum;
import com.erp.model.oms.enums.SoReturnChangeListTypeEnum;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.model.sys.dto.SysDepartmentUserNumberDTO;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.model.sys.entity.SysAccountingCompanyEntity;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.ReturnReasonEnum;
import com.erp.model.wms.enums.ReturnTypeEnum;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.AuthDataFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.*;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.oms.convert.SoReturnConverter;
import com.erp.server.oms.mapper.SoReturnMapper;
import com.erp.server.oms.query.SoReturnQueryHandler;
import com.erp.server.oms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_OMS_SO_RETURN;

/**
 * <p>
 * 退货单服务实现类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-05-10
 */
@Slf4j
@Service
public class SoReturnServiceImpl extends SuperServiceImpl<SoReturnMapper, SoReturnEntity> implements SoReturnService {

    @Resource
    private SoReturnDetailService soReturnDetailService;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private SoInfoService soInfoService;

    @Resource
    private SoDetailService soDetailService;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private OperateLogService operateLogService;

    @Resource
    private SoReturnNoticeFeign soReturnNoticeFeign;

    @Resource
    private SoReturnReceiveFeign soReturnReceiveFeign;

    @Resource
    private WorkflowFeign workflowFeign;

    @Resource
    private SoOutstockFeign soOutstockFeign;

    @Resource
    private CustomerInfoService customerInfoService;

    @Resource
    private WmsTaskFeign wmsTaskFeign;

    @Resource
    private MQProducerService mQProducerService;

    @Resource
    private DmpTaskFeign dmpTaskFeign;
    @Resource
    private CustomerAddressService customerAddressService;

    @Resource
    private SoReturnInstockFeign soReturnInstockFeign;

    @Resource
    private SoReturnQueryHandler soReturnQueryHandler;

    @Resource
    private DocNoGenHelper docNoGenHelper;

    @Resource
    private DmpMqFeign dmpMqFeign;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Resource
    private SkuMappingService skuMappingService;

    @Resource
    private RedisUtil redisUtil;
    @Resource
    private AuthDataFeign authDataFeign;


    @Override
    public PagingVO<SoReturnDTO.PagingView> paging(PagingDTO<SoReturnDTO.PagingParam> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<SoReturnDTO.PagingView> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if (CollectionUtils.isEmpty(pageData.getRecords())) {
            return new PagingVO(new Page());
        }
        //明细数据
        List<SoReturnDTO.PagingView> records = pageData.getRecords();
        //获取sku的id集合
        List<String> skuIdList = records.stream().map(SoReturnDTO.PagingView::getSkuId).collect(Collectors.toList());
        //根据ids查询sku信息
        List<ProductDetailEntity> detailEntityList = plmTaskFeign.getByIdList(skuIdList);
        List<String> soIds = records.stream().map(SoReturnDTO.PagingView::getSourceId).distinct().collect(Collectors.toList());
        List<CustomerInfoEntity> customerInfoEntities = customerInfoService.list();
        List<SoOutstockDetailEntity> soOutstockDetailEntities = soOutstockFeign.listDetailBySoIds(soIds);
        List<String> soDetailIds = records.stream().map(SoReturnDTO.PagingView::getSourceDetailId).collect(Collectors.toList());
        List<SoDetailEntity> soDetailEntities = soDetailService.listSoDetailByIds(soDetailIds);
        List<String> detailIds = records.stream().map(SoReturnDTO.PagingView::getDetailId).collect(Collectors.toList());
        List<SoReturnInstockDetailEntity> soReturnInstockDetailEntityList = soReturnInstockFeign.listDetailBySoReturnDetailIds(detailIds);


        //查询审核流程
        List<String> ids = records.stream().map(SoReturnDTO.PagingView::getId).distinct().collect(Collectors.toList());
        ValidList<ProcessManagementDTO.HistoryActivityDTO> dtoList = ids.stream().map(obj -> new ProcessManagementDTO.HistoryActivityDTO(SourceTypeEnum.SO_RETURN.getCode(), obj)).collect(Collectors.toCollection(ValidList::new));
        ApiResult<List<ProcessManagementDTO.CurApproveInfoDTO>> listApiResult = workflowFeign.curApprover(dtoList);
        if (200 != listApiResult.getCode()) {
            throw new ServiceException(new ApiResult(ApiError.DEFAULT.code,listApiResult.getMsg()));
        }

        if (CollectionUtils.isNotEmpty(records)) {
            records.forEach(obj -> {
                obj.setApproveStatusName(ApproveStatusEnum.getName(obj.getApproveStatus()));
                obj.setInvalidStatusName(InvalidStatusEnum.getName(obj.getInvalidStatus()));
                obj.setTypeName(BillTypeEnum.getName(obj.getType()));
                ProductDetailEntity productDetailEntity = detailEntityList.stream().filter(entityClass -> entityClass.getId().equals(obj.getSkuId())).findFirst().orElse(new ProductDetailEntity());
                obj.setProductName(productDetailEntity.getName());
                SoDetailEntity soDetailEntity = soDetailEntities.stream().filter(detail -> detail.getId().equals(obj.getSourceDetailId())).findFirst().orElse(null);
                if(null != soDetailEntity){
                    obj.setSalesQty(soDetailEntity.getQty());
                    Integer actualQty = soOutstockDetailEntities.stream().filter(detail -> soDetailEntity.getMainId().equals(detail.getSoId()) && detail.getSkuId().equals(obj.getSkuId()) && detail.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus())).map(SoOutstockDetailEntity::getActualQty).reduce(MathUtil.ZERO, Integer::sum);
                    obj.setDeliveryQty(actualQty);
                    obj.setUnDeliveryQty(soDetailEntity.getQty() - actualQty);
                    obj.setSalesAmount(soDetailEntity.getAmount());
                    if(StringUtils.isBlank(obj.getCurrency())){
                        obj.setCurrency(soDetailEntity.getCurrency());
                    }
                    if(StringUtils.isBlank(obj.getCurrencySymbol())){
                        obj.setCurrencySymbol(soDetailEntity.getCurrencySymbol());
                    }
                }
                obj.setUnit(productDetailEntity.getUnitName());
                CustomerInfoEntity customerInfoEntity = customerInfoEntities.stream().filter(req -> req.getId().equals(obj.getCustomerId())).findFirst().orElse(new CustomerInfoEntity());
                obj.setCustomerName(customerInfoEntity.getName());
                Integer returnInStockQty = soReturnInstockDetailEntityList.stream().filter(detail -> obj.getDetailId().equals(detail.getSoReturnDetailId())  ).map(SoReturnInstockDetailEntity::getRealQty).reduce(MathUtil.ZERO, Integer::sum);
                obj.setReturnInStockQty(returnInStockQty);

                //最新审核人
                if (CollectionUtils.isNotEmpty(listApiResult.getData())) {
                    String curApprove = listApiResult.getData().stream().filter(e -> e.getBusinessId().equals(obj.getId()) && CharSequenceUtil.isNotBlank(e.getCurApproveName())).map(ProcessManagementDTO.CurApproveInfoDTO::getCurApproveName).collect(Collectors.joining(","));
                    obj.setApproveUserName(CharSequenceUtil.blankToDefault(curApprove,obj.getApproveUserName()));
                }
            });
        }
        return new PagingVO(pageData);
    }

    @Override
    public List<SoReturnDTO.StatusCountDTO> listCount(PermissionsDTO dto) {
        SoReturnChangeListTypeEnum[] values = SoReturnChangeListTypeEnum.values();
        List<SoReturnDTO.StatusCountDTO> list = new ArrayList<>();
        for (SoReturnChangeListTypeEnum item : values) {
            SoReturnDTO.PagingParam pagingParam = new SoReturnDTO.PagingParam();
            pagingParam.setPermissionSql(dto.getPermissionSql());
            SoReturnDTO.StatusCountDTO resultDTO = new SoReturnDTO.StatusCountDTO();
            String tabSql = soReturnQueryHandler.getTabSql(item.getCode());
            HashMap<String,String> map = new HashMap<>();
            map.put("default",tabSql);
            pagingParam.setSqlMap(map);
            Integer count = this.baseMapper.listCount(pagingParam);
            resultDTO.setCount(ObjectUtils.isEmpty(count) ? MathUtil.ZERO : count);
            resultDTO.setTabFlag(item.getCode());
            resultDTO.setTabFlagName(item.getName());
            list.add(resultDTO);
        }
        return list;
    }

    //新增编辑销售退货单时校验sku明细
    private void checkAddSoDetail(SoReturnDTO.Add dto){
        String sourceId = dto.getSourceId();
        String customerId = dto.getCustomerId();
        List<SoReturnDetailDTO.Add> detailList = dto.getDetailList();
        if(CollUtil.isEmpty(detailList)){
            throw new ServiceException("明细不能为空");
        }else{
            boolean allMatch = detailList.stream().allMatch(v -> v.getReturnQty() !=null && v.getReturnQty() > 0);
            if(Boolean.FALSE.equals(allMatch)){
                throw new ServiceException("退货数量不能小于1");
            }
        }

        //客户跟销售单号不为空时，校验是否为销售单号下的客户
        if(StringUtils.isNotBlank(sourceId) && StringUtils.isNotBlank(customerId)){
            Optional<SoInfoEntity> soInfoEntity = soInfoService.lambdaQuery()
                    .eq(SoInfoEntity::getId, sourceId)
                    .eq(SoInfoEntity::getCustomerId, customerId).oneOpt();
            if(!soInfoEntity.isPresent()){
                throw new ServiceException("销售单号下不存在该客户信息");
            }
        }
        if(StringUtils.isNotBlank(sourceId)){
            //判断为空元素是否存在
            boolean nullExist = detailList.stream()
                    .anyMatch(v -> StringUtils.isBlank(v.getSourceDetailId()));
            if (nullExist) {
                throw new ServiceException("产品明细必须是销售详情的明细数据");
            }
            List<String> sourceDetailIds = detailList.stream()
                    .map(SoReturnDetailDTO.Add::getSourceDetailId)
                    .collect(Collectors.toList());
            List<SoDetailEntity> soDetailEntities = soDetailService.listSoDetailByIds(sourceDetailIds);
            if (CollectionUtils.isEmpty(soDetailEntities)) {
                throw new ServiceException("产品明细不存在");
            }
            //判断sku明细是否属于该销售订单
            boolean anyMatch = soDetailEntities.stream()
                    .anyMatch(v -> StringUtils.isBlank(v.getMainId()) || (!v.getMainId().equals(sourceId)));
            if(anyMatch){
                throw new ServiceException("产品明细必须是销售详情明细数据");
            }
        }
    }

    //新增编辑销售退货单时校验sku明细
    private void checkUpdateSoDetail(SoReturnDTO.Update dto){
        String sourceId = dto.getSourceId();
        String customerId = dto.getCustomerId();
        List<SoReturnDetailDTO.Update> detailList = dto.getDetailList();
        if(CollUtil.isEmpty(detailList)){
            throw new ServiceException("明细不能为空");
        }else{
            boolean allMatch = detailList.stream().allMatch(v -> v.getReturnQty() !=null && v.getReturnQty() > 0);
            if(Boolean.FALSE.equals(allMatch)){
                throw new ServiceException("退货数量不能小于1");
            }
        }
        //客户跟销售单号不为空时，校验是否为销售单号下的客户
        if(StringUtils.isNotBlank(sourceId) && StringUtils.isNotBlank(customerId)){
            Optional<SoInfoEntity> soInfoEntity = soInfoService.lambdaQuery()
                    .eq(SoInfoEntity::getId, sourceId)
                    .eq(SoInfoEntity::getCustomerId, customerId).oneOpt();
            if(!soInfoEntity.isPresent()){
                throw new ServiceException("销售单号下不存在该客户信息");
            }
        }
        if(StringUtils.isNotBlank(sourceId)){
            //判断为空元素是否存在
            boolean nullExist = detailList.stream()
                    .anyMatch(v -> StringUtils.isBlank(v.getSourceDetailId()));
            if (nullExist) {
                throw new ServiceException("产品明细必须是销售详情的明细数据");
            }
            List<String> sourceDetailIds = detailList.stream()
                    .map(SoReturnDetailDTO.Update::getSourceDetailId)
                    .collect(Collectors.toList());
            List<SoDetailEntity> soDetailEntities = soDetailService.listSoDetailByIds(sourceDetailIds);
            if (CollectionUtils.isEmpty(soDetailEntities)) {
                throw new ServiceException("产品明细不存在");
            }
            //判断sku明细是否属于该销售订单
            boolean anyMatch = soDetailEntities.stream()
                    .anyMatch(v -> StringUtils.isBlank(v.getMainId()) || (!v.getMainId().equals(sourceId)));
            if(anyMatch){
                throw new ServiceException("产品明细必须是销售详情明细数据");
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public String add(SoReturnDTO.Add dto) {
        //校验数据
        checkAddSoDetail(dto);

        //获取销售单信息
        SoReturnEntity soReturnEntity = new SoReturnEntity();
        //销售单号不为空按原业务逻辑创建
        if(StringUtils.isNotBlank(dto.getSourceId())){
            //生成销售退货单
            generateSoReturn(dto,soReturnEntity);
        }else {
            //根据客户id生成销售退货单
            generateSoReturnByCutomer(dto,soReturnEntity);
        }
        //生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_THDD);
        soReturnEntity.setCode(code);
        //仓库
        if(StringUtils.isNotBlank(dto.getWarehouseId())){
            List<WarehouseDTO.UpdateDTO> warehouseList = wmsTaskFeign.listWarehouseByIds(Arrays.asList(dto.getWarehouseId()));
            WarehouseDTO.UpdateDTO updateDTO = warehouseList.stream().filter(w -> w.getId().equals(dto.getWarehouseId())).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(updateDTO)) {
                soReturnEntity.setWarehouseName(updateDTO.getName());
                soReturnEntity.setInventoryOrgId(updateDTO.getOrgId());
                //获取核算公司
                SysAccountingCompanyEntity companyEntity = sysUserFeign.getCompanyById(updateDTO.getOrgId());
                if (ObjectUtil.isNotEmpty(companyEntity)) {
                    soReturnEntity.setInventoryOrgName(companyEntity.getCompanyName());
                }
            }
        }
        this.save(soReturnEntity);
        //操作日志
        operateLogService.addModuleOperateLog(String.format("新增了一个销售退货入库单【%s】", code), ModuleTypeEnum.SO_RETURN.getCode(), soReturnEntity.getId(), "新增操作");
        //查询skuNo
        String customerId = dto.getCustomerId();
        List<SoReturnDetailDTO.Add> detailList = dto.getDetailList();
        List<String> skuIds = detailList.stream().map(SoReturnDetailDTO.Add::getSkuId).collect(Collectors.toList());
        List<ProductDetailEntity> productDetailEntities = plmTaskFeign.getByIdList(skuIds);
        if(CollectionUtils.isNotEmpty(productDetailEntities)){
            detailList.stream().forEach(r -> {
                ProductDetailEntity entity = productDetailEntities.stream()
                        .filter(v -> v.getId().equals(r.getSkuId()))
                        .findFirst()
                        .orElse(new ProductDetailEntity());
                r.setSkuNo(entity.getSkuNo());
            });
            dto.setDetailList(detailList);
        }
        //根据customerId和sku 获取对应的平台sku
        SkuMappingDTO.SkuParamDTO skuParamDTO = new SkuMappingDTO.SkuParamDTO();
        skuParamDTO.setSkuIdList(skuIds);
        skuParamDTO.setCutomerId(customerId);
        List<SkuMappingDTO.ProductSkuInfoDTO> productSkuInfoList = skuMappingService.listSkuBySkuNos(skuParamDTO);
        if(CollectionUtils.isNotEmpty(productSkuInfoList)){
            detailList.stream().forEach(r -> {
                SkuMappingDTO.ProductSkuInfoDTO productSkuInfo = productSkuInfoList.stream()
                        .filter(v -> v.getSkuNo().equals(r.getSkuNo()))
                        .findFirst()
                        .orElse(new SkuMappingDTO.ProductSkuInfoDTO());
                r.setListingId(productSkuInfo.getListingId());
                r.setPlatformSkuName(productSkuInfo.getPlatformSkuName());
                r.setPlatformSkuNo(productSkuInfo.getPlatformSkuNo());
            });
            dto.setDetailList(detailList);
        }
        if(StringUtils.isNotBlank(dto.getSourceId())){
            //原业务逻辑
            soReturnDetailService.add(dto, soReturnEntity.getId());
        }else {
            //无销售单号，以客户为维度新增
            soReturnDetailService.addByCutomer(dto, soReturnEntity.getId());
        }
        return soReturnEntity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(SoReturnDTO.Update dto) {
        //校验数据
        checkUpdateSoDetail(dto);
        //获取销售单信息
        SoReturnEntity soReturnEntity = new SoReturnEntity();
        SoReturnDTO.Add addDto = new SoReturnDTO.Add();
        BeanMapper.copy(dto, addDto);

        //销售单号不为空按原业务逻辑创建
        if(StringUtils.isNotBlank(dto.getSourceId())){
            //生成销售退货单
            generateSoReturn(addDto,soReturnEntity);
        }else {
            //根据客户id生成销售退货单
            generateSoReturnByCutomer(addDto,soReturnEntity);
        }
        soReturnEntity.setId(dto.getId());
        SoReturnEntity entity = this.getById(dto.getId());
        soReturnEntity.setApproveStatus(entity.getApproveStatus());
        soReturnEntity.setWarehouseId(dto.getWarehouseId());
        //仓库
        if(StringUtils.isNotBlank(dto.getWarehouseId())){
            List<WarehouseDTO.UpdateDTO> warehouseList = wmsTaskFeign.listWarehouseByIds(Arrays.asList(dto.getWarehouseId()));
            WarehouseDTO.UpdateDTO updateDTO = warehouseList.stream().filter(w -> w.getId().equals(dto.getWarehouseId())).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(updateDTO)) {
                soReturnEntity.setWarehouseName(updateDTO.getName());
                soReturnEntity.setInventoryOrgId(updateDTO.getOrgId());
                //获取核算公司
                SysAccountingCompanyEntity companyEntity = sysUserFeign.getCompanyById(updateDTO.getOrgId());
                if (ObjectUtil.isNotEmpty(companyEntity)) {
                    soReturnEntity.setInventoryOrgName(companyEntity.getCompanyName());
                }
            }
        }
        //操作日志
        operateLogService.addModuleOperateLogByObj(entity, soReturnEntity, ModuleTypeEnum.SO_RETURN.getCode(), entity.getId(), "", "");

        boolean flag = this.updateById(soReturnEntity);

        //根据customerId和skunos 获取对应的平台sku
        String customerId = dto.getCustomerId();
        List<SoReturnDetailDTO.Update> detailList = dto.getDetailList();
        List<String> skuIds = detailList.stream().map(SoReturnDetailDTO.Update::getSkuId).collect(Collectors.toList());
        List<ProductDetailEntity> productDetailEntities = plmTaskFeign.getByIdList(skuIds);
        if(CollectionUtils.isNotEmpty(productDetailEntities)){
            detailList.stream().forEach(r -> {
                ProductDetailEntity e = productDetailEntities.stream()
                        .filter(v -> v.getId().equals(r.getSkuId()))
                        .findFirst()
                        .orElse(new ProductDetailEntity());
                r.setSkuNo(e.getSkuNo());
            });
            dto.setDetailList(detailList);
        }
        SkuMappingDTO.SkuParamDTO skuParamDTO = new SkuMappingDTO.SkuParamDTO();
        skuParamDTO.setSkuIdList(skuIds);
        skuParamDTO.setCutomerId(customerId);
        List<SkuMappingDTO.ProductSkuInfoDTO> productSkuInfoList = skuMappingService.listSkuBySkuNos(skuParamDTO);
        if(CollectionUtils.isNotEmpty(productSkuInfoList)){
            detailList.stream().forEach(r -> {
                SkuMappingDTO.ProductSkuInfoDTO productSkuInfo = productSkuInfoList.stream()
                        .filter(v -> v.getSkuNo().equals(r.getSkuNo()))
                        .findFirst()
                        .orElse(new SkuMappingDTO.ProductSkuInfoDTO());
                r.setListingId(productSkuInfo.getListingId());
                r.setPlatformSkuName(productSkuInfo.getPlatformSkuName());
                r.setPlatformSkuNo(productSkuInfo.getPlatformSkuNo());
                r.setSkuNo(productSkuInfo.getSkuNo());
            });
            dto.setDetailList(detailList);
        }
        if(StringUtils.isNotBlank(dto.getSourceId())){
            //原业务逻辑
            soReturnDetailService.update(dto);
        }else {
            //无销售单号，以客户为维度新增
            soReturnDetailService.updateByCutomer(dto);
        }
        return flag;
    }

    //根据客户id生成销售退货单
    private void generateSoReturnByCutomer(SoReturnDTO.Add dto,SoReturnEntity soReturnEntity){
        //币种符号集合
        Map<String, String> currencySymbol = getCurrencySymbol(Collections.singletonList(dto.getCurrency()));

        soReturnEntity.setCustomerId(dto.getCustomerId());
        CustomerInfoEntity customerInfoEntity = customerInfoService.getById(dto.getCustomerId());
        if(null != customerInfoEntity){
            soReturnEntity.setCustomerId(customerInfoEntity.getId());
            soReturnEntity.setCustomerName(customerInfoEntity.getName());
        }
        List<CustomerAddressEntity> customerAddressList = customerAddressService.lambdaQuery()
                .eq(CustomerAddressEntity::getMainId, dto.getCustomerId())
                .eq(CustomerAddressEntity::getDisabled, Boolean.FALSE)
                .last(" order by create_time desc")
                .list();
        //客户信息
        if(CollectionUtils.isNotEmpty(customerAddressList)){
            CustomerAddressEntity customerAddressEntity = customerAddressList.stream().filter(v -> v.getIsDefault().equals(Boolean.TRUE)).findFirst().orElse(new CustomerAddressEntity());
            soReturnEntity.setReceiverName(customerAddressEntity.getPerson());
            soReturnEntity.setTelNumber(customerAddressEntity.getTelNumber());
            soReturnEntity.setReceiveAddress(customerAddressEntity.getAddress());
        }
        //默认B2B单据类型
        soReturnEntity.setType(OrderTypeEnum.B2B.getCode());
        //销售组织匹配客户的使用组织 ，销售员匹配客户的销售员，销售部门通过销售员查找所属部门
        soReturnEntity.setSalesOrgId(customerInfoEntity.getUseOrgId());
        soReturnEntity.setSalesOrgName(customerInfoEntity.getUseOrgName());
        soReturnEntity.setSellerId(customerInfoEntity.getSellerId());
        soReturnEntity.setSellerName(customerInfoEntity.getSellerName());
        if(StringUtils.isNotBlank(customerInfoEntity.getSellerId())){
            SysDepartmentUserNumberDTO deptByUserId = sysUserFeign.getDeptByUserId(customerInfoEntity.getSellerId());
            soReturnEntity.setSalesDeptId(deptByUserId.getDepartmentId());
            soReturnEntity.setSalesDeptName(deptByUserId.getDepartmentName());
        }
        soReturnEntity.setSourceType(dto.getSourceType());
        soReturnEntity.setWarehouseId(dto.getWarehouseId());
        soReturnEntity.setBillDate(dto.getBillDate());
        soReturnEntity.setCurrency(dto.getCurrency());
        soReturnEntity.setCurrencySymbol(currencySymbol.get(dto.getCurrency()));
        soReturnEntity.setReturnLogisticCode(dto.getReturnLogisticCode());
    }

    //生成销售退货单
    private void generateSoReturn(SoReturnDTO.Add dto,SoReturnEntity soReturnEntity){
        SoInfoEntity soInfoEntity = soInfoService.getById(dto.getSourceId());
        //币种符号集合
        Map<String, String> currencySymbol = getCurrencySymbol(Collections.singletonList(dto.getCurrency()));
        soReturnEntity.setType(soInfoEntity.getOrderType());
        soReturnEntity.setSalesOrgId(soInfoEntity.getSalesOrgId());
        soReturnEntity.setSalesOrgName(soInfoEntity.getSalesOrgName());
        soReturnEntity.setSalesDeptId(soInfoEntity.getSalesDeptId());
        if (StringUtils.isNotBlank(soInfoEntity.getSalesDeptId())) {
            SysDepartmentDTO dept = sysUserFeign.getUserDeptById(soInfoEntity.getSalesDeptId());
            if (dept != null) {
                soReturnEntity.setSalesDeptName(dept.getName());
            }
        }
        soReturnEntity.setSellerId(soInfoEntity.getSellerId());
        soReturnEntity.setSellerName(soInfoEntity.getSellerName());
        soReturnEntity.setCustomerId(soInfoEntity.getCustomerId());
        Optional<CustomerInfoEntity> byIdOpt = customerInfoService.getByIdOpt(soInfoEntity.getCustomerId());
        if(byIdOpt.isPresent()){
            soReturnEntity.setCustomerName(byIdOpt.get().getName());
        }
        //收货地址
        if(StringUtils.isNotBlank(soInfoEntity.getReceiveAddressId())){
            CustomerAddressDTO.ViewDTO customerAddress = customerAddressService.getCustomerAddressById(soInfoEntity.getReceiveAddressId());
            if(null != customerAddress){
                soReturnEntity.setReceiveAddress(customerAddress.getAddress());
            }
        }
        soReturnEntity.setReceiverName(soInfoEntity.getReceiverName());
        soReturnEntity.setTelNumber(soInfoEntity.getTelNumber());
        soReturnEntity.setDeliveryModeDict(soInfoEntity.getDeliveryMode());
        soReturnEntity.setIsTax(soInfoEntity.getIsTax());
        soReturnEntity.setAddressTypeDict(soInfoEntity.getAddressType());
        soReturnEntity.setSourceId(dto.getSourceId());
        soReturnEntity.setSourceCode(soInfoEntity.getCode());
        soReturnEntity.setSourceType(dto.getSourceType());
        soReturnEntity.setWarehouseId(dto.getWarehouseId());
        soReturnEntity.setBillDate(dto.getBillDate());
        soReturnEntity.setCurrency(dto.getCurrency());
        soReturnEntity.setCurrencySymbol(currencySymbol.get(dto.getCurrency()));
        soReturnEntity.setReturnLogisticCode(dto.getReturnLogisticCode());
    }

    @Override
    public BigDecimal calLocalCurrency(BigDecimal exchangeRate, BigDecimal returnAmount) {
        return returnAmount
                .multiply(exchangeRate)
                .setScale(4, RoundingMode.DOWN)
                .stripTrailingZeros();
    }

    @Override
    public BigDecimal calReturnAmount(BigDecimal amount, Integer qty, Integer returnQty) {
        return amount
                .divide(BigDecimal.valueOf(qty), 4, RoundingMode.DOWN)
                .multiply(BigDecimal.valueOf(returnQty))
                .stripTrailingZeros();
    }

    @Override
    public SoReturnDTO.View view(String id) {
        SoReturnDTO.View viewDTO = new SoReturnDTO.View();
        SoReturnEntity soReturnEntity = this.getById(id);
        //创库保存详情表的集合
        List<SoReturnDetailDTO.View> detailViewDTOS = new ArrayList<>();
        List<SoReturnDetailEntity> detailEntityList = soReturnDetailService.listDetailByMainId(id);
        if (CollectionUtils.isEmpty(detailEntityList)) {
            throw new ServiceException(ApiError.ERROR_92023);
        }
        if(StringUtils.isNotBlank(soReturnEntity.getSourceId())){
            SoInfoEntity soInfoEntity = soInfoService.getById(soReturnEntity.getSourceId());
            BeanMapperUtils.copy(soInfoEntity, viewDTO);
            viewDTO.setSoInfoCreateTime(soInfoEntity.getCreateTime());
        }
        BeanMapperUtils.copy(soReturnEntity, viewDTO);
        //汇率
        viewDTO.setExchangeRate(detailEntityList.get(0).getExchangeRate());
        //能否编辑销售单号
        viewDTO.setCanChangeSoInfo(soReturnEntity.getSourceType().equals(SourceTypeEnum.SO_INFO.getCode()) ? Boolean.FALSE : Boolean.TRUE);
        //获取sku的id集合
        List<String> skuIdList = detailEntityList.stream().map(SoReturnDetailEntity::getSkuId).collect(Collectors.toList());
        //根据ids查询sku信息
        List<ProductDetailEntity> productDetailEntitys = plmTaskFeign.getByIdList(skuIdList);
        //获取销售单详情表id集合
        List<String> orderDetailIds = detailEntityList.stream().map(SoReturnDetailEntity::getSourceDetailId).collect(Collectors.toList());
        //获取销售单详情信息
        List<SoDetailEntity> soDetailEntities = soDetailService.listSoDetailByIds(orderDetailIds);
        viewDTO.setApproveStatusName(ApproveStatusEnum.getName(viewDTO.getApproveStatus()));
        viewDTO.setInvalidStatusName(InvalidStatusEnum.getName(viewDTO.getInvalidStatus()));
        List<CustomerInfoEntity> customerInfoEntities = customerInfoService.list();
        CustomerInfoEntity customerInfoEntity = customerInfoEntities.stream().filter(req -> req.getId().equals(soReturnEntity.getCustomerId())).findFirst().orElse(new CustomerInfoEntity());
        soReturnEntity.setCustomerName(customerInfoEntity.getName());
        List<String> soIds = soDetailEntities.stream().map(SoDetailEntity::getMainId).distinct().collect(Collectors.toList());
        List<SoOutstockDetailEntity> soOutstockDetailEntities = soOutstockFeign.listDetailBySoIds(soIds);
        CustomerAddressEntity customerAddressEntity = customerAddressService.getById(soReturnEntity.getCustomerId());
        if (ObjectUtil.isNotEmpty(customerAddressEntity)) {
            viewDTO.setReceiveAddress(customerAddressEntity.getAddress());
        }

        for (SoReturnDetailEntity detailEntity : detailEntityList) {
            SoReturnDetailDTO.View detailView = new SoReturnDetailDTO.View();
            BeanMapperUtils.copy(detailEntity, detailView);
            ProductDetailEntity productDetailEntity = productDetailEntitys.stream().filter(entityClass -> entityClass.getId().equals(detailEntity.getSkuId())).findFirst().orElse(new ProductDetailEntity());
            SoDetailEntity soDetailEntity = soDetailEntities.stream().filter(detail -> detail.getId().equals(detailEntity.getSourceDetailId())).findFirst().orElse(null);
            detailView.setProductName(productDetailEntity.getName());
            detailView.setUnitName(productDetailEntity.getUnitName());
            detailView.setReturnReasonDictName(ReturnReasonEnum.getName(detailEntity.getReturnReasonDict()));
            if(null != soDetailEntity){
                detailView.setSalesQty(soDetailEntity.getQty());
                Integer actualQty = soOutstockDetailEntities.stream().filter(detail -> soDetailEntity.getMainId().equals(detail.getSoId()) && detail.getSkuId().equals(soDetailEntity.getSkuId()) && detail.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus())).map(SoOutstockDetailEntity::getActualQty).reduce(MathUtil.ZERO, Integer::sum);
                detailView.setDeliveryQty(actualQty);
                detailView.setUnDeliveryQty(soDetailEntity.getQty() - actualQty);
                detailView.setSalesAmount(soDetailEntity.getAmount());
                detailView.setCurrency(soDetailEntity.getCurrency());
                detailView.setCurrencySymbol(soDetailEntity.getCurrencySymbol());
            }
            detailViewDTOS.add(detailView);
        }
        viewDTO.setDetailList(detailViewDTOS);
        return viewDTO;
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO submit(SoReturnEntity entity,Boolean isNeedProcess) {
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_98004);
        }

        //未作废、待提交、审核不通过才可以提交
        if (Boolean.TRUE.equals(entity.getInvalidStatus()) || (!entity.getApproveStatus().equals(ApproveStatusEnum.WAIT_SUBMIT.getStatus())
                && !entity.getApproveStatus().equals(ApproveStatusEnum.REJECT.getStatus()))) {
            throw new ServiceException(ApiError.ERROR_98010);
        }
        //提交流程
        if(isNeedProcess){
            startProcess(entity);
        }
        //操作日志
        operateLogService.addModuleOperateLog(String.format("提交了一个销售退货订单【%s】",entity.getCode()), ModuleTypeEnum.SO_RETURN.getCode(), entity.getId(), "提交操作");
        //更新审核状态
        lambdaUpdate().set(SoReturnEntity::getApproveStatus, ApproveStatusEnum.APPROVE_ING.getStatus())
                .eq(SoReturnEntity::getId, entity.getId())
                .update(new SoReturnEntity());
        return BatchResultDTO.success(entity.getId(),entity.getCode(),"操作成功");
    }

    /**
     * 启动流程
     *
     * @param entity
     * @return void
     * @Author will
     * @Date 2025/10/22 12:07
     **/

    public void startProcess(SoReturnEntity entity) {
        ProcessManagementDTO.StartDTO startDTO = new ProcessManagementDTO.StartDTO();
        startDTO.setBusinessId(entity.getId());
        startDTO.setBusinessCode(entity.getCode());
        startDTO.setBusinessKey(SourceTypeEnum.SO_RETURN.getCode());
        startDTO.setBusinessName(entity.getCode());
        startDTO.setUserId(entity.getSellerId());
        startDTO.setVariablesMap(getVariablesMap(entity));
        ApiResult<ProcessManagementDTO.StartResultDTO> listApiResult = workflowFeign.start(startDTO);
        if (!listApiResult.isSuccess()) {
            throw new ServiceException(listApiResult.getMsg());
        }
    }

    /**
     * variablesMap值赋值
     * @author will
     * @date 2025/10/22 10:51
     * @param entity
     * @return Map<String,Object>
     */
    private Map<String,Object> getVariablesMap(SoReturnEntity entity) {
        Map<String, Object> variablesMap = BeanUtil.beanToMap(entity);
        List<SoReturnDetailEntity> detailList = soReturnDetailService.lambdaQuery().eq(SoReturnDetailEntity::getMainId,entity.getId()).list();
        if (CollUtil.isEmpty(detailList)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL,"销售退货单");
        }
        variablesMap.put(ThirdConstants.DETAIL_LIST, BeanUtil.copyToList(detailList,Map.class));
        return variablesMap;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean addAndSubmit(SoReturnDTO.Add dto) {
        String id = this.add(dto);
        if (StringUtils.isBlank(id)) {
            throw new ServiceException(ApiError.ERROR_1019);
        }
        SoReturnEntity entity = this.getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_92173);
        }
        BatchResultDTO submit = this.submit(entity, Boolean.TRUE);
        return submit.getSuccess();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean updateAndSubmit(SoReturnDTO.Update dto) {
        Boolean update = this.update(dto);
        if (!update) {
            throw new ServiceException(ApiError.ERROR_1020);
        }
        SoReturnEntity entity = this.getById(dto.getId());
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_92173);
        }
        BatchResultDTO submit = this.submit(entity, Boolean.TRUE);
        return submit.getSuccess();
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO approve(BaseApproveParamDTO baseApproveParamDTO, SoReturnEntity entity) {
        List<SoReturnEntity> entityList = Arrays.asList(entity);
        //判断是否是审核中的状态
        long count = entityList.stream().filter(v -> !v.getInvalidStatus()
                && v.getApproveStatus().equals(ApproveStatusEnum.APPROVE_ING.getStatus())
        ).count();

        if (count != entityList.size()) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        //调用审核流程
        approveProcess(entity, new ApproveOneDTO(entity.getId(), baseApproveParamDTO.getType(), baseApproveParamDTO.getComment()));
        //操作日志
        List<Pair<String, String>> pairList = entityList.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog(String.format("审核【%s】了一个销售退货订单", ApproveTypeEnum.getName(baseApproveParamDTO.getType())).concat("【%s】").concat(StringUtils.isNotBlank(baseApproveParamDTO.getComment()) ? String.format(",意见：%s", baseApproveParamDTO.getComment()) : ""), ModuleTypeEnum.SO_DELIVERY_NOTICE.getCode(), pairList, "审核操作");
        return BatchResultDTO.success(entity.getId(),entity.getCode(),"审核成功");
    }

    /**
     * 审核流程调用
     * @author will
     * @date 2025/10/22 16:23
     * @param entity
     * @param dto
     * @return void
     */
    private void approveProcess(SoReturnEntity entity, ApproveOneDTO dto) {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ProcessManagementDTO.ApproveDTO approveDTO = new ProcessManagementDTO.ApproveDTO();
        approveDTO.setBusinessId(entity.getId());
        approveDTO.setBusinessKey(SourceTypeEnum.SO_RETURN.getCode());
        approveDTO.setApproveType(ApproveTypeEnum.getByCode(dto.getType()));
        approveDTO.setComment(dto.getComment());
        approveDTO.setUserId(userInfo.getUid());
        approveDTO.setVariablesMap(getVariablesMap(entity));
        ApiResult<ProcessManagementDTO.ApproveResultDTO> listApiResult = workflowFeign.approve(approveDTO);
        Integer code = listApiResult.getCode();
        if (200 != code) {
            throw new ServiceException(ApiError.ERROR_94006);
        }
        ProcessManagementDTO.ApproveResultDTO data = listApiResult.getData();
        if (ObjectUtil.isEmpty(data.getIsExistProcess()) || !data.getIsExistProcess()) {
            // 无需走流程的数据则直接更新状态
            approveEnd(dto, entity);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 180000)
    public Boolean approveEnd(ApproveOneDTO dto, SoReturnEntity entity) {
        //意见
        if (ApproveTypeEnum.PASS.getStatus().equals(dto.getType())) {
            LoginUser userInfo = UserContext.getDefaultLoginUser();
            //审核通过
            lambdaUpdate().set(SoReturnEntity::getApproveStatus, ApproveStatusEnum.APPROVE.getStatus())
                    .set(SoReturnEntity::getApproveUserId, userInfo.getUid())
                    .set(SoReturnEntity::getApproveUserName, userInfo.getUserName())
                    .set(SoReturnEntity::getApproveTime, LocalDateTime.now())
                    .eq(SoReturnEntity::getId, entity.getId())
                    .update(new SoReturnEntity());
            //增加广播通知
//            entityList.forEach(obj -> this.syncOrderToDmp(obj, SyncOperateEnum.OPERATE_APPROVE.getCode()));
        } else {
            //审核不通过
            lambdaUpdate().set(SoReturnEntity::getApproveStatus, ApproveStatusEnum.REJECT.getStatus())
                    .eq(SoReturnEntity::getId, entity.getId())
                    .update(new SoReturnEntity());
        }
        return Boolean.TRUE;
    }


    /**
     * 增加广播推送
     *
     * @param entity
     * @param syncOperate
     */
    @Override
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    public void syncOrderToDmp(SoReturnEntity entity, String syncOperate) {
        //判断是否需要推送记录

        if (!dmpTaskFeign.needPushMQ(LocalDateTime.now())) {
            return;
        }
        Map<String, Object> resultMap = new HashMap<>();

        //业务id
        resultMap.put("id", entity.getId());
        resultMap.put("operate", syncOperate);
        BiReturnOrderInfoEntity dmpOrderInfoEntity = this.returnOrderDataConvert(entity);
        resultMap.put("entity", dmpOrderInfoEntity);

        //添加推送任务
        DmpPushTaskFeignDTO taskFeignDTO = new DmpPushTaskFeignDTO();
        taskFeignDTO.setSourceId(entity.getId());
        taskFeignDTO.setSourceCode(entity.getCode());
        taskFeignDTO.setSourceType(SourceTypeEnum.SO_INFO.getCode());
        taskFeignDTO.setMqTopic(RocketMqTopic.SYNC_RETURN_ORDER_TO_DMP_TOPIC);
        taskFeignDTO.setMqTag(RocketMqTagEnum.APPROVED_RETURN_ORDER_TO_DMP_TAG.getName());
        taskFeignDTO.setMqData(JSONUtil.toJsonStr(resultMap));
        taskFeignDTO.setSourcePlatformName(PlatformEnum.ERP_OMS.getDesc());
        taskFeignDTO.setTargetPlatformName(PlatformEnum.ERP_DMP.getDesc());
        taskFeignDTO.setSyncOperate(syncOperate);

        DmpPushTaskEntity dmpPushTaskEntity = dmpMqFeign.saveTask(taskFeignDTO);

        //推送DMP
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                dmpMqFeign.delayLevel3SendTask(Collections.singletonList(dmpPushTaskEntity));
            }
        });

        log.info("推送消息开始：{}", taskFeignDTO.toString());
    }

    @Override
    public PagingVO<SoReturnDTO.PagingView> exportSoReturn(PagingDTO<SoReturnDTO.PagingParam> dto) {
        dto.getParams().setPermissionSql(dto.getPermissionSql());
        Page<SoReturnDTO.PagingView> page = baseMapper.soDeliveryNoticeExportExcel(new Page<>(dto.getCurrPage(), dto.getPageSize()),dto.getParams());
        //获取sku的id集合
        List<String> skuIdList = page.getRecords().stream().map(SoReturnDTO.PagingView::getSkuId).collect(Collectors.toList());
        //根据ids查询sku信息
        List<ProductDetailEntity> detailEntityList = plmTaskFeign.getByIdList(skuIdList);
        List<String> soIds = page.getRecords().stream().map(SoReturnDTO.PagingView::getSourceId).distinct().collect(Collectors.toList());
        List<CustomerInfoEntity> customerInfoEntities = customerInfoService.list();
        List<SoOutstockDetailEntity> soOutstockDetailEntities = soOutstockFeign.listDetailBySoIds(soIds);
        List<String> soDetailIds = page.getRecords().stream().map(SoReturnDTO.PagingView::getSourceDetailId).collect(Collectors.toList());
        List<SoDetailEntity> soDetailEntities = soDetailService.listSoDetailByIds(soDetailIds);

        //查询审核流程
        List<String> ids = page.getRecords().stream().map(SoReturnDTO.PagingView::getId).distinct().collect(Collectors.toList());
        ValidList<ProcessManagementDTO.HistoryActivityDTO> dtoList = ids.stream().map(obj -> new ProcessManagementDTO.HistoryActivityDTO(SourceTypeEnum.SO_RETURN.getCode(), obj)).collect(Collectors.toCollection(ValidList::new));
        ApiResult<List<ProcessManagementDTO.CurApproveInfoDTO>> listApiResult = workflowFeign.curApprover(dtoList);
        if (200 != listApiResult.getCode()) {
            throw new ServiceException(new ApiResult(ApiError.DEFAULT.code,listApiResult.getMsg()));
        }

        for (SoReturnDTO.PagingView pagingView : page.getRecords()) {
            pagingView.setApproveStatusName(ApproveStatusEnum.getName(pagingView.getApproveStatus()));
            pagingView.setInvalidStatusName(InvalidStatusEnum.getName(pagingView.getInvalidStatus()));
            pagingView.setTypeName(BillTypeEnum.getName(pagingView.getType()));
            ProductDetailEntity productDetailEntity = detailEntityList.stream().filter(entityClass -> entityClass.getId().equals(pagingView.getSkuId())).findFirst().orElse(new ProductDetailEntity());
            if(StringUtils.isNotBlank(pagingView.getSourceDetailId())){
                SoDetailEntity soDetailEntity = soDetailEntities.stream().filter(detail -> detail.getId().equals(pagingView.getSourceDetailId())).findFirst().orElse(new SoDetailEntity());
                pagingView.setSalesQty(soDetailEntity.getQty());
                Integer actualQty = soOutstockDetailEntities.stream().filter(detail -> soDetailEntity.getMainId().equals(detail.getSoId()) && detail.getSkuId().equals(pagingView.getSkuId()) && detail.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus())).map(SoOutstockDetailEntity::getActualQty).reduce(MathUtil.ZERO, Integer::sum);
                pagingView.setDeliveryQty(actualQty);
                pagingView.setUnDeliveryQty(soDetailEntity.getQty() - actualQty);
                pagingView.setSalesAmount(soDetailEntity.getAmount());
                if(StringUtils.isBlank(pagingView.getCurrency())){
                    pagingView.setCurrency(soDetailEntity.getCurrency());
                }
                if(StringUtils.isBlank(pagingView.getCurrencySymbol())){
                    pagingView.setCurrencySymbol(soDetailEntity.getCurrencySymbol());
                }
            }
            pagingView.setProductName(productDetailEntity.getName());
            pagingView.setUnit(productDetailEntity.getUnitName());
            CustomerInfoEntity customerInfoEntity = customerInfoEntities.stream().filter(req -> req.getId().equals(pagingView.getCustomerId())).findFirst().orElse(new CustomerInfoEntity());
            pagingView.setCustomerName(customerInfoEntity.getName());

            //最新审核人
            if (CollectionUtils.isNotEmpty(listApiResult.getData())) {
                String curApprove = listApiResult.getData().stream().filter(e -> e.getBusinessId().equals(pagingView.getId()) && CharSequenceUtil.isNotBlank(e.getCurApproveName())).map(ProcessManagementDTO.CurApproveInfoDTO::getCurApproveName).collect(Collectors.joining(","));
                pagingView.setApproveUserName(CharSequenceUtil.blankToDefault(curApprove,pagingView.getApproveUserName()));
            }
        }
        return new PagingVO<>(page);
    }

    /**
     * 销售出货单字段转换
     *
     * @param soReturnEntity
     * @return
     */
    private BiReturnOrderInfoEntity returnOrderDataConvert(SoReturnEntity soReturnEntity) {
        BiReturnOrderInfoEntity entity = SoReturnConverter.INSTANCE.soReturnOrderToDmpReturn(soReturnEntity);
        //原始订单
        SoInfoEntity soInfoEntity = null;
        Map<String, SoDetailEntity> soDetailEntityMap = null;
        entity.setRefundTime(Objects.nonNull(soReturnEntity.getBillDate()) ? soReturnEntity.getBillDate().atStartOfDay() : null);
        try {
            if (com.alibaba.nacos.common.utils.StringUtils.isNotEmpty(soReturnEntity.getSourceId())) {
                soInfoEntity = soInfoService.getById(soReturnEntity.getSourceId());
            }
            if (Objects.nonNull(soInfoEntity)) {
                entity.setPaidTime(Objects.nonNull(soInfoEntity.getReceiveDate()) ? soInfoEntity.getReceiveDate().atStartOfDay() : null);
                entity.setOrderTime(soInfoEntity.getCreateTime());
                entity.setOrderCode(soInfoEntity.getCode());
            }
            if (Objects.nonNull(soInfoEntity) && com.alibaba.nacos.common.utils.StringUtils.isNotEmpty(soInfoEntity.getId())) {
                List<SoDetailEntity> soDetailEntities = soDetailService.listSoDetailByMainId(soInfoEntity.getId());
                if (CollUtil.isNotEmpty(soDetailEntities)) {
                    soDetailEntityMap = soDetailEntities.stream().collect(Collectors.toMap(SoDetailEntity::getId, Function.identity()));
                    SoDetailEntity detailEntity = soDetailEntities.stream().filter(soDetailEntity -> Objects.nonNull(soDetailEntity.getExchangeRate())).findFirst().orElse(null);
                    BigDecimal exchangeRate;
                    if (Objects.nonNull(detailEntity) && Objects.nonNull(detailEntity.getExchangeRate())) {
                        exchangeRate = detailEntity.getExchangeRate();
                    } else {
                        exchangeRate = BigDecimal.ONE;
                    }
                    entity.setCurrencyRate(exchangeRate);
                    BigDecimal orderFee = BigDecimal.ZERO;
                    soDetailEntities.forEach(
                            soDetailEntity -> orderFee.add(Optional.ofNullable(soDetailEntity.getPrice()).orElse(BigDecimal.ZERO).multiply(exchangeRate))
                    );
                    entity.setOrderFee(orderFee);
                }
            }
        } catch (Exception e) {
            log.error("请求erp-oms soInfoFeign.getSoInfoById 异常:{}", e.getMessage());
            throw new ServiceException(ApiError.NO_PERMISSION.code, "获取原始订单异常");
        }
        CustomerInfoEntity customerInfo = null;
        try {
            if (com.alibaba.nacos.common.utils.StringUtils.isNotEmpty(soReturnEntity.getCustomerId())) {
                customerInfo = customerInfoService.getCustomerById(soReturnEntity.getCustomerId());
                if (Objects.nonNull(customerInfo)) {
                    entity.setShopNo(customerInfo.getCode());
                    entity.setBuyerUserId(customerInfo.getCode());
                }
            }

        } catch (Exception e) {
            log.error("请求erp-oms customerFeign.getCustomerById异常:{}", e.getMessage());
        }
        //国家字典
        if (Objects.nonNull(customerInfo) && com.alibaba.nacos.common.utils.StringUtils.isNotEmpty(customerInfo.getCountryId())) {
            try {
                DictCountryEntity country = sysUserFeign.getCountryById(customerInfo.getCountryId());
                if (Objects.nonNull(country)) {
                    entity.setCountryNameCn(country.getNameCn());
                    entity.setCountryNameEn(country.getNameEn());
                }
            } catch (Exception e) {
                log.error("erp-sys sysUserFeign.getCountryById {}异常：{}", customerInfo.getCountryId(), e.getMessage());
            }
        }

        //退货单详情
        List<SoReturnDetailEntity> details = soReturnDetailService.listDetailByMainId(soReturnEntity.getId());
        //明细字段转换
        if (CollUtil.isNotEmpty(details)) {
            //订单明细
            List<BiReturnOrderItemEntity> orderItemEntities = new ArrayList<>(details.size());

            Map<String, SoDetailEntity> finalSoDetailEntityMap = soDetailEntityMap;
            details.forEach(soReturnDetail -> {
                BiReturnOrderItemEntity biReturnOrderItemEntity = SoReturnConverter.INSTANCE.soReturnOrderToDmpReturnItem(soReturnDetail);
                //保存时会重置主表id
                biReturnOrderItemEntity.setReturnOrderId(entity.getId());
                if (com.alibaba.nacos.common.utils.StringUtils.isNotEmpty(soReturnDetail.getSkuId())) {
                    List<ProductDetailEntity> detailEntityList = plmTaskFeign.getByIdList(Collections.singletonList(soReturnDetail.getSkuId()));
                    if (CollectionUtils.isNotEmpty(detailEntityList)) {
                        biReturnOrderItemEntity.setItemName(detailEntityList.get(0).getName());
                        biReturnOrderItemEntity.setProductUnit(detailEntityList.get(0).getUnitId());
                        biReturnOrderItemEntity.setPictureUrl(detailEntityList.get(0).getImagesUrl());
                        biReturnOrderItemEntity.setSpecifics(detailEntityList.get(0).getVariantProperty());
                    }
                }
                //获取订单详情表
                if (com.alibaba.nacos.common.utils.StringUtils.isNotEmpty(soReturnDetail.getSourceDetailId())) {
                    SoDetailEntity soDetail = finalSoDetailEntityMap.get(soReturnDetail.getSourceDetailId());
                    if (Objects.nonNull(soDetail)) {
                        biReturnOrderItemEntity.setSellPrice(soDetail.getAmount());
                        if (Objects.nonNull(soDetail.getTaxAmount()) && Objects.nonNull(soDetail.getQty()) && Objects.nonNull(soReturnDetail.getReturnQty())) {
                            biReturnOrderItemEntity.setAmountAfter(soDetail.getTaxAmount().divide(BigDecimal.valueOf(soDetail.getQty()), 4, BigDecimal.ROUND_HALF_UP).multiply(BigDecimal.valueOf(soReturnDetail.getReturnQty())));
                        }
                        biReturnOrderItemEntity.setCleanCostPrice(soDetail.getSaleCost());
                        if (Objects.nonNull(soDetail.getIsGift()) && soDetail.getIsGift()) {
                            biReturnOrderItemEntity.setIsGift(1);
                        } else {
                            biReturnOrderItemEntity.setIsGift(2);
                        }
                    }
                }
                orderItemEntities.add(biReturnOrderItemEntity);
            });
            entity.setItemList(orderItemEntities);
        }

        return entity;
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO disApprove(SoReturnEntity entity) {
        //已审核支持反审核
        if(!entity.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus())){
            return BatchResultDTO.fail(entity.getId(),entity.getCode(),ApiError.ERROR_99003.msg);
        }
        List<String> ids = Arrays.asList(entity.getId());
        //TODO 待加审核流程

        //有退货通知单不能反审核
        List<SoReturnNoticeEntity> soReturnNoticeEntities = soReturnNoticeFeign.listBySourceId(ids);
        if (CollectionUtils.isNotEmpty(soReturnNoticeEntities)) {
            throw new ServiceException(ApiError.ERROR_92012);
        }
        //有退货签收单不能反审核
        List<SoReturnReceiveEntity> soReturnReceiveEntities = soReturnReceiveFeign.listBySourceId(ids);
        if (CollectionUtils.isNotEmpty(soReturnReceiveEntities)) {
            throw new ServiceException(ApiError.ERROR_92013);
        }
        //修改状态为待提交
        lambdaUpdate().set(SoReturnEntity::getApproveStatus, ApproveStatusEnum.WAIT_SUBMIT.getStatus())
                .in(SoReturnEntity::getId, ids)
                .update(new SoReturnEntity());
        //推送到DMP
//        this.syncOrderToDmp(entity, SyncOperateEnum.OPERATE_DISAPPROVE.getCode());
        //操作日志
        operateLogService.addModuleOperateLog(String.format("反审核销售退货订单【%s】", entity.getCode()), ModuleTypeEnum.SO_RETURN.getCode(),entity.getId(), "反审核操作");
        return BatchResultDTO.success(entity.getId(),entity.getCode(),"操作成功");
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    public Boolean cancelProcess(ApproveDTO.BatchCancelProcessDTO dto) {
        List<String> ids = dto.getIds();
        List<SoReturnEntity> entityList = this.listByIds(ids);
        if (CollectionUtils.isEmpty(ids)) {
            throw new ServiceException(ApiError.ERROR_98004);
        }
        //审核中可以撤销
        long count = entityList.stream().filter(entity -> entity.getInvalidStatus() == false
                && entity.getApproveStatus().equals(ApproveStatusEnum.APPROVE_ING.getStatus())
        ).count();

        if (count != entityList.size()) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        //撤销现有流程
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ids.forEach(obj -> {
            ProcessManagementDTO.RevokeDTO revokeDTO = new ProcessManagementDTO.RevokeDTO();
            revokeDTO.setBusinessId(obj);
            revokeDTO.setBusinessKey(SourceTypeEnum.SO_RETURN.getCode());
            revokeDTO.setUserId(userInfo.getUid());
            workflowFeign.revokeProcess(revokeDTO);
        });
        //修改状态为待提交
        lambdaUpdate().set(SoReturnEntity::getApproveStatus, ApproveStatusEnum.WAIT_SUBMIT.getStatus())
                .in(SoReturnEntity::getId, ids)
                .update(new SoReturnEntity());

        //操作日志
        List<Pair<String, String>> pairList = entityList.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("发货通知单【%s】取消流程", ModuleTypeEnum.SO_DELIVERY_NOTICE.getCode(), pairList, "取消流程操作");

        return Boolean.TRUE;
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    public Boolean invalid(List<String> ids, String remark) {
        List<SoReturnEntity> entityList = this.listByIds(ids);
        if (CollectionUtils.isEmpty(ids)) {
            throw new ServiceException(ApiError.ERROR_98004);
        }
        //审核不通过 待提交可以作废
        long count = entityList.stream().filter(entity -> entity.getInvalidStatus() == false
                && (entity.getApproveStatus().equals(ApproveStatusEnum.WAIT_SUBMIT.getStatus())
                || entity.getApproveStatus().equals(ApproveStatusEnum.REJECT.getStatus()))
        ).count();

        if (count != entityList.size()) {
            throw new ServiceException(ApiError.ERROR_98005);
        }

        //修改状态为待提交
        lambdaUpdate().set(SoReturnEntity::getInvalidStatus, Boolean.TRUE)
                .set(SoReturnEntity::getInvalidRemark, remark)
                .in(SoReturnEntity::getId, ids)
                .update(new SoReturnEntity());
        //操作日志
        List<Pair<String, String>> pairList = entityList.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("作废了一个发货通知单【%s】，作废原因：".concat(remark), ModuleTypeEnum.SO_DELIVERY_NOTICE.getCode(), pairList, "作废操作");

        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(List<String> ids) {
        List<SoReturnEntity> entityList = this.listByIds(ids);
        if (CollectionUtils.isEmpty(ids)) {
            throw new ServiceException(ApiError.ERROR_98004);
        }
        //待提交支持删除
        long count = entityList.stream().filter(entity -> entity.getInvalidStatus() == false
                && entity.getApproveStatus().equals(ApproveStatusEnum.WAIT_SUBMIT.getStatus())
        ).count();
        if (count != entityList.size()) {
            throw new ServiceException(ApiError.ERROR_98009);
        }

        //删除详情表
        soReturnDetailService.delete(ids);
        boolean flag = this.removeByIds(ids);
        //删除主表
        return flag;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<BatchResultDTO> delete(List<String> ids, boolean returnDetails) {
        List<SoReturnEntity> entityList = this.listByIds(ids);
        if (CollectionUtils.isEmpty(ids)) {
            throw new ServiceException(ApiError.ERROR_98004);
        }
//        //待提交支持删除
//        long count = entityList.stream().filter(entity -> entity.getInvalidStatus() == false
//                && entity.getApproveStatus().equals(ApproveStatusEnum.WAIT_SUBMIT.getStatus())
//        ).count();
//        if (count != entityList.size()) {
//            throw new ServiceException(ApiError.ERROR_98009);
//        }
        List<SoReturnEntity> removeList=new ArrayList<>();
        List<BatchResultDTO> resultDTOList=new ArrayList<>();
        String waitSubmitStatus = ApproveStatusEnum.WAIT_SUBMIT.getStatus();
        for (SoReturnEntity entity : entityList) {
            if (!entity.getApproveStatus().equals(waitSubmitStatus)
                    ||entity.getInvalidStatus()){
                resultDTOList.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), ApiError.ERROR_98009.msg));
                continue;
            }
            removeList.add(entity);
            resultDTOList.add(BatchResultDTO.success(entity.getId(), entity.getCode(),"删除成功"));
        }
        List<String> removeIdList = removeList.stream().map(SoReturnEntity::getId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(removeIdList)){
            return resultDTOList;
        }
        //删除详情表
        soReturnDetailService.delete(removeIdList);
        boolean flag = this.removeByIds(removeIdList);
        if (!flag){
            throw new ServiceException(ApiError.ERROR_DATA_DELETE_ERROR);
        }
        
        // 添加批量操作日志
        String content = "批量删除销售退货订单";
        List<Pair<String, String>> pairList = resultDTOList.stream()
                .map(entity -> new Pair<>(entity.getId(), entity.getCode()))
                .collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog(content, ModuleTypeEnum.SO_RETURN.getCode(), pairList, "删除");
        
        // 返回成功结果
        return resultDTOList;
    }

    @Override
    public Boolean exportExcel(SoReturnDTO.PagingParam dto) {

        downloadTaskFeign.saveDownloadTask("销售退货订单", EXPORT_OMS_SO_RETURN.getCode() ,dto);
        return true;
    }

    @Override
    public List<SoReturnDTO.GenerateSoReturnNoticeView> generateSoReturnNoticeView(List<String> ids) {
        List<SoReturnDTO.GenerateSoReturnNoticeView> list = baseMapper.generateSoReturnNoticeView(ids);
        //获取sku的id集合
        List<String> skuIdList = list.stream().map(SoReturnDTO.GenerateSoReturnNoticeView::getSkuId).collect(Collectors.toList());
        //根据ids查询sku信息
        List<ProductDetailEntity> detailEntityList = plmTaskFeign.getByIdList(skuIdList);
        //获取销售订单详情表id集合
        List<String> orderDetailIds = list.stream().map(SoReturnDTO.GenerateSoReturnNoticeView::getSourceDetailId).collect(Collectors.toList());
        //获取销售单详情信息
        List<SoDetailEntity> soDetailEntities = soDetailService.listSoDetailByIds(orderDetailIds);
        //获取退货单详情表id
        List<String> soReturnDetailIds = list.stream().map(SoReturnDTO.GenerateSoReturnNoticeView::getId).collect(Collectors.toList());
        //获取退货通知单详情
        List<SoReturnNoticeDetailEntity> returnNoticeDetailEntities = soReturnNoticeFeign.listDetailBySourceDetailIds(soReturnDetailIds);
        List<String> soIds = soDetailEntities.stream().map(SoDetailEntity::getMainId).distinct().collect(Collectors.toList());
        List<SoOutstockDetailEntity> soOutstockDetailEntities = soOutstockFeign.listDetailBySoIds(soIds);
        for (SoReturnDTO.GenerateSoReturnNoticeView generateSoReturnNoticeView : list) {
            SoDetailEntity soDetailEntity = soDetailEntities.stream().filter(detail -> detail.getId().equals(generateSoReturnNoticeView.getSourceDetailId())).findFirst().orElse(new SoDetailEntity());
            generateSoReturnNoticeView.setSalesQty(soDetailEntity.getQty());
            Integer actualQty = soOutstockDetailEntities.stream().filter(detail -> soDetailEntity.getMainId().equals(detail.getSoId()) && detail.getSkuId().equals(generateSoReturnNoticeView.getSkuId()) && detail.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus())).map(SoOutstockDetailEntity::getActualQty).reduce(MathUtil.ZERO, Integer::sum);
            generateSoReturnNoticeView.setDeliveryQty(actualQty);
            Integer noticeReturnQty = returnNoticeDetailEntities.stream().filter(detail -> generateSoReturnNoticeView.getId().equals(detail.getSourceDetailId()) && detail.getSkuId().equals(generateSoReturnNoticeView.getSkuId())).map(SoReturnNoticeDetailEntity::getReturnQty).reduce(MathUtil.ZERO, Integer::sum);
            generateSoReturnNoticeView.setReturnQty(generateSoReturnNoticeView.getReturnQty() - noticeReturnQty);
            ProductDetailEntity productDetailEntity = detailEntityList.stream().filter(entityClass -> entityClass.getId().equals(generateSoReturnNoticeView.getSkuId())).findFirst().orElse(new ProductDetailEntity());
            generateSoReturnNoticeView.setProductName(productDetailEntity.getName());
            generateSoReturnNoticeView.setSourceDetailId(generateSoReturnNoticeView.getSourceDetailId());
            generateSoReturnNoticeView.setReturnTypeDictName(ReturnTypeEnum.getName(generateSoReturnNoticeView.getReturnTypeDict()));
            generateSoReturnNoticeView.setReturnReasonDictName(ReturnReasonEnum.getName(generateSoReturnNoticeView.getReturnReasonDict()));
        }
        return list;
    }

    @Override
    public SoReturnDTO.SoReturnEntityDTO getSoReturnById(String id) {
        SoReturnEntity byId = this.getById(id);
        SoReturnDTO.SoReturnEntityDTO dto = new SoReturnDTO.SoReturnEntityDTO();
        BeanMapper.copy(byId, dto);
        List<SoReturnDetailEntity> list = soReturnDetailService.lambdaQuery().eq(SoReturnDetailEntity::getMainId, id).list();
        dto.setExchangeRate(list.get(0).getExchangeRate());
        return dto;
    }

    @Override
    public List<SoReturnEntity> listSoReturnByApproveStatus() {
        String permissionSql = authDataFeign.getWarehousePermissionSql("sr.warehouse_id");
        return baseMapper.listSoReturnByApproveStatus(ApproveStatusEnum.APPROVE.getStatus(), permissionSql);
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    public Boolean generateSoReturnSave(List<SoInfoDTO.GenerateSoReturnView> list) {
        List<String> soIdList = list.stream().map(SoInfoDTO.GenerateSoReturnView::getSoId).distinct().collect(Collectors.toList());
        //B2B销售订单
        List<SoInfoEntity> soInfoEntities = soInfoService.listByIds(soIdList);
        //去重后的币种集合
        List<String> currencys = soInfoEntities.stream().map(SoInfoEntity::getCurrency).filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
        //汇率集合
        Map<String, BigDecimal> currencyMap = getCurrencyMap(currencys);
        //币种符号集合
        Map<String, String> currencySymbol = getCurrencySymbol(currencys);
        //获取
        for (String soId : soIdList) {
            List<SoInfoDTO.GenerateSoReturnView> viewList = list.stream().filter(req -> req.getSoId().equals(soId)).collect(Collectors.toList());
            SoInfoDTO.GenerateSoReturnView soReturnView = viewList.get(0);
            SoInfoEntity soInfoEntity = soInfoEntities.stream().filter(req -> req.getId().equals(soId)).findFirst().orElse(new SoInfoEntity());
            SoReturnDTO.Add add = new SoReturnDTO.Add();
            add.setSourceId(soId);
            add.setSourceType(SourceTypeEnum.SO_INFO.getCode());
            add.setWarehouseId(soReturnView.getWarehouseId());
            add.setBillDate(soReturnView.getReturnDate());
            add.setCustomerId(soReturnView.getCustomerId());
            add.setCurrency(soInfoEntity.getCurrency());
            add.setCurrencySymbol(currencySymbol.get(soInfoEntity.getCurrency()));
            add.setExchangeRate(currencyMap.get(soInfoEntity.getCurrency()));
            add.setReturnLogisticCode(soReturnView.getReturnLogisticCode());
            List<SoReturnDetailDTO.Add> detailList = new ArrayList<>();
            for (SoInfoDTO.GenerateSoReturnView view : viewList) {
                SoReturnDetailDTO.Add detailAdd = new SoReturnDetailDTO.Add();
                detailAdd.setReturnQty(view.getReturnQty());
                detailAdd.setReturnTypeDict(view.getReturnTypeDict());
                detailAdd.setReturnReasonDict(view.getReturnReasonDict());
                detailAdd.setSourceDetailId(view.getDetailId());
                detailAdd.setRemark(view.getRemark());
                detailAdd.setReturnAmount(view.getReturnAmount());
                detailAdd.setTaxReturnAmount(view.getTaxReturnAmount());
                detailAdd.setReturnAmountLocalCurrency(this.calLocalCurrency(view.getExchangeRate(), view.getReturnAmount()));
                detailAdd.setTaxReturnAmountLocalCurrency(this.calLocalCurrency(view.getExchangeRate(), view.getTaxReturnAmount()));
                detailAdd.setExchangeRate(currencyMap.get(soInfoEntity.getCurrency()));
                detailList.add(detailAdd);
            }
            add.setDetailList(detailList);
            this.add(add);
        }
        return Boolean.TRUE;
    }
    //获取币种符号集合
    @Override
    public Map<String,String> getCurrencySymbol(List<String> currencys) {
        List<CurrencyDTO.ViewDTO> currencySymbols = sysUserFeign.listByCurrency(currencys);
        Map<String,String> currencySymbolMap = new HashMap<>();
        if(CollUtil.isNotEmpty(currencySymbols)){
            for (CurrencyDTO.ViewDTO currencySymbol : currencySymbols) {
                currencySymbolMap.put(currencySymbol.getId(),currencySymbol.getSymbol());
            }
        }
        return currencySymbolMap;
    }

    //获取货币汇率
    @Override
    public Map<String,BigDecimal> getCurrencyMap(List<String> currencys) {
        Map<String,BigDecimal> currencyMap = new HashMap<>();
        if(CollectionUtils.isNotEmpty(currencys)){
            LocalDate localDate = LocalDate.now();
            for (String currency : currencys) {
                //查询redis中存储的成本信息
                String existKey = StrUtil.format(RedisKeyConstant.SETTLEMENT_EXCHANGE_RATE, CurrencyEnum.CNY.getCurrencyCode(),currency);
                List<BiSettlementExchangeRateEntity> rateList = (List<BiSettlementExchangeRateEntity>) redisUtil.get(existKey);
                if(CollectionUtils.isEmpty(rateList)){
                    //从dmp获取对应的币种汇率
                    BigDecimal exchangeRate = dmpTaskFeign.getRate(localDate.toString(), currency);
                    currencyMap.put(currency,exchangeRate);
                }else {
                    //从缓存汇率
                    BigDecimal exchangeRate = rateList.stream().filter(obj -> obj.getSettlementDateBegin().isEqual(localDate)
                                    || obj.getSettlementDateEnd().isEqual(localDate)
                                    || (obj.getSettlementDateBegin().isBefore(localDate) && obj.getSettlementDateEnd().isAfter(localDate)))
                            .sorted(Comparator.comparing(BiSettlementExchangeRateEntity::getUpdateTime, Comparator.reverseOrder()))
                            .map(BiSettlementExchangeRateEntity::getExchangeRate)
                            .findFirst().orElse(null);
                    currencyMap.put(currency,exchangeRate);
                }
            }
        }
        return currencyMap;
    }

    @Override
    public List<SoReturnDTO.PagingView> listSoReturnDetailBySourceId(String sourceId) {
        List<SoReturnDTO.PagingView> list = this.baseMapper.listSoReturnDetailBySourceId(sourceId);
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        //获取sku的id集合
        List<String> skuIdList = list.stream().map(SoReturnDTO.PagingView::getSkuId).collect(Collectors.toList());
        //根据ids查询sku信息
        List<ProductDetailEntity> detailEntityList = plmTaskFeign.getByIdList(skuIdList);
        List<String> soIds = list.stream().map(SoReturnDTO.PagingView::getSourceId).distinct().collect(Collectors.toList());
        List<CustomerInfoEntity> customerInfoEntities = customerInfoService.list();
        List<SoOutstockDetailEntity> soOutstockDetailEntities = soOutstockFeign.listDetailBySoIds(soIds);
        List<String> soDetailIds = list.stream().map(SoReturnDTO.PagingView::getSourceDetailId).collect(Collectors.toList());
        List<SoDetailEntity> soDetailEntities = soDetailService.listSoDetailByIds(soDetailIds);
        for (SoReturnDTO.PagingView obj : list) {
            obj.setApproveStatusName(ApproveStatusEnum.getName(obj.getApproveStatus()));
            obj.setInvalidStatusName(InvalidStatusEnum.getName(obj.getInvalidStatus()));
            obj.setTypeName(BillTypeEnum.getName(obj.getType()));
            ProductDetailEntity productDetailEntity = detailEntityList.stream().filter(entityClass -> entityClass.getId().equals(obj.getSkuId())).findFirst().orElse(new ProductDetailEntity());
            SoDetailEntity soDetailEntity = soDetailEntities.stream().filter(detail -> detail.getId().equals(obj.getSourceDetailId())).findFirst().orElse(new SoDetailEntity());
            obj.setProductName(productDetailEntity.getName());
            obj.setSalesQty(soDetailEntity.getQty());

            Integer actualQty = soOutstockDetailEntities.stream().filter(detail -> soDetailEntity.getMainId().equals(detail.getSoId()) && detail.getSkuId().equals(obj.getSkuId()) && detail.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus())).map(SoOutstockDetailEntity::getActualQty).reduce(MathUtil.ZERO, Integer::sum);
            obj.setDeliveryQty(actualQty);
            obj.setUnDeliveryQty(soDetailEntity.getQty() - actualQty);
            obj.setUnit(productDetailEntity.getUnitName());
            obj.setSalesAmount(soDetailEntity.getAmount());
            CustomerInfoEntity customerInfoEntity = customerInfoEntities.stream().filter(req -> req.getId().equals(obj.getCustomerId())).findFirst().orElse(new CustomerInfoEntity());
            obj.setCustomerName(customerInfoEntity.getName());
        }
        return list;
    }

    /**
     * 根据来源id 集合获取到对应的下推数据
     *
     * @param soIds
     * @return java.lang.Integer
     * @author yl
     * @date 2023-05-29 16:47
     */
    @Override
    public Integer getPushDownBySourceIds(List<String> soIds) {
        if (CollectionUtils.isEmpty(soIds)) {
            return 0;
        }
        return this.lambdaQuery().in(SoReturnEntity::getSourceId, soIds).
                eq(SoReturnEntity::getInvalidStatus, Boolean.FALSE).
                count();
    }

    @Override
    public Boolean updateSyncKingdeeId(String id, String syncKingdeeId) {
        return  this.lambdaUpdate()
                .eq(SoReturnEntity::getId,id)
                .set(StringUtils.isNotBlank(syncKingdeeId),SoReturnEntity::getSyncKingdeeId,syncKingdeeId)
                .update(new SoReturnEntity());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateAddress(String soId, String receiveAddress, String receiverName, String telNumber) {
        lambdaUpdate().set(StringUtils.isNotBlank(receiveAddress), SoReturnEntity::getReceiveAddress, receiveAddress)
                .set(StringUtils.isNotBlank(receiverName), SoReturnEntity::getReceiverName, receiverName)
                .set(StringUtils.isNotBlank(telNumber), SoReturnEntity::getTelNumber, telNumber)
                .eq(SoReturnEntity::getSourceType, SourceTypeEnum.SO_INFO)
                .eq(SoReturnEntity::getSourceId, soId)
                .update(new SoReturnEntity());
    }

    @Override
    public List<SoReturnDTO.PdaSoReturn> pdaList(SoReturnDTO.PdaSoReturnParam dto) {
        List<SoReturnDTO.PdaSoReturn> pdaSoReturns = baseMapper.pdaList(dto);
        if (CollectionUtils.isEmpty(pdaSoReturns)) {
            return new ArrayList<>();
        }
        List<String> soReturnIds = pdaSoReturns.stream().map(req -> req.getId()).collect(Collectors.toList());
        List<SoReturnDetailEntity> soReturnDetailEntityList = soReturnDetailService.listDetailByMainIds(soReturnIds);
        List<String> srdIds = soReturnDetailEntityList.stream().map(req -> req.getId()).collect(Collectors.toList());
        List<SoReturnReceiveDetailEntity> soReturnReceiveDetailEntities = soReturnReceiveFeign.listDetailBySourceDetailIds(srdIds);

        //获取未全部到货的退货单详情id
        // 转换 soReturnDetailEntityList 为 Map
        Map<String, SoReturnDetailEntity> detailEntityMap = soReturnDetailEntityList.stream()
                .collect(Collectors.toMap(SoReturnDetailEntity::getId, entity -> entity));

        // 获取未全部到货的退货单详情 ID
        List<String> soReturnDetailIds = soReturnReceiveDetailEntities.stream()
                .collect(Collectors.groupingBy(SoReturnReceiveDetailEntity::getSourceDetailId))
                .entrySet().stream()
                .filter(entry -> {
                    String sourceDetailId = entry.getKey();
                    List<SoReturnReceiveDetailEntity> receiveDetails = entry.getValue();
                    int receiveQty = receiveDetails.stream().mapToInt(SoReturnReceiveDetailEntity::getReceiveQty).sum();
                    SoReturnDetailEntity detailEntity = detailEntityMap.getOrDefault(sourceDetailId, new SoReturnDetailEntity());
                    return receiveQty < detailEntity.getReturnQty();
                })
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        List<String> collect = soReturnReceiveDetailEntities.stream().map(req -> req.getSourceDetailId()).distinct().collect(Collectors.toList());
        List<String> ids = srdIds.stream().filter(poid -> !collect.contains(poid)).collect(Collectors.toList());
        soReturnDetailIds.addAll(ids);

        if (CollectionUtils.isEmpty(soReturnDetailIds)) {
            return new ArrayList<>();
        }

        //根据未到货的退货单详情id获取退货单id
        List<SoReturnDetailEntity> returnDetailEntityList = soReturnDetailService.listByIds(soReturnDetailIds);
        List<String> notAllReceiveSoReturnId = returnDetailEntityList.stream().map(req -> req.getMainId()).distinct().collect(Collectors.toList());

        //获取到未到货的退货单返回数据
        List<SoReturnDTO.PdaSoReturn> list = pdaSoReturns.stream().filter(req -> notAllReceiveSoReturnId.contains(req.getId())).collect(Collectors.toList());
        list.sort(Comparator.comparing(SoReturnDTO.PdaSoReturn::getSoCode).reversed());
        list.forEach(req -> req.setApproveStatusName(ApproveStatusEnum.getName(req.getApproveStatus())));
        return list;
    }

    @Override
    public SoReturnDTO.View pdaView(String id) {
        SoReturnDTO.View viewDTO = new SoReturnDTO.View();
        SoReturnEntity soReturnEntity = this.getById(id);
        //创库保存详情表的集合
        List<SoReturnDetailDTO.View> detailViewDTOS = new ArrayList<>();
        List<SoReturnDetailEntity> detailEntityList = soReturnDetailService.listDetailByMainId(id);
        if (CollectionUtils.isEmpty(detailEntityList)) {
            throw new ServiceException(ApiError.ERROR_92023);
        }
        SoInfoEntity soInfoEntity = soInfoService.getById(soReturnEntity.getSourceId());
        BeanMapperUtils.copy(soInfoEntity, viewDTO);
        BeanMapperUtils.copy(soReturnEntity, viewDTO);
        //获取sku的id集合
        List<String> skuIdList = detailEntityList.stream().map(SoReturnDetailEntity::getSkuId).collect(Collectors.toList());
        //根据ids查询sku信息
        List<ProductDetailEntity> productDetailEntitys = plmTaskFeign.getByIdList(skuIdList);
        //获取销售单详情表id集合
        List<String> orderDetailIds = detailEntityList.stream().map(SoReturnDetailEntity::getSourceDetailId).collect(Collectors.toList());
        //获取销售单详情信息
        List<SoDetailEntity> soDetailEntities = soDetailService.listSoDetailByIds(orderDetailIds);
        viewDTO.setApproveStatusName(ApproveStatusEnum.getName(viewDTO.getApproveStatus()));
        viewDTO.setInvalidStatusName(InvalidStatusEnum.getName(viewDTO.getInvalidStatus()));
        viewDTO.setTypeName(BillTypeEnum.getName(viewDTO.getType()));
        List<CustomerInfoEntity> customerInfoEntities = customerInfoService.list();
        CustomerInfoEntity customerInfoEntity = customerInfoEntities.stream().filter(req -> req.getId().equals(soInfoEntity.getCustomerId())).findFirst().orElse(new CustomerInfoEntity());
        soReturnEntity.setCustomerName(customerInfoEntity.getName());
        List<String> soIds = soDetailEntities.stream().map(SoDetailEntity::getMainId).distinct().collect(Collectors.toList());
        List<SoOutstockDetailEntity> soOutstockDetailEntities = soOutstockFeign.listDetailBySoIds(soIds);
        CustomerAddressEntity customerAddressEntity = customerAddressService.getById(soInfoEntity.getReceiveAddressId());
        if (ObjectUtil.isNotEmpty(customerAddressEntity)) {
            viewDTO.setReceiveAddress(customerAddressEntity.getAddress());
        }
        for (SoReturnDetailEntity detailEntity : detailEntityList) {
            SoReturnDetailDTO.View detailView = new SoReturnDetailDTO.View();
            BeanMapperUtils.copy(detailEntity, detailView);
            ProductDetailEntity productDetailEntity = productDetailEntitys.stream().filter(entityClass -> entityClass.getId().equals(detailEntity.getSkuId())).findFirst().orElse(new ProductDetailEntity());
            SoDetailEntity soDetailEntity = soDetailEntities.stream().filter(detail -> detail.getId().equals(detailEntity.getSourceDetailId())).findFirst().orElse(new SoDetailEntity());
            detailView.setProductName(productDetailEntity.getName());
            detailView.setSalesQty(soDetailEntity.getQty());
            Integer actualQty = soOutstockDetailEntities.stream().filter(detail -> soDetailEntity.getMainId().equals(detail.getSoId()) && detail.getSkuId().equals(soDetailEntity.getSkuId()) && detail.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus())).map(SoOutstockDetailEntity::getActualQty).reduce(MathUtil.ZERO, Integer::sum);
            detailView.setDeliveryQty(actualQty);
            detailView.setUnDeliveryQty(soDetailEntity.getQty() - actualQty);
            detailView.setSalesAmount(soDetailEntity.getAmount());
            detailView.setCurrency(soDetailEntity.getCurrency());
            detailView.setCurrencySymbol(soDetailEntity.getCurrencySymbol());
            detailView.setReturnReasonDictName(ReturnReasonEnum.getName(detailEntity.getReturnReasonDict()));
            detailViewDTOS.add(detailView);
        }

        /*Map<String, SoReturnDetailDTO.View> collect = detailViewDTOS.stream().collect(Collectors.groupingBy(n -> n.getSkuNo(), Collectors.collectingAndThen(Collectors.toList(), m -> {
            int salesQty = m.stream().mapToInt(SoReturnDetailDTO.View::getSalesQty).sum();
            int returnQty = m.stream().mapToInt(SoReturnDetailDTO.View::getReturnQty).sum();
            int deliveryQty = m.stream().mapToInt(SoReturnDetailDTO.View::getDeliveryQty).sum();
            int unDeliveryQty = m.stream().mapToInt(SoReturnDetailDTO.View::getUnDeliveryQty).sum();
            String sodId = m.stream().max(Comparator.comparing(SoReturnDetailDTO.View::getId)).map(SoReturnDetailDTO.View::getId).get();
            SoReturnDetailDTO.View view = new SoReturnDetailDTO.View();
            BeanMapper.copy(m.get(MathUtil.ZERO), view);
            view.setId(sodId);
            view.setSalesQty(salesQty);
            view.setReturnQty(returnQty);
            view.setDeliveryQty(deliveryQty);
            view.setUnDeliveryQty(unDeliveryQty);
            return view;
        })));

        List<SoReturnDetailDTO.View> viewList = new ArrayList<>();
        for (Map.Entry<String, SoReturnDetailDTO.View> stringUpdateDTOEntry : collect.entrySet()) {
            viewList.add(stringUpdateDTOEntry.getValue());
        }*/
        viewDTO.setDetailList(detailViewDTOS);
        return viewDTO;
    }
}
