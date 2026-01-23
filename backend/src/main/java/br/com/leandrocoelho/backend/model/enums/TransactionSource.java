package br.com.leandrocoelho.backend.model.enums;

public enum TransactionSource {
    MANUAL,
    PLUGGY,   // Open Finance
    OFX,      // Arquivo legado
    PDF_AI    // Importação via Grok
}
