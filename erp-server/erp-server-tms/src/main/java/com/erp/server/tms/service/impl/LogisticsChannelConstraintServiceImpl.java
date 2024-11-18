package com.erp.server.tms.service.impl;


import cn.hutool.core.bean.BeanUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelCommonException;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.entity.BaseEntity;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.date.DateUtil;
import com.erp.model.oms.dto.excel.LogisticsProductExcelDTO;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.model.tms.dto.LogisticsChannelConstraintDTO;
import com.erp.model.tms.dto.excel.LogisticsChannelConstraintExcelDTO;
import com.erp.model.tms.entity.LogisticsChannelConstraintEntity;
import com.erp.model.tms.entity.LogisticsChannelEntity;
import com.erp.rpc.sys.feign.SysDictFeign;
import com.erp.server.tms.listener.LogisticsChannelConstraintExcelListener;
import com.erp.server.tms.mapper.LogisticsChannelConstraintMapper;
import com.erp.server.tms.service.LogisticsChannelConstraintService;
import com.erp.server.tms.service.LogisticsChannelService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 物流渠道规则约束 服务实现类
 * </p>
 *
 * @author lrp
 * @since 2024-02-29
 */
@Slf4j
@Service
public class LogisticsChannelConstraintServiceImpl extends SuperServiceImpl<LogisticsChannelConstraintMapper, LogisticsChannelConstraintEntity> implements LogisticsChannelConstraintService {

    @Resource
    private LogisticsChannelService logisticsChannelService;

    @Resource
    private LogisticsChannelConstraintServiceImpl service;

