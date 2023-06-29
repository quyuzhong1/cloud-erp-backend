package com.erp.server.oms.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.ExcelUtil;
import com.erp.model.oms.dto.SkuMapingDTO;
import com.erp.model.oms.dto.excel.SkuMapingImportExcelDTO;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.entity.SkuMapingEntity;
import com.erp.model.oms.enums.DictBasicEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.sys.dto.DictBasicDTO;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.oms.listener.SkuMapingExcelListener;
import com.erp.server.oms.mapper.SkuMapingMapper;
import com.erp.server.oms.service.DictBasicService;
import com.erp.server.oms.service.ShopInfoService;
import com.erp.server.oms.service.SkuMapingService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * sku 对照表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-06-28
 */
@Service
@Slf4j
public class SkuMapingServiceImpl extends SuperServiceImpl<SkuMapingMapper, SkuMapingEntity> implements SkuMapingService {


    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private ShopInfoService shopInfoService;

    @Resource
    private DictBasicService dictBasicService;

    @Override
    public void downloadTemplate(HttpServletResponse response) {

        String path = "classpath:excel/skuMaping.xlsx";
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
                    "attchement;filename=" + new String(excelName.getBytes("gb2312"), "ISO8859-1"));
            response.setContentType("application/msexcel");
            wb.write(output);
            wb.close();
        } catch (Exception e) {
            log.error("SkuMaping downloadTemplate  出错了 e>>>>>>>", e);
            throw new ServiceException(ApiError.ERROR_95131);
        }
    }


    /**
     * 导入sku对照信息
     *
     * @param excelFile
     * @param response
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-06-29 11:01
     */
    @Override
    public Boolean importExcel(MultipartFile excelFile, HttpServletResponse response) {
        List<SkuVO> skuList = plmTaskFeign.listApproveSku();
        List<ShopInfoEntity> shopInfoList = shopInfoService.list();
        List<SkuMapingEntity> skuMapingList = this.listEffectiveList();
        String key = DictBasicEnum.PLATFORM.getType();
        List<DictBasicDTO.ViewDTO> dictBasicList = dictBasicService.getByKey(key);

        SkuMapingExcelListener excelListenerUtil = new SkuMapingExcelListener(this, skuList, shopInfoList, skuMapingList, dictBasicList);
        try {
            EasyExcel.read(excelFile.getInputStream(), SkuMapingImportExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (Exception e) {
            log.error("sku 对照表导入错误！", e);
            return Boolean.FALSE;
        }
        List<SkuMapingImportExcelDTO> errorList = excelListenerUtil.getErrorList();
        if (errorList.size() > 0) {
            String fileName = "sku对照错误信息";
            ExcelUtil.export(fileName, "error", errorList, SkuMapingImportExcelDTO.class, response);
            return Boolean.FALSE;
        }

        return Boolean.TRUE;

    }

    /**
     * 分页查询
     *
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.oms.dto.SkuMapingDTO.PagingViewDTO>
     * @author yl
     * @date 2023-06-29 18:07
     */
    @Override
    public PagingVO<SkuMapingDTO.PagingViewDTO> paging(PagingDTO<SkuMapingDTO.PagingParamDTO> dto) {
        SkuMapingDTO.PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        String searchType = params.getSearchType();
        Boolean matchResult = null;
        if ("yes".equals(searchType)) {
            matchResult = Boolean.TRUE;
        }
        if ("no".equals(searchType)) {
            matchResult = Boolean.FALSE;
        }
        IPage pageData = baseMapper.paging(query, params, matchResult, LocalDateTime.now());
        List<SkuMapingDTO.PagingViewDTO> list = pageData.getRecords();
        if (CollectionUtils.isEmpty(list)) {
            return new PagingVO<>(pageData);
        }
        fillDb(list);
        return new PagingVO<>(pageData);


    }

    /**
     * 填充数据
     *
     * @param list
     * @return void
     * @author yl
     * @date 2023-06-29 19:15
     */
    private void fillDb(List<SkuMapingDTO.PagingViewDTO> list) {
        List<String> skuIdList = list.stream().map(SkuMapingDTO.PagingViewDTO::getProductSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.getSkuInfoByIds(skuIdList);
        for (SkuMapingDTO.PagingViewDTO item : list) {
            String skuId = item.getProductSkuId();
            String skuName = skuList.stream().filter(s -> s.getSkuId().equals(skuId)).
                    findFirst().map(SkuVO::getSkuName).orElse("");
            item.setPlatformSkuName(skuName);
        }
    }


    /**
     * 获取到有效的sku 对照表
     *
     * @param
     * @return java.util.List<com.erp.model.oms.entity.SkuMapingEntity>
     * @author yl
     * @date 2023-06-29 11:29
     */
    private List<SkuMapingEntity> listEffectiveList() {
        LocalDateTime now = LocalDateTime.now();
        List<SkuMapingEntity> resultList = this.lambdaQuery().
                lt(SkuMapingEntity::getEffectiveTime, now).
                //失效日期要大于现在日期
                        ge(SkuMapingEntity::getExpireTime, now).list();

        return resultList;
    }
}
