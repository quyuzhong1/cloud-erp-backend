package com.erp.server.tms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.erp.model.oms.dto.DictInvoiceHsDTO;
import com.erp.model.plm.dto.ProductCustomsDTO;
import com.erp.model.plm.entity.ProductLogisticsEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.tms.dto.excel.DictHsCodeExcelDTO;
import com.erp.model.tms.entity.DictHsCodeEntity;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.tms.listener.DictHsCodeExcelListener;
import com.erp.server.tms.mapper.DictHsCodeMapper;
import com.erp.server.tms.service.DictHsCodeService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.tms.service.OperateLogService;
import com.common.core.exception.ServiceException;
import jodd.util.StringUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.tms.dto.DictHsCodeDTO;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
import org.springframework.web.multipart.MultipartFile;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import static com.common.business.enums.FileTaskEventEnum.EXPORT_TMS_DICT_HS_CODE;

/**
 * <p>
 * 出口申报要素表 服务实现类
 * </p>
 *
 * @author jack
 * @since 2025-07-11
 */
@Slf4j
@Service
public class DictHsCodeServiceImpl extends SuperServiceImpl<DictHsCodeMapper, DictHsCodeEntity> implements DictHsCodeService {
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(DictHsCodeDTO.AddDTO addDTO) {
        DictHsCodeEntity dictHsCodeEntity = new DictHsCodeEntity();
        BeanMapperUtils.copy(addDTO, dictHsCodeEntity);

        Integer count = lambdaQuery()
                .eq(DictHsCodeEntity::getHsCode, dictHsCodeEntity.getHsCode())
                .count();
        if(count > 0 ){
            throw new ServiceException(ApiError.ERROR_96008,dictHsCodeEntity.getHsCode());
        }

        if(StringUtil.isBlank(addDTO.getCountry())){
            //默认中国
            dictHsCodeEntity.setCountry("CN");
        }
        log.info("开始新增出口申报要素单");
        boolean save = super.save(dictHsCodeEntity);
        if(!save) {
            throw new ServiceException("出口申报要素单保存失败");
        }

        // 操作日志
        operateLogService.addModuleOperateLog("新增出口申报要素信息", ModuleTypeEnum.DICT_HS_CODE.getCode(), dictHsCodeEntity.getId(), "新增操作");
        return new BaseResultDTO.AddDTO(dictHsCodeEntity.getId(), dictHsCodeEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(DictHsCodeDTO.UpdateDTO addOrUpdateDTO) {
        DictHsCodeEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "出口申报要素单"));
        DictHsCodeEntity dictHsCodeEntity =  BeanMapperUtils.map(DictHsCodeEntity.class, addOrUpdateDTO);

        Integer count = lambdaQuery()
                .eq(DictHsCodeEntity::getHsCode, dictHsCodeEntity.getHsCode())
                .ne(DictHsCodeEntity::getId, dictHsCodeEntity.getId())
                .count();
        if(count > 0 ){
            throw new ServiceException(ApiError.ERROR_96008,dictHsCodeEntity.getHsCode());
        }

        //不相等时
        if(!Objects.equals(old.getHsCode(), dictHsCodeEntity.getHsCode())){
            //海关编码没有被物流产品信息引用时可以删除，否则提示：该海关编码已被使用，请修改物流产品线信息海关编码后删除
            List<ProductLogisticsEntity> productLogisticsList = FeignQuery.create(ProductLogisticsEntity.class).eq(ProductLogisticsEntity::getCustomsCode, old.getHsCode()).list();
            if(CollUtil.isNotEmpty(productLogisticsList)){
                throw new ServiceException("该海关编码已被使用，请修改物流产品线信息海关编码后删除");
            }
        }

        log.info("编辑 开始修改出口申报要素单数据，id：【{}】", old.getId());
        boolean save = super.updateById(dictHsCodeEntity);
        if(!save) {
            throw new ServiceException("出口申报要素单保存失败");
        }
        // 记录主单操作日志
        log.info("编辑 开始记录出口申报要素单日志数据，id：【{}】", dictHsCodeEntity.getId());
        String msg = StrUtil.format("用户【{}】编辑海关编码为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), dictHsCodeEntity.getHsCode(), "出口申报要素单");
        operateLogService.addModuleOperateLogByObj(old, dictHsCodeEntity, ModuleTypeEnum.DICT_HS_CODE.getCode(), dictHsCodeEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public PagingVO<DictHsCodeDTO.ListDTO> paging(PagingDTO<DictHsCodeDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<DictHsCodeDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        return new PagingVO(pageData);
    }

    @Override
    public BatchResultDTO delete(String id) {
        DictHsCodeEntity entity = getByIdOpt(id).orElseThrow(() -> new ServiceException("出库申报要素不存在"));

        //海关编码没有被物流产品信息引用时可以删除，否则提示：该海关编码已被使用，请修改物流产品线信息海关编码后删除
        List<ProductLogisticsEntity> productLogisticsList = FeignQuery.create(ProductLogisticsEntity.class).eq(ProductLogisticsEntity::getCustomsCode, entity.getHsCode()).list();
        if(CollUtil.isNotEmpty(productLogisticsList)){
            throw new ServiceException("该海关编码已被使用，请修改物流产品线信息海关编码后删除");
        }

        // 删除主单数据
        super.removeById(id);
        // 删除日志数据
        String msg = StrUtil.format("用户【{}】删除【{}】单据海关编码为【{}】", UserContext.getDefaultLoginUser().getUserName(), "出口申报要素单" , entity.getHsCode());
        operateLogService.addModuleOperateLog(msg, null, entity.getId(), "删除操作");
        return BatchResultDTO.success(entity.getId(), entity.getHsCode(),OperationTypeEnum.DELETE);
    }

    @Override
    public DictHsCodeDTO.ViewDTO view(String id) {
        DictHsCodeEntity entity = getByIdOpt(id).orElseThrow(() -> new ServiceException("出库申报要素不存在"));
        DictHsCodeDTO.ViewDTO view = new DictHsCodeDTO.ViewDTO();
        BeanMapper.copy(entity,view);
        return view;
    }

    @Override
    public void exportList(DictHsCodeDTO.PagingParamDTO param, HttpServletResponse response) {
        downloadTaskFeign.saveDownloadTask("出口申报要素导出", EXPORT_TMS_DICT_HS_CODE.getCode(), param);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean importFile(MultipartFile excelFile, HttpServletResponse response) {
        DictHsCodeExcelListener excelListenerUtil = new DictHsCodeExcelListener();
        try {
            EasyExcel.read(excelFile.getInputStream(), DictHsCodeExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (Exception e) {
            log.error("导入出口申请要素错误！", e);
            return Boolean.FALSE;
        }


        List<DictHsCodeExcelDTO> successList = excelListenerUtil.getSuccessList();
        if(CollUtil.isNotEmpty(successList)){
            List<String> hsCodes = successList.stream().map(DictHsCodeExcelDTO::getHsCode).collect(Collectors.toList());
            List<DictHsCodeEntity> oldList = lambdaQuery().in(DictHsCodeEntity::getHsCode, hsCodes).list();
            Map<String, DictHsCodeEntity> oldMap = oldList.stream().collect(Collectors.toMap(DictHsCodeEntity::getHsCode, w -> w , (o1,o2)->o1));

            for (DictHsCodeExcelDTO dto : successList) {
                DictHsCodeEntity oldEntity = oldMap.getOrDefault(dto.getHsCode(), null);
                if(Objects.nonNull(oldEntity)){
                    DictHsCodeEntity dictHsCodeEntity =  BeanMapperUtils.map(DictHsCodeEntity.class, dto);
                    dictHsCodeEntity.setId(oldEntity.getId());
                    dictHsCodeEntity.setVersion(oldEntity.getVersion());
                    dictHsCodeEntity.setCountry(oldEntity.getCountry());
                    updateById(dictHsCodeEntity);
                    // 记录主单操作日志
                    String msg = StrUtil.format("用户【{}】编辑海关编码为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), dictHsCodeEntity.getHsCode(), "出口申报要素单");
                    operateLogService.addModuleOperateLogByObj(oldEntity, dictHsCodeEntity, ModuleTypeEnum.DICT_HS_CODE.getCode(), dictHsCodeEntity.getId(), msg);
                }else {
                    DictHsCodeEntity entity = new DictHsCodeEntity();
                    BeanMapper.copy(dto, entity);
                    if(StringUtil.isBlank(entity.getCountry())){
                        //默认中国
                        entity.setCountry("CN");
                    }
                    save(entity);
                    // 操作日志
                    operateLogService.addModuleOperateLog("新增出口申报要素信息", ModuleTypeEnum.DICT_HS_CODE.getCode(), entity.getId(), "新增操作");
                }
            }
        }

        List<DictHsCodeExcelDTO> errorList = excelListenerUtil.getErrorList();
        if (errorList.size() > 0) {
            String fileName = "出口申报要素错误信息";
            ExcelUtil.export(fileName, "error", errorList, DictHsCodeExcelDTO.class, response);
            return Boolean.FALSE;
        }

        return Boolean.TRUE;
    }

    @Override
    public void downloadTemplate(HttpServletResponse response) {
        String path = "classpath:excel/dictHsCodeTemplate.xlsx";
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
    public List<DictHsCodeDTO.SearchDTO> searchByKey(DictHsCodeDTO.SearchParamDTO dto) {
        return baseMapper.searchByKey(dto);
    }

    @Override
    public PagingVO<DictHsCodeDTO.ListBRDTO> pagingByBR(PagingDTO<DictHsCodeDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<DictHsCodeDTO.ListBRDTO> pageData = this.baseMapper.pagingByBR(query, pagingParamDTO.getParams());
        return new PagingVO(pageData);
    }
}
