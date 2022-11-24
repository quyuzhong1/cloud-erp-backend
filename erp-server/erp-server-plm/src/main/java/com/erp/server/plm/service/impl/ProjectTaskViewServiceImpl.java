package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.date.DateUtil;
import com.common.web.service.RedisService;
import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import com.erp.model.plm.dto.*;
import com.erp.server.plm.constant.IsConstant;
import com.erp.server.plm.enums.ApprovalStatusEnum;
import com.erp.server.plm.enums.ProjectStateEnum;
import com.erp.server.plm.enums.TaskStateEnum;
import com.erp.server.plm.mapper.ProjectTaskMapper;
import com.erp.server.plm.service.ProjectTaskViewService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/11/23 18:57
 */
@Service
public class ProjectTaskViewServiceImpl implements ProjectTaskViewService {

    @Autowired
    private ProjectTaskMapper projectTaskMapper;

    @Autowired
    private RedisService redisService;


    @Autowired(required = false)
    private HttpServletResponse response;

    /**
     * @description: 项目视图按人员查询
     * @author Will
     * @date: 2022/11/23 12:00
     * @param dto
     * @return List<ProductTaskPersonnelChildDTO>
     */
    @Override
    public List<ProductTaskPersonnelChildDTO> getPersonnelView(ProductTaskViewSearchDTO dto) {
        //查询所有任务
        List<ProductTaskViewDTO> list = projectTaskMapper.getAllTaskPersonnelView(dto);
        //返回结果集
        List<ProductTaskPersonnelChildDTO> resultList = new LinkedList<>();
        if (CollectionUtils.isEmpty(list)) {
            return resultList;
        }
        //根据人员分组
        Map<String, List<ProductTaskViewDTO>> map = list.stream().collect(Collectors.groupingBy(ProductTaskViewDTO::getChargeId));
        int parentId = 1;
        for (Map.Entry<String,List<ProductTaskViewDTO>> entry : map.entrySet()) {
            List<ProductTaskViewDTO> value = entry.getValue();
            //最小计划开始时间
            Date minStartTime = value.stream().filter(obj-> ObjectUtils.isNotNull(obj.getPlanStartTime())).sorted(Comparator.comparing(ProductTaskViewDTO::getPlanStartTime)).map(ProductTaskViewDTO::getPlanStartTime).findFirst().orElse(null);
            //最大计划结束时间
            Date maxEndTime = value.stream().filter(obj-> ObjectUtils.isNotNull(obj.getPlanEndTime())).sorted(Comparator.comparing(ProductTaskViewDTO::getPlanEndTime).reversed()).map(ProductTaskViewDTO::getPlanEndTime).findFirst().orElse(null);
            String chargeName = value.get(0).getChargeName();
            ProductTaskPersonnelChildDTO parentDto = new ProductTaskPersonnelChildDTO();
            parentDto.setId(parentId);
            parentDto.setParentId(IsConstant.NO);
            parentDto.setChargeId(entry.getKey());
            parentDto.setChargeName(chargeName);
            parentDto.setPlanStartTime(minStartTime);
            parentDto.setPlanEndTime(maxEndTime);
            parentId ++;
            //同一人员下的任务
            List<ProductTaskPersonnelChildDTO> childrenList = new LinkedList<>();
            for (ProductTaskViewDTO obj:value) {
                ProductTaskPersonnelChildDTO childDto = new ProductTaskPersonnelChildDTO();
                BeanMapperUtils.copy(obj,childDto);
                childDto.setStatusName(TaskStateEnum.getName(obj.getStatus()));
                childDto.setId(parentId);
                childDto.setParentId(parentDto.getId());
                childrenList.add(childDto);
                parentId ++;
            };
            resultList.add(parentDto);
            resultList.addAll(childrenList);
        }
        return resultList;
    }

