package com.erp.server.tms.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.constant.ThirdConstants;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.DeclarePlatformEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.core.constant.EnumMessage;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.enums.CurrencyEnum;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.plm.dto.LogisticsProductDTO;
import com.erp.model.plm.entity.BasicDictEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.entity.SysPostUserEntity;
import com.erp.model.sys.vo.FsBatchSendMessageDTO;
import com.erp.model.sys.vo.ThirdUnionDTO;
import com.erp.model.tms.dto.CfgSettingValueDTO;
import com.erp.model.tms.dto.ProductRegistrationDTO;
import com.erp.model.tms.dto.SettingForecastDTO;
import com.erp.model.tms.dto.transfer.TransferLogisticsCreateProductReq;
import com.erp.model.tms.entity.CfgSettingEntity;
import com.erp.model.tms.entity.ProductRegistrationEntity;
import com.erp.model.tms.entity.TransferLogisticsAuthEntity;
import com.erp.model.tms.enums.CfgSettingEnum;
import com.erp.model.tms.enums.ProductRegistrationEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.plm.feign.LogisticsProductFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysPostFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.sdk.fs.service.FsService;
import com.erp.server.tms.convert.ProductRegistrationConverter;
import com.erp.server.tms.handler.TransferLogisticsRegistry;
import com.erp.server.tms.mapper.ProductRegistrationMapper;
import com.erp.server.tms.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_TMS_PRODUCT_REGISTRATION;

/**
 * <p>
 * 产品备案表 服务实现类
 * </p>
 *
 * @author lrp
 * @since 2024-03-14
 */
@Slf4j
@Service
public class ProductRegistrationServiceImpl extends SuperServiceImpl<ProductRegistrationMapper, ProductRegistrationEntity> implements ProductRegistrationService {
    @Autowired
    private OperateLogService operateLogService;

    @Resource
    private LogisticsProductFeign logisticsProductFeign;

    @Resource
    private TransferLogisticsRegistry transferLogisticsRegistry;

    @Resource
    private TransferLogisticsAuthService transferLogisticsAuthService;

    @Resource
    @Lazy
    private ProductRegistrationServiceImpl service;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private CfgSettingService cfgSettingService;

    @Resource
    private SysPostFeign sysPostFeign;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private FsService fsService;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @Value("${third.fs.appUrl}")
    private String fsAppUrl;

