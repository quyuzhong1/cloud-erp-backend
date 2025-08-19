package com.erp.server.plm.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.constant.CommonConstants;
import com.common.core.enums.ApiError;
import com.common.core.enums.CurrencyEnum;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.ExcelUtil;
import com.erp.model.plm.dto.excel.ProductCustomsExcelDTO;
import com.erp.model.plm.dto.ProductCustomsDTO;
import com.erp.model.plm.dto.ProductCustomsSkuDTO;
import com.erp.model.plm.entity.ProductCustomsEntity;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.enums.CustomsTypeEnum;
import com.erp.model.plm.enums.SysLogClassPathEnum;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.plm.listener.ProductCustomsExcelListener;
import com.erp.server.plm.mapper.ProductCustomsMapper;
import com.erp.server.plm.service.ProductCustomsService;
import com.erp.server.plm.service.ProductDetailService;
import com.erp.server.plm.service.SysLogService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_PLM_PRODUCT_CUSTOMS;

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

    @Resource
    private DownloadTaskFeign downloadTaskFeign;

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
            if(record.getCountry().equals(CommonConstants.DEFAULT)){
                record.setCountryName("默认");
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean add(ProductCustomsDTO.AddListDTO dto) {
        return addOrUpdate(dto,Boolean.FALSE);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(ProductCustomsDTO.AddListDTO dto) {
        return addOrUpdate(dto,Boolean.TRUE);
    }

    private Boolean addOrUpdate(ProductCustomsDTO.AddListDTO dto,Boolean isDelete) {
        if(Objects.isNull(dto) || CollectionUtil.isEmpty( dto.getList())){
            return Boolean.FALSE;
        }
        List<ProductCustomsDTO.AddDTO> updateList = dto.getList();
        //旧sku
        List<String> skuIds = updateList.stream().map(ProductCustomsDTO.AddDTO::getSkuId).distinct().collect(Collectors.toList());
        List<ProductCustomsEntity> oldList = lambdaQuery()
                .in(ProductCustomsEntity::getSkuId, skuIds)
                .list();
        //按sku维度进行分组
        Map<String, List<ProductCustomsEntity>> oldSkuGroup = oldList.stream().collect(Collectors.groupingBy(ProductCustomsEntity::getSkuId));

        List<ProductDetailEntity> productDetailEntities = productDetailService.listByIds(skuIds);
        Map<String, ProductDetailEntity> skuMap = productDetailEntities.stream().collect(Collectors.toMap(ProductDetailEntity::getId , e -> e));

        //国家信息
        List<DictCountryEntity> dictCountry = FeignQuery.create(DictCountryEntity.class).list();
        Map<String, String> dictCountryMap = dictCountry.stream().collect(Collectors.toMap(DictCountryEntity::getId, DictCountryEntity::getNameCn,(o1,o2)-> o1));

        //按sku维度进行分组
        for (ProductCustomsDTO.AddDTO entry : updateList) {
            String skuId = entry.getSkuId();
            ProductDetailEntity productDetailEntity = skuMap.get(skuId);

            List<ProductCustomsDTO.CommonDTO> value = entry.getDetailDTOList();

            List<ProductCustomsEntity> oldValue = oldSkuGroup.getOrDefault(skuId, null);
            Map<String, ProductCustomsEntity> oldMap = new HashMap<>();
            if(CollUtil.isNotEmpty(oldValue)) {
                //skuId ： country
                oldMap = oldValue.stream().collect(Collectors.toMap(e -> e.getSkuId() +":"+e.getCountry(), e -> e, (o1, o2) -> o1));
            }

            if(Boolean.TRUE.equals(isDelete)){
                //删除
                deleteBySkuId(oldValue, value, productDetailEntity);
            }

            for (ProductCustomsDTO.CommonDTO updateDTO : value) {
                ProductCustomsEntity productCustomsEntity = new ProductCustomsEntity();
                BeanMapper.copy(updateDTO,productCustomsEntity);
                productCustomsEntity.setSkuId(skuId);
                //目的国海关编码
                productCustomsEntity.setCustomsCode(updateDTO.getDestinationCustomsCode());
                //查询国家
                String country = productCustomsEntity.getCountry();
                if(StringUtils.isNotBlank(country)){
                    productCustomsEntity.setCountryName(dictCountryMap.getOrDefault(country,""));
                }
                productCustomsEntity.setToCurrency(CurrencyEnum.USD.getCurrencyCode());
                productCustomsEntity.setToCurrencySymbol(CurrencyEnum.USD.getCurrencySymbol());

                productCustomsEntity.setType(CustomsTypeEnum.CLEARANCECUSTOMS.getCode());

                ProductCustomsEntity oldEntity = oldMap.getOrDefault( productCustomsEntity.getSkuId() +":"+productCustomsEntity.getCountry(),null);
                if (Objects.nonNull(oldEntity)) {
                    productCustomsEntity.setId(oldEntity.getId());
                    productCustomsEntity.setVersion(oldEntity.getVersion());
                    self.updateById(productCustomsEntity);
                    // 操作日志
                    String format = String.format("编辑【%s】清关信息", StringUtils.isBlank(productCustomsEntity.getCountryName()) ? "默认" : productCustomsEntity.getCountryName());
                    sysLogService.addSysLogByUpdate(oldEntity,productCustomsEntity, SysLogClassPathEnum.PRODUCTCUSTOMSENTITY.getDesc(), productCustomsEntity.getSkuId(), productDetailEntity.getProductId(),format);
                }else {
                    self.save(productCustomsEntity);
                    // 操作日志
                    String format = String.format("新增【%s】清关信息",  StringUtils.isBlank(productCustomsEntity.getCountryName()) ? "默认" : productCustomsEntity.getCountryName());
                    sysLogService.addSysLogBySave(format, SysLogClassPathEnum.PRODUCTCUSTOMSENTITY.getDesc(), productCustomsEntity.getSkuId(), productDetailEntity.getProductId());
                }
            }
        }
        return Boolean.TRUE;
    }


    private void deleteBySkuId(List<ProductCustomsEntity> oldValue, List<ProductCustomsDTO.CommonDTO> value, ProductDetailEntity productDetailEntity) {
        if(CollUtil.isNotEmpty(oldValue)){
            // 处理删除的数据
            List<String> newIds = value.stream().map(ProductCustomsDTO.CommonDTO::getId).collect(Collectors.toList());
            List<ProductCustomsEntity> remove = oldValue.stream()
                    .filter(old -> !newIds.contains(old.getId()))
                    .collect(Collectors.toList());
            if(CollUtil.isNotEmpty(remove)){
                List<String> removeIds = remove.stream().map(ProductCustomsEntity::getId).collect(Collectors.toList());
                lambdaUpdate().set(ProductCustomsEntity::getIsDeleted, Boolean.TRUE)
                        .in(ProductCustomsEntity::getId,removeIds )
                        .update();

                // 操作日志
                for (ProductCustomsEntity productCustomsEntity : remove) {
                    String format = String.format("删除【%s】清关信息", StringUtils.isBlank(productCustomsEntity.getCountryName()) ? "默认" : productCustomsEntity.getCountryName());
                    sysLogService.addSysLogBySave(format, SysLogClassPathEnum.PRODUCTCUSTOMSENTITY.getDesc(), productCustomsEntity.getSkuId(), productDetailEntity.getProductId());
                }
            }
        }
    }

    @Override
    public ProductCustomsDTO.ViewDTO view(String skuId) {
        List<ProductCustomsDTO.ViewDetailDTO> detailDTOList = baseMapper.view(skuId);
        if(CollUtil.isEmpty(detailDTOList)){
            throw new ServiceException(ApiError.ERROR_95107);
        }
        ProductCustomsDTO.ViewDTO view = new ProductCustomsDTO.ViewDTO();
        ProductCustomsDTO.ViewDetailDTO viewDetailDTO = detailDTOList.get(0);
        BeanMapper.copy(viewDetailDTO,view);
        view.setDetailDTOList(detailDTOList);
        return view;
    }

    @Override
    public void exportList(ProductCustomsDTO.PagingParamDTO param, HttpServletResponse response) {
        downloadTaskFeign.saveDownloadTask("目的国清关导出", EXPORT_PLM_PRODUCT_CUSTOMS.getCode(), param);
    }

    @Override
    public Boolean importFile(MultipartFile excelFile, HttpServletResponse response) {
        //SKU
        Map<String, String> skuMap = productDetailService.list().stream().collect(Collectors.toMap(ProductDetailEntity::getSkuNo, ProductDetailEntity::getId, (o1, o2) -> o1));

        //国家信息
        List<DictCountryEntity> dictCountry = FeignQuery.create(DictCountryEntity.class).list();
        Map<String, String> dictCountryMap = dictCountry.stream().collect(Collectors.toMap(DictCountryEntity::getNameCn, DictCountryEntity::getId,(o1,o2)-> o1));
        dictCountryMap.put("默认",CommonConstants.DEFAULT);
        ProductCustomsExcelListener excelListenerUtil = new ProductCustomsExcelListener(dictCountryMap, skuMap);
        try {
            EasyExcel.read(excelFile.getInputStream(), ProductCustomsExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (Exception e) {
            log.error("导入目的国清关错误！", e);
            return Boolean.FALSE;
        }
        List<ProductCustomsEntity> successList = excelListenerUtil.getSuccessList();
        if(CollUtil.isNotEmpty(successList)){
            List<String> skuIds = successList.stream().map(ProductCustomsEntity::getSkuId).distinct().collect(Collectors.toList());
            List<ProductCustomsEntity> oldList = lambdaQuery().in(ProductCustomsEntity::getSkuId, skuIds).list();
            for (ProductCustomsEntity productCustomsEntity : successList) {
                ProductCustomsEntity oldEntity = oldList.stream().filter(e -> e.getSkuId().equals(productCustomsEntity.getSkuId()) && e.getCountry().equals(productCustomsEntity.getCountry())).findFirst().orElse(null);
                if(Objects.nonNull(oldEntity)){
                    productCustomsEntity.setId(oldEntity.getId());
                    productCustomsEntity.setVersion(oldEntity.getVersion());
                    self.updateById(productCustomsEntity);
                    // 操作日志
                    String format = String.format("编辑【%s】清关信息", StringUtils.isBlank(productCustomsEntity.getCountryName()) ? "默认" : productCustomsEntity.getCountryName());
                    sysLogService.addSysLogByUpdate(oldEntity,productCustomsEntity, SysLogClassPathEnum.PRODUCTCUSTOMSENTITY.getDesc(), productCustomsEntity.getSkuId(), "",format);
                }else {
                    // 操作日志
                    self.save(productCustomsEntity);
                    String format = String.format("新增【%s】清关信息",  StringUtils.isBlank(productCustomsEntity.getCountryName()) ? "默认" : productCustomsEntity.getCountryName());
                    sysLogService.addSysLogBySave(format, SysLogClassPathEnum.PRODUCTCUSTOMSENTITY.getDesc(), productCustomsEntity.getSkuId(), "");
                }
            }
            //新增sku国家默认的记录，如果有则不新增
            self.addDefaultCustoms(skuIds);
        }
        List<ProductCustomsExcelDTO> errorList = excelListenerUtil.getErrorList();
        if (errorList.size() > 0) {
            String fileName = "目的国清关错误信息";
            ExcelUtil.export(fileName, "error", errorList, ProductCustomsExcelDTO.class, response);
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public void downloadTemplate(HttpServletResponse response) {
        String path = "classpath:excel/productCustomsTemplate.xlsx";
        String excelName = "template.xlsx";
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        try {
            InputStream inputStream = resourceLoader.getResource(path).getInputStream();
            XSSFWorkbook wb = new XSSFWorkbook(inputStream);
            // 输出Excel文件
            OutputStream output = response.getOutputStream();
            response.reset();
            // 设置文件头
            response.setHeader("Content-Disposition",
                    "attchement;filename=" + new String(excelName.getBytes("gb2312"), StandardCharsets.ISO_8859_1));
            response.setContentType("application/msexcel");
            wb.write(output);
            wb.close();
        } catch (Exception e) {
            log.error("warehouse downloadTemplate  出错了 e==", e);
            throw new ServiceException(ApiError.ERROR_95131);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO delete(String id) {
        ProductCustomsEntity entity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到目的国清关信息"));

        String country = entity.getCountry();
        if(Objects.equals(country, CommonConstants.DEFAULT)){
            throw new ServiceException("国家等于默认的明细行不允许删除");
        }

        String productId = "";
        ProductDetailEntity productDetailEntity = productDetailService.getById(entity.getId());
        if(Objects.nonNull(productDetailEntity)){
            productId = productDetailEntity.getProductId();
        }

        // 删除主单数据
        super.removeById(id);

        // 删除日志数据
        String format = String.format("删除【%s】清关信息", StringUtils.isBlank(entity.getCountryName()) ? "默认" : entity.getCountryName());
        sysLogService.addSysLogBySave(format, SysLogClassPathEnum.PRODUCTCUSTOMSENTITY.getDesc(), entity.getSkuId(), productId);
        return BatchResultDTO.success(entity.getId(), entity.getSkuNo()+":"+entity.getCountryName(), OperationTypeEnum.DELETE);
    }

    @Override
    public List<ProductCustomsEntity> listByIds(List<String> ids) {
        return baseMapper.listByIds(ids);
    }
}
