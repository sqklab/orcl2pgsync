import { NgbTimeStruct } from "@ng-bootstrap/ng-bootstrap";
import { OperationAction } from "../constants";

export interface SynchronizationInfo {
  createdUser?: string;
  updatedUser?: string;
  id?: string;
  index?: number;
  sourceDatabase: string;
  sourceSchema: string;
  sourceTable: string;

  targetDatabase: string;
  targetSchema: string;
  targetTable: string;

  sourceQuery: string;
  targetQuery: string;
  sourceCompareDatabase: string;
  targetCompareDatabase: string;
  isComparable?: boolean;
  enableColumnComparison?: boolean;
  comparisonInfoId: number;

  operation: string;
  topicName: string;
  state: number;
  division: string;
  synchronizerName: string;
  logFile: string;
  logFileMinimized: string;
  numberOfError: number;
  numberOfResolve: number;
  numberOfToTal: number;
  checked?: boolean;
  syncType?: SyncType; // RD: Read model . DT: Domain table
  syncRdRequestParams: GroupTargetSync[];
  linkSyncRequest: LinkSyncRequest;
  scn?: number;
  commitScn?: number;
  msgTimestamp?: number;
  receivedTime?: string;
  receivedDate?: Date;
  receivedDateTime?: string;
  primaryKeys?: string;
  uniqueKeys?: string;
  isPartitioned?: boolean;
  detectKey?: boolean;
  consumerGroup?: any;
  isBatch?: boolean;
  isUpsert?: boolean;
  isChangeInsertOnFailureUpdate?: boolean;
  isAllColumnConditionsOnUpdate?: boolean;
  maxPollRecords?: number;
  enableTruncate?: boolean;
  primaryKeysPartitionInfo?: PrimaryKeysPartitionInfo;
}

export interface PrimaryKeysPartitionInfo {
  primaryKeys: string;
  uniqueKeys: string;
  isPartitioned: boolean;
  enableTruncate?: boolean;
}

export interface LinkSyncRequest {
  ids: number[];
}
export interface GroupTargetSync {
  sourceDatabase: string;
  sourceSchema: string;
  sourceTable: string;

  targetDatabase: string;
  targetSchema: string;
  targetTable: string;

  sqlInsert: string;
  sqlUpdate: string;
  sqlDelete: string;

  comparisonInfoId: number;
  sourceQuery: string;
  targetQuery: string;
  sourceCompareDatabase: string;
  targetCompareDatabase: string;
  isComparable?: boolean;
  enableColumnComparison?: boolean;

  division: string;

  numberRows: number;

  invalidSourceDB?: boolean;
  invalidSourceSchema?: boolean;
  invalidSourceTable?: boolean;

  invalidTargetDB?: boolean;
  invalidTargetSchema?: boolean;
  invalidTargetTable?: boolean;
  invalidSQLCreate?: boolean;
  invalidSQLUpdate?: boolean;
  invalidSQLDelete?: boolean;
}
export interface LogFileInfo {
  name: string;
  size: string;
  path: string;
  length: number;
  index?: number;
  isLogFile?: boolean;
};
export interface SyncErrorInfo {
  id?: number;
  index?: number;
  topicName: string;
  syncMessage: string;
  sqlRedo: string;
  sqlRd: string;
  errorMessage: string;
  errorTime: Date;
  state: string;
  updatedAt: Date;
  errorType: string;
  checked?: boolean;
  environment: string;
  errorVersion: string;
};

export interface SyncErrorCountOperationsDto {
  totalInsert: number;
  totalUpdate: number;
  totalDelete: number;
}

export interface SyncErrorDto {
  syncErrorEntities: SyncErrorInfo[];
  totalPage: number;
  environment: string;
  errorVersion: string;
  allTypes: string[];
  errorStates: string[];
}

export interface SyncRequestMessage {
  state: string;
  timestamp: number;
  schema: any;
  payload: any;
}

