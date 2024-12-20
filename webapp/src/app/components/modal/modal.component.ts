import { Component, OnInit, Input, Output, EventEmitter, OnChanges } from '@angular/core';
import { TypeModal } from './interface';

@Component({
  selector: 'app-modal',
  templateUrl: './modal.component.html',
  styleUrls: ['./modal.component.scss']
})
export class ModalComponent implements OnInit, OnChanges {
  @Input() isShow = false;
  @Input() title = '';
  @Input() message = '';
  @Input() type: TypeModal = 'warning';
  @Input() cancel?: boolean;
  @Input() isShowCheckBox?: boolean;
  @Input() titleOfCheckBox?: '';
  @Output() closeModal = new EventEmitter();
  @Output() confirm = new EventEmitter();
  @Output() confirmDeleteKakaConnector = new EventEmitter();

  constructor() { }

  ngOnInit(): void { }

  ngOnChanges(): void {
    if (this.isShow) {
      document.getElementById('openModal').click();
      setTimeout(() => {
        let backdrop = document.querySelectorAll('div.modal-backdrop');
        if (backdrop.length > 0) {
          // @ts-ignore
          backdrop[0].style.zIndex = 1050;
        }
      }, 0)
    }
  }

  handleCloseModal(): void {
    this.closeModal.emit();
  }

  handleConfirm(): void {
    this.confirm.emit();
  }

  handleCheckedDeleteKafkaConnector(event): void {
    if (this.isShowCheckBox) {
      this.confirmDeleteKakaConnector.emit(event.target.checked);
    }
  }

}
