package com.erp.server.sys.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.constant.BusinessNoConstant;
import com.common.business.dto.base.BaseDropDownDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.StrUtils;
import com.erp.model.oms.entity.SoInfoEntity;
import com.erp.model.sys.dto.*;
import com.erp.model.sys.entity.SysDepartmentEntity;
import com.erp.model.sys.entity.SysDepartmentUserEntity;
import com.erp.server.sys.constant.SysConstant;
import com.erp.server.sys.mapper.SysDepartmentMapper;
import com.erp.server.sys.service.SysCodeService;
import com.erp.server.sys.service.SysDepartmentService;
import com.erp.server.sys.service.SysDepartmentUserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Service
public class SysDepartmentServiceImpl extends ServiceImpl<SysDepartmentMapper, SysDepartmentEntity> implements SysDepartmentService {

    @Autowired
    private SysDepartmentUserService sysDepartmentUserService;



    @Autowired
    private SysCodeService sysCodeService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdateSysDept(SysDepartmentEntity sysDepartment) {
        String id = sysDepartment.getId();
        if (StringUtils.isBlank(id)) {
            id = IdWorker.getIdStr();
        }
        String parentId = sysDepartment.getParentId();
        if (StringUtils.isBlank(parentId)) {
            sysDepartment.setParentId("0");
        }
        sysDepartment.setId(id);
        
        String name = sysDepartment.getName();
        if(StringUtils.isBlank(name)) {
        	throw new ServiceException("部门名称不能为空");
        }
        Integer nameCount = lambdaQuery().eq(SysDepartmentEntity::getName, name).ne(SysDepartmentEntity::getId, id).count();
        if(nameCount != null && nameCount > 0) {
        	throw new ServiceException("存在相同部门名称");
        }
        SysDepartmentEntity entity = this.getById(id);

        //编号赋值，为兼容历史数据修改数据无编码时也重新生成编码
        if (ObjectUtils.isEmpty(entity) || StringUtils.isBlank(entity.getCode())) {
            //编号
            String code = sysCodeService.getSeqNo(new SysCodeDTO(BusinessNoConstant.BM, BusinessNoTypeEnum.CODE_USER.getCode()));
            sysDepartment.setCode(code);
        }
        if (sysDepartment.getId().equals(sysDepartment.getParentId())) {
            throw new ServiceException("部门不能设置自己为上级部门");
        }
        this.saveOrUpdate(sysDepartment);


    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<BatchResultDTO> removeByIdList(List<String> ids) {
        List<SysDepartmentEntity> list = this.listByIds(ids);

        List<SysDepartmentEntity> removeList=new ArrayList<>();
        List<BatchResultDTO> resultDTOList=new ArrayList<>();
        for (SysDepartmentEntity entity : list) {
            LambdaQueryWrapper<SysDepartmentEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SysDepartmentEntity::getParentId, entity.getId());
            int count = this.count(queryWrapper);
            if (count>0){
                resultDTOList.add(BatchResultDTO.fail(entity.getId(),entity.getName(),ApiError.ERROR_9013.msg));
            }
            removeList.add(entity);
            resultDTOList.add(BatchResultDTO.success(entity.getId(), entity.getCode(),"删除成功"));
        }
        List<String> removeIdList = removeList.stream().map(SysDepartmentEntity::getId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(removeIdList)){
            return resultDTOList;
        }
        boolean flag = this.removeByIds(removeIdList);
        //删除成功就要去移除对应的员工
        if (flag) {
            sysDepartmentUserService.removeByDepartmentIds(removeIdList);
            if (CollectionUtils.isNotEmpty(removeList)) {
                //金蝶删除
                //list.forEach(obj -> syncKingdeeSysDeptService.syncDataToKingdee(obj, SyncKingdeeOperateEnum.OPERATE_DELETE.getCode()));
            }
        }
        return resultDTOList;
    }

