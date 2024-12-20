package com.lguplus.fleta.ports.repository.operation;

import com.lguplus.fleta.domain.dto.operation.OperationResult;
import com.lguplus.fleta.domain.model.operation.BaseOperationResultEntity;
import com.lguplus.fleta.domain.model.operation.OpPtVoBuyEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OpPtVoBuyRepository extends JpaRepository<BaseOperationResultEntity, String> {
	@Query(value = "SELECT * FROM tbl_op_pt_vo_buy where session=:session AND where_condition=:where_condition", nativeQuery = true)
	Page<OpPtVoBuyEntity> pagedResults(@Param("session") String session, @Param("where_condition") String where, Pageable page);

	@Transactional(timeout = 300)
	@Modifying
	@Query(value = "DELETE FROM tbl_op_pt_vo_buy where op_date_time < :timeAlive", nativeQuery = true)
	void deleteOutDateResult(@Param("timeAlive") LocalDateTime timeAlive);

	@Transactional
	@Modifying
	@Query(value = "DELETE FROM tbl_op_pt_vo_buy where uuid in :uuids", nativeQuery = true)
	void deleteByUuids(@Param("uuids") List<String> uuids);

	@Transactional
	@Modifying
	@Query(value = "DELETE FROM tbl_op_pt_vo_buy where session=:session AND where_condition=:where_condition", nativeQuery = true)
	void deleteBySessionAndWhereCondition(@Param("session") String session, @Param("where_condition") String where);

	@Query(value = "select correction_type, count(*) as total FROM tbl_op_pt_vo_buy where session=:session " +
			"AND where_condition=:where_condition group by correction_type", nativeQuery = true)
	List<OperationResult> countDifferent(@Param("session") String session, @Param("where_condition") String where);
}
