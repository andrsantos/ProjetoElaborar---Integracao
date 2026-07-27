import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class NotificationService {
  private message: string | null = null;

  constructor() { }

  setMessage(message: string): void {
    this.message = message;
  }


  getAndClearMessage(): string | null {
    const tempMessage = this.message;
    this.message = null; 
    return tempMessage;
  }
  
}