export interface DataSourceInfoDTO {
  id?: number;
  serverName: string;
  url: string;
  createdBy: string;
  updatedBy: string;
  createdAt: string;
  updatedAt: string;
  username: string;
  password: string;
  maxPoolSize: number;
  idleTimeout: number;
  status: string;
  driverClassName: string;
  checked?: boolean;
  testing?: boolean;
  isPending?: boolean;
}

export interface LoginResponse {
  token: string;
}

export interface SynchronizationParamsResponse {
  synchronizationParams: SynchronizationInfo[];
  totalPage: number;
  isToSlow?: boolean;
};

export interface DataSourceResponse {
  dataSourceDescriptions: DataSourceInfoDTO[];
  totalPage: number;
};

export interface Product {
  id?: string;
  productCode: string;
  ruleName: string;
  link: string;
}
export interface StartSynchronizeBody {
  ids: number[];
  currentPage: number;
  pageSize: number;
  sortField: string;
}
export interface StartDatasourceBody {
  ids: number[];
  currentPage: number;
  pageSize: number;
  sortField: string;
}
export interface Account {
  username: string;
  password: string;
}

export interface CommonResponse<T> {
  message: string;
  status: number;
  body: T;
}

export interface IUser {
  id?: string;
  username: string;
  name: string;
  locked: boolean;
  token?: string;
}

export enum SyncType {
  RD = 'RD',
  DT = 'DT'
}

export enum Division {
  Oracle2Postgres = 'Oracle2Postgres',
  Postgres2Postgres = 'Postgres2Postgres',
  Postgres2Oracle = 'Postgres2Oracle',
  Custom ='Custom'
}

export enum GraphType {
  MINUTE = 'MINUTE',
  HOUR = 'HOUR',
  DAY = 'DAY'
}

export enum SyncState {
  Running = 'RUNNING',
  Stopped = 'STOPPED',
  Pending = 'PENDING',
  Linked = 'LINKED'
}

// for DB COMPARISON
export interface DbComparisonScheduleDto {
  id: number;
  state: string;
  time: string;
  timeDaily: string;
  timeDisplay: NgbTimeStruct;
  index?: string; // 1st, 2nd, 3rd, ...
}

export interface QuickRunComparisonQuery {
  query: string;
  database: string;
}

// for db schedule

export interface ViewComparisonInfoDto {
  id: number;
  index?: number;
  syncId: number;
  syncRdId: number;
  synchronizerName: string;
  sourceDatabase: string;
  sourceSchema: string;
  sourceTable: string;
  targetDatabase: string;
  targetSchema: string;
  targetTable: string;
  // rd source
  rdSourceDatabase: string;
  rdSourceSchema: string;
  rdSourceTable: string;
  // rd target
  rdTargetDatabase: string;
  rdTargetSchema: string;
  rdTargetTable: string;

  isComparable: string;
  enableColumnComparison: boolean;

  sourceQuery: string;
  targetQuery: string;
  sourceCompareDatabase: string;
  targetCompareDatabase: string;
  division: string;
  lastModified: Date;
}
export interface DbComparisonResultDto {
  id: number;
  compareDate: number;
  compareTime: number;
  sourceCount: number;
  targetCount: number;
  comparisonState: string;
  groupDateTime: GroupDateTime;
}
export interface ViewComparisonResultDto extends ViewComparisonInfoDto {
  compareResultId: number;
  compareDate: Date;
  compareTime: string;
  sourceCount: number;
  comparisonState: string;
  targetCount: number;
  checked?: boolean;
  division: string;
  scn?: number;
  commitScn?: number;
  msgTimestamp?: number;
  receivedTime?: string;
  receivedDate?: Date;
  receivedDateTime?: Date;
  errorMsgSource: string;
  errorMsgTarget: string;
}
export interface ComparisonResultDto {
  map: any[];
  diffCount: number;
  headers: string[];
  rawValues: any;
}