    @Override
    public List<BatchResultDTO> add(ProductRegistrationDTO.AddDTO addDTO) {
        if(CollectionUtils.isEmpty(addDTO.getSkuIds())){
            throw new ServiceException("sku不能为空");
        }
        List<BatchResultDTO> resultDTOList = new ArrayList<>();
        //查询授权信息
        TransferLogisticsAuthEntity transferLogisticsAuthEntity = transferLogisticsAuthService.getByMainId("",addDTO.getDeclareSupplierId());
        if(Objects.isNull(transferLogisticsAuthEntity)){
            throw new ServiceException("授权信息为空");
        }
        TransferLogisticsService transferLogisticsService = transferLogisticsRegistry.getHandler(transferLogisticsAuthEntity.getLogisticsPlatform());
        if(Objects.isNull(transferLogisticsService)){
            throw new ServiceException("未开发平台");
        }
        List<String> skuIds = addDTO.getSkuIds();
        List<ProductRegistrationEntity> entities = this.listBySkuListAndPlatform(skuIds,addDTO.getDeclareSupplierId());
        Set<String> existSkuIds = entities.stream().map(ProductRegistrationEntity::getSkuId).collect(Collectors.toSet());
        //过滤掉备案表存在的sku id
        List<String> noExistsSkuList = skuIds.stream().filter(v->!existSkuIds.contains(v)).collect(Collectors.toList());
        List<ProductRegistrationEntity> addList = new ArrayList<>();
        //产品信息
        List<LogisticsProductDTO.ProductDTO> productDTOList = logisticsProductFeign.listLogisticsProduct(noExistsSkuList);
        for(String skuId : skuIds){
            ProductRegistrationEntity entity = entities.stream().filter(v->v.getSkuId().equals(skuId)).findFirst().orElse(null);
            if(Objects.nonNull(entity)){
                resultDTOList.add(BatchResultDTO.fail(entity.getSkuId(),entity.getSkuNo(),StrUtil.format("SKU已在{}已获取备案信息，无法重复获取",transferLogisticsService.getPlatForm().getName())));
                continue;
            }
            LogisticsProductDTO.ProductDTO productDTO = productDTOList.stream().filter(v->v.getSkuId().equals(skuId)).findFirst().orElse(null);
            if(Objects.isNull(productDTO)){
                resultDTOList.add(BatchResultDTO.fail(skuId,skuId,"查询不到物流产品备案信息"));
                continue;
            }
            TransferLogisticsCreateProductReq createProductReq = ProductRegistrationConverter.INSTANCE.convertToCreateProduct(productDTO);
            //备案产品
            ApiResult<String> result;
            try {
                result = transferLogisticsService.createProduct(createProductReq,transferLogisticsAuthEntity.getId());
            }catch (ConstraintViolationException violationException){
                result = ApiResult.error(500,violationException.getConstraintViolations().stream().map(ConstraintViolation::getMessage).collect(Collectors.toList()).toString());
            }
            if(result.isSuccess()){
                //备案成功，查询产品信息回写表
                ApiResult<ProductRegistrationEntity> queryResult = transferLogisticsService.getProductBySku(productDTO.getSkuNo(),transferLogisticsAuthEntity.getId());
                if(queryResult.isSuccess()){
                    ProductRegistrationEntity addEntity = queryResult.getData();
                    //设置推送信息
                    Map<String, Object> pushMap = BeanUtil.beanToMap(productDTO);
                    Map<String, Object> pullMap = BeanUtil.beanToMap(queryResult.getData());
                    addEntity.setSkuId(skuId);
                    addEntity.setLatestTime(LocalDateTime.now());
                    addEntity.setDeclareSupplierId(transferLogisticsAuthEntity.getMainId());
                    addEntity.setDeclareSupplierName(transferLogisticsAuthEntity.getName());
                    addEntity.setPushInfo(pushMap);
                    addEntity.setPullInfo(pullMap);
                    ProductRegistrationEntity erpEntity = ProductRegistrationConverter.INSTANCE.convertToEntity(productDTO);
                    BeanUtil.copyProperties(erpEntity,addEntity, CopyOptions.create().setIgnoreNullValue(true));
                    addList.add(addEntity);
                }else{
                    //失败返回原因
                    resultDTOList.add(BatchResultDTO.fail(skuId,productDTO.getSkuNo(),"备案产品成功，但在拉取产品信息时失败，请点击拉取备案拉取产品。"+queryResult.getMsg()));
                }
            }else{
                //失败返回原因
                if(result.getMsg().contains("sku已存在")){
                    result.setMsg(result.getMsg()+",请通过拉取备案拉取产品");
                }
                resultDTOList.add(BatchResultDTO.fail(skuId,productDTO.getSkuNo(),result.getMsg()));
            }
        }

        if(CollectionUtils.isNotEmpty(addList)){
            this.save(addList.get(0));
        }
        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】sku为【%s】", UserContext.getDefaultLoginUser().getUserName(), "产品备案信息");
        List<Pair<String, String>> pairList = addList.stream().map(obj -> new Pair<>(obj.getId(), obj.getSkuNo())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog(msg, ModuleTypeEnum.PRODUCT_REGISTRATION.getCode(), pairList, "新增操作");
        return resultDTOList;
    }

    @Override
    public List<ProductRegistrationEntity> listBySkuListAndPlatform(List<String> skuIdList,String declareSupplierId) {
        if (CollectionUtils.isEmpty(skuIdList)) {
            return Collections.emptyList();
        }
        return this.lambdaQuery().in(ProductRegistrationEntity::getSkuId, skuIdList).eq(ProductRegistrationEntity::getDeclareSupplierId,declareSupplierId).list();
    }

    @Override
    public List<ProductRegistrationEntity> listBySkuNoListAndPlatform(List<String> skuNoList,String declareSupplierId) {
        if (CollectionUtils.isEmpty(skuNoList)) {
            return this.lambdaQuery().eq(ProductRegistrationEntity::getDeclareSupplierId,declareSupplierId).list();
        }
        return this.lambdaQuery().in(ProductRegistrationEntity::getSkuNo, skuNoList).eq(ProductRegistrationEntity::getDeclareSupplierId,declareSupplierId).list();
    }

    /**
     * 查询是否备案 获取未备案的skuNo
     *
     * @param dto
     * @return
     */
    @Override
    public List<String> listNotRegistrationByParam(SettingForecastDTO.CheckRegistrationDTO dto) {
        String declarePlatform = dto.getDeclarePlatform();
        String registered = ProductRegistrationEnum.StatusEnum.REGISTERED.getCode();
        List<String> skuNoList = dto.getSkuNoList();
        List<ProductRegistrationEntity> dbList=this.lambdaQuery().
                eq(ProductRegistrationEntity::getDeclarePlatform,declarePlatform).
                eq(ProductRegistrationEntity::getStatus,registered).
                in(ProductRegistrationEntity::getSkuNo,skuNoList).list();
        //这个是查询到的
        List<String> dbSkuNoList = dbList.stream().map(ProductRegistrationEntity::getSkuNo).collect(Collectors.toList());

        return skuNoList.stream().filter(s->!dbSkuNoList.contains(s)).collect(Collectors.toList());
    }

    @Override
    public List<ProductRegistrationDTO.TabListDTO> tabList() {
        List<ProductRegistrationDTO.TabListDTO> tabListDTOList = baseMapper.tabList();
        List<ProductRegistrationDTO.TabListDTO> result = new ArrayList<>();
        for (ProductRegistrationEnum.TabEnum tabEnum : ProductRegistrationEnum.TabEnum.values()) {
            ProductRegistrationDTO.TabListDTO tabListDTO = tabListDTOList.stream().filter(v->v.getTabFlag().equals(tabEnum.getCode())).findFirst().orElse(null);
            if(Objects.isNull(tabListDTO)){
                tabListDTO = new ProductRegistrationDTO.TabListDTO();
                tabListDTO.setTabFlag(tabEnum.getCode());
                tabListDTO.setTabFlagName(tabEnum.getName());
                tabListDTO.setCount(0);
            }
            result.add(tabListDTO);
        }
        return result;
    }

    @Override
    public PagingVO<ProductRegistrationDTO.PagingVO> paging(PagingDTO<ProductRegistrationDTO.PagingParamDTO> dto) {
        ProductRegistrationDTO.PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage<ProductRegistrationDTO.PagingVO> pageData = baseMapper.paging(query, params);
        List<ProductRegistrationDTO.PagingVO> list = pageData.getRecords();
        fillPagingDb(list);
        return new PagingVO<>(pageData);
    }

    @Override
    public ProductRegistrationDTO.ViewVO view(String id) {
        ProductRegistrationEntity old = super.getById(id);
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "产品备案单"));

