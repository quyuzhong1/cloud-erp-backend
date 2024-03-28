package com.erp.server.tms.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.utils.RedisUtil;
import com.common.business.vo.PagingVO;
import com.common.core.constant.EnumMessage;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.date.DateUtil;
import com.erp.model.sys.entity.SysAccountingCompanyEntity;
import com.erp.model.tms.dto.DictBasicDTO;
import com.erp.model.tms.dto.TmsDeclareBillDTO;
import com.erp.model.tms.entity.*;
import com.erp.model.tms.enums.DeclareStatusEnum;
import com.erp.model.tms.enums.DictBasicEnum;
import com.erp.model.wms.dto.FirstMileDeliveryDTO;
import com.erp.model.wms.enums.FmDeliveryDeclareStatusEnum;
import com.erp.model.wms.enums.LogisticsMethodEnum;
import com.erp.model.wms.enums.PackingStatusEnum;
import com.erp.rpc.sys.feign.SysDictFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.WmsFirstMileDeliveryFeign;
import com.erp.server.tms.mapper.TmsDeclareBillMapper;
import com.erp.server.tms.service.*;
import com.google.common.collect.Lists;
import freemarker.template.utility.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 报关单 服务实现类
 * </p>
 *
 * @author lrp
 * @since 2024-03-27
 */
