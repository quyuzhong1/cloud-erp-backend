package com.erp.server.tms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.FastDFSClientUtil;
import com.common.core.utils.date.DateUtil;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.DictCountryDTO;
import com.erp.model.sys.entity.DictCityEntity;
import com.erp.model.tms.dto.RemotePostcodeDTO;
import com.erp.model.tms.dto.RemotePostcodeDetailDTO;
import com.erp.model.tms.entity.RemotePostcodeDetailEntity;
import com.erp.model.tms.enums.RemotePostcodeDetailMatchTypeEnum;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.tms.listener.RemotePostcodeDetailExcelListener;
import com.erp.server.tms.mapper.RemotePostcodeDetailMapper;
import com.erp.server.tms.service.OperateLogService;
import com.erp.server.tms.service.RemotePostcodeDetailService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
/**
 * <p>
 * 偏远邮编明细表 服务实现类
 * </p>
 *
 * @author jack
 * @since 2024-11-29
 */
@Slf4j
@Service
public class RemotePostcodeDetailServiceImpl extends SuperServiceImpl<RemotePostcodeDetailMapper, RemotePostcodeDetailEntity> implements RemotePostcodeDetailService {
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private SysUserFeign sysUserFeign;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean add(RemotePostcodeDTO.AddDTO addDTO, String mainId) {
        List<RemotePostcodeDetailDTO.AddDTO> details = addDTO.getDetails();

        List<DictCountryDTO.ListDTO> listDTOS = sysUserFeign.countryList();
        Map<String, String> countryMap = listDTOS.stream().collect(Collectors.toMap(DictCountryDTO.ListDTO::getId, DictCountryDTO.ListDTO::getId));

        List<String> cityNames = details.stream().map(RemotePostcodeDetailDTO.AddDTO::getCityName).filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
        // 将城市信息转换为 Map
        Map<String, String> cityNameMap = getCityNameMap(cityNames);
        List<RemotePostcodeDetailEntity> addList = new ArrayList<>();
        for (RemotePostcodeDetailDTO.AddDTO detail : details) {
            //校验国家是否存在
            if(Boolean.FALSE.equals(countryMap.containsKey(detail.getCountry()))){
                throw new ServiceException("国家【"+detail.getCountry()+"】不存在");
            }
            RemotePostcodeDetailEntity remotePostcodeDetailEntity = new RemotePostcodeDetailEntity();
            BeanMapper.copy(detail, remotePostcodeDetailEntity);
            //设置主表id
            remotePostcodeDetailEntity.setMainId(mainId);
            remotePostcodeDetailEntity.setCity(cityNameMap.getOrDefault(detail.getCityName(),""));
            addList.add(remotePostcodeDetailEntity);
        }
        boolean flag = this.saveBatch(addList);
        List<Pair<String, String>> addPairList = spliceOperateContent(addList, cityNameMap);
        operateLogService.batchAddModuleOperateLog("新增明细【%s】", ModuleTypeEnum.REMOTE_POSTCODE.getCode(), addPairList, "新增操作");
        return flag;
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(RemotePostcodeDTO.UpdateDTO dto,String mainId) {
        List<String> cityNames = dto.getDetails().stream().map(RemotePostcodeDetailDTO.UpdateDTO::getCityName).filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
        // 将城市信息转换为 Map
        Map<String, String> cityNameMap = getCityNameMap(cityNames);

        List<DictCountryDTO.ListDTO> listDTOS = sysUserFeign.countryList();
        Map<String, String> countryMap = listDTOS.stream().collect(Collectors.toMap(DictCountryDTO.ListDTO::getId, DictCountryDTO.ListDTO::getId));

        //原数据明细
        List<RemotePostcodeDetailEntity> oldList = lambdaQuery().eq(RemotePostcodeDetailEntity::getMainId, mainId).list();
        List<String> deleteIds = getDeleteIds(dto.getDetails(), oldList);
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            List<RemotePostcodeDetailEntity> removeList = oldList.stream().filter(obj -> deleteIds.contains(obj.getId())).collect(Collectors.toList());
            List<Pair<String, String>> pairList = spliceOperateContent(removeList, cityNameMap);
            //操作日志
            operateLogService.batchAddModuleOperateLog("删除了明细【%s】", ModuleTypeEnum.REMOTE_POSTCODE.getCode(),pairList,"编辑操作");
            this.removeByIds(deleteIds);
        }
        Map<String, RemotePostcodeDetailEntity> detailEntityMap = oldList.stream().collect(Collectors.toMap(RemotePostcodeDetailEntity::getId, obj -> obj));
        List<RemotePostcodeDetailEntity> addList = new ArrayList<>();
        for (RemotePostcodeDetailDTO.UpdateDTO detail : dto.getDetails()) {
            //校验国家是否存在
            if(Boolean.FALSE.equals(countryMap.containsKey(detail.getCountry()))){
                throw new ServiceException("国家【"+detail.getCountry()+"】不存在");
            }
            RemotePostcodeDetailEntity remotePostcodeDetail = new RemotePostcodeDetailEntity();
            BeanMapper.copy(detail, remotePostcodeDetail);
            remotePostcodeDetail.setMainId(mainId);
            remotePostcodeDetail.setCity(cityNameMap.getOrDefault(detail.getCityName(),""));
            if(StringUtils.isBlank(remotePostcodeDetail.getId())){
                //新增
                addList.add(remotePostcodeDetail);
            }else{
                //修改
                RemotePostcodeDetailEntity old = detailEntityMap.get(remotePostcodeDetail.getId());
                if (ObjectUtils.isEmpty(old)) {
                    throw new ServiceException(ApiError.ERROR_98002);
                }
                this.updateById(remotePostcodeDetail);
                //操作日志
                operateLogService.addModuleOperateLogByObj(old,remotePostcodeDetail, ModuleTypeEnum.REMOTE_POSTCODE.getCode(),remotePostcodeDetail.getId(),"修改明细【%s】");
            }
        }
        this.saveBatch(addList);
        //添加操作日志
        if (CollectionUtils.isNotEmpty(addList)) {
            List<Pair<String, String>> addPairList = spliceOperateContent(addList, cityNameMap);
            operateLogService.batchAddModuleOperateLog("新增明细【%s】", ModuleTypeEnum.REMOTE_POSTCODE.getCode(), addPairList, "编辑操作");
        }
        return true;
    }

