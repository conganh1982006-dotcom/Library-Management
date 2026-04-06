package org.example;

import java.time.LocalDate;

// ĐÂY LÀ CỖ MÁY THỜI GIAN CỦA HỆ THỐNG
public class SystemClock {
    // Mặc định lấy ngày hôm nay của máy tính
    private static LocalDate simulatedDate = LocalDate.now();

    // Hàm này sẽ thay thế toàn bộ lệnh LocalDate.now() trong hệ thống
    public static LocalDate now() {
        return simulatedDate;
    }

    // Hàm tua nhanh thời gian (cộng thêm số ngày)
    public static void addDays(int days) {
        simulatedDate = simulatedDate.plusDays(days);
    }

    // Reset về hiện tại nếu test xong
    public static void reset() {
        simulatedDate = LocalDate.now();
    }
}