    /**
     * @description: 项目视图按产品查询
     * @author Will
     * @date: 2022/11/23 12:00
     * @param dto
     * @return List<ProductTaskProductChildDTO>
     */
    @Override
    public List<ProductTaskProductChildDTO> getProductView(ProductTaskViewSearchDTO dto) {
        //查询所有任务
        List<ProductTaskViewDTO> list = projectTaskMapper.getAllTaskProductView(dto);
        //返回结果集
        List<ProductTaskProductChildDTO> resultList = new LinkedList<>();
        if (CollectionUtils.isEmpty(list)) {
            return resultList;
        }
        //根据产品分组
        Map<String, List<ProductTaskViewDTO>> map = list.stream().collect(Collectors.groupingBy(ProductTaskViewDTO::getProductId));
        int parentId = 1;
        for (Map.Entry<String,List<ProductTaskViewDTO>> entry : map.entrySet()) {
            List<ProductTaskViewDTO> value = entry.getValue();
            //最小计划开始时间
            Date minStartTime = value.stream().filter(obj-> ObjectUtils.isNotNull(obj.getPlanStartTime())).sorted(Comparator.comparing(ProductTaskViewDTO::getPlanStartTime)).map(ProductTaskViewDTO::getPlanStartTime).findFirst().orElse(null);
            //最大计划结束时间
            Date maxEndTime = value.stream().filter(obj-> ObjectUtils.isNotNull(obj.getPlanEndTime())).sorted(Comparator.comparing(ProductTaskViewDTO::getPlanEndTime).reversed()).map(ProductTaskViewDTO::getPlanEndTime).findFirst().orElse(null);
            String productName = value.get(0).getProductName();
            ProductTaskProductChildDTO parentDto = new ProductTaskProductChildDTO();
            parentDto.setId(parentId);
            parentDto.setParentId(IsConstant.NO);
            parentDto.setProductId(entry.getKey());
            parentDto.setProductName(productName);
            parentDto.setPlanStartTime(minStartTime);
            parentDto.setPlanEndTime(maxEndTime);
            parentId ++;
            //同一产品下的任务
            List<ProductTaskProductChildDTO> childrenList = new LinkedList<>();
            for (ProductTaskViewDTO obj:value) {
                ProductTaskProductChildDTO childDto = new ProductTaskProductChildDTO();
                BeanMapperUtils.copy(obj,childDto);
                childDto.setStatusName(TaskStateEnum.getName(obj.getStatus()));
                childDto.setId(parentId);
                childDto.setParentId(parentDto.getId());
                childrenList.add(childDto);
                parentId ++;
            };
            resultList.add(parentDto);
            resultList.addAll(childrenList);
        }
        return resultList;
    }

    /**
     * @description: 项目视图按阶段查询
     * @author Will
     * @date: 2022/11/23 12:01
     * @param dto
     * @return List<ProductTaskPhaseChildDTO>
     */
    @Override
    public List<ProductTaskPhaseChildDTO> getPhaseView(ProductTaskViewSearchDTO dto) {
        //查询所有任务
        List<ProductTaskViewDTO> list = projectTaskMapper.getAllTaskPhaseView(dto);
        //返回结果集
        List<ProductTaskPhaseChildDTO> resultList = new LinkedList<>();
        if (CollectionUtils.isEmpty(list)) {
            return resultList;
        }
        //根据产品分组
        Map<String, List<ProductTaskViewDTO>> map = list.stream().collect(Collectors.groupingBy(ProductTaskViewDTO::getPhaseName));
        int parentId = 1;
        for (Map.Entry<String,List<ProductTaskViewDTO>> entry : map.entrySet()) {
            List<ProductTaskViewDTO> value = entry.getValue();
            //最小计划开始时间
            Date minStartTime = value.stream().filter(obj-> ObjectUtils.isNotNull(obj.getPlanStartTime())).sorted(Comparator.comparing(ProductTaskViewDTO::getPlanStartTime)).map(ProductTaskViewDTO::getPlanStartTime).findFirst().orElse(null);
            //最大计划结束时间
            Date maxEndTime = value.stream().filter(obj-> ObjectUtils.isNotNull(obj.getPlanEndTime())).sorted(Comparator.comparing(ProductTaskViewDTO::getPlanEndTime).reversed()).map(ProductTaskViewDTO::getPlanEndTime).findFirst().orElse(null);
            ProductTaskPhaseChildDTO parentDto = new ProductTaskPhaseChildDTO();
            parentDto.setId(parentId);
            parentDto.setParentId(IsConstant.NO);
            parentDto.setPhaseName(entry.getKey());
            parentDto.setPlanEndTime(minStartTime);
            parentDto.setPlanEndTime(maxEndTime);
            parentId ++;
            //同一阶段下的任务
            List<ProductTaskPhaseChildDTO> childrenList = new LinkedList<>();
            for (ProductTaskViewDTO obj:value) {
                ProductTaskPhaseChildDTO childDto = new ProductTaskPhaseChildDTO();
                BeanMapperUtils.copy(obj,childDto);
                childDto.setStatusName(TaskStateEnum.getName(obj.getStatus()));
                childDto.setId(parentId);
                childDto.setParentId(parentDto.getId());
                childrenList.add(childDto);
                parentId ++;
            };
            resultList.add(parentDto);
            resultList.addAll(childrenList);
        }
        return resultList;
    }

