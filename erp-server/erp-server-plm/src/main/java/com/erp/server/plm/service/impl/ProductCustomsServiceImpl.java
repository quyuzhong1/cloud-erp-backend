package com.erp.server.plm.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.constant.CommonConstants;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.erp.model.plm.dto.ProductCustomsDTO;
import com.erp.model.plm.dto.ProductCustomsSkuDTO;
import com.erp.model.plm.entity.ProductCustomsEntity;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.entity.ProductInfoEntity;
import com.erp.model.plm.enums.CustomsTypeEnum;
import com.erp.model.scm.dto.AttachmentDTO;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.model.sys.entity.DictCurrencyEntity;
import com.erp.server.plm.mapper.ProductCustomsMapper;
import com.erp.server.plm.service.ProductCustomsService;
import com.erp.server.plm.service.ProductDetailService;
import com.erp.server.plm.service.SysLogService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-06-12
 */
@Slf4j
@Service
public class ProductCustomsServiceImpl extends SuperServiceImpl<ProductCustomsMapper, ProductCustomsEntity> implements ProductCustomsService {



    @Resource
    private SysLogService sysLogService;

    @Resource
    private ProductDetailService productDetailService;

    @Resource
    private ProductCustomsService self;

    private static final String PCCLASSPATH = String.valueOf(ProductCustomsEntity.class);

    @Override
    public List<ProductCustomsEntity> listByProductId(String productId) {
/*        if (StringUtils.isBlank(productId)){
            return Collections.emptyList();
        }
        //根据产品id获取sku列表
        List<ProductDetailEntity> productDetailEntityList = productDetailService.listSkuByProductIds(Collections.singletonList(productId));
        if (CollectionUtil.isEmpty(productDetailEntityList)){
            return Collections.emptyList();
        }
        List<String> skuIds = productDetailEntityList.stream().map(ProductDetailEntity::getId).distinct().collect(Collectors.toList());
        return baseMapper.listBySkuIds(skuIds);*/

        return baseMapper.listByProductId(productId);
    }

    @Override
    public List<ProductCustomsEntity> listBySkuId(String skuId) {
        return baseMapper.listBySkuId(skuId);
    }

    @Override
    public Boolean removeBySkuId(List<String> skuIds) {
        return lambdaUpdate().set(ProductCustomsEntity::getIsDeleted, Boolean.TRUE).in(ProductCustomsEntity::getSkuId, skuIds).update();
    }

    @Override
    public Boolean addProductCustoms() {
        List<ProductDetailEntity> list = productDetailService.list();
        List<ProductCustomsEntity> customsEntityList = new ArrayList<>();
        list.forEach(req -> {
            ProductCustomsEntity productCustomsEntity = new ProductCustomsEntity();
            productCustomsEntity.setSkuId(req.getId());
            customsEntityList.add(productCustomsEntity);
        });
        return this.saveBatch(customsEntityList);
    }

    /**
     * 获取sku定义的目的国申报海关编码
     * @param dto
     * @return
     */
    @Override
    public List<ProductCustomsEntity> listProductCustomsBySkuIds(ProductCustomsSkuDTO dto) {
        if (Objects.isNull(dto) || CollectionUtil.isEmpty(dto.getSkuIds())){
            return Collections.emptyList();
        }
        return baseMapper.listProductCustomsBySkuIds(dto);
    }

    @Override
    public void addDefaultCustoms(List<String> skuIds) {
        if (CollectionUtil.isEmpty(skuIds)){
            return;
        }
        //默认记录是否存在 不存在则新增
        List<ProductCustomsEntity> list = lambdaQuery().in(ProductCustomsEntity::getSkuId, skuIds).eq(ProductCustomsEntity::getCountry, CommonConstants.DEFAULT)
                .eq(ProductCustomsEntity::getIsDeleted, Boolean.FALSE).list();
        List<String> existSkuIds = list.stream().map(ProductCustomsEntity::getSkuId).collect(Collectors.toList());
        List<String> noExistSkuIds = skuIds.stream().filter(e -> CollectionUtil.isEmpty(existSkuIds) || !existSkuIds.contains(e)).collect(Collectors.toList());
        if (CollectionUtil.isEmpty(noExistSkuIds)){
            List<ProductCustomsEntity> entityList = new ArrayList<>(noExistSkuIds.size());
            noExistSkuIds.forEach(skuId -> {
                entityList.add(new ProductCustomsEntity().setSkuId(skuId).setCountry(CommonConstants.DEFAULT));
            });
            this.saveBatch(entityList);
        }
    }

    @Override
    public List<ProductCustomsEntity> listBySkuIds(List<String> skuIds, String country) {
        if (CollectionUtil.isEmpty(skuIds)){
            return Collections.emptyList();
        }
        return lambdaQuery().in(ProductCustomsEntity::getSkuId,skuIds)
                .eq(StringUtils.isNotEmpty(country), ProductCustomsEntity::getCountry, country)
                .list();
    }

    @Override
    public ProductCustomsEntity getBySkuIdAndCountry(String skuId, String country) {
        if (StringUtils.isBlank(skuId)){
            return null;
        }
        List<ProductCustomsEntity> list = this.lambdaQuery().eq(ProductCustomsEntity::getSkuId, skuId).eq(ProductCustomsEntity::getCountry, country).list();
        if (CollectionUtil.isNotEmpty(list)){
            return list.get(0);
        }
        return null;
    }

