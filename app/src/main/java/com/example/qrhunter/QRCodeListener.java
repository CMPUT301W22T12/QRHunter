package com.example.qrhunter;

public interface QRCodeListener {
    void qrCodeFound(String qrCode);
    void qrCodeNotFound();
}

