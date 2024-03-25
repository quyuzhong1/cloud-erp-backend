package com.erp.server.tms.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.DeclarePlatformEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.constant.EnumMessage;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.date.DateUtil;
import com.erp.model.plm.dto.LogisticsProductDTO;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.tms.dto.ProductRegistrationDTO;
import com.erp.model.tms.dto.SettingForecastDTO;
import com.erp.model.tms.dto.transfer.TransferLogisticsCreateProductReq;
import com.erp.model.tms.entity.ProductRegistrationEntity;
import com.erp.model.tms.entity.TransferLogisticsAuthEntity;
import com.erp.model.tms.enums.ProductRegistrationEnum;
import com.erp.rpc.plm.feign.LogisticsProductFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.tms.convert.ProductRegistrationConverter;
import com.erp.server.tms.handler.TransferLogisticsRegistry;
import com.erp.server.tms.mapper.ProductRegistrationMapper;
import com.erp.server.tms.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
    @Autowired
    private CommonService commonService;

    @Resource
    private LogisticsProductFeign logisticsProductFeign;

    @Resource
    private TransferLogisticsRegistry transferLogisticsRegistry;

    @Resource
    private TransferLogisticsAuthService transferLogisticsAuthService;

    @Resource
    private ProductRegistrationServiceImpl service;

    @Resource
    private PlmTaskFeign plmTaskFeign;


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
        List<LogisticsProductDTO.ProductDTO> productDTOList = logisticsProductFeign.listBySkuIdList(noExistsSkuList);
        for(String skuId : skuIds){
            ProductRegistrationEntity entity = entities.stream().filter(v->v.getSkuId().equals(skuId)).findFirst().orElse(null);
            if(Objects.nonNull(entity)){
                resultDTOList.add(BatchResultDTO.fail(entity.getSkuId(),entity.getSkuNo(),"SKU已备案"));
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
                    addEntity.setSkuId(skuId);
                    addEntity.setLatestTime(LocalDateTime.now());
                    addEntity.setDeclareSupplierId(transferLogisticsAuthEntity.getMainId());
                    addEntity.setDeclareSupplierName(transferLogisticsAuthEntity.getName());
                    //设置推送信息
                    Map<String, Object> pushMap = BeanUtil.beanToMap(productDTO);
                    addEntity.setPushInfo(pushMap);
                    addList.add(addEntity);
                }else{
                    //失败返回原因
                    resultDTOList.add(BatchResultDTO.fail(skuId,productDTO.getSkuNo(),"备案产品成功，但在拉取产品信息时失败，请点击拉取备案拉取产品。"+queryResult.getMsg()));
                }
            }else{
                //失败返回原因
                resultDTOList.add(BatchResultDTO.fail(skuId,productDTO.getSkuNo(),result.getMsg()));
            }
        }

        if(CollectionUtils.isNotEmpty(addList)){
            this.save(addList.get(0));
        }
        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】sku为【%s】", commonService.getUserInfo().getUserName(), "产品备案信息");
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
            return Collections.emptyList();
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
        return baseMapper.tabList();
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

        JSONObject jsonObject = new JSONObject(old.getPushInfo());
        LogisticsProductDTO.ProductDTO ruleDTO = JSONObject.parseObject(jsonObject.toJSONString(),new TypeReference< LogisticsProductDTO.ProductDTO>() {}.getType());
        view.setDetailList(ProductRegistrationEnum.DetailDescEnum.convertToViewList(ruleDTO,old));
        return view;
    }

    @Override
    public List<BatchResultDTO> pushFailure(List<String> ids) {
        return null;
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
            List<LogisticsProductDTO.ProductDTO> productDTOList = logisticsProductFeign.listBySkuIdList(skuIdList);
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
                    addEntity.setLatestTime(LocalDateTime.now());
                    if(Objects.isNull(productRegistrationEntity)){
                        addEntity.setSkuId(skuId);
                        addEntity.setDeclareSupplierId(transferLogisticsAuthEntity.getMainId());
                        addEntity.setDeclareSupplierName(transferLogisticsAuthEntity.getName());
                        addEntity.setDeclareCurrencySymbol(productDTO.getDeclareCurrencySymbol());
                        addList.add(addEntity);
                    }else{
                        productRegistrationEntity.setDeclareCurrencySymbol(productDTO.getDeclareCurrencySymbol());
                        BeanUtil.copyProperties(addEntity,productRegistrationEntity, CopyOptions.create().setIgnoreNullValue(true));
                        updateList.add(productRegistrationEntity);
                    }
                }else{
                    //失败返回原因
                    resultDTOList.add(BatchResultDTO.fail(skuId,productDTO.getSkuNo(),"拉取产品信息时失败"+queryResult.getMsg()));
                }
            }
            if(CollectionUtils.isNotEmpty(addList)){
                service.saveBatch(addList);
            }
            if(CollectionUtils.isNotEmpty(updateList)){
                service.updateBatchById(updateList);
            }
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
    public void export(ProductRegistrationDTO.PagingParamDTO dto, HttpServletResponse response) {
        Page query = new Page(1, Integer.MAX_VALUE,false);
        IPage<ProductRegistrationDTO.PagingVO> pageData = baseMapper.paging(query, dto);
        List<ProductRegistrationDTO.PagingVO> list = pageData.getRecords();
        fillPagingDb(list);
        ExcelUtil.export("备案列表"+ DateUtil.currentYMD(),"备案列表",list,ProductRegistrationDTO.PagingVO.class,response);
    }

    @Override
    public ApiResult<?> pullAllProduct(String declareSupplierId) {
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
        List<ProductRegistrationEntity> existEntityList = this.listBySkuNoListAndPlatform(skuNoList,declareSupplierId);
        List<SkuVO> skuVOS = plmTaskFeign.listBySkuNoList(skuNoList);
        List<ProductRegistrationEntity> addList = new ArrayList<>();
        List<ProductRegistrationEntity> updateList = new ArrayList<>();
        for (ProductRegistrationEntity entity : pullDataList) {
            ProductRegistrationEntity existEntity = existEntityList.stream().filter(v -> v.getSkuNo().equals(entity.getSkuNo())).findFirst().orElse(null);
            SkuVO skuVO = skuVOS.stream().filter(v->v.getSkuNo().equals(entity.getSkuNo())).findFirst().orElse(null);
            if(Objects.isNull(skuVO)){
                continue;
            }
            if(Objects.isNull(existEntity)){
                entity.setSkuId(skuVO.getSkuId());
                entity.setLatestTime(LocalDateTime.now());
                entity.setDeclareCurrencySymbol(skuVO.getDeclareCurrencySymbol());
                entity.setDeclareSupplierId(transferLogisticsAuthEntity.getMainId());
                entity.setDeclareSupplierName(transferLogisticsAuthEntity.getName());
                entity.setProductName(skuVO.getSkuName());
                entity.setDeclareElement(skuVO.getDeclareElement());
                addList.add(entity);
            }else{
                existEntity.setDeclareCurrencySymbol(skuVO.getDeclareCurrencySymbol());
                existEntity.setDeclareElement(skuVO.getDeclareElement());
                BeanUtil.copyProperties(entity,existEntity, CopyOptions.create().setIgnoreNullValue(true));
                existEntity.setLatestTime(LocalDateTime.now());
                if(StringUtils.isBlank(existEntity.getProductName())){
                    existEntity.setProductName(skuVO.getSkuName());
                }
                updateList.add(existEntity);
            }
        }
        this.batchAddOrUpdate(addList,updateList);
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
        List<LogisticsProductDTO.ProductDTO> productDTOList = logisticsProductFeign.listBySkuIdList(skuIdList);
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


}