    @Override
    public PagingVO<ProductCustomsDTO.ListDTO> paging(PagingDTO<ProductCustomsDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<ProductCustomsDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    private void fillList(List<ProductCustomsDTO.ListDTO> records) {
        for (ProductCustomsDTO.ListDTO record : records) {
            record.setTypeName(CustomsTypeEnum.getName(record.getType()));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean add(ProductCustomsDTO.AddListDTO dto) {
        if(Objects.isNull(dto) || CollectionUtil.isEmpty( dto.getList())){
            return Boolean.FALSE;
        }

        List<ProductCustomsDTO.AddDTO> addList = dto.getList();
        //国家信息
        List<String> countryIdList = addList.stream().map(ProductCustomsDTO.AddDTO::getCountry).distinct().collect(Collectors.toList());
        List<DictCountryEntity> dictCountry = FeignQuery.create(DictCurrencyEntity.class).eq(DictCountryEntity::getId, countryIdList).list();
        Map<String, String> dictCountryMap = dictCountry.stream().collect(Collectors.toMap(DictCountryEntity::getId, DictCountryEntity::getNameCn));

        for (ProductCustomsDTO.AddDTO addDTO : addList) {
            ProductCustomsEntity productCustomsEntity = new ProductCustomsEntity();
            BeanMapper.copy(dto,productCustomsEntity);
            //目的国海关编码
            productCustomsEntity.setCustomsCode(addDTO.getDestinationCustomsCode());

            //查询国家
            String country = addDTO.getCountry();
            if(StringUtils.isNotBlank(country)){
                productCustomsEntity.setCountryName(dictCountryMap.getOrDefault(dictCountry,""));
            }else {
                //不存在都设置为默认
                productCustomsEntity.setCountry(CommonConstants.DEFAULT);
            }

            self.save(productCustomsEntity);
            // 操作日志
            String format = String.format("用户【%s】新增【目的国清关信息】为【%s】", UserContext.getNonLoginUser().getUserName(), StringUtils.isBlank(productCustomsEntity.getCountryName()) ? "默认" : productCustomsEntity.getCountryName());
            sysLogService.addSysLogBySave(format, PCCLASSPATH, productCustomsEntity.getSkuId(), "");
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean update(ProductCustomsDTO.UpdateListDTO dto) {
        if(Objects.isNull(dto) || CollectionUtil.isEmpty( dto.getList())){
            return Boolean.FALSE;
        }
        List<ProductCustomsDTO.UpdateDTO> updateList = dto.getList();
        //旧sku
        List<String> skuIds = updateList.stream().map(ProductCustomsDTO.UpdateDTO::getSkuId).distinct().collect(Collectors.toList());
        List<ProductCustomsEntity> oldList = lambdaQuery()
                .in(ProductCustomsEntity::getSkuId, skuIds)
                .ne(ProductCustomsEntity::getCountry,CommonConstants.DEFAULT)
                .list();
        //按sku维度进行分组
        Map<String, List<ProductCustomsEntity>> oldSkuGroup = oldList.stream().collect(Collectors.groupingBy(ProductCustomsEntity::getSkuId));

        //国家信息
        List<String> countryIdList = updateList.stream().map(ProductCustomsDTO.UpdateDTO::getCountry).distinct().collect(Collectors.toList());
        List<DictCountryEntity> dictCountry = FeignQuery.create(DictCurrencyEntity.class).eq(DictCountryEntity::getId, countryIdList).list();
        Map<String, String> dictCountryMap = dictCountry.stream().collect(Collectors.toMap(DictCountryEntity::getId, DictCountryEntity::getNameCn));

        //按sku维度进行分组
        Map<String, List<ProductCustomsDTO.UpdateDTO>> skuGroup = updateList.stream().collect(Collectors.groupingBy(ProductCustomsDTO.UpdateDTO::getSkuId));

        for (Map.Entry<String, List<ProductCustomsDTO.UpdateDTO>> entry : skuGroup.entrySet()) {
            String skuId = entry.getKey();

            List<ProductCustomsDTO.UpdateDTO> value = entry.getValue();

            List<ProductCustomsEntity> oldValue = oldSkuGroup.getOrDefault(skuId, null);
            if(CollUtil.isNotEmpty(oldValue)){
                // 处理删除的数据
                List<String> newIds = value.stream().map(ProductCustomsDTO.UpdateDTO::getId).collect(Collectors.toList());
                List<ProductCustomsEntity> remove = oldValue.stream()
                        .filter(old -> !newIds.contains(old.getId()))
                        .collect(Collectors.toList());
                if(CollUtil.isNotEmpty(remove)){
                    List<String> removeIds = remove.stream().map(ProductCustomsEntity::getId).collect(Collectors.toList());
                    lambdaUpdate().set(ProductCustomsEntity::getIsDeleted, Boolean.TRUE)
                            .in(ProductCustomsEntity::getId,removeIds )
                            .update();





                }
            }




        }


        return Boolean.TRUE;
    }

    @Override
    public ProductCustomsDTO.ViewDTO view(String id) {
        return null;
    }

    @Override
    public void exportList(ProductCustomsDTO.PagingParamDTO dto, HttpServletResponse response) {

    }

    @Override
    public Boolean importFile(MultipartFile excelFile, HttpServletResponse response) {
        return null;
    }

    @Override
    public void downloadTemplate(HttpServletResponse response) {

    }

    @Override
    public BatchResultDTO delete(String id) {
        return null;
    }

    @Override
    public List<ProductCustomsEntity> listBySkuIds(List<String> skuIds) {
        return baseMapper.listBySkuIds(skuIds);
    }


}
