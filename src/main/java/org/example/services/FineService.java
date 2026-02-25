package org.example.services;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class FineService {

    // 1. "Bộ vi xử lý" cốt lõi: Thuật toán tính tiền phạt
    public long calculateFine(long daysLate) {
        if (daysLate <= 0) {
            return 0; // Trả đúng hạn hoặc sớm hơn, không phạt
        }

        if (daysLate <= 10) {
            // Trễ từ 1 đến 10 ngày: 1k/ngày
            return daysLate * 1000;
        } else {
            // Trễ trên 10 ngày: 10 ngày đầu 1k, các ngày sau 2k
            return (10 * 1000) + ((daysLate - 10) * 2000);
        }
    }

    // 2. Chức năng dành cho Admin: Tính toán và hiển thị hóa đơn phạt
    // (Sử dụng LocalDate để giả lập ngày hẹn trả và ngày trả thực tế)
    public void adminViewFineReport(LocalDate dueDate, LocalDate returnDate) {
        System.out.println("\n=======================================");
        System.out.println("         BOOK RETURN REPORT     ");
        System.out.println("=======================================");
        System.out.println("📅 Payment appointment date: " + dueDate);
        System.out.println("📅 Actual payment date: " + returnDate);

        // Tính toán khoảng cách giữa 2 ngày
        long daysLate = ChronoUnit.DAYS.between(dueDate, returnDate);

        if (daysLate <= 0) {
            System.out.println(" Status: GOOD. Reader returned the book on time.");
            System.out.println(" Fine amount: 0 VND");
        } else {
            long fineAmount = calculateFine(daysLate);
            System.out.println("⚠️ Status: VIOLATION (Late " + daysLate + " day)");
            System.out.println("💸 TOTAL FINES " + fineAmount + " VNĐ");

            // Gợi ý nhỏ cho Admin
            if (daysLate > 10) {
                System.out.println("📌 Note: An increased penalty (2000 VND/day) has been applied. " + (daysLate - 10) + " day beyond frame.");
            }
        }
        System.out.println("=======================================\n");
    }
}