import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, firstValueFrom, timeout } from 'rxjs';
import {
  AtsAnalysisResult,
  AtsAnalyzeRequest,
  AtsOptimizeResult,
  Certification,
  CvImportProgressEvent,
  CvImportResult,
  Education,
  Experience,
  Language,
  Portfolio,
  Project,
  PublishStatus,
  Skill,
  TradeCategory,
  UserProfile,
} from '../models/portfolio.models';

@Injectable({ providedIn: 'root' })
export class PortfolioService {
  private readonly http = inject(HttpClient);
  private readonly base = '/api/portfolio';
  private readonly cvImportTimeoutMs = 300_000;

  getPortfolio(): Observable<Portfolio> {
    return this.http.get<Portfolio>(this.base);
  }

  getProfile(): Observable<UserProfile> {
    return this.http.get<UserProfile>(`${this.base}/profile`);
  }

  saveProfile(profile: UserProfile): Observable<UserProfile> {
    return this.http.put<UserProfile>(`${this.base}/profile`, profile);
  }

  uploadPhoto(file: File): Observable<{ photoUrl: string | null }> {
    const form = new FormData();
    form.append('file', file);
    return this.http.post<{ photoUrl: string | null }>(`${this.base}/profile/photo`, form);
  }

  getExperiences(): Observable<Experience[]> {
    return this.http.get<Experience[]>(`${this.base}/experiences`);
  }

  saveExperiences(items: Experience[]): Observable<Experience[]> {
    return this.http.put<Experience[]>(`${this.base}/experiences`, items);
  }

  getProjects(): Observable<Project[]> {
    return this.http.get<Project[]>(`${this.base}/projects`);
  }

  saveProjects(items: Project[]): Observable<Project[]> {
    return this.http.put<Project[]>(`${this.base}/projects`, items);
  }

  getEducation(): Observable<Education[]> {
    return this.http.get<Education[]>(`${this.base}/education`);
  }

  saveEducation(items: Education[]): Observable<Education[]> {
    return this.http.put<Education[]>(`${this.base}/education`, items);
  }

  getSkills(): Observable<Skill[]> {
    return this.http.get<Skill[]>(`${this.base}/skills`);
  }

  saveSkills(items: Skill[]): Observable<Skill[]> {
    return this.http.put<Skill[]>(`${this.base}/skills`, items);
  }

  getLanguages(): Observable<Language[]> {
    return this.http.get<Language[]>(`${this.base}/languages`);
  }

  saveLanguages(items: Language[]): Observable<Language[]> {
    return this.http.put<Language[]>(`${this.base}/languages`, items);
  }

  getCertifications(): Observable<Certification[]> {
    return this.http.get<Certification[]>(`${this.base}/certifications`);
  }

  saveCertifications(items: Certification[]): Observable<Certification[]> {
    return this.http.put<Certification[]>(`${this.base}/certifications`, items);
  }

  getPreviewHtml(): Observable<string> {
    return this.http.get('/api/render/preview', { responseType: 'text' });
  }

  getPublishStatus(): Observable<PublishStatus> {
    return this.http.get<PublishStatus>(`${this.base}/publish/status`);
  }

  publish(): Observable<PublishStatus> {
    return this.http.post<PublishStatus>(`${this.base}/publish`, {});
  }

  unpublish(): Observable<PublishStatus> {
    return this.http.post<PublishStatus>(`${this.base}/unpublish`, {});
  }

  getTradeCategories(): Observable<TradeCategory[]> {
    return this.http.get<TradeCategory[]>('/api/trades/categories');
  }

  analyzeCv(request: AtsAnalyzeRequest): Observable<AtsAnalysisResult> {
    return this.http.post<AtsAnalysisResult>('/api/ats/analyze', request);
  }

  optimizeCv(request: AtsAnalyzeRequest): Observable<AtsOptimizeResult> {
    return this.http.post<AtsOptimizeResult>('/api/ats/optimize', request);
  }

