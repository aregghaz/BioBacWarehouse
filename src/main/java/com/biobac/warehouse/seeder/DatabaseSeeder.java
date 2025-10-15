package com.biobac.warehouse.seeder;

import com.biobac.warehouse.entity.AssetCategory;
import com.biobac.warehouse.entity.AssetStatus;
import com.biobac.warehouse.entity.DepreciationMethod;
import com.biobac.warehouse.entity.WarehouseType;
import com.biobac.warehouse.repository.AssetCategoryRepository;
import com.biobac.warehouse.repository.AssetStatusRepository;
import com.biobac.warehouse.repository.DepreciationMethodRepository;
import com.biobac.warehouse.repository.WarehouseTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {
    private final WarehouseTypeRepository warehouseTypeRepository;
    private final AssetCategoryRepository assetCategoryRepository;
    private final AssetStatusRepository assetStatusRepository;
    private final DepreciationMethodRepository depreciationMethodRepository;

    @Override
    public void run(String... args) throws Exception {
        if (warehouseTypeRepository.count() == 0) {
            WarehouseType companyType1 = new WarehouseType("cold storage");
            warehouseTypeRepository.save(companyType1);

            WarehouseType companyType2 = new WarehouseType("production");
            warehouseTypeRepository.save(companyType2);

            WarehouseType companyType3 = new WarehouseType("virtual");
            warehouseTypeRepository.save(companyType3);
        }

        if (assetCategoryRepository.count() == 0) {
            List<AssetCategory> categories = List.of(
                    AssetCategory.builder().name("Оборудование").build(),
                    AssetCategory.builder().name("Транспорт").build(),
                    AssetCategory.builder().name("Мебель").build(),
                    AssetCategory.builder().name("Техника").build(),
                    AssetCategory.builder().name("Здание").build(),
                    AssetCategory.builder().name("Другое").build()
            );
            assetCategoryRepository.saveAll(categories);
        }

        if (assetStatusRepository.count() == 0) {
            List<AssetStatus> statuses = List.of(
                    AssetStatus.builder().name("В эксплуатации").build(),
                    AssetStatus.builder().name("Приостановлено").build(),
                    AssetStatus.builder().name("Списано").build()
            );
            assetStatusRepository.saveAll(statuses);
        }

        if (depreciationMethodRepository.count() == 0) {
            List<DepreciationMethod> methods = List.of(
                    DepreciationMethod.builder().name("Линейный").build(),
                    DepreciationMethod.builder().name("Ручной ввод").build()
            );
            depreciationMethodRepository.saveAll(methods);
        }
    }
}
