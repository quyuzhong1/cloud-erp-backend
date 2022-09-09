package com.cloud.erp.chrome.service.impl;

import cn.hutool.http.HttpUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.erp.chrome.constant.TaskState;
import com.cloud.erp.chrome.dto.GyyShipmentsDTO;
import com.cloud.erp.chrome.entity.OrderGyyDeliverEntity;
import com.cloud.erp.chrome.mapper.OrderGyyDeliverMapper;
import com.cloud.erp.chrome.service.ChromeTaskInfoService;
import com.cloud.erp.chrome.service.CsvServer;
import com.cloud.erp.chrome.service.OrderGyyDeliverService;
import com.erp.common.exception.ServiceException;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;

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
//            CsvReader csvReader = CsvUtil.getReader();
//            CsvData data = csvReader.read(file);
//            List<CsvRow> rows = data.getRows();
//            List<OrderGyyDeliverEntity> saveList=new ArrayList<>(rows.size());
//            if(CollectionUtils.isNotEmpty(rows)){
//                for(CsvRow item:rows){
//                    OrderGyyDeliverEntity entity=new OrderGyyDeliverEntity();
//                    entity.setAmount(item.get(25));
//                }
//
//            }
            Resource resource = new ClassPathResource("");
            String path=resource.getFile().getPath();
            ClassPathResource classPathResource = new ClassPathResource("csv/temp.csv");


            File tempFile=new File(path+"\\csv\\"+"System.currentTimeMillis().csv");

            file = HttpUtil.downloadFileFromUrl(dto.getOssUrl(), tempFile);

            List<OrderGyyDeliverEntity> saveList = csvServer.getObjectListByFile(file, OrderGyyDeliverEntity.class);
            System.out.println(saveList.size());
            if (CollectionUtils.isNotEmpty(saveList)) {
                this.saveBatch(saveList);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceException(1, "管易云保存数据失败");
        } finally {
            if (file != null) {
               // file.delete();
            }
        }
       chromeTaskInfoService.updateTaskState(dto.getTaskId(), TaskState.FINISH);
    }

    public static void main(String[] args) throws IOException {
        Resource resource = new ClassPathResource("");
        String path=resource.getFile().getPath();
        System.out.println(path+"\\csv");
    }
}