    /**
     * @description: 项目视图按量产入库时间查询
     * @author Will
     * @date: 2022/11/23 12:01
     * @param dto
     * @return List<ProductTaskInWarehouseTimeChildDTO>
     */
    @Override
    public List<ProductTaskInWarehouseTimeChildDTO> getInWarehouseTimeView(ProductTaskViewSearchDTO dto) {
        //查询所有任务
        List<ProductTaskInWarehouseTimeChildDTO> list = projectTaskMapper.getAllTaskInWarehouseTimeView(dto);
        //返回结果集
        List<ProductTaskInWarehouseTimeChildDTO> resultList = new LinkedList<>();
        if (CollectionUtils.isEmpty(list)) {
            return resultList;
        }
        //根据产品分组
        Map<String, List<ProductTaskInWarehouseTimeChildDTO>> map = list.stream().filter(obj-> StringUtils.isNotBlank(obj.getTimeInterval())).collect(Collectors.groupingBy(ProductTaskInWarehouseTimeChildDTO::getTimeInterval));
        int parentId = 1;
        for (Map.Entry<String,List<ProductTaskInWarehouseTimeChildDTO>> entry : map.entrySet()) {
            List<ProductTaskInWarehouseTimeChildDTO> value = entry.getValue();
            String timeInterval = value.get(0).getTimeInterval();
            ProductTaskInWarehouseTimeChildDTO parentDto = new ProductTaskInWarehouseTimeChildDTO();
            parentDto.setTimeInterval(timeInterval);
            parentDto.setId(parentId);
            parentDto.setParentId(IsConstant.NO);
            parentId ++;
            //同一时间区间下的任务
            List<ProductTaskInWarehouseTimeChildDTO> childrenList = new LinkedList<>();
            for (ProductTaskInWarehouseTimeChildDTO obj:value) {
                ProductTaskInWarehouseTimeChildDTO childDto = new ProductTaskInWarehouseTimeChildDTO();
                BeanMapperUtils.copy(obj,childDto);
                //判断是项目状态还是产品状态
                childDto.setStatusName(obj.getIsProjectStatus().equals(IsConstant.YES) ? ProjectStateEnum.getName(obj.getStatus()) : ApprovalStatusEnum.getName(obj.getStatus()));
                childDto.setId(parentId);
                childDto.setParentId(parentDto.getId());
                childrenList.add(childDto);
                parentId ++;
            };
            resultList.add(parentDto);
            resultList.addAll(childrenList);
        }
        return resultList;
    }

    @Override
    public void exportExcel(ProductTaskViewSearchDTO dto, HttpServletResponse response) {
        //导出时类型必填
        if (ObjectUtils.isNull(dto.getType())) {
            throw new ServiceException(ApiError.ERROR_95075);
        }
        switch (dto.getType()) {
            case 1 :
                exportExcelByPersonnel(dto);
                break;
            case 2 :
                exportExcelByProduct(dto);
                break;
            case 3 :
                exportExcelByPhase(dto);
                break;
            case 4 :
                exportExcelByInWarehouseTime(dto);
                break;
            default:
                break;
        }
    }