        ProductRegistrationDTO.ViewVO view = BeanMapperUtils.map(ProductRegistrationDTO.ViewVO.class, old);
        view.setDeclarePlatformName(old.getDeclareSupplierName());
        view.setStatusName(EnumMessage.getNameByCode(ProductRegistrationEnum.StatusEnum.class,view.getStatus()));
        //处理单位
        List<BasicDictEntity> sysDictBasicEntityList = plmTaskFeign.listDictByType("declareUnit");
        BasicDictEntity basicDictEntity = sysDictBasicEntityList.stream().filter(v->v.getValue().equals(old.getDeclareUnit())).findFirst().orElse(null);
        if(basicDictEntity!=null){
            old.setDeclareUnit(basicDictEntity.getName());
        }

        JSONObject jsonObject = new JSONObject(old.getPushInfo());
        LogisticsProductDTO.ProductDTO ruleDTO = JSONObject.parseObject(jsonObject.toJSONString(),new TypeReference< LogisticsProductDTO.ProductDTO>() {}.getType());

        JSONObject pullJson = new JSONObject(old.getPullInfo());
        ProductRegistrationEntity pullEntity = JSONObject.parseObject(pullJson.toJSONString(),new TypeReference<ProductRegistrationEntity>() {}.getType());


        //最新产品信息
        List<LogisticsProductDTO.ProductDTO> productDTOList = logisticsProductFeign.listLogisticsProduct(Arrays.asList(old.getSkuId()));
        LogisticsProductDTO.ProductDTO latestDTO = productDTOList.stream().findFirst().orElse(new LogisticsProductDTO.ProductDTO());
        view.setDetailList(ProductRegistrationEnum.DetailDescEnum.convertToViewList(ruleDTO,pullEntity,latestDTO));
        return view;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<BatchResultDTO> cancel(List<String> ids) {
        List<ProductRegistrationEntity> list = this.listByIds(ids);
        List<ProductRegistrationEntity> updateList = new ArrayList<>();
        List<BatchResultDTO> resultDTOList = new ArrayList<>();
        for(ProductRegistrationEntity entity : list){
            if(!entity.getStatus().equals(ProductRegistrationEnum.StatusEnum.REGISTERED.getCode())){
                resultDTOList.add(BatchResultDTO.fail(entity.getId(),entity.getSkuNo(),"仅可操作已备案的状态"));
                continue;
            }
            entity.setStatus(ProductRegistrationEnum.StatusEnum.CANCEL.getCode());
            updateList.add(entity);
        }
        if(CollectionUtils.isNotEmpty(updateList)){
            this.updateBatchById(updateList);
        }
        return resultDTOList;
    }

