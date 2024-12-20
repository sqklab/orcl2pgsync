package com.lguplus.fleta.domain.model.operation;

import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "tbl_op_pt_vo_watch_history")
@NoArgsConstructor
public class OpPtVoWatchHistoryEntity extends BaseOperationResultEntity {
}