    @Resource
    private SysDictFeign dictFeign;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public  List<BatchResultDTO> addAndUpdate(LogisticsChannelConstraintDTO.AddOrUpdateDTO dto) {
        LogisticsChannelEntity channelEntity = logisticsChannelService.getById(dto.getChannelId());
        if(Objects.isNull(channelEntity)){
            throw new ServiceException("获取不到物流渠道信息");
        }
        //设置国家名称
        List<String> countryList = dto.getCommonDTOList().stream().map(LogisticsChannelConstraintDTO.CommonDTO::getCountry).distinct().collect(Collectors.toList());
        List<DictCountryEntity> dictCountryEntityList = dictFeign.listCountryByIds(countryList);
        dto.getCommonDTOList().forEach(v->{
            DictCountryEntity dictCountry = dictCountryEntityList.stream().filter(t->t.getId().equals(v.getCountry())).findFirst().orElse(null);
            if(Objects.isNull(dictCountry)){
                throw new ServiceException("获取不到国家信息");
            }
            v.setCountryName(dictCountry.getNameCn());
        });
        List<BatchResultDTO> resultDTOList = new ArrayList<>();
        //当前数据库数据
        List<LogisticsChannelConstraintEntity> list = this.lambdaQuery().eq(LogisticsChannelConstraintEntity :: getChannelId,dto.getChannelId()).list();
        //前端传过来的数据
        List<LogisticsChannelConstraintDTO.CommonDTO> commonDTOList = dto.getCommonDTOList();
        //修改的数据
        List<LogisticsChannelConstraintDTO.CommonDTO> updateList = commonDTOList.stream().filter(v-> StringUtils.isNotBlank(v.getId())).collect(Collectors.toList());
        List<LogisticsChannelConstraintEntity> needUpdateList = new ArrayList<>();
        List<LogisticsChannelConstraintEntity> needAddList = new ArrayList<>();
        for(LogisticsChannelConstraintDTO.CommonDTO updateDto : updateList){
            LogisticsChannelConstraintEntity nowEntity = list.stream().filter(v->v.getId().equals(updateDto.getId())).findFirst().orElse(null);
            if(Objects.isNull(nowEntity)){
                resultDTOList.add(BatchResultDTO.fail(updateDto.getCountry(),updateDto.getCountryName(),"获取不到要更新的数据"));
                continue;
            }
            //判断是否有变化，无变化则忽略
            if(updateDto.equalsEntity(nowEntity)){
                continue;
            }
            //校验数据
            if(!updateDto.isValid()){
                resultDTOList.add(BatchResultDTO.fail(updateDto.getCountry(),updateDto.getCountryName(),"字段至少填写一个且大于0，不能都为空"));
                continue;
            }
            //校验数据
            if(!updateDto.isValidSize()){
                resultDTOList.add(BatchResultDTO.fail(updateDto.getCountry(),updateDto.getCountryName(),"超尺寸填写其中一个，其他字段必须填写完整"));
                continue;
            }
            BeanUtil.copyProperties(updateDto,nowEntity);
            needUpdateList.add(nowEntity);
        }
        //新增的数据
        List<LogisticsChannelConstraintDTO.CommonDTO> addList = commonDTOList.stream().filter(v-> StringUtils.isBlank(v.getId())).collect(Collectors.toList());
        for(LogisticsChannelConstraintDTO.CommonDTO addDTO : addList){
            //判断国家是否存在，不能重复
            LogisticsChannelConstraintEntity sameCountryEntity = list.stream().filter(v->v.getCountry().equals(addDTO.getCountry())).findFirst().orElse(null);
            if(Objects.nonNull(sameCountryEntity)){
                resultDTOList.add(BatchResultDTO.fail(addDTO.getCountry(),addDTO.getCountryName(),"国家配置已存在，不能重复配置"));
                continue;
            }
            if(addList.stream().filter(v->v.getCountry().equals(addDTO.getCountry())).count() > 1){
                resultDTOList.add(BatchResultDTO.fail(addDTO.getCountry(),addDTO.getCountryName(),"存在两条相同国家配置，不能重复配置"));
                continue;
            }
            //校验数据
            if(!addDTO.isValid()){
                resultDTOList.add(BatchResultDTO.fail(addDTO.getCountry(),addDTO.getCountryName(),"字段至少填写一个且大于0，不能都为空"));
                continue;
            }
            //校验数据
            if(!addDTO.isValidSize()){
                resultDTOList.add(BatchResultDTO.fail(addDTO.getCountry(),addDTO.getCountryName(),"超尺寸填写其中一个，其他字段必须填写完整"));
                continue;
            }
            LogisticsChannelConstraintEntity addEntity = new LogisticsChannelConstraintEntity();
            BeanUtil.copyProperties(addDTO,addEntity,"id");
            addEntity.setChannelId(dto.getChannelId());
            needAddList.add(addEntity);
        }
        //删除的数据
        List<LogisticsChannelConstraintEntity> needDeleteList = list.stream().filter(v-> commonDTOList.stream().noneMatch(t->v.getId().equals(t.getId()) || v.getCountry().equals(t.getCountry()))).collect(Collectors.toList());
        if(CollectionUtils.isNotEmpty(needAddList)){
            this.saveBatch(needAddList);
        }
        if(CollectionUtils.isNotEmpty(needUpdateList)){
            this.updateBatchById(needUpdateList);
        }
        if(CollectionUtils.isNotEmpty(needDeleteList)){
            this.removeByIds(needDeleteList.stream().map(BaseEntity::getId).collect(Collectors.toList()));
        }
        return resultDTOList;
    }


    @Override
    public List<LogisticsChannelConstraintDTO.ListDTO> getList(String channelId) {
        List<LogisticsChannelConstraintEntity> list = this.lambdaQuery().eq(LogisticsChannelConstraintEntity :: getChannelId,channelId).list();
        return BeanMapper.copyList(list, LogisticsChannelConstraintDTO.ListDTO.class);
    }