  importCvFile(file: File): Observable<CvImportResult> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<CvImportResult>(`${this.base}/cv/import`, formData).pipe(
      timeout(this.cvImportTimeoutMs),
    );
  }

  async importCvWithProgress(
    file: File,
    onProgress: (event: CvImportProgressEvent) => void,
  ): Promise<CvImportResult> {
    const simulatedSteps: CvImportProgressEvent[] = [
      { phase: 'extract', progress: 10, message: 'Lecture du document...' },
      { phase: 'parse', progress: 25, message: 'Analyse du profil...', section: 'profile' },
      { phase: 'parse', progress: 40, message: 'Analyse des expériences...', section: 'experiences' },
      { phase: 'parse', progress: 55, message: 'Analyse de la formation...', section: 'education' },
      { phase: 'parse', progress: 70, message: 'Analyse des compétences...', section: 'skills' },
      { phase: 'parse', progress: 85, message: 'Finalisation...' },
    ];

    let stepIndex = 0;
    onProgress(simulatedSteps[0]);
    const timer = setInterval(() => {
      stepIndex += 1;
      if (stepIndex < simulatedSteps.length) {
        onProgress(simulatedSteps[stepIndex]);
      }
    }, 450);

    try {
      const imported = await firstValueFrom(this.importCvFile(file));
      this.emitExtractedSections(imported, onProgress);
      const engineLabel = imported.parserEngine
        ? `Analyse terminée (${imported.parserEngine})`
        : 'Analyse terminée (API CV)';
      onProgress({ phase: 'complete', progress: 100, message: engineLabel, result: imported });
      return imported;
    } finally {
      clearInterval(timer);
    }
  }

  private emitExtractedSections(
    imported: CvImportResult,
    onProgress: (event: CvImportProgressEvent) => void,
  ): void {
    const sections: Array<{ key: string; found: boolean; count?: number }> = [
      { key: 'profile', found: this.hasProfileData(imported.profile) },
      { key: 'experiences', found: (imported.experiences?.length ?? 0) > 0, count: imported.experiences?.length },
      { key: 'projects', found: (imported.projects?.length ?? 0) > 0, count: imported.projects?.length },
      { key: 'education', found: (imported.education?.length ?? 0) > 0, count: imported.education?.length },
      { key: 'skills', found: (imported.skills?.length ?? 0) > 0, count: imported.skills?.length },
      { key: 'languages', found: (imported.languages?.length ?? 0) > 0, count: imported.languages?.length },
      {
        key: 'certifications',
        found: (imported.certifications?.length ?? 0) > 0,
        count: imported.certifications?.length,
      },
    ];

    for (const section of sections) {
      if (section.found) {
        onProgress({
          phase: 'parse',
          progress: 92,
          message: 'Sections détectées',
          section: section.key,
          found: true,
          count: section.count,
        });
      }
    }
  }

  private hasProfileData(profile: CvImportResult['profile']): boolean {
    if (!profile) {
      return false;
    }
    return [
      profile.firstName,
      profile.lastName,
      profile.email,
      profile.title,
      profile.summary,
      profile.phone,
    ].some((value) => value?.trim());
  }

  formatCvImportError(err: unknown): string {
    if (err instanceof Error && err.message && !err.message.startsWith('Http')) {
      return err.message.endsWith('.') ? err.message : `${err.message}.`;
    }
    if (err && typeof err === 'object' && 'name' in err && err.name === 'TimeoutError') {
      return 'Import trop long (>5 min). Réessayez ou utilisez un fichier plus léger.';
    }
    if (err instanceof HttpErrorResponse) {
      if (err.status === 403) {
        return 'Accès refusé (403). Reconnectez-vous puis réessayez l\'import.';
      }
      const message = typeof err.error === 'object' && err.error && 'error' in err.error
        ? (err.error as { error?: string }).error
        : undefined;
      if (typeof message === 'string') {
        return message;
      }
    }
    return 'Impossible d\'extraire les informations du CV. Utilisez un PDF ou Word texte (non scanné).';
  }
}
