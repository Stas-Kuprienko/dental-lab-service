package org.lab.dental.service.reports;

import org.lab.dental.entity.ProductEntity;
import org.lab.dental.repository.ProductRepository;
import org.lab.dental.service.ProductTypeService;
import org.lab.dental.service.ReportService;
import org.lab.enums.WorkStatus;
import org.lab.model.DentalWork;
import org.lab.model.ProductMap;
import org.lab.model.ProductType;
import org.lab.model.ProfitRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@Service
public class MyReportService implements ReportService {

    private final ProductTypeService productTypeService;
    private final ReportMapper reportMapper;
    private final XLSXFileTool xlsxFileTool;
    private final ProductRepository productRepository;


    @Autowired
    public MyReportService(ProductTypeService productTypeService,
                           ReportMapper reportMapper,
                           XLSXFileTool xlsxFileTool,
                           ProductRepository productRepository) {
        this.productTypeService = productTypeService;
        this.reportMapper = reportMapper;
        this.xlsxFileTool = xlsxFileTool;
        this.productRepository = productRepository;
    }


    @Override
    public ByteArrayInputStream createFile(List<DentalWork> workList, UUID userId, YearMonth yearMonth) {
        ProductMap productMap = productTypeService.getAllByUserId(userId);
        List<String> titles = productMap
                .getEntries()
                .stream()
                .map(ProductType::getTitle)
                .toList();
        List<List<String>> data = reportMapper.mapDentalWorkReport(workList, titles);
        return xlsxFileTool.createReport(data);
    }

    @Override
    public List<DentalWork> readReport(MultipartFile file, UUID userId, LocalDate completeAt, WorkStatus status) {
        ProductMap productMap = productTypeService.getAllByUserId(userId);
        List<String> titles = productMap.getEntries().stream().map(ProductType::getTitle).toList();
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(file.getBytes());
            List<List<String>> data = xlsxFileTool.parseReport(inputStream, titles);
            return reportMapper.parseReportToDentalWorks(data, productMap, completeAt, status);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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

    @Override
    public List<ProfitRecord> countMonthlyProfit(UUID userId, int year) {
        List<Object[]> data = productRepository.countMonthlyProfit(userId, year);
        return data.stream()
                .map(row -> ProfitRecord.builder()
                        .year(year)
                        .month(Month.of((Integer) row[0]))
                        .value((Double) row[1])
                        .userId(userId)
                        .build())
                .toList();
    }


    private double productListToProfit(List<ProductEntity> products) {
        return products
                .stream()
                .mapToDouble(p -> p.getPrice().doubleValue() * p.getQuantity())
                .sum();
    }
}
