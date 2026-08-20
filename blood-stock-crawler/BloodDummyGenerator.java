package org.example;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class BloodDummyGenerator {
    private final String dbUrl, dbUser, dbPassword;
    private final Random random;
    private final AtomicInteger donorSequence = new AtomicInteger(1);
    private final AtomicInteger unitSequence;

    public BloodDummyGenerator(String dbUrl, String dbUser, String dbPassword) {
        this(dbUrl, dbUser, dbPassword, new Random());
    }

    public BloodDummyGenerator(String dbUrl, String dbUser, String dbPassword, long seed) {
        this(dbUrl, dbUser, dbPassword, new Random(seed));
    }

    private BloodDummyGenerator(String dbUrl, String dbUser, String dbPassword, Random random) {
        this.dbUrl = dbUrl;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
        this.random = random;
        this.unitSequence = new AtomicInteger(100000 + random.nextInt(900000));
    }

    public void initDatabase() {
        String sql = """
                CREATE TABLE blood_units (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    donor_id VARCHAR(30) NOT NULL,
                    blood_type VARCHAR(5) NOT NULL,
                    rh_type VARCHAR(5) NOT NULL,
                    gender VARCHAR(10) NOT NULL,
                    birth_date DATE NOT NULL,
                    unit_id VARCHAR(30) NOT NULL UNIQUE,
                    product_type VARCHAR(30) NOT NULL,
                    collection_date DATETIME NOT NULL,
                    expiration_date DATETIME NOT NULL,
                    INDEX idx_donor_id (donor_id),
                    INDEX idx_blood_type (blood_type),
                    INDEX idx_rh_type (rh_type),
                    INDEX idx_product_type (product_type),
                    INDEX idx_expiration_date (expiration_date)
                )""";

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DROP TABLE IF EXISTS blood_units");
            stmt.executeUpdate(sql);
            System.out.println("DB 테이블 초기화 완료");
        } catch (SQLException e) {
            throw new RuntimeException("DB 초기화 실패", e);
        }
    }

    public void saveUnitsFromStock(String productName, BloodInfoService.BloodStock stock) {
        if (stock == null) throw new IllegalArgumentException("BloodStock이 null입니다.");

        ProductType productType = ProductType.fromDescription(productName);
        Map<BloodType, Integer> stockMap = Map.of(
                BloodType.A, Math.max(0, stock.a()),
                BloodType.B, Math.max(0, stock.b()),
                BloodType.O, Math.max(0, stock.o()),
                BloodType.AB, Math.max(0, stock.ab())
        );

        int totalCount = stockMap.values().stream().mapToInt(Integer::intValue).sum();
        System.out.printf("%s 총 생성 수량 : %d개%n", productType.description(), totalCount);

        if (totalCount == 0) return;

        List<BloodType> bloodTypes = createBloodTypeList(stockMap);
        saveToDatabase(bloodTypes, productType);
    }

    private List<BloodType> createBloodTypeList(Map<BloodType, Integer> stock) {
        List<BloodType> result = new ArrayList<>();
        stock.forEach((type, count) -> {
            for (int i = 0; i < count; i++) result.add(type);
        });
        Collections.shuffle(result, random);
        return result;
    }

    private void saveToDatabase(List<BloodType> bloodTypes, ProductType productType) {
        String sql = """
                INSERT INTO blood_units (
                    donor_id, blood_type, rh_type, gender, birth_date,
                    unit_id, product_type, collection_date, expiration_date
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)""";

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            int count = 0;
            for (BloodType bloodType : bloodTypes) {
                LocalDateTime collectionDate = randomCollectionDate(productType);

                pstmt.setString(1, generateDonorId());
                pstmt.setString(2, bloodType.name());
                pstmt.setString(3, random.nextDouble() < 0.85 ? RhType.POSITIVE.description() : RhType.NEGATIVE.description());
                pstmt.setString(4, random.nextBoolean() ? Gender.MALE.description() : Gender.FEMALE.description());
                pstmt.setObject(5, randomBirthDate());
                pstmt.setString(6, generateUnitId());
                pstmt.setString(7, productType.description());
                pstmt.setObject(8, collectionDate);
                pstmt.setObject(9, collectionDate.plusDays(productType.expirationDays()));

                pstmt.addBatch();
                if (++count % 500 == 0) pstmt.executeBatch();
            }
            pstmt.executeBatch();
            System.out.printf("%s DB 저장 완료 : %d개%n", productType.description(), count);

        } catch (SQLException e) {
            throw new RuntimeException(productType.description() + " DB 저장 실패", e);
        }
    }

    private String generateDonorId() {
        return String.format("DNR-%d-%05d", LocalDate.now().getYear(), donorSequence.getAndIncrement());
    }

    private String generateUnitId() {
        return String.format("BLD-%d-%06d", LocalDate.now().getYear(), unitSequence.getAndIncrement());
    }

    private LocalDate randomBirthDate() {
        int age = 18 + random.nextInt(53);
        int randomDays = random.nextInt(365);
        return LocalDate.now().minusYears(age).minusDays(randomDays);
    }

    private LocalDateTime randomCollectionDate(ProductType productType) {
        int daysAgo = (random.nextInt(100) < 15)
                ? productType.expirationDays() + 1 + random.nextInt(30)
                : random.nextInt(productType.expirationDays());

        return LocalDateTime.now()
                .minusDays(daysAgo)
                .withHour(9 + random.nextInt(9))
                .withMinute(random.nextInt(60))
                .withSecond(0).withNano(0);
    }

    public enum BloodType { A, B, O, AB }

    public enum RhType {
        POSITIVE("Rh+"), NEGATIVE("Rh-");
        private final String description;
        RhType(String description) { this.description = description; }
        public String description() { return description; }
    }

    public enum Gender {
        MALE("남"), FEMALE("여");
        private final String description;
        Gender(String description) { this.description = description; }
        public String description() { return description; }
    }

    public enum ProductType {
        WHOLE_BLOOD("전혈", 35),
        PLASMA("혈장", 365), // 혈장(유효기간 365일) 추가
        PLATELET_SINGLE("혈소판단종", 5),
        PLATELET_MULTI("혈소판다종", 5);

        private final String description;
        private final int expirationDays;

        ProductType(String description, int expirationDays) {
            this.description = description;
            this.expirationDays = expirationDays;
        }

        public String description() { return description; }
        public int expirationDays() { return expirationDays; }

        public static ProductType fromDescription(String value) {
            if (value == null) throw new IllegalArgumentException("혈액 제제 종류가 null입니다.");

            for (ProductType type : values()) {
                if (type.description.equals(value)) return type;
            }
            if (value.contains("혈장")) return PLASMA;
            if (value.contains("적혈구") || value.contains("전혈")) return WHOLE_BLOOD;
            if (value.contains("성분채혈") || value.contains("단종")) return PLATELET_SINGLE;
            if (value.contains("농축혈소판") || value.contains("다종")) return PLATELET_MULTI;

            throw new IllegalArgumentException("지원하지 않는 혈액 제제 종류: " + value);
        }
    }
}