package com.erp.server.tms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.DeclarePlatformEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.constant.EnumMessage;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.plm.dto.LogisticsProductDTO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.tms.dto.ProductRegistrationDTO;
import com.erp.model.tms.dto.SettingForecastDTO;
import com.erp.model.tms.dto.transfer.TransferLogisticsCreateProductReq;
import com.erp.model.tms.entity.ProductRegistrationEntity;
import com.erp.model.tms.entity.TransferLogisticsAuthEntity;
import com.erp.model.tms.enums.ProductRegistrationEnum;
import com.erp.rpc.plm.feign.LogisticsProductFeign;
import com.erp.server.tms.convert.ProductRegistrationConverter;
import com.erp.server.tms.handler.TransferLogisticsRegistry;
import com.erp.server.tms.mapper.ProductRegistrationMapper;
import com.erp.server.tms.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.math.BigDecimal;
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
            createProductReq.setFirstQauntity(BigDecimal.valueOf(0.01));
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
                    addList.add(addEntity);
                }else{
                    //失败返回原因
                    resultDTOList.add(BatchResultDTO.fail(skuId,productDTO.getSkuNo(),"备案产品成功，但在拉取产品信息时失败，请点击拉取备案拉取产品。"+result.getMsg()));
                }
            }else{
                //失败返回原因
                resultDTOList.add(BatchResultDTO.fail(skuId,productDTO.getSkuNo(),result.getMsg()));
            }
        }

        if(CollectionUtils.isNotEmpty(addList)){
            service.saveBatch(addList);
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

        List<LogisticsProductDTO.ProductDTO> productDTOList = logisticsProductFeign.listBySkuIdList(Arrays.asList(old.getSkuId()));
        LogisticsProductDTO.ProductDTO productDTO = productDTOList.stream().findFirst().orElse(new LogisticsProductDTO.ProductDTO());
        view.setDetailList(ProductRegistrationEnum.DetailDescEnum.convertToViewList(productDTO,old));
        return view;
    }

    @Override
    public List<BatchResultDTO> pushFailure(List<String> ids) {
        return null;
    }

    @Override
    public List<BatchResultDTO> cancel(List<String> ids) {
        return null;
    }

    @Override
    public void pull(ProductRegistrationDTO.AddDTO dto) {

    }

    @Override
    public List<BatchResultDTO> delete(List<String> ids) {
        return null;
    }

    @Override
    public void export(ProductRegistrationDTO.PagingParamDTO dto, HttpServletResponse response) {

    }

    private void fillPagingDb(List<ProductRegistrationDTO.PagingVO> list) {
        //TODO：待处理备案审核状态
        list.forEach(v->{
            v.setStatusName(EnumMessage.getNameByCode(ProductRegistrationEnum.StatusEnum.class,v.getStatus()));
            v.setDeclarePlatformName(EnumMessage.getNameByCode(DeclarePlatformEnum.class,v.getDeclarePlatform()));
        });
    }

}
