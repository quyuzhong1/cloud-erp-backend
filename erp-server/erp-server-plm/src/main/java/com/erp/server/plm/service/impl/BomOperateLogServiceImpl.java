package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.common.dto.base.BaseIdDTO;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.modules.sys.dto.FindUserDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.plm.entity.BomOperateLogEntity;
import com.erp.model.plm.vo.BomOperateVO;
import com.erp.model.plm.enums.BomOperationTypeEnum;
import com.erp.server.plm.mapper.BomOperateLogMapper;
import com.erp.server.plm.service.BomOperateLogService;
import com.erp.server.plm.service.CommonService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * bom 操作记录日志表(BomOperateLog)表服务实现类
 *
 * @author yl
 * @since 2023-01-09 11:45:26
 */
@Service
public class BomOperateLogServiceImpl extends ServiceImpl<BomOperateLogMapper, BomOperateLogEntity> implements BomOperateLogService {


    @Resource
    private CommonService commonService;

    /**
     * 保存 bom 的操作记录
     *
     * @param bomId
     * @param operateType
     * @param content
     * @return void
     * @author yl
     * @date 2023-01-09 17:24
     */
    @Override
    public void saveOperate(String bomId, String operateType, String content) {
        BomOperateLogEntity operateLog = new BomOperateLogEntity();
        operateLog.setBomId(bomId);
        operateLog.setContent(content);
        operateLog.setType(operateType);
        this.save(operateLog);
    }

    /**
     * 获取bom的操作记录
     *
     * @param bomId
     * @return java.util.List<com.erp.model.plm.vo.BomOperateVO>
     * @author yl
     * @date 2023-01-11 17:55
     */
    @Override
    public List<BomOperateVO> getOperateLog(String bomId) {
        List<BomOperateLogEntity> list = getByBomId(bomId);
        List<BomOperateVO> resultList = BeanMapper.copyList(list, BomOperateVO.class);
        for (BomOperateVO item : resultList) {
            String type = item.getType();
            String typeName = BomOperationTypeEnum.getName(type);
            item.setTypeName(typeName);
        }
        return resultList;
    }

    @Override
    public PagingVO<List<BomOperateVO>> paging(PagingDTO<BaseIdDTO> dto) {
        BaseIdDTO params = dto.getParams();
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage pageData = baseMapper.paging(query, params.getId());
        List<FindUserDTO> userList = commonService.getAllUser();
        List<BomOperateVO> resultList = pageData.getRecords();
        for (BomOperateVO item : resultList) {
            String type = item.getType();
            String typeName = BomOperationTypeEnum.getName(type);
            item.setTypeName(typeName);
            FindUserDTO findUserDTO = userList.stream().filter(user -> user.getUserId().equals(item.getCreateUserId())).findFirst().orElse(null);
            if (findUserDTO != null) {
                item.setCreateUserName(findUserDTO.getUserName());
            }
        }
        return new PagingVO(pageData);
    }


    /**
     * 根据Bom 表id 获取到 信息
     *
     * @param
     * @return java.util.List<com.erp.model.plm.entity.BomOperateLogEntity>
     * @author yl
     * @date 2023-01-11 18:01
     */
    public List<BomOperateLogEntity> getByBomId(String bomId) {
        LambdaQueryWrapper<BomOperateLogEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BomOperateLogEntity::getBomId, bomId);
        queryWrapper.orderByDesc(BomOperateLogEntity::getCreateTime);
        return this.list(queryWrapper);
    }
}
