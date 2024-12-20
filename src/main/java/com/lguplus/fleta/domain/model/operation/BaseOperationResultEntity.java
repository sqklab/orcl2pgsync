package com.lguplus.fleta.domain.model.operation;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.io.Serializable;
import java.time.LocalDateTime;

@Setter
@Getter
@MappedSuperclass
public abstract class BaseOperationResultEntity implements Serializable {

	@Column(name = "uuid", updatable = false, nullable = false)
	@GeneratedValue(generator = "UUID")
	@GenericGenerator(
			name = "UUID",
			strategy = "org.hibernate.id.UUIDGenerator"
	)
	@Id
	private String uuid;

	@Column(name = "primary_keys")
	private String primaryKeys;

	@Column(name = "session")
	private String session;

	@Column(name = "op_date_time")
	private LocalDateTime operationDate;

	@Column(name = "correction_type")
	private String correctionType;

	@Column(name = "where_condition")
	private String whereCondition;
}
