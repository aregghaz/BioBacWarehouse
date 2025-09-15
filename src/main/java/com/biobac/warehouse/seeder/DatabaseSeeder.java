package com.biobac.warehouse.seeder;

import com.biobac.warehouse.entity.WarehouseType;
import com.biobac.warehouse.repository.WarehouseTypeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DatabaseSeeder implements CommandLineRunner {
    private final WarehouseTypeRepository warehouseTypeRepository;

    public DatabaseSeeder(WarehouseTypeRepository warehouseTypeRepository) {
        this.warehouseTypeRepository = warehouseTypeRepository;
    }

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
    }
}
