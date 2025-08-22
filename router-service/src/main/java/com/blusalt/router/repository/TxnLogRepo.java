package com.blusalt.router.repository;

import com.blusalt.router.model.TxnLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TxnLogRepo extends JpaRepository<TxnLog, Long> {

}