    @Override
    public List<BatchResultDTO> pull(ProductRegistrationDTO.AddDTO dto) {
        List<String> skuIdList = dto.getSkuIds();
        List<BatchResultDTO> resultDTOList = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(skuIdList)){
            //查询授权信息
            TransferLogisticsAuthEntity transferLogisticsAuthEntity = transferLogisticsAuthService.getByMainId("",dto.getDeclareSupplierId());
            if(Objects.isNull(transferLogisticsAuthEntity)){
                throw new ServiceException("授权信息为空");
            }
            TransferLogisticsService transferLogisticsService = transferLogisticsRegistry.getHandler(transferLogisticsAuthEntity.getLogisticsPlatform());
            if(Objects.isNull(transferLogisticsService)){
                throw new ServiceException("未开发平台");
            }
            List<LogisticsProductDTO.ProductDTO> productDTOList = logisticsProductFeign.listLogisticsProduct(skuIdList);
            List<ProductRegistrationEntity> entities = this.listBySkuListAndPlatform(skuIdList,dto.getDeclareSupplierId());
            List<ProductRegistrationEntity> addList = new ArrayList<>();
            List<ProductRegistrationEntity> updateList = new ArrayList<>();
            for(String skuId : skuIdList){
                LogisticsProductDTO.ProductDTO productDTO = productDTOList.stream().filter(v->v.getSkuId().equals(skuId)).findFirst().orElse(null);
                if(Objects.isNull(productDTO)){
                    resultDTOList.add(BatchResultDTO.fail(skuId,skuId,"查询不到物流产品备案信息"));
                    continue;
                }
                ProductRegistrationEntity productRegistrationEntity = entities.stream().filter(v->v.getSkuId().equals(skuId)).findFirst().orElse(null);
                //查询产品信息
                ApiResult<ProductRegistrationEntity> queryResult = transferLogisticsService.getProductBySku(productDTO.getSkuNo(),transferLogisticsAuthEntity.getId());
                if(queryResult.isSuccess()){
                    ProductRegistrationEntity addEntity = queryResult.getData();
                    Map<String, Object> pullMap = BeanUtil.beanToMap(queryResult.getData());
                    addEntity.setLatestTime(LocalDateTime.now());
                    if(addEntity.getStatus().equals(ProductRegistrationEnum.StatusEnum.REGISTERED.getCode()) && !judgeEquals(addEntity,productDTO)){
                        addEntity.setStatus(ProductRegistrationEnum.StatusEnum.CANCEL.getCode());
                        addEntity.setFailureReason("报关信息与数大臣ERP不一致，请核实修改");
                    }
                    if(Objects.isNull(productRegistrationEntity)){
                        addEntity.setSkuId(skuId);
                        addEntity.setDeclareSupplierId(transferLogisticsAuthEntity.getMainId());
                        addEntity.setDeclareSupplierName(transferLogisticsAuthEntity.getName());
                        addEntity.setDeclareCurrencySymbol(productDTO.getDeclareCurrencySymbol());
                        ProductRegistrationEntity erpEntity = ProductRegistrationConverter.INSTANCE.convertToEntity(productDTO);
                        BeanUtil.copyProperties(erpEntity,addEntity, CopyOptions.create().setIgnoreNullValue(true));
                        addEntity.setPullInfo(pullMap);
                        addList.add(addEntity);
                    }else{
                        productRegistrationEntity.setDeclareCurrencySymbol(productDTO.getDeclareCurrencySymbol());
                        BeanUtil.copyProperties(addEntity,productRegistrationEntity, CopyOptions.create().setIgnoreNullValue(true));
                        ProductRegistrationEntity erpEntity = ProductRegistrationConverter.INSTANCE.convertToEntity(productDTO);
                        BeanUtil.copyProperties(erpEntity,productRegistrationEntity, CopyOptions.create().setIgnoreNullValue(true));
                        productRegistrationEntity.setPullInfo(pullMap);
                        updateList.add(productRegistrationEntity);
                    }
                }else{
                    //失败返回原因
                    resultDTOList.add(BatchResultDTO.fail(skuId,productDTO.getSkuNo(),"拉取产品信息时失败"+queryResult.getMsg()));
                }
            }
            List<ProductRegistrationEntity> sendMsgList = new ArrayList<>();
            if(CollectionUtils.isNotEmpty(addList)){
                service.saveBatch(addList);
                sendMsgList.addAll(addList);
            }
            if(CollectionUtils.isNotEmpty(updateList)){
                service.updateBatchById(updateList);
                sendMsgList.addAll(updateList);
            }
            sendMsgList = sendMsgList.stream().filter(v->!v.getStatus().equals(ProductRegistrationEnum.StatusEnum.REGISTERED.getCode())).collect(Collectors.toList());
            service.sendMsgWhenNotRegistration(sendMsgList);
        }else{
            //没有传skuId 则全量拉取
            this.pullAllProduct(dto.getDeclareSupplierId());
        }
        return resultDTOList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<BatchResultDTO> delete(List<String> ids) {
        List<ProductRegistrationEntity> list = this.listByIds(ids);
        List<String> removeList = new ArrayList<>();
        List<BatchResultDTO> resultDTOList = new ArrayList<>();
        for(ProductRegistrationEntity entity : list){
            if(!entity.getStatus().equals(ProductRegistrationEnum.StatusEnum.DRAFT.getCode())){
                resultDTOList.add(BatchResultDTO.fail(entity.getId(),entity.getSkuNo(),"仅支持备案不通过的状态："));
                continue;
            }
            removeList.add(entity.getId());
        }
        if(CollectionUtils.isNotEmpty(removeList)){
            this.removeByIds(removeList);
        }
        return resultDTOList;
    }

