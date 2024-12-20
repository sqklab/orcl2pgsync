import { NgbTimeStruct } from "@ng-bootstrap/ng-bootstrap";
import { TimeOnServer } from "../services/interface";

export class Utils {

    public static convertDateServer(timeOnServer): TimeOnServer {
        const datetime = new Date(timeOnServer);
        const hour = datetime.getHours();
        const minute = datetime.getMinutes();
        const timeDisplay = {
          hour: +hour,
          minute: +minute,
          second: 0
        } as NgbTimeStruct;
        return {
            time: hour + ':' + minute,
            timeDisplay: timeDisplay
        } as TimeOnServer;
    }
}