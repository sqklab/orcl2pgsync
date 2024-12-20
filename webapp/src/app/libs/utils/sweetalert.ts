import Swal from 'sweetalert2';

export class SweetAlert {
  /**
   *
   * @param message
   * @param iconStatus = error | success
   */
  public static notifyMessage(
    message: string,
    iconStatus: any = 'success'
  ): void {
    const toast = Swal.mixin({
      toast: true,
      position: 'top-end',
      showConfirmButton: false,
      timer: 3000,
    });
    toast.fire({
      icon: iconStatus,
      text: message,
      showConfirmButton: false,
    });
  }
  public static notifyMessageAlwaysShow(
    message: string,
    iconStatus: any = 'success'
  ): void {
    const toast = Swal.mixin({
      toast: true,
      position: 'top-end',
      showConfirmButton: false,
      timer: 6000,
    });
    toast.fire({
      icon: iconStatus,
      text: message,
      showConfirmButton: false,
    });
  }
}