    @Override
    public void export(ProductRegistrationDTO.PagingParamDTO dto) {
        downloadTaskFeign.saveDownloadTask("备案列表", EXPORT_TMS_PRODUCT_REGISTRATION.getCode(), dto);
    }

    @Override
    public ApiResult pullAllProduct(String declareSupplierId) {
        if(StringUtils.isBlank(declareSupplierId)){
            return ApiResult.error(500,"平台商Id不能为空");
        }
        //查询授权信息
        TransferLogisticsAuthEntity transferLogisticsAuthEntity = transferLogisticsAuthService.getByMainId("",declareSupplierId);
        if(Objects.isNull(transferLogisticsAuthEntity)){
            throw new ServiceException("授权信息为空");
        }
        TransferLogisticsService transferLogisticsService = transferLogisticsRegistry.getHandler(transferLogisticsAuthEntity.getLogisticsPlatform());
        if(Objects.isNull(transferLogisticsService)){
            throw new ServiceException("未开发平台");
        }
        //拉取第三方平台备案产品
        ApiResult<List<ProductRegistrationEntity>> apiResult = transferLogisticsService.getAllProductInfo(transferLogisticsAuthEntity.getId());
        if(!apiResult.isSuccess()){
            return apiResult;
        }
        List<ProductRegistrationEntity> pullDataList = apiResult.getData();
        if (CollectionUtils.isEmpty(pullDataList)) {
            return ApiResult.success();
        }
        List<String> skuNoList = pullDataList.stream().map(ProductRegistrationEntity::getSkuNo).collect(Collectors.toList());
        //查询现在已存在的
        List<ProductRegistrationEntity> existEntityList = this.listBySkuNoListAndPlatform(null,declareSupplierId);
        //产品信息
        List<LogisticsProductDTO.ProductDTO> productDTOList = logisticsProductFeign.listBySkuNoList(skuNoList);
        List<ProductRegistrationEntity> addList = new ArrayList<>();
        List<ProductRegistrationEntity> updateList = new ArrayList<>();
        for (ProductRegistrationEntity entity : pullDataList) {
            ProductRegistrationEntity existEntity = existEntityList.stream().filter(v -> v.getSkuNo().equals(entity.getSkuNo())).findFirst().orElse(null);
            LogisticsProductDTO.ProductDTO productDTO = productDTOList.stream().filter(v->v.getSkuNo().equals(entity.getSkuNo())).findFirst().orElse(null);
            if(Objects.isNull(productDTO)){
                continue;
            }
            if((Objects.nonNull(existEntity) && (StringUtils.isBlank(existEntity.getProductName()) ||StringUtils.isBlank(existEntity.getDeclareElement())))
             || (Objects.isNull(existEntity)) || !judgeEquals(entity,productDTO)){
                //因为保宏拉取批量接口没有返中文名称和申报要素，通过请求单个的接口获取对应信息
                ApiResult<ProductRegistrationEntity> queryResult = transferLogisticsService.getProductBySku(entity.getSkuNo(),transferLogisticsAuthEntity.getId());
                if(queryResult.isSuccess()){
                    ProductRegistrationEntity addEntity = queryResult.getData();
                    Map<String, Object> pullMap = BeanUtil.beanToMap(queryResult.getData());
                    addEntity.setLatestTime(LocalDateTime.now());
                    addEntity.setDeclareCurrencySymbol(CurrencyEnum.getSymbolByCode(addEntity.getCurrency()));
                    addEntity.setDeclareSupplierId(transferLogisticsAuthEntity.getMainId());
                    addEntity.setDeclareSupplierName(transferLogisticsAuthEntity.getName());
                    addEntity.setSkuId(productDTO.getSkuId());
                    if(addEntity.getStatus().equals(ProductRegistrationEnum.StatusEnum.REGISTERED.getCode()) && !judgeEquals(addEntity,productDTO)){
                        addEntity.setStatus(ProductRegistrationEnum.StatusEnum.CANCEL.getCode());
                        addEntity.setFailureReason("报关信息与数大臣ERP不一致，请核实修改");
                    }
                    if(Objects.isNull(existEntity)){
                        ProductRegistrationEntity erpEntity = ProductRegistrationConverter.INSTANCE.convertToEntity(productDTO);
                        BeanUtil.copyProperties(erpEntity,addEntity, CopyOptions.create().setIgnoreNullValue(true));
                        addEntity.setPullInfo(pullMap);
                        addList.add(addEntity);
                    }else{
                        BeanUtil.copyProperties(addEntity,existEntity, CopyOptions.create().setIgnoreNullValue(true));
                        ProductRegistrationEntity erpEntity = ProductRegistrationConverter.INSTANCE.convertToEntity(productDTO);
                        BeanUtil.copyProperties(erpEntity,existEntity, CopyOptions.create().setIgnoreNullValue(true));
                        existEntity.setPullInfo(pullMap);
                        updateList.add(existEntity);
                    }
                }else{
                    log.error("拉取产品信息时失败"+queryResult.getMsg());
                }
            }else{
                entity.setLatestTime(LocalDateTime.now());
                entity.setDeclareCurrencySymbol(CurrencyEnum.getSymbolByCode(entity.getCurrency()));
                entity.setDeclareSupplierId(transferLogisticsAuthEntity.getMainId());
                entity.setDeclareSupplierName(transferLogisticsAuthEntity.getName());
                entity.setSkuId(productDTO.getSkuId());
                entity.setProductName(existEntity.getProductName());
                entity.setDeclareElement(existEntity.getDeclareElement());
                if(entity.getStatus().equals(ProductRegistrationEnum.StatusEnum.REGISTERED.getCode()) && !judgeEquals(entity,productDTO)){
                    entity.setStatus(ProductRegistrationEnum.StatusEnum.CANCEL.getCode());
                    entity.setFailureReason("报关信息与数大臣ERP不一致，请核实修改");
                }
                BeanUtil.copyProperties(entity, existEntity, CopyOptions.create().setIgnoreNullValue(true));
                ProductRegistrationEntity erpEntity = ProductRegistrationConverter.INSTANCE.convertToEntity(productDTO);
                BeanUtil.copyProperties(erpEntity,existEntity, CopyOptions.create().setIgnoreNullValue(true));
                updateList.add(existEntity);
            }
        }
        //将ERP存在，第三方不存在的SKU备案状态改为取消
        List<ProductRegistrationEntity> needCancelList = existEntityList.stream().filter(v -> !skuNoList.contains(v.getSkuNo()) && v.getStatus().equals(ProductRegistrationEnum.StatusEnum.REGISTERED.getCode())).collect(Collectors.toList());
        needCancelList.forEach(v->{
            v.setStatus(ProductRegistrationEnum.StatusEnum.CANCEL.getCode());
            v.setFailureReason("第三方平台不存在该SKU，请核实修改");
            updateList.add(v);
        });
        this.batchAddOrUpdate(addList,updateList);
        service.sendMsgWhenNotRegistration(null);
        return ApiResult.success();
    }