    /**
     * 获取部门树结构
     *
     * @return
     */
    @Override
    public List<DepartmentDTO> findDepartmentTree() {
        List<SysDepartmentEntity> allList = this.list();
        //获取所有部门人员
        List<SysDepartmentUserNumberDTO> userNumberList = sysDepartmentUserService.findUserNumber();
        List<DepartmentDTO> departList = BeanMapperUtils.copyList(DepartmentDTO.class, allList);
        List<SysDepartmentTreeDTO> flagList = baseMapper.findTree();
        List<DepartmentDTO> treeList = departList.stream().
                filter(item -> "0".equals(item.getParentId()))
                .map(item -> {
                    item.setParentName("");
                    //根据用数据库查询的 树结构数据 获取到 该部门id 下有多少子的部门id
                    List<String> childrenDepartIds = getAllDepartIdsById(item.getId(), flagList);
                    item.setChildrenList(getChildren(item, departList, userNumberList, flagList));
                    int userNumber = userNumberList.stream().filter(u -> childrenDepartIds.contains(u.getDepartmentId())).collect(Collectors.groupingBy(SysDepartmentUserNumberDTO::getUserId)).size();
                    item.setUserNumber(userNumber);
                    return item;
                }).collect(Collectors.toList());

        return treeList;
    }


    /**
     * 根据部门id 获取下面有多少的 子集部门
     *
     * @param departId
     * @param treeList
     * @return java.util.List<java.lang.String>
     * @author yl
     * @date 2022-08-01 14:24
     */
    private List<String> getAllDepartIdsById(String departId, List<SysDepartmentTreeDTO> treeList) {
        List<String> resultList = new LinkedList<>();
        if (CollectionUtils.isNotEmpty(treeList)) {
            for (SysDepartmentTreeDTO vo : treeList) {
                //如果路径包含了 就说有
                if (vo.getPath().contains(departId)) {
                    resultList.add(vo.getId());
                }

            }
        }
        return resultList.stream().distinct().collect(Collectors.toList());
    }


    /**
     * 批量保存部门树结构
     *
     * @param sysDepartmentTree
     */
    @Override
    public void saveBatchDepartment(List<SysDepartmentDTO> sysDepartmentTree) {
        List<SysDepartmentEntity> batchList = new LinkedList<>();
        if (CollectionUtils.isNotEmpty(sysDepartmentTree)) {
            for (SysDepartmentDTO dto : sysDepartmentTree) {
                getSaveTree("0", batchList, dto);
            }

            long count = batchList.stream().filter(d -> d.getId().equals(d.getParentId())).count();
            if (count > 0) {
                throw new ServiceException("部门不能和上级部门相同");
            }
            this.saveOrUpdateBatch(batchList);
        }

    }


    /**
     * 根据部门id 获取到父级id 是部门id 的所有集合
     *
     * @param departmentId
     * @return java.util.List<java.lang.String>
     * @author yl
     * @date 2022-07-18 11:44
     */
    @Override
    public List<String> getDepartmentIds(String departmentId) {
        List<SysDepartmentTreeDTO> treeList = baseMapper.findTree();
        List<String> resultList = new LinkedList<>();
        if (CollectionUtils.isNotEmpty(treeList)) {
            for (SysDepartmentTreeDTO vo : treeList) {
                //如果路径包含了 就说有
                if (StringUtils.isNotBlank(departmentId) && vo.getPath().contains(departmentId)) {
                    resultList.add(vo.getId());
                }
            }
        }
        return resultList.stream().distinct().collect(Collectors.toList());
    }

    @Override
    public SysDepartmentDTO getDepartmentById(String deptId) {
        SysDepartmentEntity sysDepartmentEntity = this.getById(deptId);
        SysDepartmentDTO dto = new SysDepartmentDTO();
        if (ObjectUtils.isNotEmpty(sysDepartmentEntity)) {
            BeanMapperUtils.copy(sysDepartmentEntity, dto);
        }
        return dto;
    }

    @Override
    public SysDepartmentDTO getUserDeptByCode(String code) {
        SysDepartmentEntity sysDepartmentEntity = lambdaQuery().eq(SysDepartmentEntity::getCode, code).last("limit 1").one();
        SysDepartmentDTO dto = new SysDepartmentDTO();
        if (ObjectUtils.isNotEmpty(sysDepartmentEntity)) {
            BeanMapperUtils.copy(sysDepartmentEntity, dto);
        }
        return dto;
    }

