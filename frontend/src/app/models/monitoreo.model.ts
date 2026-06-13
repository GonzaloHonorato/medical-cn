export type EstadoPaciente = 'CRITICO' | 'OBSERVACION' | 'ESTABLE';
export type SeveridadAlerta = 'ALTA' | 'MEDIA' | 'BAJA';

export interface SignoVital {
  id: number;
  pacienteId: number;
  pacienteNombre: string;
  habitacion: string;
  frecuenciaCardiaca: number;
  presionSistolica: number;
  presionDiastolica: number;
  saturacionOxigeno: number;
  temperatura: number;
  frecuenciaRespiratoria: number;
  fechaRegistro: string;
}

export interface PacienteResumen {
  id: number;
  nombre: string;
  rut: string;
  edad: number;
  habitacion: string;
  diagnostico: string;
  estado: EstadoPaciente;
  ultimoSignoVital: SignoVital | null;
}

export interface AlertaMedica {
  id: number;
  pacienteId: number;
  pacienteNombre: string;
  habitacion: string;
  tipo: string;
  severidad: SeveridadAlerta;
  mensaje: string;
  fechaRegistro: string;
}

export interface EventoClinico {
  id: number;
  pacienteId: number | null;
  pacienteNombre: string;
  habitacion: string;
  tipo: string;
  origen: string;
  mensaje: string;
  severidad: SeveridadAlerta;
  valor: string | null;
  fechaEvento: string;
  fechaRecepcion: string;
}

export interface DashboardMedico {
  pacientesActivos: number;
  alertasActivas: number;
  pacientes: PacienteResumen[];
  alertas: AlertaMedica[];
  eventos: EventoClinico[];
}

export interface SignoVitalRequest {
  pacienteId: number;
  frecuenciaCardiaca: number;
  presionSistolica: number;
  presionDiastolica: number;
  saturacionOxigeno: number;
  temperatura: number;
  frecuenciaRespiratoria: number;
}