    @Override
    public void downloadTemplate(HttpServletResponse response) {
        String path = "classpath:excel/logisticsChannelConstraint.xlsx";
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
            log.error(" LogisticsChannelConstraint downloadTemplate  出错了 e>>>>>>>{}", e);
            throw new ServiceException(ApiError.ERROR_95131);
        }
    }

    @Override
    public Boolean importExcel(MultipartFile excelFile, HttpServletResponse response) {
        LogisticsChannelConstraintExcelListener excelListenerUtil = new LogisticsChannelConstraintExcelListener();
        try {
            EasyExcel.read(excelFile.getInputStream(), LogisticsChannelConstraintExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (IOException e) {
            log.error("导入物流国家设置错误！", e);
            throw new ServiceException(ApiError.ERROR_95124);
        } catch (ExcelCommonException e) {
            log.error("导入格式错误！", e);
            throw new ServiceException(ApiError.ERROR_1016);
        }
        List<LogisticsChannelConstraintDTO.AddOrUpdateDTO> addOrUpdateDTOList = excelListenerUtil.getAddOrUpdateDTOList();
        List<LogisticsChannelConstraintExcelDTO> errorList = excelListenerUtil.getErrorList();
        service.handleImportData(addOrUpdateDTOList);
        if (!errorList.isEmpty()) {
            ExcelUtil.export("物流国家设置导入","物流国家设置导入",errorList,LogisticsChannelConstraintExcelDTO.class,response);
            return Boolean.FALSE;
        }
        return true;
    }

    @Override
    public LogisticsChannelConstraintEntity getByChannelAndCountry(String channelId, String country) {
        if(StringUtils.isBlank(country)){
            return null;
        }
        return this.lambdaQuery().eq(LogisticsChannelConstraintEntity::getChannelId,channelId).eq(LogisticsChannelConstraintEntity::getCountry,country).one();
    }

    @Transactional(rollbackFor = Exception.class)
    public void handleImportData(List<LogisticsChannelConstraintDTO.AddOrUpdateDTO> addOrUpdateDTOList) {
        if(CollectionUtils.isEmpty(addOrUpdateDTOList)){
            return;
        }
        List<String> channelIds = addOrUpdateDTOList.stream().map(LogisticsChannelConstraintDTO.AddOrUpdateDTO::getChannelId).collect(Collectors.toList());
        List<LogisticsChannelConstraintEntity> allEntityList = this.lambdaQuery().in(LogisticsChannelConstraintEntity :: getChannelId,channelIds).list();
        Map<String,List<LogisticsChannelConstraintEntity>> entityMap = allEntityList.stream().collect(Collectors.groupingBy(LogisticsChannelConstraintEntity::getChannelId));
        List<LogisticsChannelConstraintEntity> needAddList = new ArrayList<>();
        List<LogisticsChannelConstraintEntity> needUpdateList = new ArrayList<>();
        for(LogisticsChannelConstraintDTO.AddOrUpdateDTO addOrUpdateDTO : addOrUpdateDTOList){
            List<LogisticsChannelConstraintEntity> entityList = entityMap.getOrDefault(addOrUpdateDTO.getChannelId(),new ArrayList<>());
            List<LogisticsChannelConstraintDTO.CommonDTO> commonDTOList = addOrUpdateDTO.getCommonDTOList();
            Collections.reverse(commonDTOList);
            Set<String> existCountry = new HashSet<>();
            for (LogisticsChannelConstraintDTO.CommonDTO commonDTO : commonDTOList) {
                if(existCountry.contains(commonDTO.getCountry())){
                    continue;
                }
                existCountry.add(commonDTO.getCountry());
                LogisticsChannelConstraintEntity entity = entityList.stream().filter(v->v.getCountry().equals(commonDTO.getCountry())).findFirst().orElse(null);
                if(Objects.nonNull(entity)){
                    BeanUtil.copyProperties(commonDTO,entity,"id");
                    needUpdateList.add(entity);
                }else{
                    LogisticsChannelConstraintEntity addEntity = new LogisticsChannelConstraintEntity();
                    BeanUtil.copyProperties(commonDTO,addEntity);
                    addEntity.setChannelId(addOrUpdateDTO.getChannelId());
                    needAddList.add(addEntity);
                }
            }
        }
        if(CollectionUtils.isNotEmpty(needAddList)){
            this.saveBatch(needAddList);
        }
        if(CollectionUtils.isNotEmpty(needUpdateList)){
            this.updateBatchById(needUpdateList);
        }
    }
}