export interface ViewExportComparison {
  sourceDatabase: string;
  sourceSchema: string;
  sourceTable: string;
  targetDatabase: string;
  targetSchema: string;
  targetTable: string;

  sourceCount: number;
  targetCount: number;
  comparisonState: string;

  lastModified: string;
}
export interface ViewComparisonResultDtoResponse {
  entities: ViewComparisonResultDto[];
  totalPage: number;
};

export interface GroupDateTime {
  compareDate: string;
  compareTime: string;
  active?: boolean;
}

export interface ComparisonSummary {
  total: number;
  fail: number;
  equal: number;
  different: number;
  remaining: number;
  sourceCount: number;
  targetCount: number;
  msgDtBehind: number;
  msgRdBehind: number;
}

// export interface MessageBehind {
//   messagesBehindDT: number;
//   messagesBehindRD: number;
// }

export interface SynInfo {
  topicName: string;
  id: number;
}

export interface GroupLinkSync {
  model?: any;
}

export interface CustomDateTime {
  dateTime: string;
  month?: number;
  formatter: string;
  zoneId: string;
}

// db schedule
export interface DBScheduleDto {
  id: number;
  fkIdProcedure: number;
  name: string;
  plSQL: string;
  db: string;
  schema: string;
  table: string;
  type: number; // 0:day, 1:week, 2:monthy, 3:quarterly, 4: yearly
  status: boolean;
  filterStatus?: boolean[];
  createdAt?: string;
  updatedAt?: string;
  createdUser: string;
  updatedUser: string;

  timeDisplay: NgbTimeStruct;

  // schedule configs
  timeDaily: string; // for daily

  dayOfWeek?: string; // for week
  timesOfWeek?: string;
  timesOfWeekDisplay?: string[];

  schedulesOfMonth: MonthQuarterYear[]; // for monthy
  schedulesOfQuarterly: MonthQuarterYear[]; // for quarterly
  schedulesOfYear: MonthQuarterYear[]; // for yearly

  monthly?: string;
  quarterly?: string;
  yearly?: string;

  //optional
  checked?: boolean;
}

export interface MonthQuarterYear {
  month: number;
  day: string; // could be: day or lastday
  time: string;
  isLastDay?: boolean;
  timeDisplay: NgbTimeStruct;
}

export interface TimeOnServer {
  time: string; // HH:mm
  timeDisplay: NgbTimeStruct;
}

export interface DBScheduleResultDto {
  id: number;
  plSQL: string;
  db: string;
  schema: string;
  table: string;
  type: number; //0:day, 1:week
  errorMsg: string;
  status: boolean;
  startAt: string;
  scheduleTime: string;
  scheduleDate: string;
  endAt: string;
  checked?: boolean;
}

export interface DayOfWeek {
  actived: boolean;
  name: string;
  value: number;
}

export interface DbScheduleProcedureResponse {
  entities: DBScheduleDto[];
  totalPage: number;
}

export interface DbScheduleProcedureResultResponse {
  entities: DBScheduleResultDto[];
  totalPage: number;
}

export interface ResultSummaryDto {
  success: number;
  failure: number;
  total: number;
}

export interface ViewExportDbSchedule {
  plSQL: string;
  db: string;
  schema: string;
  table: string;
  status: string;
  startTime: string;
  endTime: string;
}
export interface MessageDataForYAxisGraphDto {
  name: string;
  data: number[];
};

export interface MessageDataForXAxisGraphDto {
  name: string;
  data: string[];
};
export interface DataAnalysisDtoResponse {
  messageDataForYAxisGraphDtoList: MessageDataForYAxisGraphDto[];
  messageDataForXAxisGraphDtoList: MessageDataForXAxisGraphDto;
  min: number;
  max: number;
};


export interface ScheduleAllDto {
  numSuccess: number;
  numFailure: number;
};