    @Override
//    @Cacheable(cacheNames = "cache:sys:listDept",keyGenerator = "myKeyGenerator")
    public List<SysDepartmentEntity> listDept() {
        List<SysDepartmentEntity> list = lambdaQuery()
                .in(SysDepartmentEntity::getType, new ArrayList<>(Arrays.asList(1, 2)))
                .orderByAsc(SysDepartmentEntity::getName)
                .list();
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }
        return list;
    }


    /**
     * 根据部门名 获取部门id 以及下面的部门id
     *
     * @param deptName
     * @return java.util.List<java.lang.String>
     * @author yl
     * @date 2022-12-28 15:26
     */
    @Override
    public List<String> getDeptIds(String deptName) {
        List<String> resultList = new ArrayList<>();
        LambdaQueryWrapper<SysDepartmentEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysDepartmentEntity::getType, 1);
        queryWrapper.eq(SysDepartmentEntity::getName, deptName);
        queryWrapper.last("LIMIT 1");
        SysDepartmentEntity dept = this.getOne(queryWrapper);
        if (dept != null) {
            List<SysDepartmentTreeDTO> flagList = baseMapper.findTree();
            String deptId = dept.getId();
            //根据用数据库查询的 树结构数据 获取到 该部门id 下有多少子的部门id
            List<String> childrenDepartIds = getAllDepartIdsById(deptId, flagList);
            resultList.add(deptId);
            resultList.addAll(childrenDepartIds);
        }
        return resultList;
    }

    @Override