    @Override
    public List<ProductRegistrationEntity> listBySkuId(String skuId) {
        return lambdaQuery().eq(ProductRegistrationEntity::getSkuId,skuId)
                .list();
    }

    public void batchAddOrUpdate(List<ProductRegistrationEntity> addList,List<ProductRegistrationEntity> updateList){
        if(CollectionUtils.isNotEmpty(addList)){
            List<List<ProductRegistrationEntity>> partAddList =  ListUtil.partition(addList,1000);
            partAddList.forEach(v-> service.saveBatch(v));
        }
        if(CollectionUtils.isNotEmpty(updateList)){
            List<List<ProductRegistrationEntity>> partUpdateList =  ListUtil.partition(updateList,1000);
            partUpdateList.forEach(v-> service.updateBatchById(v));
        }

    }
    private void fillPagingDb(List<ProductRegistrationDTO.PagingVO> list) {
        List<String> skuIdList = list.stream().map(ProductRegistrationDTO.PagingVO::getSkuId).distinct().collect(Collectors.toList());
        List<LogisticsProductDTO.ProductDTO> productDTOList = logisticsProductFeign.listLogisticsProduct(skuIdList);
        list.forEach(v->{
            v.setStatusName(EnumMessage.getNameByCode(ProductRegistrationEnum.StatusEnum.class,v.getStatus()));
            v.setDeclarePlatformName(EnumMessage.getNameByCode(DeclarePlatformEnum.class,v.getDeclarePlatform()));
            //处理报关申报价
            v.setCompletePrice(Objects.isNull(v.getDeclareCurrencySymbol())?"":v.getDeclareCurrencySymbol()+(Objects.isNull(v.getDeclarePrice())?"":v.getDeclarePrice()));
            //处理备案审核状态
            LogisticsProductDTO.ProductDTO productDTO = productDTOList.stream().filter(o->o.getSkuId().equals(v.getSkuId())).findFirst().orElse(new LogisticsProductDTO.ProductDTO());
            if(Objects.nonNull(productDTO.getApproveStatus())){
                v.setRegistrationApproveStatus(productDTO.getApproveStatus());
                v.setRegistrationApproveStatusName(ApproveStatusEnum.getName(productDTO.getApproveStatus()));
            }
        });
    }

