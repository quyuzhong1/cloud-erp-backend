package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.entity.ProjectInfoEntity;

import java.util.List;

/**
 * <p>
 * 产品项目表 服务类
 * </p>
 *
 * @author yl
 * @since 2022-09-13
 */
public interface ProjectInfoService extends IService<ProjectInfoEntity> {


    ProjectInfoDTO projectInfo(ProductTaskCountShowDTO productTaskCountShowDTO);


    /**
     * 启动项目
     * @param dto
     * @return
     */
    Boolean startProject(StartProjectDTO dto);



    void updateCharge(String projectId, String useName, String userId,Boolean isUpdate);

    PagingVO<List<ProductShowDTO>> paging(PagingDTO<ProductSearchDTO> dto);

    void checkProjectFinish(String productId);

    List<StartItemSourceDTO> getStartItemSourceList();

    
    /**
     * 添加项目
     * @author yl
     * @date 2023-03-10 16:48
     * @param productId
     * @param productName
     * @param projectChargeId
     * @return void
     */
    void addProject(String productId,String productName,String projectChargeId);

    void removeByProductId(String productId);

    boolean archive(String productId);

    ProjectInfoEntity getByProductId(String productId);

    List<BasicDTO> listProjectInfo(ProductSearchDTO dto);

    List<ProjectInfoEntity> getByProductIdList(List<String> productIdList);

    void updateChargeByProductId(String productId, String projectChargeId);

    /**
     * 统计
     * @author yl
     * @date 2023-06-13 15:18
     * @param productIdList
     * @return java.util.List<com.erp.model.plm.dto.ProductDTO.CountBaseDTO>
     */
    List<ProductDTO.CountBaseDTO> listStatusCount(List<String> productIdList);
    /**
     * 获取到延期的数量
     * @author yl
     * @ate 2023-06-13 15:50
     * @param productIdList
     * @return int
     */
    int getDelayCount(List<String> productIdList);
}
