import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { EventoClinico } from '../models/monitoreo.model';
import { RuntimeConfigService } from './runtime-config.service';

@Injectable({ providedIn: 'root' })
export class EventosClinicosSocketService {
  private runtimeConfig = inject(RuntimeConfigService);

  connect(): Observable<EventoClinico> {
    return new Observable<EventoClinico>(subscriber => {
      const socket = new WebSocket(this.runtimeConfig.wsUrl('/ws/eventos-clinicos'));

      socket.onmessage = event => {
        try {
          subscriber.next(JSON.parse(event.data) as EventoClinico);
        } catch (error) {
          subscriber.error(error);
        }
      };

      socket.onerror = event => subscriber.error(event);
      socket.onclose = () => subscriber.complete();

      return () => {
        if (socket.readyState === WebSocket.OPEN || socket.readyState === WebSocket.CONNECTING) {
          socket.close();
        }
      };
    });
  }
}