    private Boolean judgeEquals(ProductRegistrationEntity addEntity,LogisticsProductDTO.ProductDTO productDTO){
        if(Objects.isNull(addEntity.getGrossWeight())){
            addEntity.setGrossWeight(BigDecimal.ZERO);
        }
        if(Objects.isNull(addEntity.getDeclarePrice())){
            addEntity.setDeclarePrice(BigDecimal.ZERO);
        }
        if(Objects.isNull(productDTO.getDeclarePrice())){
            productDTO.setDeclarePrice(BigDecimal.ZERO);
        }
        return Objects.equals(addEntity.getSkuNo(), productDTO.getSkuNo())
//                Objects.equals(addEntity.getProductName(), productDTO.getCnName()) &&
//                Objects.equals(addEntity.getProductNameEn(), productDTO.getEnName()) &&
//                (Objects.equals(addEntity.getCurrency(), productDTO.getDeclareCurrency()) || (Objects.equals(productDTO.getDeclareCurrency(), "CNY") && Objects.equals(addEntity.getCurrency(), "RMB"))) &&
//                addEntity.getDeclarePrice().compareTo(productDTO.getDeclarePrice()) == 0
//                &&
//                addEntity.getGrossWeight().compareTo(productDTO.getGrossWeight()) == 0 &&
//                Objects.equals(addEntity.getDeclareNameCn(), productDTO.getDeclareChineseName()) &&
//                Objects.equals(addEntity.getCustomsCode(), productDTO.getCustomsCode())
                ;

    }

