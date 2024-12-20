package com.lguplus.fleta.domain.model.operation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tbl_operation_process")
public class OperationProcessEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", updatable = false, nullable = false)
	private Long id;

	@Column(name = "session")
	private String session;

	@Column(name = "op_date_time")
	private LocalDateTime operationDate;

	@Column(name = "op_end_date_time")
	private LocalDateTime operationEndDate;

	@Column(name = "op_table")
	private String operationTable;

	@Column(name = "where_condition")
	private String whereCondition;

	@Column(name = "state")
	private Boolean state;

	@Column(name = "search_type")
	private String searchType;

	@Transient
	private Boolean isOutDate;

	public void setWhereCondition(String whereCondition) {
		if (whereCondition != null) {
			this.whereCondition = whereCondition.trim();
		}
	}
}
