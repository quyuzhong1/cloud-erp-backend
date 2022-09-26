package com.cloud.erp.chrome.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.lang.Console;
import cn.hutool.core.text.csv.CsvData;
import cn.hutool.core.text.csv.CsvReader;
import cn.hutool.core.text.csv.CsvRow;
import cn.hutool.core.text.csv.CsvUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.http.HttpUtil;
import com.cloud.erp.chrome.constant.TaskState;
import com.cloud.erp.chrome.dto.GyyShipmentsDTO;
import com.cloud.erp.chrome.entity.OrderGyyDeliverEntity;
import com.cloud.erp.chrome.mapper.OrderGyyDeliverMapper;
import com.cloud.erp.chrome.service.ChromeTaskInfoService;
import com.cloud.erp.chrome.service.CsvServer;
import com.cloud.erp.chrome.service.OrderGyyDeliverService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.common.exception.ServiceException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 管易云ERP 发货信息表 服务实现类
 * </p>
 *
 * @author yl
 * @since 2022-09-01
 */
@Service
public class OrderGyyDeliverServiceImpl extends ServiceImpl<OrderGyyDeliverMapper, OrderGyyDeliverEntity> implements OrderGyyDeliverService {


    @Autowired
    private CsvServer csvServer;

    @Autowired
    private ChromeTaskInfoService chromeTaskInfoService;


    /**
     * 保存管易云发货信息表
     *
     * @param dto
     * @return void
     * @author yl
     * @date 2022-09-01 18:04
     */
    @Override
    public void saveDeliverCsvByUrl(GyyShipmentsDTO dto) {
        File file = null;
        try {
            String projectPath = System.getProperty("user.dir"); //当前项目
            String path = projectPath + "/erp-chrome/src/main/java/temp";
            File tempFile = new File(path);
            if (!tempFile.exists()) {
                tempFile.mkdirs();
            }
            file = HttpUtil.downloadFileFromUrl(dto.getOssUrl(), tempFile);
            List<OrderGyyDeliverEntity> saveList = csvServer.getObjectListByFile(file, OrderGyyDeliverEntity.class);
            if (CollectionUtils.isNotEmpty(saveList)) {
                saveList=saveList.stream().filter(o->StringUtils.isNotBlank(o.getSkuNo())).collect(Collectors.toList());
                this.saveBatch(saveList);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceException(1, "管易云保存数据失败");
        } finally {
            if (file != null) {
                file.delete();
            }
        }
        chromeTaskInfoService.updateTaskState(dto.getTaskId(), TaskState.FINISH);
    }


}
