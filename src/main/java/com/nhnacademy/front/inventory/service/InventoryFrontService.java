package com.nhnacademy.front.inventory.service;

import com.nhnacademy.front.inventory.dto.response.InventoriesResponse;
import com.nhnacademy.front.inventory.dto.response.StorageInfoResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class InventoryFrontService {


    public List<StorageInfoResponse> getStorageInfos(List<InventoriesResponse> response){

       List<StorageInfoResponse> storageInfos =  response.stream()
                .map(r ->
                    new StorageInfoResponse(r.storageId(),r.storageName())
                ).distinct()
               .toList();


       log.info("storageInfo : {}",storageInfos);


       return storageInfos;

    }



}
