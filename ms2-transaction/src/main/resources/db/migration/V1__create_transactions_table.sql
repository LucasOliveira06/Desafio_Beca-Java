CREATE TABLE transactions (

                              id BIGSERIAL PRIMARY KEY,
                              type VARCHAR(20) NOT NULL,
                              amount NUMERIC(19, 2) NOT NULL,
                              occurred_at TIMESTAMP NOT NULL,
                              description VARCHAR(255) NOT NULL,
                              category VARCHAR(100),
                              owner_user_id BIGINT NOT NULL,
                              destination_user_id BIGINT,
                              status VARCHAR(20) NOT NULL,
                              rejection_reason VARCHAR(255),
                              created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                              CONSTRAINT check_transaction_type
                                  CHECK (type IN ('DEPOSIT', 'WITHDRAWAL', 'TRANSFER', 'PURCHASE')),

                              CONSTRAINT check_transaction_status
                                  CHECK (status IN ('APPROVED', 'REJECTED')),

                              CONSTRAINT check_transaction_amount
                                  CHECK (amount > 0),

                              CONSTRAINT check_transfer_destination
                                  CHECK (
                                      type <> 'TRANSFER'
                                          OR destination_user_id IS NOT NULL
                                      ),

                              CONSTRAINT check_purchase_category
                                  CHECK (
                                      type <> 'PURCHASE'
                                          OR category IS NOT NULL
                                      ),

                              CONSTRAINT check_rejection_reason
                                  CHECK (
                                      (status = 'APPROVED' AND rejection_reason IS NULL)
                                          OR (status = 'REJECTED' AND rejection_reason IS NOT NULL)
                                      )
);