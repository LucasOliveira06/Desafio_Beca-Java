package br.com.lucas.ms2_transaction.application.exception;

public class TransactionNotFoundException
        extends RuntimeException {

    public TransactionNotFoundException() {
        super("Transação não encontrada");
    }
}