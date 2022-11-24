package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.date.DateUtil;
import com.common.web.service.RedisService;
import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import com.erp.model.plm.dto.*;
import com.erp.server.plm.enums.ApprovalStatusEnum;
import com.erp.server.plm.enums.TaskStateEnum;
import com.erp.server.plm.mapper.ProjectTaskMapper;
import com.erp.server.plm.service.ProjectTaskViewService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
     * @return List<ProductTaskPersonnelViewDTO>
     */
    @Override
    public List<ProductTaskPersonnelViewDTO> getPersonnelView(ProductTaskViewSearchDTO dto) {
        //查询所有任务
        List<ProductTaskViewDTO> list = projectTaskMapper.getAllTaskPersonnelView(dto);
        //返回结果集
        List<ProductTaskPersonnelViewDTO> resultList = new LinkedList<>();
        if (CollectionUtils.isEmpty(list)) {
            return resultList;
        }
        //根据人员分组
        Map<String, List<ProductTaskViewDTO>> map = list.stream().collect(Collectors.groupingBy(ProductTaskViewDTO::getChargeId));
        for (Map.Entry<String,List<ProductTaskViewDTO>> entry : map.entrySet()) {
            List<ProductTaskViewDTO> value = entry.getValue();
            String chargeName = value.get(0).getChargeName();
            ProductTaskPersonnelViewDTO parentDto = new ProductTaskPersonnelViewDTO();
            parentDto.setChargeId(entry.getKey());
            parentDto.setChargeName(chargeName);
            //同一人员下的任务
            List<ProductTaskPersonnelChildDTO> childrenList = new LinkedList<>();
            value.forEach(obj->{
                ProductTaskPersonnelChildDTO childDto = new ProductTaskPersonnelChildDTO();
                BeanMapperUtils.copy(obj,childDto);
                childDto.setStatusName(TaskStateEnum.getName(obj.getStatus()));
                childrenList.add(childDto);
            });
            parentDto.setChildrenList(childrenList);
            resultList.add(parentDto);
        }
        return resultList;
    }

    /**
     * @description: 项目视图按产品查询
     * @author Will
     * @date: 2022/11/23 12:00
     * @param dto
     * @return List<ProductTaskProductViewDTO>
     */
    @Override
    public List<ProductTaskProductViewDTO> getProductView(ProductTaskViewSearchDTO dto) {
        //查询所有任务
        List<ProductTaskViewDTO> list = projectTaskMapper.getAllTaskProductView(dto);
        //返回结果集
        List<ProductTaskProductViewDTO> resultList = new LinkedList<>();
        if (CollectionUtils.isEmpty(list)) {
            return resultList;
        }
        //根据产品分组
        Map<String, List<ProductTaskViewDTO>> map = list.stream().collect(Collectors.groupingBy(ProductTaskViewDTO::getProductId));
        for (Map.Entry<String,List<ProductTaskViewDTO>> entry : map.entrySet()) {
            List<ProductTaskViewDTO> value = entry.getValue();
            String productName = value.get(0).getProductName();
            ProductTaskProductViewDTO parentDto = new ProductTaskProductViewDTO();
            parentDto.setProductId(entry.getKey());
            parentDto.setProductName(productName);
            //同一产品下的任务
            List<ProductTaskProductChildDTO> childrenList = new LinkedList<>();
            value.forEach(obj->{
                ProductTaskProductChildDTO childDto = new ProductTaskProductChildDTO();
                BeanMapperUtils.copy(obj,childDto);
                childDto.setStatusName(TaskStateEnum.getName(obj.getStatus()));
                childrenList.add(childDto);
            });
            parentDto.setChildrenList(childrenList);
            resultList.add(parentDto);
        }
        return resultList;
    }

    /**
     * @description: 项目视图按阶段查询
     * @author Will
     * @date: 2022/11/23 12:01
     * @param dto
     * @return List<ProductTaskPhaseViewDTO>
     */
    @Override
    public List<ProductTaskPhaseViewDTO> getPhaseView(ProductTaskViewSearchDTO dto) {
        //查询所有任务
        List<ProductTaskViewDTO> list = projectTaskMapper.getAllTaskPhaseView(dto);
        //返回结果集
        List<ProductTaskPhaseViewDTO> resultList = new LinkedList<>();
        if (CollectionUtils.isEmpty(list)) {
            return resultList;
        }
        //根据产品分组
        Map<String, List<ProductTaskViewDTO>> map = list.stream().collect(Collectors.groupingBy(ProductTaskViewDTO::getPhaseName));
        int seq = 1;
        for (Map.Entry<String,List<ProductTaskViewDTO>> entry : map.entrySet()) {
            List<ProductTaskViewDTO> value = entry.getValue();
            ProductTaskPhaseViewDTO parentDto = new ProductTaskPhaseViewDTO();
            parentDto.setSeq(seq);
            parentDto.setPhaseName(entry.getKey());
            //同一阶段下的任务
            List<ProductTaskPhaseChildDTO> childrenList = new LinkedList<>();
            int finalSeq = seq;
            value.forEach(obj->{
                ProductTaskPhaseChildDTO childDto = new ProductTaskPhaseChildDTO();
                BeanMapperUtils.copy(obj,childDto);
                childDto.setStatusName(TaskStateEnum.getName(obj.getStatus()));
                childDto.setSeq(finalSeq);
                childrenList.add(childDto);
            });
            parentDto.setChildrenList(childrenList);
            resultList.add(parentDto);
            seq ++;
        }
        return resultList;
    }

    /**
     * @description: 项目视图按量产入库时间查询
     * @author Will
     * @date: 2022/11/23 12:01
     * @param dto
     * @return List<ProductTaskInWarehouseTimeViewDTO>
     */
    @Override
    public List<ProductTaskInWarehouseTimeViewDTO> getInWarehouseTimeView(ProductTaskViewSearchDTO dto) {
        //查询所有任务
        List<ProductTaskInWarehouseTimeChildDTO> list = projectTaskMapper.getAllTaskInWarehouseTimeView(dto);
        //返回结果集
        List<ProductTaskInWarehouseTimeViewDTO> resultList = new LinkedList<>();
        if (CollectionUtils.isEmpty(list)) {
            return resultList;
        }
        //根据产品分组
        Map<String, List<ProductTaskInWarehouseTimeChildDTO>> map = list.stream().filter(obj-> StringUtils.isNotBlank(obj.getTimeInterval())).collect(Collectors.groupingBy(ProductTaskInWarehouseTimeChildDTO::getTimeInterval));
        for (Map.Entry<String,List<ProductTaskInWarehouseTimeChildDTO>> entry : map.entrySet()) {
            List<ProductTaskInWarehouseTimeChildDTO> value = entry.getValue();
            String timeInterval = value.get(0).getTimeInterval();
            ProductTaskInWarehouseTimeViewDTO parentDto = new ProductTaskInWarehouseTimeViewDTO();
            parentDto.setTimeInterval(timeInterval);
            //同一时间区间下的任务
            List<ProductTaskInWarehouseTimeChildDTO> childrenList = new LinkedList<>();
            value.forEach(obj->{
                ProductTaskInWarehouseTimeChildDTO childDto = new ProductTaskInWarehouseTimeChildDTO();
                BeanMapperUtils.copy(obj,childDto);
                childDto.setApprovalStatusName(ApprovalStatusEnum.getName(obj.getApprovalStatus()));
                childrenList.add(childDto);
            });
            parentDto.setChildrenList(childrenList);
            resultList.add(parentDto);
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
        List<ProductTaskViewPersonnelExcelDTO> excelList = BeanMapperUtils.copyList(ProductTaskViewPersonnelExcelDTO.class, list);
        list.forEach(obj->{obj.setStatusName(TaskStateEnum.getName(obj.getStatus()));});
        String fileName = getFileName("按人员导出");
        ExcelUtil.export(fileName, "按人员导出", excelList, ProductTaskViewPersonnelExcelDTO.class, response);
        return;
    }

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

    /**
     * @description: 按产品导出
     * @author Will
     * @date: 2022/11/23 18:52
     * @param dto
     */
    private void exportExcelByProduct(ProductTaskViewSearchDTO dto) {

    }

    /**
     * @description: 按阶段导出
     * @author Will
     * @date: 2022/11/23 18:53
     * @param dto
     */
    private void exportExcelByPhase(ProductTaskViewSearchDTO dto) {

    }

    /**
     * @description: 按入库时间导出
     * @author Will
     * @date: 2022/11/23 18:53
     * @param dto

     */
    private void exportExcelByInWarehouseTime(ProductTaskViewSearchDTO dto) {

    }


}
