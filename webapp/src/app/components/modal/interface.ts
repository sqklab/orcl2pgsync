export type TypeModal = 'warning' | 'warning-italic' | 'info' | undefined;
export interface IModalProps {
    type?: TypeModal;
    isShowCheckBox?: boolean; // field optional for db-connector only
    titleOfCheckBox?: string;
    isShow: boolean;
    title: string;
    message: string;
    cancel?: boolean;
    confirm(): void;
    confirmDeleteKafkaConnector?(event): void;
    closeModal?(): void;
}
