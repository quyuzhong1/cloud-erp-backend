package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.entity.ProductArchiveEntity;
import com.erp.model.plm.entity.ProductInfoEntity;
import com.erp.model.plm.entity.ProjectTaskEntity;
import com.erp.server.plm.constant.IsConstant;
import com.erp.server.plm.enums.ApprovalStatusEnum;
import com.erp.server.plm.enums.ProductMilepostEnum;
import com.erp.server.plm.enums.TaskStateEnum;
import com.erp.server.plm.mapper.ProjectTaskRefSkuMapper;
import com.erp.server.plm.service.ProductArchiveService;
import com.erp.server.plm.service.ProductInfoService;
import com.erp.server.plm.service.ProjectTaskProgressService;
import com.erp.server.plm.service.ProjectTaskService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/11/23 19:04
 */
@Service
public class ProjectTaskProgressServiceImpl implements ProjectTaskProgressService {

    @Autowired
    private ProductInfoService productInfoService;

    @Autowired
    private ProjectTaskService projectTaskService;

    @Autowired
    private ProductArchiveService productArchiveService;

    @Autowired
    private ProjectTaskRefSkuMapper projectTaskRefSkuMapper;

    /**
     * @param productId
     * @return List<ProductMilepostDTO>
     * @description: 根据产品id获取里程碑任务
     * @author Will
     * @date: 2022/11/18 16:20
     */
    @Override
    public List<ProductMilepostDTO> getMilepostTaskListByProductId(String productId) {
        //返回结果
        List<ProductMilepostDTO> resultList = new ArrayList<>();
        //查询产品信息，创建初始里程碑
        ProductInfoEntity productInfoEntity = productInfoService.getById(productId);
        if (ObjectUtils.isEmpty(productInfoEntity)) {
            throw new ServiceException(ApiError.ERROR_95010);
        }
        ProductMilepostDTO startDto = new ProductMilepostDTO();
        startDto.setName(ProductMilepostEnum.START_MILEPOST_MILEPOST.getName());
        resultList.add(startDto);
        //判断产品是否立项
        if (ApprovalStatusEnum.APPROVAL.equals(productInfoEntity.getApprovalStatus())) {
            //创建立项里程碑
            ProductMilepostDTO approvalDto = new ProductMilepostDTO();
            approvalDto.setName(ProductMilepostEnum.PROJECT_APPROVAL_MILEPOST.getName());
            resultList.add(approvalDto);
            //查询产品下面的任务
            List<ProjectTaskEntity> taskList = projectTaskService.getByProductId(productId);
            if (CollectionUtils.isEmpty(taskList)) {
                throw new ServiceException(ApiError.ERROR_95027);
            }
            taskList.stream().filter(e -> IsConstant.YES.equals(e.getIsMilepost())).forEach(obj -> {
                //创建产品任务里程碑
                ProductMilepostDTO dto = new ProductMilepostDTO();
                dto.setTaskId(obj.getId());
                dto.setName(obj.getName());
                resultList.add(dto);
            });
            //查询产品是否已经归档
            ProductArchiveEntity productArchiveEntity = productArchiveService.getArchiveByProductId(productId);
            if (ObjectUtils.isNotEmpty(productArchiveEntity)) {
                //创建归档里程碑
                ProductMilepostDTO archiveDto = new ProductMilepostDTO();
                archiveDto.setName(ProductMilepostEnum.PROJECT_ARCHIVE_MILEPOST.getName());
                resultList.add(archiveDto);
            }
        }
        return resultList;
    }

    /**
     * @param dto
     * @return ProductMilepostDateDTO
     * @description: 查询里程碑结束时间
     * @author Will
     * @date: 2022/11/21 9:28
     */
    @Override
    public ProductMilepostDateDTO getMilepostDate(ProductMilepostParamDTO dto) {
        Integer type = dto.getType();
        ProductMilepostDateDTO dateDTO = new ProductMilepostDateDTO();
        switch (type) {
            case 1:
                //创建里程碑结束时间
                dateDTO = this.getStartMilepostDate(dto.getProductId(), dateDTO);
                break;
            case 2:
                //立项里程碑结束时间
                dateDTO = this.getApprovalMilepostDate(dto.getProductId(), dateDTO);
                break;
            case 3:
                //任务里程碑结束时间
                dateDTO = this.getTaskMilepostDate(dto.getTaskId(), dateDTO);
                break;
            case 4:
                //归档里程碑结束时间
                dateDTO = this.getArchiveMilepostDate(dto.getProductId(), dateDTO);
                break;
            default:
                break;
        }
        return dateDTO;
    }