    @NotNull
    private Map<String, String> getCityNameMap(List<String> cityNames) {
        List<DictCityEntity> dictCityEntities = sysUserFeign.listCityByNames(cityNames);
        // 将城市信息转换为 Map，减少多次流式查找
        return dictCityEntities.stream()
                .collect(Collectors.toMap(DictCityEntity::getName, DictCityEntity::getId));
    }

    @NotNull
    private static List<Pair<String, String>> spliceOperateContent(List<RemotePostcodeDetailEntity> removeList, Map<String, String> cityNameMap) {
        List<Pair<String, String>> pairList = new ArrayList<>();
        for (RemotePostcodeDetailEntity entity : removeList) {
            Pair pair = null;
            if(StringUtils.isNotBlank(entity.getCity())) {
                pair = new Pair<>(entity.getId(),"【"+entity.getCountry() + "-" + cityNameMap.getOrDefault(entity.getCity(),entity.getCity()) + "-" + entity.getPostCode()+"】");
            }else {
                pair = new Pair<>(entity.getId(),"【"+entity.getCountry() + "-" + entity.getPostCode()+"】");
            }
            pairList.add(pair);
        }
        return pairList;
    }

    private List<String> getDeleteIds(List<RemotePostcodeDetailDTO.UpdateDTO> newList, List<RemotePostcodeDetailEntity> oldList) {
        List<String> newIds = newList.stream().filter(g -> StringUtils.isNotBlank(g.getId())).
                map(RemotePostcodeDetailDTO.UpdateDTO::getId).collect(Collectors.toList());
        List<String> oldIds = oldList.stream().map(RemotePostcodeDetailEntity
                ::getId).collect(Collectors.toList());
        return oldIds.stream().filter(s -> !newIds.contains(s)).collect(Collectors.toList());
    }