    /**
     * @description: 按人员导出
     * @author Will
     * @date: 2022/11/23 18:52
     * @param dto
     */
    private void exportExcelByPersonnel(ProductTaskViewSearchDTO dto) {
        //查询所有任务
        List<ProductTaskViewDTO> list = projectTaskMapper.getAllTaskPersonnelView(dto);
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        list.stream().sorted(Comparator.comparing(ProductTaskViewDTO::getChargeId)).forEach(obj->{obj.setStatusName(TaskStateEnum.getName(obj.getStatus()));});
        List<ProductTaskViewPersonnelExcelDTO> excelList = BeanMapperUtils.copyList(ProductTaskViewPersonnelExcelDTO.class, list);
        String fileName = getFileName("按人员导出");
        ExcelUtil.export(fileName, "按人员导出", excelList, ProductTaskViewPersonnelExcelDTO.class, response);
        return;
    }

    /**
     * @description: 按产品导出
     * @author Will
     * @date: 2022/11/23 18:52
     * @param dto
     */
    private void exportExcelByProduct(ProductTaskViewSearchDTO dto) {
        //查询所有任务
        List<ProductTaskViewDTO> list = projectTaskMapper.getAllTaskProductView(dto);
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        list.stream().sorted(Comparator.comparing(ProductTaskViewDTO::getProductId)).forEach(obj->{obj.setStatusName(TaskStateEnum.getName(obj.getStatus()));});
        List<ProductTaskViewProductExcelDTO> excelList = BeanMapperUtils.copyList(ProductTaskViewProductExcelDTO.class, list);
        String fileName = getFileName("按产品导出");
        ExcelUtil.export(fileName, "按产品导出", excelList, ProductTaskViewProductExcelDTO.class, response);
        return;
    }

    /**
     * @description: 按阶段导出
     * @author Will
     * @date: 2022/11/23 18:53
     * @param dto
     */
    private void exportExcelByPhase(ProductTaskViewSearchDTO dto) {
        //查询所有任务
        List<ProductTaskViewDTO> list = projectTaskMapper.getAllTaskPhaseView(dto);
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        list.stream().sorted(Comparator.comparing(ProductTaskViewDTO::getPhaseName)).forEach(obj->{obj.setStatusName(TaskStateEnum.getName(obj.getStatus()));});
        List<ProductTaskViewPhaseExcelDTO> excelList = BeanMapperUtils.copyList(ProductTaskViewPhaseExcelDTO.class, list);
        String fileName = getFileName("按阶段导出");
        ExcelUtil.export(fileName, "按阶段导出", excelList, ProductTaskViewPhaseExcelDTO.class, response);
        return;
    }

    /**
     * @description: 按量产入库时间导出
     * @author Will
     * @date: 2022/11/23 18:53
     * @param dto

     */
    private void exportExcelByInWarehouseTime(ProductTaskViewSearchDTO dto) {
        //查询所有任务
        List<ProductTaskInWarehouseTimeChildDTO> list = projectTaskMapper.getAllTaskInWarehouseTimeView(dto);
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        list.stream().sorted(Comparator.comparing(ProductTaskInWarehouseTimeChildDTO::getTimeInterval)).forEach(obj->{obj.setStatusName(obj.getIsProjectStatus().equals(IsConstant.YES) ? ProjectStateEnum.getName(obj.getStatus()) : ApprovalStatusEnum.getName(obj.getStatus()));});
        List<ProductTaskViewInWarehouseTimeExcelDTO> excelList = BeanMapperUtils.copyList(ProductTaskViewInWarehouseTimeExcelDTO.class, list);
        String fileName = getFileName("按量产入库时间导出");
        ExcelUtil.export(fileName, "按量产入库时间导出", excelList, ProductTaskViewInWarehouseTimeExcelDTO.class, response);
        return;
    }

    /**
     * @description: 导出文件名称
     * @author Will
     * @date: 2022/11/24 14:23
     * @param fileName
     * @return String
     */
    private String getFileName(String fileName) {
        StringBuffer sb = new StringBuffer();
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(fileName);
        sb.append(date);
        String redisKey = "file:name:" + date;
        Integer last = redisService.getCacheObject(redisKey);
        Integer lastNo = 1;
        if (last != null) {
            lastNo = last + 1;
        }
        redisService.setCacheObject(redisKey, lastNo, (long) 1, TimeUnit.DAYS);
        return sb.append(lastNo).toString();

    }
}