    @Async
    @Override
    public void sendMsgWhenNotRegistration(List<ProductRegistrationEntity> sendMsgList){
        List<ProductRegistrationEntity> list;
        if(CollectionUtils.isNotEmpty(sendMsgList)){
            list = sendMsgList;
        }else{
            list = this.lambdaQuery().ne(ProductRegistrationEntity::getStatus,ProductRegistrationEnum.StatusEnum.REGISTERED.getCode()).list();
        }
        if(CollectionUtils.isEmpty(list)){
            return;
        }
        CfgSettingEntity cfgSettingEntity = cfgSettingService.getByKey(CfgSettingEnum.NOTIC.getCode());
        if(Objects.isNull(cfgSettingEntity)){
            return;
        }

        CfgSettingValueDTO.NoticeDTO noticeDTO = JSONUtil.toBean(cfgSettingEntity.getDataJson(),CfgSettingValueDTO.NoticeDTO.class);
        if(Objects.isNull(noticeDTO)){
            return;
        }

        List<String> sendUserIds = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(noticeDTO.getProductRegistrationUserIdList())){
            sendUserIds.addAll(noticeDTO.getProductRegistrationUserIdList());
        }
        //处理岗位，获取岗位下全部人
        if(CollectionUtils.isNotEmpty(noticeDTO.getProductRegistrationPostIdList())){
            //岗位id
            List<String> postIdList = noticeDTO.getProductRegistrationPostIdList();
            List<SysPostUserEntity> userEntityList = sysPostFeign.getUserIdByPostIds(postIdList);
            if(CollectionUtils.isNotEmpty(userEntityList)){
                sendUserIds.addAll(userEntityList.stream().map(SysPostUserEntity::getUserId).distinct().collect(Collectors.toList()));
            }
        }

        //没有需要发送的人员
        if(CollectionUtils.isEmpty(sendUserIds)){
            return;
        }
        sendUserIds = sendUserIds.stream().distinct().collect(Collectors.toList());
        //获取飞书的unionid 与用户关系
        List<ThirdUnionDTO> unionIdList = sysUserFeign.getThirdUnionId(ThirdConstants.FS_PLATFORM);
        FsBatchSendMessageDTO sendMessage = new FsBatchSendMessageDTO();
        //过滤出有飞书配置的用户
        List<String> finalSendUserIds = sendUserIds;
        unionIdList =  unionIdList.stream().filter(u -> finalSendUserIds.contains(u.getUserId())).collect(Collectors.toList());
        List<String> unionIds = unionIdList.stream().map(ThirdUnionDTO::getThirdUnionId).distinct().collect(Collectors.toList());
        if(CollectionUtils.isEmpty(unionIds)){
            return;
        }
        sendMessage.setUnionIds(unionIds);
        Map<String,List<ProductRegistrationEntity>> map = list.stream().collect(Collectors.groupingBy(ProductRegistrationEntity::getDeclareSupplierName));
        map.forEach((key,value)->{
            String titleContent = StrUtil.format("{}存在{}条SKU尚未完成备案，请知悉",key,value.size());
            String messageContent = "通知类型：备案提醒\n";
            List<String> skuNoList = value.stream().map(v->v.getSkuNo()).collect(Collectors.toList());
            int size = skuNoList.size();
            if(size<=10){
                messageContent = messageContent + "备案SKU："+ skuNoList;
            }else{
                skuNoList = skuNoList.subList(0,10);
                messageContent =messageContent + "备案SKU："+ skuNoList + "...+"+(size-10);
            }
            Map contentMap = fsService.getCardMessageMap(titleContent , messageContent, fsAppUrl,false);
            sendMessage.setContentMap(contentMap);
            //发送消息
            fsService.sendMessage(sendMessage);
        });
    }

    @Override
    public PagingVO<ProductRegistrationDTO.PagingVO> exportProductRegistration(PagingDTO<ProductRegistrationDTO.PagingParamDTO> dto) {
        IPage<ProductRegistrationDTO.PagingVO> pageData = baseMapper.paging(new Page<>(dto.getCurrPage(), dto.getPageSize()), dto.getParams());
        fillPagingDb(pageData.getRecords());
        return new PagingVO<>(pageData);
    }

}
