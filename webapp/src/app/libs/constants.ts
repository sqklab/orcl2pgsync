export const TOKEN_NAME = '_token';
export const CREDENTIAL_NAME = '_credential';
export const USER_NAME_LOGGED = '_USERNAME_LOGGED';
export const CONFIG_NAME = '_config';
export const PAGE_SIZE = '_pagesize';
export const BATCH_SIZE = '_batchsize';
export const PAGE_SIZE_COMPARISON = '_pagesize_comparison';
export const PAGE_SIZE_ERROR_LOG = '_pagesize_error_log';
export const TIME_TO_RELOAD = '_time_to_reload';
export const CURRENT_PAGE = '_current_page';
export const CURRENT_ERROR_LOG = '_current_page_error_log';
export const SORT_TYPE = '_sort_type';
export const SORT_TYPE_ERROR_TYPE = '_sort_type_error_type';
export const SORT_FIELD_SYNCHRONIZER = '_sort_field_synchronizer';
export const SORT_FIELD_ERROR_LOG = '_sort_field_error_log';
export const SORT_FIELD_TOTAL_RESOLVED_SYNCHRONIZER = '_sort_field_synchronizer';
export const CURRENT_PAGE_COMPARISON = '_current_page_comparison';
export const CURRENT_PAGE_DB_SCHEDULE = '_current_page_schedule';
export const CURRENT_SEARCHING_TOPIC_NAME = '_current_searching_topic_name';
export const CURRENT_SEARCHING_TABLE_NAME = '_current_searching_table_name';
// export const OPERATION_HISTORY_KEY = '_operation_history_key';

export enum HTTP_CODE {
  'SUCCESS_ID' = '000',
  'ERROR_NOT_FOUND_ID' = 100,
  'ERROR_SEND_OTP' = 101,
  'ERROR_NO_CONTRACT_ID' = 102,
  'ERROR_NOT_FOUND_OTP' = 202,
  'SUCCESS' = 200,
  'BAD_REQUEST' = 400,
  'UNAUTHOZIRED' = 401,
  'NOT_FOUND' = 404,
  'INTERNAL_SERVER' = 500
}

export enum SortType {
  'ASC' = 'ASC',
  'DESC' = 'DESC'
}
export enum ConnectorType {
  'ORACLE' = 'com.ecer.kafka.connect.oracle.OracleSourceConnector',
  'POSTGRES' = 'io.debezium.connector.postgresql.PostgresConnector'
}

export enum OperationAction {
  'ALL' = 'ALL',
  'CONDITION' = 'CONDITION'
}