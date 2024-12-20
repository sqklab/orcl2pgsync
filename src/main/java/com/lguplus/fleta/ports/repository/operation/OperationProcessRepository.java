package com.lguplus.fleta.ports.repository.operation;

import com.lguplus.fleta.domain.model.operation.OperationProcessEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Repository
public interface OperationProcessRepository extends JpaRepository<OperationProcessEntity, Long> {


	@Transactional(timeout = 300)
	@Modifying
	@Query(value = "DELETE FROM tbl_operation_process where op_date_time < :timeAlive", nativeQuery = true)
	void deleteOutDateResult(@Param("timeAlive") LocalDateTime timeAlive);

	OperationProcessEntity getBySessionAndOperationTableAndWhereCondition(String sessionId, String table, String whereCondition);

	@Query(value = "select state from tbl_operation_process  where op_table=:table AND session=:session AND where_condition=:where_condition", nativeQuery = true)
	Boolean getOperationState(String session, String table, String where_condition);

	@Transactional
	@Modifying
	@Query(value = "DELETE FROM tbl_operation_process where op_table=:table AND session=:session AND where_condition=:where_condition", nativeQuery = true)
	void deleteBySessionAndWhereCondition(@Param("table") String table, @Param("session") String session, @Param("where_condition") String where);

	@Transactional
	@Modifying
	@Query(value = "UPDATE tbl_operation_process set state=false where state=true", nativeQuery = true)
	void resetState();
}