//    @Cacheable(cacheNames = "cache:sys:getDeptList",keyGenerator = "myKeyGenerator")
    public List<SysDepartmentDTO> getDeptList() {

        return baseMapper.getDeptList();
    }


    /**
     * 部门人员
     *
     * @param
     * @return java.util.List<com.erp.model.sys.dto.DeptUserDTO>
     * @author yl
     * @date 2023-01-06 17:26
     */
    @Override
    public List<DeptUserDTO> deptUserTree() {
        List<SysDepartmentEntity> allList = this.list();
        //获取所有部门人员
        List<SysDepartmentUserNumberDTO> userNumberList = sysDepartmentUserService.findUserNumber();
        List<DeptUserDTO> departList = new ArrayList<>(allList.size());
        for (SysDepartmentEntity item : allList) {
            DeptUserDTO dto = new DeptUserDTO();
            dto.setId(item.getId());
            dto.setName(item.getName());
            dto.setParentId(item.getParentId());
            dto.setType(item.getType());
            departList.add(dto);
        }
        List<SysDepartmentTreeDTO> flagList = baseMapper.findTree();
        List<DeptUserDTO> treeList = departList.stream().
                filter(item -> "0".equals(item.getParentId()))
                .map(item -> {
                    item.setParentName("");
                    //根据用数据库查询的 树结构数据 获取到 该部门id 下有多少子的部门id
                    List<String> childrenDepartIds = getAllDepartIdsById(item.getId(), flagList);
                    item.setChildrenList(getDeptUserChildren(item, departList, userNumberList, flagList));
                    int userNumber = userNumberList.stream().filter(u -> childrenDepartIds.contains(u.getDepartmentId())).collect(Collectors.groupingBy(SysDepartmentUserNumberDTO::getUserId)).size();
                    List<SysDepartmentUserNumberDTO> userList = userNumberList.stream().filter(u -> childrenDepartIds.contains(u.getDepartmentId())).collect(Collectors.toList());
                    item.setUserNumber(userNumber);
                    item.setUserList(userList);
                    return item;
                }).collect(Collectors.toList());

        return treeList;
    }

    @Override
    public SysDepartmentEntity getParentDepartmentById(String departmentId) {
        SysDepartmentEntity sysDepartmentEntity = this.getById(departmentId);
        if (ObjectUtils.isEmpty(sysDepartmentEntity)) {
            return sysDepartmentEntity;
        }
        return this.getById(sysDepartmentEntity.getParentId());
    }


    @Override
    public List<SysDepartmentDTO> listDeptByCodeList(List<String> codeList) {
        if (CollectionUtils.isEmpty(codeList)) {
            return Collections.EMPTY_LIST;
        }
        List<SysDepartmentEntity> list = lambdaQuery().in(SysDepartmentEntity::getCode, codeList).list();
        if (CollectionUtils.isEmpty(list)) {
            return Collections.EMPTY_LIST;
        }
        return BeanMapperUtils.copyList(SysDepartmentDTO.class, list);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void importDeptKingdee(MultipartFile file) throws IOException {
        XSSFWorkbook wb = new XSSFWorkbook(file.getInputStream());
        XSSFSheet sheet = wb.getSheetAt(0);
        // 读取数据集
        int rows = sheet.getPhysicalNumberOfRows();

        for (int i = 2; i < rows; i++) {
            XSSFRow row = sheet.getRow(i);

            // 部门名称
            String name = StrUtils.null2EmptyWithTrim(ExcelUtil.convertCellValueToString(row.getCell(6)));
            // 金蝶id
            String kingdeeId = StrUtils.null2EmptyWithTrim(ExcelUtil.convertCellValueToString(row.getCell(0)));
            // 金蝶编码
            String code = StrUtils.null2EmptyWithTrim(ExcelUtil.convertCellValueToString(row.getCell(3)));
            LambdaQueryWrapper<SysDepartmentEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SysDepartmentEntity::getName, name);
            queryWrapper.last("LIMIT 1");
            SysDepartmentEntity sysDepartmentEntity = super.getOne(queryWrapper);
            if (Objects.isNull(sysDepartmentEntity)) {
                log.info("未找到部门【{}】", name);
                continue;
            }
            if (StrUtils.isNotEmpty(sysDepartmentEntity.getSyncKingdeeId())) {
                log.info("部门【{}】已经存在金蝶id，不处理", name);
                continue;
            }
            lambdaUpdate()
                    .set(SysDepartmentEntity::getSyncKingdeeId, kingdeeId)
                    .set(SysDepartmentEntity::getCode, code)
                    .eq(SysDepartmentEntity::getId, sysDepartmentEntity.getId())
                    .update();
        }

    }


    /**
     * 根据id 集合获取到所有部门信息
     *
     * @param deptIdList
     * @return
     */
    @Override
    public List<SysDepartmentEntity> listByIdList(List<String> deptIdList) {
        if (CollectionUtils.isEmpty(deptIdList)) {
            return Collections.emptyList();
        }
        return this.listByIds(deptIdList);
    }


    /**
     * 根据用户ｉｄ集合获取到负责人
     *
     * @param userIdList
     * @return java.util.List<java.lang.String>
     * @author yl
     * @date 2023-07-14 17:26
     */
    @Override
    public List<String> listLeadByUserIdList(List<String> userIdList) {
        if (CollectionUtils.isEmpty(userIdList)) {
            return Collections.emptyList();
        }

        List<SysDepartmentUserNumberDTO> deptUserList = sysDepartmentUserService.listDeptUserByUserIdList(userIdList);
        //父部门id s
        List<String> deptPidList = deptUserList.stream().map(SysDepartmentUserNumberDTO::getDeptPid).collect(Collectors.toList());
        List<SysDepartmentUserEntity> departmentUserList = sysDepartmentUserService.listByDepartmentIds(deptPidList);
        List<String> resultList = departmentUserList.stream().filter(d -> SysConstant.YES_STATE.equals(d.getLeadState())).
                map(SysDepartmentUserEntity::getUserId).collect(Collectors.toList());
        return resultList;
    }

    @Override
    public List<SysDepartmentDTO> listSameLevelDeptIdList(List<String> deptIdList) {
        if (CollectionUtils.isEmpty(deptIdList)) {
            return Collections.EMPTY_LIST;
        }
        List<SysDepartmentTreeDTO> treeList = baseMapper.findTree();
        if (CollectionUtils.isEmpty(treeList)) {
            return Collections.EMPTY_LIST;
        }
        List<SysDepartmentDTO> resultList = new LinkedList<>();
        for (String deptId : deptIdList) {
            List<SysDepartmentTreeDTO> deptList = treeList.stream().filter(obj -> obj.getPath().contains(deptId)).distinct().collect(Collectors.toList());
            if (CollectionUtils.isEmpty(deptList)) {
                continue;
            }
            SysDepartmentDTO departmentDTO = new SysDepartmentDTO();
            SysDepartmentTreeDTO deptDTO = getBest(treeList, deptId);
            if (ObjectUtils.isEmpty(deptDTO)) {
                continue;
            }
            departmentDTO.setId(deptDTO.getId());
            departmentDTO.setName(deptDTO.getName());
            List<SysDepartmentDTO> childList = BeanMapperUtils.copyList(SysDepartmentDTO.class, deptList);
            departmentDTO.setChildrenList(childList);
            resultList.add(departmentDTO);
        }
        return resultList;
    }

    @Override
    public List<SysDepartmentTreeDTO> getDeptByParentId(String deptId) {
        if (StringUtils.isEmpty(deptId)){
            return Collections.emptyList();
        }
        return baseMapper.getDeptByParentId(deptId);
    }

    @Override
    public List<SysDepartmentEntity> getDeptByNames(List<String> deptNameList) {
        if(CollectionUtils.isEmpty(deptNameList)){
            return new ArrayList<>();
        }
        return this.lambdaQuery().in(SysDepartmentEntity::getName,deptNameList).list();
    }

    @Override
    public List<SysDepartmentEntity> getDeptByIds(List<String> ids) {
        if (CollUtil.isEmpty(ids)){
            return Collections.emptyList();
        }
        return this.lambdaQuery().in(SysDepartmentEntity::getId,ids).list();
    }

    /**
     * 查找部门最上级
     */
    private SysDepartmentTreeDTO getBest(List<SysDepartmentTreeDTO> treeList, String deptId) {
        SysDepartmentTreeDTO sysDepartmentTreeDTO = treeList.stream().filter(obj -> obj.getId().equals(deptId)).findFirst().orElse(null);
        if (ObjectUtils.isEmpty(sysDepartmentTreeDTO)) {
            return null;
        }
        if ("0".equals(sysDepartmentTreeDTO.getParentId())) {
            return sysDepartmentTreeDTO;
        } else {
            SysDepartmentTreeDTO best = getBest(treeList, sysDepartmentTreeDTO.getParentId());
            return best;
        }
    }

    @Override
    public List<SysUserDeptDTO> getByDeptNames(List<String> deptNames) {
        return this.baseMapper.getByDeptNames(deptNames);
    }

    /**
     * 递归获取批量保存的是数据
     *
     * @param parentId  父级id
     * @param batchList xuyao 保存的数据
     * @param item      参数
     * @return void
     * @author yl
     * @date 2022-07-11 17:56
     */
    private void getSaveTree(String parentId, List<SysDepartmentEntity> batchList, SysDepartmentDTO item) {
        SysDepartmentEntity entity = new SysDepartmentEntity();
        BeanMapperUtils.copy(item, entity);
        entity.setParentId(parentId);
        String id = item.getId();
        if (StringUtils.isBlank(id)) {
            id = IdWorker.getIdStr();
        }
        entity.setId(id);
        batchList.add(entity);
        List<SysDepartmentDTO> subList = item.getChildrenList();
        if (CollectionUtils.isNotEmpty(subList)) {
            for (SysDepartmentDTO item1 : subList) {
                this.getSaveTree(id, batchList, item1);
            }
        }
    }

    private List<DepartmentDTO> getChildren(DepartmentDTO item, List<DepartmentDTO> departList, List<SysDepartmentUserNumberDTO> userNumberList, List<SysDepartmentTreeDTO> flagList) {
        List<DepartmentDTO> collect = departList.stream().filter(dept -> item.getId().equals(dept.getParentId()))
                .map(d -> {
                    List<String> childrenDepartIds = getAllDepartIdsById(d.getId(), flagList);
                    d.setParentName(item.getName());
                    int userNumber = userNumberList.stream().filter(u -> childrenDepartIds.contains(u.getDepartmentId())).collect(Collectors.groupingBy(SysDepartmentUserNumberDTO::getUserId)).size();
                    d.setUserNumber(userNumber);
                    d.setChildrenList(getChildren(d, departList, userNumberList, flagList));
                    return d;
                }).collect(Collectors.toList());
        return CollectionUtils.isEmpty(collect) ? null : collect;
    }


    private List<DeptUserDTO> getDeptUserChildren(DeptUserDTO item, List<DeptUserDTO> departList, List<SysDepartmentUserNumberDTO> userNumberList, List<SysDepartmentTreeDTO> flagList) {
        List<DeptUserDTO> collect = departList.stream().filter(dept -> item.getId().equals(dept.getParentId()))
                .map(d -> {
                    List<String> childrenDepartIds = getAllDepartIdsById(d.getId(), flagList);
                    d.setParentName(item.getName());
                    int userNumber = userNumberList.stream().filter(u -> childrenDepartIds.contains(u.getDepartmentId())).collect(Collectors.groupingBy(SysDepartmentUserNumberDTO::getUserId)).size();
                    d.setUserNumber(userNumber);
                    List<SysDepartmentUserNumberDTO> userList = userNumberList.stream().filter(u -> childrenDepartIds.contains(u.getDepartmentId())).collect(Collectors.toList());
                    d.setUserList(userList);
                    d.setChildrenList(getDeptUserChildren(d, departList, userNumberList, flagList));
                    return d;
                }).collect(Collectors.toList());
        return CollectionUtils.isEmpty(collect) ? null : collect;
    }


    @Override
    public List<SysDepartmentEntity> listByParentIds() {
        List<SysDepartmentEntity> list = lambdaQuery().list();
        if (CollectionUtils.isEmpty(list)){
            return Collections.emptyList();
        }
        return fillParentIds(list);
    }

    @Override
    public List<DeptUserDTO.Tree> cascadeTree() {
        List<SysDepartmentTreeDTO> departments = baseMapper.findTree();
        if(CollectionUtils.isEmpty(departments)){
            return new ArrayList<>();
        }
        // 按路径排序
        departments.sort(Comparator.comparing(SysDepartmentTreeDTO::getPath));

        List<DeptUserDTO.Tree> rootTrees = new ArrayList<>();

        // 创建ID到Tree的映射，便于快速查找
        Map<String, DeptUserDTO.Tree> treeMap = new HashMap<>();

        for (SysDepartmentTreeDTO dept : departments) {
            // 创建当前部门的Tree对象
            DeptUserDTO.Tree currentTree = new DeptUserDTO.Tree();
            currentTree.setCode(dept.getId());
            currentTree.setValue(dept.getName());
            currentTree.setType("department"); // 假设类型为department
            currentTree.setDisabled(false);
            currentTree.setChildTreeList(new ArrayList<>());

            // 将当前Tree放入映射表
            treeMap.put(dept.getId(), currentTree);

            // 解析路径，获取父部门ID
            String[] pathParts = dept.getPath().split(",");
            if (pathParts.length == 1) {
                // 如果是根部门，直接添加到根节点列表
                rootTrees.add(currentTree);
            } else {
                // 找到父部门
                String parentId = pathParts[pathParts.length - 2];
                DeptUserDTO.Tree parentTree = treeMap.get(parentId);
                if (parentTree != null) {
                    // 将当前部门添加到父部门的子节点列表
                    parentTree.getChildTreeList().add(currentTree);
                }
            }
        }

        return rootTrees;
    }

    public List<SysDepartmentEntity> fillParentIds(List<SysDepartmentEntity> departments) {
        // 构建 id -> SysDepartmentEntity 的映射，方便查找
        Map<String, SysDepartmentTreeDTO> map = baseMapper.findTree()
                .stream()
                .collect(Collectors.toMap(SysDepartmentTreeDTO::getId, dept -> dept));

        for (SysDepartmentEntity sysDepartmentEntity : departments) {
            SysDepartmentTreeDTO sysDepartmentTreeDTO = map.get(sysDepartmentEntity.getId());
            if (null != sysDepartmentTreeDTO){
                sysDepartmentEntity.setPath(sysDepartmentTreeDTO.getPath());
            }
        }
        return departments;
    }


}
