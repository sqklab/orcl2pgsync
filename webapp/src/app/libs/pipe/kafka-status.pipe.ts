import { Pipe, PipeTransform } from '@angular/core';

const KAFKA_DISCONNECTED = 'DOWN';
const KAFKA_RUNNING = 'UP';

@Pipe({
  name: 'kafkaStatus',
})
export class KafkaStatusPipe implements PipeTransform {
  transform(value: string): string {
    if (!value) {
      return '';
    }
    const json = JSON.parse(value);
    if (json.status === KAFKA_DISCONNECTED) {
      return "<div class='bg-danger text-light w-100 p-3 text-center' role='alert'>Kafka is disconnected</div>";
    } else if (json.status === KAFKA_RUNNING) {
      return '';
      // return "<div class='bg-success text-light w-100 p-3 text-center' role='alert'>Kafka is running</div>";
    }
    return '';
  }
}
