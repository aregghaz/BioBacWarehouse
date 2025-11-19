package com.biobac.warehouse.seeder;

import com.biobac.warehouse.entity.*;
import com.biobac.warehouse.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {
    private final WarehouseTypeRepository warehouseTypeRepository;
    private final AssetCategoryRepository assetCategoryRepository;
    private final AssetStatusRepository assetStatusRepository;
    private final DepreciationMethodRepository depreciationMethodRepository;
    private final ReceiveIngredientStatusRepository receiveIngredientStatusRepository;
    private final AssetActionRepository assetActionRepository;
    private final HistoryActionRepository historyActionRepository;

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

        if (receiveIngredientStatusRepository.count() == 0) {
            List<ReceiveIngredientStatus> statuses = List.of(
                    ReceiveIngredientStatus.builder().name("завершенные").build(),
                    ReceiveIngredientStatus.builder().name("не доставлено").build(),
                    ReceiveIngredientStatus.builder().name("цена не совпадает").build(),
                    ReceiveIngredientStatus.builder().name("количество не совпадает").build(),
                    ReceiveIngredientStatus.builder().name("количество и цена не совпадает").build()
            );
            receiveIngredientStatusRepository.saveAll(statuses);
        }

        if (assetActionRepository.count() == 0) {
            List<AssetAction> actions = List.of(
                    AssetAction.builder().name("Переоценка").build(),
                    AssetAction.builder().name("Изменение срок службы").build(),
                    AssetAction.builder().name("Списано").build(),
                    AssetAction.builder().name("Ежемесячная амортизация").build()
            );
            assetActionRepository.saveAll(actions);
        }

        if (historyActionRepository.count() == 0) {
            List<HistoryAction> actions = List.of(
                    HistoryAction.builder().name("Перевод").build(),
                    HistoryAction.builder().name("Изменения").build(),
                    HistoryAction.builder().name("Прибавлено").build(),
                    HistoryAction.builder().name("Убавлено").build()
            );
            historyActionRepository.saveAll(actions);
        }
    }
}