    @Override
    public PagingVO<RemotePostcodeDetailDTO.ListDTO> paging(PagingDTO<RemotePostcodeDetailDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<RemotePostcodeDetailDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO(pageData);
        }
        // 数据处理
        return new PagingVO(pageData);
    }

    @Override
    public List<RemotePostcodeDetailDTO.TabListDTO> tabList(PermissionsDTO param) {
        RemotePostcodeDetailDTO.PagingParamDTO searchParam = new RemotePostcodeDetailDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<RemotePostcodeDetailDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        // 获取状态列表
        List<String> statusList = ApproveStatusEnum.getStatusList();
        // 不存在的状态赋值为0
        List<String> existStatusList = list.stream().map(RemotePostcodeDetailDTO.TabListDTO::getTabFlag).collect(Collectors.toList());
        statusList.parallelStream().forEach(status -> {
            if(!existStatusList.contains(status)) {
            list.add(new RemotePostcodeDetailDTO.TabListDTO(status, 0));
        }
        });
        list.add(new RemotePostcodeDetailDTO.TabListDTO("all", list.stream().mapToInt(RemotePostcodeDetailDTO.TabListDTO::getCount).sum()));
        // 计算合计数量
        return list;
    }

    @Override
    public void exportList(RemotePostcodeDetailDTO.ExportDTO param, HttpServletResponse response) {
        List<RemotePostcodeDetailDTO.ListDTO> list = this.baseMapper.listExport(param);
        if(CollUtil.isEmpty(list)) {
           return;
        }
        // 数据处理

        // 导出数据
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/remotePostcodeDetail.xlsx";
        String name = "偏远邮编明细单导出";
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date).append(name);
        try {
            new ExcelPrintUtils().patchExport(list, response, sb.toString(), excelPath);
        } catch (Exception e) {
            throw new ServiceException(ApiError.ERROR_1015);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO delete(String id) {
        RemotePostcodeDetailEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到偏远邮编明细单数据"));
        // 删除主单数据
        super.removeById(id);
        // 删除日志数据
        log.info("删除 开始删除偏远邮编明细单日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据删除操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getId(), "偏远邮编明细单");
        operateLogService.addModuleOperateLog(msg, null, entity.getId(), "删除偏远邮编明细单数据");
        return BatchResultDTO.success(entity.getId(), entity.getId(), OperationTypeEnum.DELETE);
    }

    @Override
    public RemotePostcodeDetailDTO.ViewDTO view(String id) {
        RemotePostcodeDetailEntity remotePostcodeDetailEntity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到偏远邮编明细单数据"));
        RemotePostcodeDetailDTO.ViewDTO data = BeanMapperUtils.map(RemotePostcodeDetailDTO.ViewDTO.class, remotePostcodeDetailEntity);
        // 数据填充处理
        return data;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void removeByMainIds(List<String> mainIds) {
        if (CollUtil.isEmpty(mainIds)) {
            return;
        }
        lambdaUpdate()
                .set(RemotePostcodeDetailEntity::getIsDeleted, true)
                .in(RemotePostcodeDetailEntity::getMainId, mainIds)
                .update();
    }

    @Override
    public List<RemotePostcodeDetailDTO.ViewDTO> listByMainIds(List<String> mainIds) {
        if (CollUtil.isEmpty(mainIds)) {
            return Collections.emptyList();
        }

        List<RemotePostcodeDetailEntity> list = lambdaQuery()
                .in(RemotePostcodeDetailEntity::getMainId, mainIds)
                .list();

        return CollUtil.isEmpty(list) ? Collections.emptyList() :
                list.stream()
                        .map(entity -> {
                            RemotePostcodeDetailDTO.ViewDTO viewDTO = new RemotePostcodeDetailDTO.ViewDTO();
                            BeanMapper.copy(entity, viewDTO); 
                            return viewDTO;
                        })
                        .collect(Collectors.toList());
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public RemotePostcodeDetailDTO.ImportResultDTO importFile(MultipartFile excelFile, HttpServletResponse response) {
        RemotePostcodeDetailExcelListener excelListenerUtil = new RemotePostcodeDetailExcelListener();
        try {
            EasyExcel.read(excelFile.getInputStream(), RemotePostcodeDetailDTO.ImportDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (Exception e) {
            log.error("导入错误！", e);
            throw new ServiceException(ApiError.ERROR_95124);
        }
        RemotePostcodeDetailDTO.ImportResultDTO result = new RemotePostcodeDetailDTO.ImportResultDTO();
        //导入数据处理
        List<RemotePostcodeDetailDTO.ImportDTO> successList = excelListenerUtil.getSuccessList();
        //导出错误数据
        List<RemotePostcodeDetailDTO.ImportDTO> errorList = excelListenerUtil.getErrorList();
        List<RemotePostcodeDetailDTO.ImportDTO> resultList = new ArrayList<>();
        if(CollUtil.isNotEmpty(successList)){
            // 将城市信息转换为 Map，减少多次流式查找
            List<String> cityNames = successList.stream().map(RemotePostcodeDetailDTO.ImportDTO::getCityName).filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
            Map<String, String> cityMap = getCityNameMap(cityNames);
            // 将国家信息转换为 Map，减少多次流式查找
            List<DictCountryDTO.ListDTO> listDTOS = sysUserFeign.countryList();
            Map<String, String> countryMap = listDTOS.stream().collect(Collectors.toMap(DictCountryDTO.ListDTO::getId, DictCountryDTO.ListDTO::getId));
            for (RemotePostcodeDetailDTO.ImportDTO dto : successList) {
                if(Boolean.FALSE.equals(countryMap.containsKey(dto.getCountry()))){
                    dto.setErrorMsg("国家二字码不存在");
                    errorList.add(dto);
                    continue;
                }
                if(StringUtils.isNotBlank(dto.getCityName()) && Boolean.FALSE.equals(cityMap.containsKey(dto.getCityName()))){
                    dto.setErrorMsg("城市不存在");
                    errorList.add(dto);
                    continue;
                }
                dto.setCity(cityMap.get(dto.getCityName()));
                // 匹配类型名称
                dto.setMatchType(RemotePostcodeDetailMatchTypeEnum.PRECISEMATCH.getCode());
                dto.setMatchTypeName(RemotePostcodeDetailMatchTypeEnum.PRECISEMATCH.getName());
                resultList.add(dto);
            }
        }
        result.setSuccessList(resultList);
        String url = "";
        if (CollectionUtils.isNotEmpty(errorList)) {
            String fileName = "偏远邮编详情错误.xlsx";
            File file = ExcelUtil.exportFile(fileName, "error", errorList, RemotePostcodeDetailDTO.ImportDTO.class);
            if (file != null && !file.isDirectory()) {
                url = FastDFSClientUtil.uploadFile(file, fileName);
            }
        }
        result.setErrorUrl(url);
        return result;
    }

    @Override
    public void downloadTemplate(HttpServletResponse response) {
        String path = "classpath:excel/remotePostcodeDetailTemplate.xlsx";
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
}