///////////// for CONNECTOR  /////////////
export interface SourceRecordInfoDto {
  id: number;
  status: Status;
  info: Info;
  connectorClass: string;
  worker_id: string;
  createAt: string;
  updateAt: string;
  createdUser: string;
  updatedUser: string;
}

export interface Status {
  name: string;
  connector: Connector;
  tasks: Task[];
  type: string;
}

export interface Connector {
  state: string;
  worker_id: string;
}

export interface Task {
  id: number;
  state: string;
  worker_id: string;
  trace?: string;
  connector?: string;
}

export interface Info {
  name: string;
  config: Map<string, string>;
  tasks: Task[];
  type: string;
}

export interface Config {
  // type?: string;
  // jsonConfig?: string;

  connectorClass: string;
  resetOffset: string;
  dbHostname: string;
  tasksMax: string;
  dbUserPassword: string;
  tableBlacklist: string;
  tableWhitelist: string;
  dbUser: string;
  dbPort: string;
  dbFetchSize: string;
  multitenant: string;
  name: string;
  topic: string;
  parseDmlData: string;
  dbName: string;
  dbNameAlias: string;
}

export interface ViewKafkaConsumerGroup {
  topic: string;
  messagesBehind: number;
  receivedDate: Date;
  receivedTime: string;
}

export interface OperationDto {
  sourceDatabase: string;
  sourceSchema: string;
  sourceSql: string;

  targetDatabase: string;
  targetSchema: string;
  targetSql: string;

  table: string;
  columnIdValue: string;
  columnIdName: string;
  whereStm: string;
  whereStmFillData: string;
  offset?: number;
  actor?: string;
  sessionId?: string;
}

// export interface OperationHistory {
//   actor: string;
//   action: OperationAction; // search all, search by condition
//   // userName: string;
//   query: string;
// }

export interface CompareDiffItem {
  source: Map<string, string>;
  target: Map<string, string>;
  operation: string;
  selected?: boolean;
  code: string;
  errorMessage: string;
}

export interface OperationResponse {
  entities: CompareDiffItem[];
  totalPage: number;
  primaryKeys: string;
  uniqueKeys: string;
}

export interface PublicationDto {
  publicationInfoList: PublicationInfo[];
  total: number;
}

export interface PublicationInfo {
  name: string;
  schemaName: string;
  table: string;
  checked: boolean;
}

export interface OperationSummary {
  insert: number;
  update: number;
  delete: number;
  state: boolean;
  operationDate: string;
  isOutDate: boolean;
}

export interface DiffDeleteReq {
  table: string;
  session: string;
  where: string;
}

export interface CorrectionResult {
  success: boolean;
  errorMessage: string;
  code: string;
}

export interface CorrectionBatchResult {
  success: boolean;
  exceptions: CorrectionResult[];
}

export interface Operator {
  actor: string;
  sessionId: string;
}

export interface RedoLogTable {
  name: string;
  selected?: boolean;
}

export interface CurrentScnInfo {
  scn: string;
  findTime: string;
}
export interface LogMnrContents {
  startScn: string;
  endScn: string;
  operationTypes: string[],
  db: string;
  schema: string;
  tables: string[];
}

export interface LogMnrContentsTables {
  name: string;
  checked?: boolean;
}

export interface LogMnrContentResult {
  scn: string;
	startScn: string;
	commitScn: string;
	timestamp: string;
	operation: string;
	segOwner: string;
	tableName: string;
	sqlRedo: string;
	sqlUndo: string;
  selected?: boolean;
}

export interface WrapLogMnrContent {
  logContents: LogMnrContentResult[];
  count: number;
}

export interface HistoryInfoDto{
  HistoryInfoList: HistoryInfo[];
}
export interface HistoryInfo {
  historyId: number;
  operation: string;
  state: string;
  syncJson: string;
  synchronizerId: number;
  topic: string;
  time: string;
}
