package com.example.qrhunter;

/**
 * Listener for scanning QR Codes
 */
public interface QRCodeListener {
    void qrCodeFound(String qrCode);
    void qrCodeNotFound();
}