    /**
     * @param productId
     * @return List<ProductPhaseProgressDTO>
     * @description: 查询任务完成进度
     * @author Will
     * @date: 2022/11/21 10:39
     */
    @Override
    public List<ProductPhaseProgressDTO> getFinishProgressList(String productId) {
        //查询产品信息
        ProductInfoEntity productInfoEntity = productInfoService.getById(productId);
        if (ObjectUtils.isEmpty(productInfoEntity)) {
            throw new ServiceException(ApiError.ERROR_95010);
        }
        //查询产品下面的任务
        List<ProjectTaskEntity> taskList = projectTaskService.getByProductId(productId);
        Map<String, List<ProjectTaskEntity>> map = taskList.stream().collect(Collectors.groupingBy(ProjectTaskEntity::getPhaseName));
        //查询产品下面的sku
        List<ProductTaskRefSkuDTO> refList = projectTaskRefSkuMapper.getTaskRefSkuName(productId);
        //结果集
        List<ProductPhaseProgressDTO> resultList = new ArrayList<>();

        for (Map.Entry<String, List<ProjectTaskEntity>> entry : map.entrySet()) {
            //阶段进度对象
            ProductPhaseProgressDTO phaseDto = new ProductPhaseProgressDTO();
            //阶段名称
            String phaseName = entry.getKey();
            //阶段下任务集合
            List<ProjectTaskEntity> value = entry.getValue();
            //任务总数量
            long totalCount = value.stream().count();
            //任务完成数量
            long finishCount = value.stream().filter(obj -> TaskStateEnum.APPROVAL_PASS.getCode().equals(obj.getStatus())).count();

            List<ProductSkuProgressDTO> skuList = new ArrayList<>();
            phaseDto.setPhaseName(phaseName);
            phaseDto.setTotalQty(totalCount);
            phaseDto.setFinishQty(finishCount);
            //添加sku任务进度
            if (CollectionUtils.isNotEmpty(refList)) {
                Map<String, List<ProductTaskRefSkuDTO>> refMap = refList.stream().collect(Collectors.groupingBy(ProductTaskRefSkuDTO::getSkuId));
                for (Map.Entry<String, List<ProductTaskRefSkuDTO>> refEntry : refMap.entrySet()) {
                    List<ProductTaskRefSkuDTO> refValue = refEntry.getValue();
                    //sku进度对象
                    ProductSkuProgressDTO skuDto = new ProductSkuProgressDTO();
                    //任务id集合
                    List<String> taskIds = refValue.stream().distinct().map(ProductTaskRefSkuDTO::getTaskId).collect(Collectors.toList());
                    //sku名称
                    String skuName = refEntry.getValue().get(0).getSkuName();
                    //sku下任务总数
                    long skuTotalCount = refValue.stream().count();
                    //sku下任务完成数量
                    long skuFinishCount = value.stream().filter(obj -> taskIds.contains(obj.getId()) && TaskStateEnum.APPROVAL_PASS.getCode().equals(obj.getStatus())).count();
                    skuDto.setSkuName(skuName);
                    skuDto.setTotalQty(skuTotalCount);
                    skuDto.setFinishQty(skuFinishCount);
                    skuList.add(skuDto);
                }
                phaseDto.setSkuList(skuList);
            }
            resultList.add(phaseDto);
        }
        return resultList;
    }

    /**
     * @param productId
     * @param dateDTO
     * @return ProductMilepostDateDTO
     * @description: 创建里程碑结束时间
     * @author Will
     * @date: 2022/11/18 18:37
     */
    private ProductMilepostDateDTO getStartMilepostDate(String productId, ProductMilepostDateDTO dateDTO) {
        ProductInfoEntity productInfoEntity = productInfoService.getById(productId);
        dateDTO.setRealityEndTime(productInfoEntity.getCreateTime());
        return dateDTO;

    }


    /**
     * @param productId
     * @param dateDTO
     * @return ProductMilepostDateDTO
     * @description: 立项里程碑结束时间
     * @author Will
     * @date: 2022/11/18 18:37
     */
    private ProductMilepostDateDTO getApprovalMilepostDate(String productId, ProductMilepostDateDTO dateDTO) {
        ProductInfoEntity productInfoEntity = productInfoService.getById(productId);
        dateDTO.setRealityEndTime(productInfoEntity.getApprovalTime());
        return dateDTO;
    }


    /**
     * @param taskId
     * @param dateDTO
     * @return ProductMilepostDateDTO
     * @description: 任务里程碑结束时间
     * @author Will
     * @date: 2022/11/18 18:37
     */
    private ProductMilepostDateDTO getTaskMilepostDate(String taskId, ProductMilepostDateDTO dateDTO) {
        ProjectTaskEntity projectTaskEntity = projectTaskService.getById(taskId);
        dateDTO.setPlanEndTime(projectTaskEntity.getPlanEndTime());
        dateDTO.setRealityEndTime(projectTaskEntity.getRealityEndTime());
        return dateDTO;
    }


    /**
     * @param productId
     * @param dateDTO
     * @return ProductMilepostDateDTO
     * @description: 归档里程碑结束时间
     * @author Will
     * @date: 2022/11/18 18:37
     */
    private ProductMilepostDateDTO getArchiveMilepostDate(String productId, ProductMilepostDateDTO dateDTO) {
        ProductArchiveEntity productArchiveEntity = productArchiveService.getArchiveByProductId(productId);
        dateDTO.setRealityEndTime(productArchiveEntity.getCreateTime());
        return dateDTO;
    }

}
