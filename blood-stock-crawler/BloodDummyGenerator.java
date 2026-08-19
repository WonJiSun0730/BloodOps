package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.time.LocalDate;
import java.time.LocalDateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class BloodDummyGenerator {

    private final String dbUrl;
    private final String dbUser;
    private final String dbPassword;

    private final Random random;

    private final AtomicInteger donorSequence;
    private final AtomicInteger unitSequence;


    // =========================================================
    // 생성자
    // =========================================================

    public BloodDummyGenerator(
            String dbUrl,
            String dbUser,
            String dbPassword
    ) {
        this(
                dbUrl,
                dbUser,
                dbPassword,
                new Random()
        );
    }


    public BloodDummyGenerator(
            String dbUrl,
            String dbUser,
            String dbPassword,
            long seed
    ) {
        this(
                dbUrl,
                dbUser,
                dbPassword,
                new Random(seed)
        );
    }


    private BloodDummyGenerator(
            String dbUrl,
            String dbUser,
            String dbPassword,
            Random random
    ) {

        this.dbUrl = dbUrl;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;

        this.random = random;

        this.donorSequence =
                new AtomicInteger(1);

        this.unitSequence =
                new AtomicInteger(
                        100000 + random.nextInt(900000)
                );
    }


    // =========================================================
    // DB 초기화
    // =========================================================

    public void initDatabase() {

        String createTableSql = """
                CREATE TABLE blood_units (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,

                    donor_id VARCHAR(30) NOT NULL,

                    blood_type VARCHAR(5) NOT NULL,

                    rh_type VARCHAR(5) NOT NULL,

                    gender VARCHAR(10) NOT NULL,

                    age INT NOT NULL,

                    unit_id VARCHAR(30) NOT NULL UNIQUE,

                    product_type VARCHAR(30) NOT NULL,

                    collection_date DATETIME NOT NULL,

                    expiration_date DATETIME NOT NULL,

                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,

                    INDEX idx_blood_type (blood_type),
                    INDEX idx_rh_type (rh_type),
                    INDEX idx_product_type (product_type),
                    INDEX idx_expiration_date (expiration_date)
                )
                """;


        try (
                Connection connection =
                        DriverManager.getConnection(
                                dbUrl,
                                dbUser,
                                dbPassword
                        );

                Statement statement =
                        connection.createStatement()
        ) {

            // 기존 테이블 삭제
            statement.executeUpdate(
                    "DROP TABLE IF EXISTS blood_units"
            );

            // 테이블 생성
            statement.executeUpdate(
                    createTableSql
            );

            System.out.println(
                    "DB 테이블 초기화 완료"
            );

        } catch (SQLException e) {

            throw new RuntimeException(
                    "DB 초기화 실패",
                    e
            );
        }
    }


    // =========================================================
    // 크롤링 수치 기반 혈액팩 저장
    // =========================================================

    public void saveUnitsFromStock(
            String productName,
            BloodInfoService.BloodStock stock
    ) {

        if (stock == null) {
            throw new IllegalArgumentException(
                    "BloodStock이 null입니다."
            );
        }


        ProductType productType =
                ProductType.fromDescription(
                        productName
                );


        /*
         * 현재 혈액보유량
         *
         * A형
         * B형
         * O형
         * AB형
         */
        Map<BloodType, Integer> stockMap =
                new EnumMap<>(BloodType.class);

        stockMap.put(
                BloodType.A,
                Math.max(0, stock.a())
        );

        stockMap.put(
                BloodType.B,
                Math.max(0, stock.b())
        );

        stockMap.put(
                BloodType.O,
                Math.max(0, stock.o())
        );

        stockMap.put(
                BloodType.AB,
                Math.max(0, stock.ab())
        );


        int totalCount =
                stockMap.values()
                        .stream()
                        .mapToInt(Integer::intValue)
                        .sum();


        System.out.println(
                productName
                        + " 총 생성 수량 : "
                        + totalCount
                        + "개"
        );


        if (totalCount == 0) {

            System.out.println(
                    productName
                            + " 보유량이 0이므로 생성하지 않습니다."
            );

            return;
        }


        // 혈액형 목록 생성
        List<BloodType> bloodTypes =
                createBloodTypeList(
                        stockMap
                );


        // DB 저장
        saveToDatabase(
                bloodTypes,
                productType
        );
    }


    // =========================================================
    // 혈액형 목록 생성
    // =========================================================

    private List<BloodType> createBloodTypeList(
            Map<BloodType, Integer> stock
    ) {

        List<BloodType> result =
                new ArrayList<>();


        for (BloodType type : BloodType.values()) {

            int count =
                    stock.getOrDefault(
                            type,
                            0
                    );

            for (int i = 0; i < count; i++) {

                result.add(type);
            }
        }


        Collections.shuffle(
                result,
                random
        );


        return result;
    }


    // =========================================================
    // DB INSERT
    // =========================================================

    private void saveToDatabase(
            List<BloodType> bloodTypes,
            ProductType productType
    ) {

        String sql = """
                INSERT INTO blood_units (
                    donor_id,
                    blood_type,
                    rh_type,
                    gender,
                    age,
                    unit_id,
                    product_type,
                    collection_date,
                    expiration_date
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;


        try (
                Connection connection =
                        DriverManager.getConnection(
                                dbUrl,
                                dbUser,
                                dbPassword
                        );

                PreparedStatement pstmt =
                        connection.prepareStatement(sql)
        ) {

            /*
             * 대량 INSERT 성능을 위해
             * 하나씩 executeUpdate() 하지 않고
             * batch 사용
             */

            int count = 0;


            for (BloodType bloodType : bloodTypes) {

                RhType rhType =
                        randomRhType();

                Gender gender =
                        randomGender();

                int age =
                        randomAge();

                String donorId =
                        generateDonorId();

                String unitId =
                        generateUnitId();

                LocalDateTime collectionDate =
                        randomCollectionDate(
                                productType
                        );

                LocalDateTime expirationDate =
                        calculateExpirationDate(
                                collectionDate,
                                productType
                        );


                pstmt.setString(
                        1,
                        donorId
                );

                pstmt.setString(
                        2,
                        bloodType.name()
                );

                pstmt.setString(
                        3,
                        rhType.description()
                );

                pstmt.setString(
                        4,
                        gender.description()
                );

                pstmt.setInt(
                        5,
                        age
                );

                pstmt.setString(
                        6,
                        unitId
                );

                pstmt.setString(
                        7,
                        productType.description()
                );

                pstmt.setObject(
                        8,
                        collectionDate
                );

                pstmt.setObject(
                        9,
                        expirationDate
                );


                pstmt.addBatch();

                count++;


                // 500개마다 batch 실행
                if (count % 500 == 0) {

                    pstmt.executeBatch();
                }
            }


            // 남은 데이터 처리
            pstmt.executeBatch();


            System.out.println(
                    productType.description()
                            + " DB 저장 완료 : "
                            + count
                            + "개"
            );


        } catch (SQLException e) {

            throw new RuntimeException(
                    productType.description()
                            + " DB 저장 실패",
                    e
            );
        }
    }


    // =========================================================
    // 헌혈자 ID
    // =========================================================

    private String generateDonorId() {

        return String.format(
                "DNR-%d-%05d",
                LocalDate.now().getYear(),
                donorSequence.getAndIncrement()
        );
    }


    // =========================================================
    // 혈액팩 ID
    // =========================================================

    private String generateUnitId() {

        return String.format(
                "BLD-%d-%06d",
                LocalDate.now().getYear(),
                unitSequence.getAndIncrement()
        );
    }


    // =========================================================
    // Rh
    // =========================================================

    private RhType randomRhType() {

        // 약 85% Rh+
        return random.nextDouble() < 0.85
                ? RhType.POSITIVE
                : RhType.NEGATIVE;
    }


    // =========================================================
    // 성별
    // =========================================================

    private Gender randomGender() {

        return random.nextBoolean()
                ? Gender.MALE
                : Gender.FEMALE;
    }


    // =========================================================
    // 나이
    // =========================================================

    private int randomAge() {

        // 18 ~ 70
        return 18 + random.nextInt(53);
    }


    // =========================================================
    // 채혈일
    // =========================================================

    private LocalDateTime randomCollectionDate(
            ProductType productType
    ) {

        /*
         * 만료 데이터도 일부 생성할 수 있도록
         * 제제의 유효기간을 고려한다.
         *
         * 약 15%는 이미 만료된 데이터
         */

        int daysAgo;


        if (random.nextInt(100) < 15) {

            daysAgo =
                    productType.expirationDays()
                            + 1
                            + random.nextInt(30);

        } else {

            daysAgo =
                    random.nextInt(
                            productType.expirationDays()
                    );
        }


        int hour =
                9 + random.nextInt(9);

        int minute =
                random.nextInt(60);


        return LocalDateTime.now()
                .minusDays(daysAgo)
                .withHour(hour)
                .withMinute(minute)
                .withSecond(0)
                .withNano(0);
    }


    // =========================================================
    // 유효기간 계산
    // =========================================================

    private LocalDateTime calculateExpirationDate(
            LocalDateTime collectionDate,
            ProductType productType
    ) {

        return collectionDate.plusDays(
                productType.expirationDays()
        );
    }


    // =========================================================
    // 혈액형
    // =========================================================

    public enum BloodType {

        A,
        B,
        O,
        AB
    }


    // =========================================================
    // Rh
    // =========================================================

    public enum RhType {

        POSITIVE("Rh+"),
        NEGATIVE("Rh-");

        private final String description;


        RhType(String description) {

            this.description =
                    description;
        }


        public String description() {

            return description;
        }
    }


    // =========================================================
    // 성별
    // =========================================================

    public enum Gender {

        MALE("Male"),
        FEMALE("Female");

        private final String description;


        Gender(String description) {

            this.description =
                    description;
        }


        public String description() {

            return description;
        }
    }


    // =========================================================
    // 혈액 제제 종류
    // =========================================================

    public enum ProductType {

        WHOLE_BLOOD(
                "전혈",
                35
        ),

        RED_BLOOD_CELL(
                "적혈구",
                35
        ),

        PLASMA(
                "혈장",
                365
        ),

        CONCENTRATED_PLATELET(
                "농축혈소판",
                5
        ),

        APHERESIS_PLATELET(
                "성분채혈혈소판",
                5
        );


        private final String description;
        private final int expirationDays;


        ProductType(
                String description,
                int expirationDays
        ) {

            this.description =
                    description;

            this.expirationDays =
                    expirationDays;
        }


        public String description() {

            return description;
        }


        public int expirationDays() {

            return expirationDays;
        }


        public static ProductType fromDescription(
                String value
        ) {

            if (value == null) {

                throw new IllegalArgumentException(
                        "혈액 제제 종류가 null입니다."
                );
            }


            for (ProductType type : values()) {

                if (type.description.equals(value)) {

                    return type;
                }
            }


            throw new IllegalArgumentException(
                    "지원하지 않는 혈액 제제 종류: "
                            + value
            );
        }
    }
}