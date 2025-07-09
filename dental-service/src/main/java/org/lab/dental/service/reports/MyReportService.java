package org.lab.dental.service.reports;

import org.lab.dental.entity.DentalWorkEntity;
import org.lab.dental.entity.ProductEntity;
import org.lab.dental.entity.ProductTypeEntity;
import org.lab.dental.repository.ProductRepository;
import org.lab.dental.service.DentalWorkService;
import org.lab.dental.service.ProductTypeService;
import org.lab.dental.service.ReportService;
import org.lab.model.ProfitRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@Service
public class MyReportService implements ReportService {

    private final DentalWorkService dentalWorkService;
    private final ProductTypeService productTypeService;
    private final ReportMapper reportMapper;
    private final XLSXFileTool xlsxFileTool;
    private final ProductRepository productRepository;


    @Autowired
    public MyReportService(DentalWorkService dentalWorkService,
                           ProductTypeService productTypeService,
                           ReportMapper reportMapper,
                           XLSXFileTool xlsxFileTool,
                           ProductRepository productRepository) {
        this.dentalWorkService = dentalWorkService;
        this.productTypeService = productTypeService;
        this.reportMapper = reportMapper;
        this.xlsxFileTool = xlsxFileTool;
        this.productRepository = productRepository;
    }


    @Override
    public ByteArrayInputStream createFile(UUID userId, YearMonth yearMonth) {
        List<DentalWorkEntity> workList = dentalWorkService.getAllForMonthByUserId(userId, yearMonth);
        List<ProductTypeEntity> typeEntities = productTypeService.getAllByUserId(userId);
        List<String> titles = typeEntities
                .stream()
                .map(ProductTypeEntity::getTitle)
                .toList();
        List<List<String>> data = reportMapper.mapDentalWorkReport(workList, titles);
        return xlsxFileTool.createReport(data);
    }

    @Override
    public ProfitRecord countProfits(UUID userId, YearMonth month) {
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();
        List<ProductEntity> products = productRepository.findAllByUserIdAndMonth(userId, start, end);
        double profit = productListToProfit(products);
        return ProfitRecord.builder()
                .userId(userId)
                .year(month.getYear())
                .month(month.getMonth())
                .value(profit)
                .build();
    }


    private double productListToProfit(List<ProductEntity> products) {
        return products
                .stream()
                .mapToDouble(p -> p.getPrice().doubleValue() * p.getQuantity())
                .sum();
    }
}
