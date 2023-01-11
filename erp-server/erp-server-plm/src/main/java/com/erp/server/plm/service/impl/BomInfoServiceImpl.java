package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.BusinessNoCreateUtil;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import com.erp.common.modules.sys.dto.FindUserDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.plm.dto.AddBomDTO;
import com.erp.model.plm.dto.BomDTO;
import com.erp.model.plm.dto.BomSearchPagingDTO;
import com.erp.model.plm.dto.BomSkuDTO;
import com.erp.model.plm.entity.BomInfoEntity;
import com.erp.model.plm.vo.BomPagingVO;
import com.erp.model.plm.vo.SkuVO;
import com.erp.server.plm.constant.BomConstant;
import com.erp.server.plm.constant.BomOperateContent;
import com.erp.server.plm.enums.BomOperationTypeEnum;
import com.erp.server.plm.enums.BomStateEnum;
import com.erp.server.plm.mapper.BomInfoMapper;
import com.erp.server.plm.service.*;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * bom 信息表(BomInfo)表服务实现类
 *
 * @author yl
 * @since 2023-01-09 11:45:28
 */
@Service
public class BomInfoServiceImpl extends ServiceImpl<BomInfoMapper, BomInfoEntity> implements BomInfoService {


    @Resource
    private BomSkuService bomSkuService;

    @Resource
    private BomOperateLogService bomOperateLogService;


    @Resource
    private ProductDetailService productDetailService;

    @Resource
    private CommonService commonService;

    /**
     * 添加bom
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-01-09 12:18
     */
    @Override
    @Transactional
    public Boolean insert(AddBomDTO dto) {
        //sku信息
        List<BomSkuDTO> bomSkuList = dto.getSkuList();
        if (CollectionUtils.isEmpty(bomSkuList)) {
            throw new ServiceException(ApiError.ERROR_95094);
        }
        //获取到最大的序号
        Integer maxSequence = getMaxSequence();
        //获取到 编号
        String serialNumber = BusinessNoCreateUtil.getBusinessNo(BomConstant.BOM, maxSequence);
        BomInfoEntity bom = new BomInfoEntity();
        String bomId = IdWorker.getIdStr();
        bom.setType(dto.getType());
        bom.setVersion(dto.getVersion());
        bom.setId(bomId);
        bom.setSerialNumber(serialNumber);
        bom.setSequence(maxSequence + 1);
        String submitAudit = BomConstant.SUBMIT_AUDIT;
        boolean isSubmitAudit = submitAudit.equals(dto.getSubmitType());
        if (isSubmitAudit) {
            bom.setState(BomStateEnum.WAIT_AUDIT.getState());
        }
        Boolean saveResult = this.save(bom);
        //保存成功
        if (saveResult) {
            //但是待审核的时候
            if (isSubmitAudit) {
                //这里要发起一个流程
            }
            //添加 bom 与sku 关系
            bomSkuService.saveBomSku(bomId, bomSkuList);

            //添加 bom的操作日志
            String operateContent = String.format(BomOperateContent.ADD, serialNumber);
            bomOperateLogService.saveOperate(bomId, BomOperationTypeEnum.ADD.getType(), operateContent);

        }

        return saveResult;
    }


    /**
     * 分页获取bom 列表
     *
     * @param dto
     * @return com.erp.common.vo.PagingVO<java.util.List < com.erp.model.plm.vo.BomPagingVO>>
     * @author yl
     * @date 2023-01-10 17:30
     */
    @Override
    public PagingVO<List<BomPagingVO>> paging(PagingDTO<BomSearchPagingDTO> dto) {
        BomSearchPagingDTO params = dto.getParams();
        List<FindUserDTO> userList = commonService.getAllUser();
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage pageData = baseMapper.paging(query, params);
        List<BomPagingVO> list = pageData.getRecords();
        //对应sku集合
        List<String> skuNoList = list.stream().map(BomPagingVO::getSkuNo).collect(Collectors.toList());
        List<SkuVO> skuList = productDetailService.getSkuBySkuNos(skuNoList);
        for (BomPagingVO item : list) {
            String skuNo = item.getSkuNo();
            SkuVO skuVO = skuList.stream().filter(s -> s.getSkuNo().equals(skuNo)).findFirst().orElse(null);
            FindUserDTO createUser = userList.stream().filter(u -> u.getUserId().equals(item.getCreateUserId())).findFirst().orElse(null);
            if (createUser != null) {
                item.setCreateUserName(createUser.getUserName());
            }
            Integer state = item.getState();
            item.setStateName(BomStateEnum.getName(state));
            if (skuVO != null) {
                item.setSkuName(skuVO.getSkuName());
                item.setSpuNo(skuVO.getSpuNo());
                item.setSpuName(skuVO.getSpuName());
            }
        }
        return new PagingVO(pageData);
    }

    @Override
    public BomDTO getBomDetails(String id) {
        BomDTO result = new BomDTO();
        BomInfoEntity bom = this.getById(id);
        if (Objects.isNull(bom)) {
            throw new ServiceException(ApiError.ERROR_95095);
        }
        BeanMapper.copy(bom,result);
        List<BomSkuDTO> skuList=bomSkuService.getByBomId(id);
        result.setSkuList(skuList);
        return result;
    }


    /**
     * 获取到最大的编号
     *
     * @param
     * @return java.lang.Integer
     * @author yl
     * @date 2023-01-10 14:58
     */
    private Integer getMaxSequence() {
        return baseMapper.getMaxSequence();
    }
}
