package org.example;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BloodStockManagementService {

    private final String dbUrl;
    private final String dbUser;
    private final String dbPassword;

    public BloodStockManagementService(String dbUrl, String dbUser, String dbPassword) {
        this.dbUrl = dbUrl;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
    }

    // =========================================================================
    // 1. [헌혈 완료] 신규 헌혈 혈액 입고 등록 ('보유중' 상태로 INSERT)
    // =========================================================================
    public String registerNewBloodUnit(String donorId, String bloodType, String rhType,
                                       String gender, String birthDate, String productType) {
        String unitId = String.format("BLD-%d-%06d", LocalDateTime.now().getYear(), (int)(Math.random() * 900000) + 100000);

        int expirationDays = switch (productType) {
            case "혈장" -> 365;
            case "혈소판단종", "혈소판다종" -> 5;
            default -> 35; // 전혈
        };

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expirationDate = now.plusDays(expirationDays);

        String sql = """
            INSERT INTO blood_units (
                donor_id, blood_type, rh_type, gender, birth_date,
                unit_id, product_type, collection_date, expiration_date, status
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, '보유중')
        """;

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, donorId);
            pstmt.setString(2, bloodType);
            pstmt.setString(3, rhType);
            pstmt.setString(4, gender);
            pstmt.setDate(5, Date.valueOf(birthDate));
            pstmt.setString(6, unitId);
            pstmt.setString(7, productType);
            pstmt.setTimestamp(8, Timestamp.valueOf(now));
            pstmt.setTimestamp(9, Timestamp.valueOf(expirationDate));

            pstmt.executeUpdate();
            return unitId;
        } catch (SQLException e) {
            throw new RuntimeException("신규 혈액 입고 등록 실패", e);
        }
    }

    // =========================================================================
    // 2. [교차시험/예약] FIFO 원칙 기반 단순 예약 ('보유중' -> '예약됨')
    // =========================================================================
    public List<String> reserveBloodForCrossMatching(String productType, String bloodType, int count) {
        String selectSql = """
            SELECT id, unit_id 
            FROM blood_units
            WHERE product_type = ? 
              AND blood_type = ? 
              AND status = '보유중'
              AND expiration_date > NOW()
            ORDER BY expiration_date ASC
            LIMIT ?
            FOR UPDATE
        """;

        String updateSql = "UPDATE blood_units SET status = '예약됨' WHERE id = ?";
        List<String> reservedUnitIds = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
            conn.setAutoCommit(false);

            try (PreparedStatement selectPstmt = conn.prepareStatement(selectSql);
                 PreparedStatement updatePstmt = conn.prepareStatement(updateSql)) {

                selectPstmt.setString(1, productType);
                selectPstmt.setString(2, bloodType);
                selectPstmt.setInt(3, count);

                List<Long> targetIds = new ArrayList<>();
                try (ResultSet rs = selectPstmt.executeQuery()) {
                    while (rs.next()) {
                        targetIds.add(rs.getLong("id"));
                        reservedUnitIds.add(rs.getString("unit_id"));
                    }
                }

                if (targetIds.size() < count) {
                    conn.rollback();
                    throw new IllegalStateException("가용 재고가 부족합니다. (요청: " + count + "개, 가능: " + targetIds.size() + "개)");
                }

                for (Long id : targetIds) {
                    updatePstmt.setLong(1, id);
                    updatePstmt.addBatch();
                }
                updatePstmt.executeBatch();

                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException("혈액 예약 처리 실패", e);
        }
        return reservedUnitIds;
    }

    // =========================================================================
    // 3. [수혈 완료 / 출고] 예약된 혈액 출고 처리 ('예약됨' -> '사용완료')
    // =========================================================================
    public boolean issueBloodUnit(String unitId) {
        String sql = "UPDATE blood_units SET status = '사용완료' WHERE unit_id = ? AND status = '예약됨'";

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, unitId);
            int updated = pstmt.executeUpdate();
            return updated > 0;
        } catch (SQLException e) {
            throw new RuntimeException("혈액 출고 처리 실패", e);
        }
    }

    // =========================================================================
    // 4. [예약 취소 / 가용 환원] 미사용 혈액 복원 ('예약됨' -> '보유중')
    // =========================================================================
    public boolean cancelReservation(String unitId) {
        String sql = "UPDATE blood_units SET status = '보유중' WHERE unit_id = ? AND status = '예약됨'";

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, unitId);
            int updated = pstmt.executeUpdate();
            return updated > 0;
        } catch (SQLException e) {
            throw new RuntimeException("예약 취소 처리 실패", e);
        }
    }

    // =========================================================================
    // 5. [대시보드] 상태별(보유중, 예약됨 등) 재고 현황 요약
    // =========================================================================
    public List<StockSummaryDto> getStockSummaryByStatus(String status) {
        String sql = """
            SELECT product_type, blood_type, COUNT(*) AS count
            FROM blood_units
            WHERE status = ? AND expiration_date > NOW()
            GROUP BY product_type, blood_type
            ORDER BY product_type, blood_type
        """;

        List<StockSummaryDto> list = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new StockSummaryDto(
                            rs.getString("product_type"),
                            rs.getString("blood_type"),
                            rs.getInt("count")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("재고 현황 조회 실패", e);
        }
        return list;
    }

    public record StockSummaryDto(String productType, String bloodType, int count) {}
}

/*
===================================================================================
 [BloodStockManagementService 사용 가이드] - 팀원 참고용 주석
===================================================================================

 1. 클래스 개요
    - DB(blood_units)에 저장된 혈액 더미데이터를 기반으로 의료진이 혈액 재고 현황을 파악하고,
      선입선출(FIFO) 기반 출고 대상 추천 및 폐기 혈액 모니터링 기능을 제공합니다.

 2. 인스턴스 생성 방법
    - BloodStockManagementService service = new BloodStockManagementService(
          "jdbc:mysql://localhost:3307/blood_db?serverTimezone=UTC&useSSL=false",
          "root",
          "1234"
      );

 3. 주요 기능 및 메서드 사용 예시

   1) getAvailableStockSummary()
      - 유효기간이 남은(expiration_date > NOW()) 현재 보유 재고를 제제/혈액형별로 집계합니다.
      - 사용 예시:
        List<StockSummaryDto> summary = service.getAvailableStockSummary();
        for (StockSummaryDto dto : summary) {
            System.out.printf("제제: %s | 혈액형: %s | 수량: %d개%n",
                dto.productType(), dto.bloodType(), dto.count());
        }

   2) getFifoRecommendUnits(String productType, String bloodType, int limit)
      - 특정 제제와 혈액형 중 만료 일자가 가장 얼마 남지 않은 혈액팩을 선입선출(FIFO) 순으로 조회합니다.
      - 제제 종류 파라미터 값: "전혈", "혈장", "혈소판단종", "혈소판다종"
      - 사용 예시:
        // O형 혈장 3팩 출고 시 선입선출 우선순위 상위 3팩 추출
        List<BloodUnitDto> recommendList = service.getFifoRecommendUnits("혈장", "O", 3);

   3) getExpiredUnits()
      - 유효기간이 지난(expiration_date <= NOW()) 폐기 대상 혈액팩 목록 전체를 조회합니다.
      - 사용 예시:
        List<BloodUnitDto> expiredList = service.getExpiredUnits();
        System.out.println("폐기 대상 혈액팩 총 수량: " + expiredList.size());

===================================================================================
*/
