import { DatePipe, DecimalPipe } from '@angular/common';
import { Component, OnDestroy, OnInit, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { MonitoreoService } from '../../services/monitoreo.service';
import { DashboardMedico, PacienteResumen, SeveridadAlerta } from '../../models/monitoreo.model';

@Component({
  selector: 'app-monitor-alertas',
  standalone: true,
  imports: [DatePipe, DecimalPipe, ReactiveFormsModule],
  templateUrl: './monitor-alertas.component.html',
  styleUrl: './monitor-alertas.component.css'
})
export class MonitorAlertasComponent implements OnInit, OnDestroy {
  private monitoreoService = inject(MonitoreoService);
  private fb = inject(FormBuilder);
  readonly auth = inject(AuthService);

  dashboard = signal<DashboardMedico | null>(null);
  loading = signal(false);
  saving = signal(false);
  error = signal<string | null>(null);
  selectedPacienteId = signal<number | null>(null);
  private refreshTimer: number | null = null;

  pacientesCriticos = computed(() =>
    this.dashboard()?.pacientes.filter(paciente => paciente.estado === 'CRITICO').length ?? 0
  );

  pacienteSeleccionado = computed(() =>
    this.dashboard()?.pacientes.find(paciente => paciente.id === this.selectedPacienteId()) ?? null
  );

  form = this.fb.group({
    pacienteId: [null as number | null, Validators.required],
    frecuenciaCardiaca: [90, [Validators.required, Validators.min(20), Validators.max(240)]],
    presionSistolica: [120, [Validators.required, Validators.min(50), Validators.max(260)]],
    presionDiastolica: [80, [Validators.required, Validators.min(30), Validators.max(180)]],
    saturacionOxigeno: [96, [Validators.required, Validators.min(50), Validators.max(100)]],
    temperatura: [36.8, [Validators.required, Validators.min(30), Validators.max(45)]],
    frecuenciaRespiratoria: [18, [Validators.required, Validators.min(4), Validators.max(60)]]
  });

  ngOnInit(): void {
    if (this.canLoadProtectedData()) {
      this.loadDashboard();
    }

    this.refreshTimer = window.setInterval(() => {
      if (this.canLoadProtectedData()) {
        this.loadDashboard(false);
      }
    }, 5000);
  }

  ngOnDestroy(): void {
    if (this.refreshTimer) {
      window.clearInterval(this.refreshTimer);
    }
  }

  loadDashboard(showLoading = true): void {
    if (showLoading) {
      this.loading.set(true);
    }
    this.error.set(null);

    this.monitoreoService.getDashboard().subscribe({
      next: dashboard => {
        this.dashboard.set(dashboard);
        this.loading.set(false);

        if (!this.selectedPacienteId() && dashboard.pacientes.length > 0) {
          this.selectPaciente(dashboard.pacientes[0]);
        }
      },
      error: () => {
        this.error.set('No se pudo cargar el monitor clinico. Revisa sesion y BFF.');
        this.loading.set(false);
      }
    });
  }

  selectPaciente(paciente: PacienteResumen): void {
    this.selectedPacienteId.set(paciente.id);
    this.form.patchValue({ pacienteId: paciente.id });
  }

  registrarSignosVitales(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const raw = this.form.getRawValue();
    this.saving.set(true);
    this.error.set(null);

    this.monitoreoService.registrarSignosVitales({
      pacienteId: raw.pacienteId!,
      frecuenciaCardiaca: raw.frecuenciaCardiaca!,
      presionSistolica: raw.presionSistolica!,
      presionDiastolica: raw.presionDiastolica!,
      saturacionOxigeno: raw.saturacionOxigeno!,
      temperatura: raw.temperatura!,
      frecuenciaRespiratoria: raw.frecuenciaRespiratoria!
    }).subscribe({
      next: () => {
        this.saving.set(false);
        this.loadDashboard(false);
      },
      error: () => {
        this.error.set('No se pudo registrar la lectura. Verifica los rangos ingresados.');
        this.saving.set(false);
      }
    });
  }

  atenderAlerta(id: number): void {
    this.monitoreoService.atenderAlerta(id).subscribe({
      next: () => this.loadDashboard(false),
      error: () => this.error.set('No se pudo atender la alerta.')
    });
  }

  severityClass(severidad: SeveridadAlerta): string {
    return `severity-${severidad.toLowerCase()}`;
  }

  hasError(field: string): boolean {
    const control = this.form.get(field);
    return !!(control?.invalid && control?.touched);
  }

  private canLoadProtectedData(): boolean {
    return this.auth.isReady() && !!this.auth.currentUser() && !!this.auth.idToken();
  }
}