@Slf4j
@Service
public class TmsDeclareBillServiceImpl extends SuperServiceImpl<TmsDeclareBillMapper, TmsDeclareBillEntity> implements TmsDeclareBillService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;

    /**
     * 限制报关单最多sku
     */
    private final int limitSkuNo = 50;

    @Resource
    private WmsFirstMileDeliveryFeign wmsFirstMileDeliveryFeign;

    @Resource
    private TmsFirstMileLogisticService logisticService;

    @Resource
    private LogisticsSupplierService logisticsSupplierService;

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private TmsDeclareBillDetailService detailService;

    @Resource
    @Lazy
    private TmsDeclareBillServiceImpl service;

    @Resource
    private DictBasicService dictBasicService;

    @Resource
    private SysUserFeign sysUserFeign;

    @Override
    public Boolean addFmDeclare(TmsDeclareBillDTO.AddDTO addDTO) {
        TmsDeclareBillDTO.QuerySourceDTO querySourceDTO = TmsDeclareBillDTO.QuerySourceDTO.builder()
                .packingStatus(PackingStatusEnum.PACKING.getCode())
                .declareStatus(FmDeliveryDeclareStatusEnum.WAIT.getCode())
                .ids(Arrays.asList(addDTO.getSourceId()))
                .build();
        List<TmsDeclareBillDTO.DeliveryDTO> deliveryDTOList = wmsFirstMileDeliveryFeign.getCanGenerateDeclare(querySourceDTO);
        if(CollectionUtils.isEmpty(deliveryDTOList)){
            throw new ServiceException("没有可生成报关单的发货单");
        }
        TmsDeclareBillDTO.DeliveryDTO deliveryDTO = deliveryDTOList.get(0);
        TmsDeclareBillEntity baseTmsDeclareBillEntity = new TmsDeclareBillEntity();
        BeanMapperUtils.copy(addDTO, baseTmsDeclareBillEntity);
        BeanMapperUtils.copy(deliveryDTO, baseTmsDeclareBillEntity);
        baseTmsDeclareBillEntity.setSourceType(SourceTypeEnum.FIRST_MILE_DELIVERY.getCode());
        baseTmsDeclareBillEntity.setDeclareStatus(DeclareStatusEnum.WAIT.getCode());
        baseTmsDeclareBillEntity.setType(SourceTypeEnum.FM_DECLARE_BILL.getCode());
        //50个明细为一个报关单
        List<TmsDeclareBillDTO.ProductDetail> allProductDetailList = deliveryDTO.getProductDetailList();
        if(CollectionUtils.isEmpty(allProductDetailList)){
            throw new ServiceException("没有可生成报关单的明细");
        }
        List<List<TmsDeclareBillDTO.ProductDetail>> productDetailListList = Lists.partition(allProductDetailList, limitSkuNo);
        for(List<TmsDeclareBillDTO.ProductDetail> productDetailList : productDetailListList){
            TmsDeclareBillEntity tmsDeclareBillEntity = BeanUtil.copyProperties(baseTmsDeclareBillEntity,TmsDeclareBillEntity.class);
            List<TmsDeclareBillDetailEntity> detailEntityList = BeanUtil.copyToList(productDetailList,TmsDeclareBillDetailEntity.class);
            service.add(tmsDeclareBillEntity,detailEntityList,SourceTypeEnum.FIRST_MILE_DELIVERY);
        }
        //更新发货单的报关状态
        FirstMileDeliveryDTO.UpdateStatusDTO dto = new FirstMileDeliveryDTO.UpdateStatusDTO();
        dto.setIds(Arrays.asList(addDTO.getSourceId()));
        dto.setDeclareStatus(FmDeliveryDeclareStatusEnum.FINISH.getCode());
        wmsFirstMileDeliveryFeign.updateStatus(dto);
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    public BaseResultDTO.AddDTO add(TmsDeclareBillEntity tmsDeclareBillEntity,List<TmsDeclareBillDetailEntity> detailEntityList,SourceTypeEnum sourceTypeEnum) {
        //生成合同号
        String code = this.generateContractCode(sourceTypeEnum);
        tmsDeclareBillEntity.setCode(code);

        log.info("开始新增报关单");
        boolean save = super.save(tmsDeclareBillEntity);
        if(!save) {
            throw new ServiceException("报关单保存失败");
        }
        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】合同号为【{}】", commonService.getUserInfo().getUserName(), "报关单" , tmsDeclareBillEntity.getCode());
        operateLogService.addModuleOperateLog(msg, sourceTypeEnum.getCode(), tmsDeclareBillEntity.getId(), "新增操作");
        detailService.add(tmsDeclareBillEntity,detailEntityList);
        return new BaseResultDTO.AddDTO(tmsDeclareBillEntity.getId(), code);
    }

    private String generateContractCode(SourceTypeEnum sourceTypeEnum) {
        String key = StrUtil.format(RedisCacheConstants.TMS_DECLARE_CODE,sourceTypeEnum.getCode(), DateUtil.currentYMD());
        Object value = redisUtil.get(key);
        int number;
        if(value == null) {
            number = 1;
        }else{
            number = (Integer) value;
            number++;
        }
        redisUtil.set(key,number,86400);
        return StrUtil.format("{}{}{}", "HT", DateUtil.currentYMD(), StringUtil.leftPad(String.valueOf(number), 3, "0"));
    }
    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(TmsDeclareBillDTO.UpdateDTO updateDTO) {
        TmsDeclareBillEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "报关单"));
        TmsDeclareBillEntity tmsDeclareBillEntity =  BeanMapperUtils.map(TmsDeclareBillEntity.class, updateDTO);
        if(!old.getDeclareStatus().equals(DeclareStatusEnum.WAIT.getCode())){
            throw new ServiceException("报关单状态不是待报关，不能编辑");
        }
        boolean save = super.updateById(tmsDeclareBillEntity);
        if(!save) {
            throw new ServiceException("报关单保存失败");
        }
        log.info("编辑 开始记录报关单日志数据，单号：【{}】", tmsDeclareBillEntity.getCode());
        String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), tmsDeclareBillEntity.getCode(), "报关单");
        operateLogService.addModuleOperateLogByObj(old, tmsDeclareBillEntity, SourceTypeEnum.FM_DECLARE_BILL.getCode(), tmsDeclareBillEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public List<TmsDeclareBillDTO.TabListDTO> tabList(SourceTypeEnum sourceTypeEnum) {
        List<TmsDeclareBillDTO.TabListDTO> tabList = baseMapper.tabList(sourceTypeEnum.getCode());
        List<TmsDeclareBillDTO.TabListDTO> result = new ArrayList<>();
        for(DeclareStatusEnum statusEnum : DeclareStatusEnum.values()){
            TmsDeclareBillDTO.TabListDTO tabListDTO = new TmsDeclareBillDTO.TabListDTO();
            tabListDTO.setTabFlag(statusEnum.getCode());
            tabListDTO.setTabFlagName(statusEnum.getName());
            TmsDeclareBillDTO.TabListDTO queryResult = tabList.stream().filter(v->v.getTabFlag().equals(tabListDTO.getTabFlag())).findFirst().orElse(new TmsDeclareBillDTO.TabListDTO());
            tabListDTO.setCount(queryResult.getCount());
            result.add(tabListDTO);
        }
        return result;
    }

    @Override
    public PagingVO<TmsDeclareBillDTO.PagingVO> paging(PagingDTO<TmsDeclareBillDTO.PagingParamDTO> dto) {
        TmsDeclareBillDTO.PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage<TmsDeclareBillDTO.PagingVO> pageData = baseMapper.paging(query, params);
        List<TmsDeclareBillDTO.PagingVO> list = pageData.getRecords();
        fillPagingDb(list,params.getType());
        return new PagingVO<>(pageData);
    }

    @Override
    public TmsDeclareBillDTO.StatisticsVO statisticsByFm() {
        TmsDeclareBillDTO.StatisticsVO statisticsVO = new TmsDeclareBillDTO.StatisticsVO();
        List<TmsDeclareBillDTO.StatisticsAllDTO> statisticsAllDTOList = this.baseMapper.statistics(TmsDeclareBillDTO.StatisticsDTO.builder()
                        .beginDate(DateUtil.getStartOfMonth(-1))
                        .endDate(DateUtil.getEndOfMonth(0))
                        .declareStatus(DeclareStatusEnum.DECLARED.getCode())
                        .type(SourceTypeEnum.FM_DECLARE_BILL.getCode())
                .build());
        FirstMileDeliveryDTO.StatisticsReq deliveryStaticsReq = new FirstMileDeliveryDTO.StatisticsReq();
        deliveryStaticsReq.setStatus(ApproveStatusEnum.APPROVE.getStatus());
        deliveryStaticsReq.setBeginDate(DateUtil.getStartOfMonth(-1));
        deliveryStaticsReq.setEndDate(DateUtil.getEndOfMonth(0));
        List<FirstMileDeliveryDTO.LogisticStatisticsDTO> deliveryLogisticDTOList = wmsFirstMileDeliveryFeign.logisticStatistics(deliveryStaticsReq);
        statisticsVO.setLastMonthDelivery(!deliveryLogisticDTOList.isEmpty() ?deliveryLogisticDTOList.get(0).getCount():0);
        statisticsVO.setThisMonthDelivery(deliveryLogisticDTOList.size()>1?deliveryLogisticDTOList.get(1).getCount():0);

        statisticsVO.setLastMonthDeclare(!statisticsAllDTOList.isEmpty() ?statisticsAllDTOList.get(0).getCount():0);
        statisticsVO.setThisMonthDeclare(statisticsAllDTOList.size()>1?statisticsAllDTOList.get(1).getCount():0);
        return statisticsVO;
    }

    private void fillPagingDb(List<TmsDeclareBillDTO.PagingVO> list,String type) {
        if(CollectionUtils.isEmpty(list)){
            return;
        }
        List<DictBasicDTO.ViewDTO> declareTypeDict = dictBasicService.getByKey(DictBasicEnum.DECLARE_DECLARE_TYPE.getType());
        List<String> sourceCodes = list.stream().map(TmsDeclareBillDTO.PagingVO::getSourceCode).collect(Collectors.toList());
        List<String> ids = list.stream().map(TmsDeclareBillDTO.PagingVO::getId).collect(Collectors.toList());
        List<TmsDeclareBillDTO.MergedDTO> mergedDTOList = baseMapper.getMergedDTOList(ids);
        Map<String, List<String>> mergedMap = mergedDTOList.stream()
                .collect(Collectors.groupingBy(TmsDeclareBillDTO.MergedDTO::getId, Collectors.mapping(TmsDeclareBillDTO.MergedDTO::getSourceCode, Collectors.toList())));
        if(type.equals(SourceTypeEnum.FM_DECLARE_BILL.getCode())){
            //处理供应商
            List<LogisticsBillEntity> logisticsList =  logisticService.listByOutstcockCode(sourceCodes);
            List<String> supplierIds = logisticsList.stream().map(LogisticsBillEntity::getLogisticsSupplierId).distinct().collect(Collectors.toList());
            List<LogisticsSupplierEntity> supplierList = CollectionUtils.isNotEmpty(supplierIds)?logisticsSupplierService.listByIds(supplierIds):new ArrayList<>();
            list.forEach(v->{
                //供应商
                LogisticsBillEntity logistics = logisticsList.stream().filter(e->e.getOutstockCode().equals(v.getSourceCode())).findFirst().orElse(null);
                if(logistics!=null){
                    LogisticsSupplierEntity supplier = supplierList.stream().filter(e->e.getId().equals(logistics.getLogisticsSupplierId())).findFirst().orElse(new LogisticsSupplierEntity());
                    v.setLogisticsSupplierId(supplier.getSupplierId());
                    v.setLogisticsSupplierName(supplier.getSupplierName());
                }
                //发货类型
                v.setBusinessTypeName(SourceTypeEnum.getName(v.getBusinessType()));
            });
        }else if (type.equals(SourceTypeEnum.B2B_DECLARE_BILL.getCode())){

        }

        list.forEach(v->{
            v.setDeclareStatusName(EnumMessage.getNameByCode(DeclareStatusEnum.class,v.getDeclareStatus()));
            DictBasicDTO.ViewDTO declareType = declareTypeDict.stream().filter(e->e.getCode().equals(v.getDeclareType())).findFirst().orElse(new DictBasicDTO.ViewDTO());
            v.setDeclareTypeName(declareType.getName());
            List<String> sourceCodeList = new ArrayList<>();
            sourceCodeList.add(v.getSourceCode());
            List<String> mergedSourceCodeList = mergedMap.getOrDefault(v.getId(),new ArrayList<>());
            sourceCodeList.addAll(mergedSourceCodeList);
            v.setSourceCodeList(sourceCodeList);
        });
    }

    @Override
    public List<TmsDeclareBillDTO.DeliveryDTO> getCanGenerateDeliveryOrder(TmsDeclareBillDTO.QuerySourceDTO querySourceDTO) {
        List<TmsDeclareBillDTO.DeliveryDTO> deliveryDTOList = wmsFirstMileDeliveryFeign.getCanGenerateDeclare(querySourceDTO);
        List<String> sourceCodes = deliveryDTOList.stream().map(TmsDeclareBillDTO.DeliveryDTO::getSourceCode).collect(Collectors.toList());
        List<LogisticsBillEntity> logisticsBillEntityList = logisticService.listByOutstcockCode(sourceCodes);
        List<String> supplierIds = logisticsBillEntityList.stream().map(LogisticsBillEntity::getLogisticsSupplierId).distinct().collect(Collectors.toList());
        List<LogisticsSupplierEntity> logisticsSupplierEntityList = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(supplierIds)){
            logisticsSupplierEntityList = logisticsSupplierService.listByIds(supplierIds);
        }
        for (TmsDeclareBillDTO.DeliveryDTO deliveryDTO : deliveryDTOList) {
            LogisticsBillEntity logisticsBillEntity = logisticsBillEntityList.stream().filter(v->v.getOutstockId().equals(deliveryDTO.getSourceId())).findFirst().orElse(new LogisticsBillEntity());
            LogisticsSupplierEntity logisticsSupplierEntity = logisticsSupplierEntityList.stream().filter(v->v.getId().equals(logisticsBillEntity.getLogisticsSupplierId())).findFirst().orElse(new LogisticsSupplierEntity());
            deliveryDTO.setShippingMethod(logisticsBillEntity.getShippingMethod());
            deliveryDTO.setShippingMethodName(LogisticsMethodEnum.getName(logisticsBillEntity.getShippingMethod()));
            deliveryDTO.setLogisticsSupplierId(logisticsBillEntity.getLogisticsSupplierId());
            deliveryDTO.setLogisticsSupplierName(logisticsSupplierEntity.getSupplierName());
            deliveryDTO.setCounterNo(logisticsBillEntity.getCounterNo());
        }
        return deliveryDTOList;
    }

    @Override
    public TmsDeclareBillDTO.ViewDTO view(String id) {
        TmsDeclareBillEntity entity = this.getById(id);
        Optional.ofNullable(entity).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST, "头程报关单"));
        TmsDeclareBillDTO.ViewDTO viewDTO = BeanUtil.copyProperties(entity,TmsDeclareBillDTO.ViewDTO.class);
        List<TmsDeclareBillDetailEntity> detailEntityList = detailService.listByMainIds(Arrays.asList(entity.getId()));
        List<TmsDeclareBillDTO.ProductDetail> productDetailList = BeanUtil.copyToList(detailEntityList,TmsDeclareBillDTO.ProductDetail.class);
        productDetailList.forEach(v->v.setTotalPrice(v.getPrice().multiply(new BigDecimal(v.getQty()))));
        viewDTO.setProductDetailList(productDetailList);
        List<TmsDeclareBillDTO.DeliveryDTO> deliveryDTOList = this.getCanGenerateDeliveryOrder(TmsDeclareBillDTO.QuerySourceDTO.builder().ids(Arrays.asList(entity.getSourceId())).build());
        if(CollectionUtils.isEmpty(deliveryDTOList)){
            throw new ServiceException("未找到发货单信息");
        }
        TmsDeclareBillDTO.DeliveryDTO deliveryDTO = deliveryDTOList.get(0);
        BeanUtil.copyProperties(deliveryDTO,viewDTO, CopyOptions.create().setOverride(false));
        fillViewDTO(viewDTO);
        return viewDTO;
    }

    private void fillViewDTO(TmsDeclareBillDTO.ViewDTO viewDTO) {
        //处理字典值
        List<DictBasicEntity> dictBasicEntityList = dictBasicService.getByKeyList(Arrays.asList(DictBasicEnum.DECLARE_DECLARE_TYPE.getType(),
                DictBasicEnum.DECLARE_SUPERVISION_METHOD.getType(),
                DictBasicEnum.DECLARE_NATURE_LEVY.getType(),
                DictBasicEnum.DECLARE_PACK_TYPE.getType(),
                DictBasicEnum.DECLARE_TRANSACTION_METHOD.getType()));
        viewDTO.setDeclareTypeName(dictBasicEntityList.stream().filter(v->v.getType().equals(DictBasicEnum.DECLARE_DECLARE_TYPE.getType())&&v.getCode().equals(viewDTO.getDeclareType())).map(DictBasicEntity::getName).findFirst().orElse(""));
        viewDTO.setDictSupervisionMethodName(dictBasicEntityList.stream().filter(v->v.getType().equals(DictBasicEnum.DECLARE_SUPERVISION_METHOD.getType())&&v.getCode().equals(viewDTO.getDictSupervisionMethod())).map(DictBasicEntity::getName).findFirst().orElse(""));
        viewDTO.setDictNatureLevyName(dictBasicEntityList.stream().filter(v->v.getType().equals(DictBasicEnum.DECLARE_NATURE_LEVY.getType())&&v.getCode().equals(viewDTO.getDictNatureLevy())).map(DictBasicEntity::getName).findFirst().orElse(""));
        viewDTO.setDictPackTypeName(dictBasicEntityList.stream().filter(v->v.getType().equals(DictBasicEnum.DECLARE_PACK_TYPE.getType())&&v.getCode().equals(viewDTO.getDictPackType())).map(DictBasicEntity::getName).findFirst().orElse(""));
        viewDTO.setDictTransactionMethodName(dictBasicEntityList.stream().filter(v->v.getType().equals(DictBasicEnum.DECLARE_TRANSACTION_METHOD.getType())&&v.getCode().equals(viewDTO.getDictTransactionMethod())).map(DictBasicEntity::getName).findFirst().orElse(""));
        //处理发货人
        SysAccountingCompanyEntity sysAccountingCompanyEntity = sysUserFeign.getCompanyById(viewDTO.getSenderId());
        if(Objects.nonNull(sysAccountingCompanyEntity)){
            viewDTO.setSenderName(sysAccountingCompanyEntity.getCompanyName());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<BatchResultDTO> updateToDeclare(TmsDeclareBillDTO.UpdateDeclareStatusDTO dto) {
        if(Objects.isNull(dto.getDate())){
            throw new ServiceException("报关日期不能为空");
        }
        List<TmsDeclareBillEntity> entityList = this.listByIds(dto.getIds());
        List<TmsDeclareBillEntity> updateList = new ArrayList<>();
        List<BatchResultDTO> resultList = new ArrayList<>();
        for (TmsDeclareBillEntity entity : entityList) {
            if(!entity.getDeclareStatus().equals(DeclareStatusEnum.WAIT.getCode())){
                resultList.add(BatchResultDTO.fail(entity.getId(),entity.getCode(),"只有待报关的单据才能更新成已报关"));
                continue;
            }
            entity.setDeclareStatus(DeclareStatusEnum.DECLARED.getCode());
            updateList.add(entity);
        }
        if(CollectionUtils.isNotEmpty(updateList)){
            this.updateBatchById(updateList);
        }
        return resultList;
    }

    @Override
    public List<BatchResultDTO> cancelDeclare(TmsDeclareBillDTO.UpdateDeclareStatusDTO dto) {
        return null;
    }

    @Override
    public List<BatchResultDTO> mergeDeclare(TmsDeclareBillDTO.MergeDeclareDTO dto) {
        return null;
    }

    @Override
    public List<BatchResultDTO> cancelMerge(TmsDeclareBillDTO.MergeDeclareDTO dto) {
        return null;
    }

    @Override
    public void export(TmsDeclareBillDTO.PagingParamDTO pagingParamDTO, HttpServletResponse response) {

    }

    @Override
    public void exportDeclare(TmsDeclareBillDTO.PagingParamDTO pagingParamDTO, HttpServletResponse response) {

    }

    @Override
    public List<BatchResultDTO> delete(TmsDeclareBillDTO.DeleteDTO dto) {
        return null;
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(TmsDeclareBillEntity tmsDeclareBillEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
